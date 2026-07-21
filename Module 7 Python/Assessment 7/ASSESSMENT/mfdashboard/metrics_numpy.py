"""NumPy-based statistical metrics required by the case study."""

from __future__ import annotations

import numpy as np
import pandas as pd

from .logging_config import get_logger

logger = get_logger()


def mean_investment_amount(transactions: pd.DataFrame) -> float:
    """Mean investment (transaction) amount."""
    return float(np.mean(transactions["Amount"].to_numpy(dtype=float)))


def median_investor_income(investors: pd.DataFrame) -> float:
    """Median investor annual income (NaN if the column is absent)."""
    if "AnnualIncome" not in investors.columns:
        logger.warning("AnnualIncome column absent — median income unavailable.")
        return float("nan")
    return float(np.median(investors["AnnualIncome"].to_numpy(dtype=float)))


def std_of_nav(nav_history: pd.DataFrame) -> float:
    """Standard deviation of NAV values."""
    return float(np.std(nav_history["NAV"].to_numpy(dtype=float)))


def percentile_fund_returns(nav_history: pd.DataFrame, percentiles=(90, 95)) -> dict[int, float]:
    """Percentile (90th & 95th) of per-fund total returns.

    Fund return is computed as (last NAV - first NAV) / first NAV per fund.
    """
    returns = []
    for _, grp in nav_history.sort_values("Date").groupby("FundID"):
        navs = grp["NAV"].to_numpy(dtype=float)
        if len(navs) >= 2 and navs[0] != 0:
            returns.append((navs[-1] - navs[0]) / navs[0] * 100)
    returns = np.array(returns, dtype=float)
    if returns.size == 0:
        return {p: float("nan") for p in percentiles}
    return {p: float(np.percentile(returns, p)) for p in percentiles}


def correlation_income_investment(
    investors: pd.DataFrame, transactions: pd.DataFrame
) -> float:
    """Correlation between investor annual income and their total investment amount."""
    if "AnnualIncome" not in investors.columns:
        logger.warning("AnnualIncome column absent — income/investment correlation unavailable.")
        return float("nan")
    invested = (
        transactions.groupby("InvestorID")["Amount"].sum().rename("TotalInvestment")
    )
    merged = investors.merge(invested, left_on="InvestorID", right_index=True, how="inner")
    if len(merged) < 2:
        return float("nan")
    income = merged["AnnualIncome"].to_numpy(dtype=float)
    investment = merged["TotalInvestment"].to_numpy(dtype=float)
    return float(np.corrcoef(income, investment)[0, 1])


def average_daily_nav(nav_history: pd.DataFrame) -> float:
    """Average NAV across all records (average daily NAV)."""
    return float(np.mean(nav_history["NAV"].to_numpy(dtype=float)))


def compute_all(datasets: dict[str, pd.DataFrame]) -> dict[str, object]:
    """Compute every NumPy metric and return a dict of results."""
    investors = datasets["investors"]
    transactions = datasets["transactions"]
    nav_history = datasets["nav_history"]

    pctiles = percentile_fund_returns(nav_history)
    results = {
        "mean_investment_amount": mean_investment_amount(transactions),
        "median_investor_income": median_investor_income(investors),
        "std_of_nav": std_of_nav(nav_history),
        "fund_return_90th_pct": pctiles.get(90),
        "fund_return_95th_pct": pctiles.get(95),
        "corr_income_vs_investment": correlation_income_investment(investors, transactions),
        "average_daily_nav": average_daily_nav(nav_history),
    }
    logger.info("Computed NumPy statistical metrics.")
    return results
