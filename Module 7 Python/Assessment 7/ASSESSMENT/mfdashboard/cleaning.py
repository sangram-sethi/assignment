"""Data cleaning: column standardisation, missing-value imputation, duplicate & outlier removal."""

from __future__ import annotations

import numpy as np
import pandas as pd

from .logging_config import get_logger

logger = get_logger()


def standardise_transactions(transactions: pd.DataFrame) -> pd.DataFrame:
    """Derive canonical transaction columns used across the pipeline.

    The current dataset records only purchases with ``UnitsPurchased``/``PurchaseNAV``/
    ``PurchaseDate`` and no explicit amount. We derive ``Units``, ``NAV``,
    ``TransactionDate``, ``TransactionType`` and ``Amount`` (= units x NAV) so that the
    downstream analysis/finance code has a consistent schema.
    """
    tx = transactions.copy()
    if "Units" not in tx.columns and "UnitsPurchased" in tx.columns:
        tx["Units"] = pd.to_numeric(tx["UnitsPurchased"], errors="coerce")
    if "NAV" not in tx.columns and "PurchaseNAV" in tx.columns:
        tx["NAV"] = pd.to_numeric(tx["PurchaseNAV"], errors="coerce")
    if "TransactionDate" not in tx.columns and "PurchaseDate" in tx.columns:
        tx["TransactionDate"] = tx["PurchaseDate"]
    if "TransactionType" not in tx.columns:
        tx["TransactionType"] = "Purchase"
    if "Amount" not in tx.columns:
        tx["Amount"] = tx["Units"] * tx["NAV"]
    logger.info("Standardised transactions (derived Amount = Units x NAV).")
    return tx



def remove_duplicate_transactions(transactions: pd.DataFrame) -> pd.DataFrame:
    """Drop duplicate transactions (same TransactionID or identical rows)."""
    before = len(transactions)
    cleaned = transactions.drop_duplicates()
    if "TransactionID" in cleaned.columns:
        cleaned = cleaned.drop_duplicates(subset="TransactionID", keep="first")
    removed = before - len(cleaned)
    if removed:
        logger.info("Removed %d duplicate transaction(s).", removed)
    return cleaned.reset_index(drop=True)


def clean_missing_values(datasets: dict[str, pd.DataFrame]) -> dict[str, pd.DataFrame]:
    """Impute missing values per the case-study rules (where the columns exist).

    - Annual Income  -> median          (if AnnualIncome present)
    - Expense Ratio  -> mean            (if ExpenseRatio present)
    - NAV            -> previous day's NAV (forward fill per fund)
    - Risk Profile   -> "Moderate"      (if RiskProfile present)
    """
    if "investors" in datasets:
        inv = datasets["investors"].copy()
        if "AnnualIncome" in inv.columns:
            inv["AnnualIncome"] = pd.to_numeric(inv["AnnualIncome"], errors="coerce")
            median_income = inv["AnnualIncome"].median()
            n_income = int(inv["AnnualIncome"].isna().sum())
            inv["AnnualIncome"] = inv["AnnualIncome"].fillna(median_income)
            logger.info(
                "Imputed AnnualIncome (%d nulls -> median %.2f).", n_income, median_income
            )
        if "RiskProfile" in inv.columns:
            n_risk = int(inv["RiskProfile"].isna().sum())
            inv["RiskProfile"] = inv["RiskProfile"].fillna("Moderate")
            logger.info("Imputed RiskProfile (%d nulls -> 'Moderate').", n_risk)
        datasets["investors"] = inv

    if "funds" in datasets:
        funds = datasets["funds"].copy()
        if "ExpenseRatio" in funds.columns:
            funds["ExpenseRatio"] = pd.to_numeric(funds["ExpenseRatio"], errors="coerce")
            mean_expense = funds["ExpenseRatio"].mean()
            n_exp = int(funds["ExpenseRatio"].isna().sum())
            funds["ExpenseRatio"] = funds["ExpenseRatio"].fillna(mean_expense)
            logger.info("Imputed ExpenseRatio (%d nulls -> mean %.4f).", n_exp, mean_expense)
        datasets["funds"] = funds

    if "nav_history" in datasets:
        nav = datasets["nav_history"].copy()
        nav["NAV"] = pd.to_numeric(nav["NAV"], errors="coerce")
        nav["Date"] = pd.to_datetime(nav["Date"], errors="coerce")
        nav = nav.sort_values(["FundID", "Date"])
        n_nav = int(nav["NAV"].isna().sum())
        nav["NAV"] = nav.groupby("FundID")["NAV"].ffill().bfill()
        datasets["nav_history"] = nav
        logger.info("Imputed NAV (%d nulls -> previous day NAV per fund).", n_nav)

    return datasets


def remove_outliers(datasets: dict[str, pd.DataFrame]) -> dict[str, pd.DataFrame]:
    """Remove outliers per the case-study rules.

    - Investment Amount > 99th percentile   -> removed from transactions
    - NAV daily change > 3 std deviations    -> removed from nav_history
    """
    if "transactions" in datasets:
        tx = datasets["transactions"].copy()
        tx["Amount"] = pd.to_numeric(tx["Amount"], errors="coerce")
        threshold = tx["Amount"].quantile(0.99)
        before = len(tx)
        tx = tx[tx["Amount"] <= threshold]
        removed = before - len(tx)
        datasets["transactions"] = tx.reset_index(drop=True)
        logger.info(
            "Removed %d transaction outlier(s) with Amount > 99th pct (%.2f).",
            removed, threshold,
        )

    if "nav_history" in datasets:
        nav = datasets["nav_history"].copy()
        nav = nav.sort_values(["FundID", "Date"])
        nav["NAV_Change"] = nav.groupby("FundID")["NAV"].diff()
        std = nav["NAV_Change"].std()
        mean = nav["NAV_Change"].mean()
        if std and not np.isnan(std):
            before = len(nav)
            mask = nav["NAV_Change"].isna() | (
                (nav["NAV_Change"] - mean).abs() <= 3 * std
            )
            nav = nav[mask]
            removed = before - len(nav)
            logger.info(
                "Removed %d NAV outlier(s) with daily change > 3 std (%.4f).",
                removed, std,
            )
        datasets["nav_history"] = nav.drop(columns="NAV_Change").reset_index(drop=True)

    return datasets


def clean_pipeline(datasets: dict[str, pd.DataFrame]) -> dict[str, pd.DataFrame]:
    """Run the full cleaning pipeline in order."""
    if "transactions" in datasets:
        datasets["transactions"] = standardise_transactions(datasets["transactions"])
        datasets["transactions"] = remove_duplicate_transactions(datasets["transactions"])
    datasets = clean_missing_values(datasets)
    datasets = remove_outliers(datasets)
    logger.info("Data cleaning pipeline complete.")
    return datasets
