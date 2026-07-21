"""Pandas-based merging, investor segmentation and fund analysis."""

from __future__ import annotations

import pandas as pd

from .logging_config import get_logger

logger = get_logger()

# Business thresholds (₹).
TEN_LAKHS = 1_000_000
FIFTEEN_LAKHS = 1_500_000

# Transaction types that add vs. remove units (supports both naming schemes).
INFLOW_TYPES = {"Purchase", "SIP", "Buy"}
OUTFLOW_TYPES = {"Redemption", "Sell"}


def latest_nav_per_fund(nav_history: pd.DataFrame) -> pd.Series:
    """Return the most recent NAV for each fund."""
    latest = (
        nav_history.sort_values("Date")
        .groupby("FundID")
        .tail(1)
        .set_index("FundID")["NAV"]
    )
    return latest


def signed_units(transactions: pd.DataFrame) -> pd.DataFrame:
    """Add a SignedUnits column: positive for inflows, negative for redemptions."""
    tx = transactions.copy()
    tx["Units"] = pd.to_numeric(tx["Units"], errors="coerce").fillna(0)
    sign = tx["TransactionType"].map(
        lambda t: -1 if t in OUTFLOW_TYPES else 1
    )
    tx["SignedUnits"] = tx["Units"] * sign
    tx["SignedAmount"] = pd.to_numeric(tx["Amount"], errors="coerce").fillna(0) * sign
    return tx


def build_master(datasets: dict[str, pd.DataFrame]) -> pd.DataFrame:
    """Merge investors + transactions + funds + latest NAV into one master frame."""
    investors = datasets["investors"]
    funds = datasets["funds"]
    nav = datasets["nav_history"]
    tx = signed_units(datasets["transactions"])

    latest_nav = latest_nav_per_fund(nav).rename("LatestNAV")

    master = (
        tx.merge(investors, on="InvestorID", how="left")
        .merge(funds, on="FundID", how="left")
        .merge(latest_nav, on="FundID", how="left")
    )

    # Some funds have no NAV history; fall back to the transaction's own NAV so the
    # holding is valued at cost instead of being dropped (which would distort returns).
    missing_nav = master["LatestNAV"].isna()
    n_missing_funds = master.loc[missing_nav, "FundID"].nunique()
    if missing_nav.any():
        master["LatestNAV"] = master["LatestNAV"].fillna(master["NAV"])
        logger.warning(
            "%d fund(s) lack NAV history - valued at purchase NAV as a fallback.",
            n_missing_funds,
        )

    logger.info("Built merged master dataset (%d rows).", len(master))
    return master


def holdings(master: pd.DataFrame) -> pd.DataFrame:
    """Net units held per investor-fund and their current market value."""
    grp = (
        master.groupby(["InvestorID", "FundID"], as_index=False)
        .agg(NetUnits=("SignedUnits", "sum"), LatestNAV=("LatestNAV", "first"))
    )
    grp["CurrentValue"] = grp["NetUnits"] * grp["LatestNAV"]
    return grp


def portfolio_value_by_investor(master: pd.DataFrame) -> pd.DataFrame:
    """Total current portfolio value per investor."""
    hold = holdings(master)
    pv = (
        hold.groupby("InvestorID", as_index=False)["CurrentValue"]
        .sum()
        .rename(columns={"CurrentValue": "PortfolioValue"})
    )
    return pv.sort_values("PortfolioValue", ascending=False).reset_index(drop=True)


def top_investors(master: pd.DataFrame, investors: pd.DataFrame, n: int = 20) -> pd.DataFrame:
    """Top-N investors by current portfolio value."""
    pv = portfolio_value_by_investor(master)
    result = pv.merge(investors, on="InvestorID", how="left").head(n)
    logger.info("Identified top %d investors by portfolio value.", n)
    return result


def high_value_investors(
    master: pd.DataFrame,
    investors: pd.DataFrame,
    min_investment: float = TEN_LAKHS,
    min_transactions: int = 10,
    min_income: float = FIFTEEN_LAKHS,
    fallback_top_n: int = 10,
) -> pd.DataFrame:
    """Investors matching the high-value criteria that the dataset supports.

    Ideal criteria (case study):
      - Total investment > ``min_investment`` (default ₹10 Lakhs)
      - More than ``min_transactions`` transactions (default 10)
      - High Risk Profile          (only if RiskProfile column present)
      - Annual Income > ``min_income`` (default ₹15 Lakhs; only if column present)

    Criteria whose source columns are absent from the current dataset are skipped
    and logged, so the segmentation degrades gracefully. If *no* investor meets the
    strict thresholds, the function falls back to the top ``fallback_top_n``
    investors ranked by total investment, so the report is never empty when data
    exists. Thresholds are configurable so they can be tuned to the dataset scale.
    """
    total_invested = (
        master.groupby("InvestorID")["Amount"].sum().rename("TotalInvestment")
    )
    tx_count = master.groupby("InvestorID").size().rename("TransactionCount")

    summary = (
        investors.merge(total_invested, on="InvestorID", how="left")
        .merge(tx_count, on="InvestorID", how="left")
    )
    summary["TotalInvestment"] = summary["TotalInvestment"].fillna(0)
    summary["TransactionCount"] = summary["TransactionCount"].fillna(0)

    mask = (summary["TotalInvestment"] > min_investment) & (
        summary["TransactionCount"] > min_transactions
    )
    applied = [f"TotalInvestment>{min_investment:,.0f}", f"TransactionCount>{min_transactions}"]

    if "RiskProfile" in summary.columns:
        mask &= summary["RiskProfile"].str.strip().str.lower() == "high"
        applied.append("RiskProfile=High")
    else:
        logger.warning("RiskProfile column absent — high-risk criterion skipped.")

    if "AnnualIncome" in summary.columns:
        mask &= summary["AnnualIncome"] > min_income
        applied.append(f"AnnualIncome>{min_income:,.0f}")
    else:
        logger.warning("AnnualIncome column absent — income criterion skipped.")

    result = summary[mask].sort_values("TotalInvestment", ascending=False).reset_index(drop=True)
    result["Qualification"] = "MeetsAllCriteria"

    if result.empty:
        # No investor clears the strict bar — fall back to the relatively highest-value ones.
        result = (
            summary[summary["TotalInvestment"] > 0]
            .sort_values("TotalInvestment", ascending=False)
            .head(fallback_top_n)
            .reset_index(drop=True)
        )
        result["Qualification"] = "Fallback:TopByInvestment"
        logger.warning(
            "No investor met strict criteria (%s). Falling back to top %d by total investment.",
            ", ".join(applied), len(result),
        )
    else:
        logger.info(
            "Identified %d high-value investor(s) using criteria: %s.",
            len(result), ", ".join(applied),
        )
    return result


def fund_performance(nav_history: pd.DataFrame) -> pd.DataFrame:
    """Per-fund return % based on first vs last NAV."""
    rows = []
    for fund_id, grp in nav_history.sort_values("Date").groupby("FundID"):
        navs = grp["NAV"].to_numpy(dtype=float)
        if len(navs) >= 2 and navs[0] != 0:
            ret = (navs[-1] - navs[0]) / navs[0] * 100
            rows.append({"FundID": fund_id, "ReturnPct": ret})
    return pd.DataFrame(rows)


def fund_analysis(datasets: dict[str, pd.DataFrame], master: pd.DataFrame) -> dict[str, object]:
    """Best/worst fund, highest expense ratio, highest AUM, most popular fund."""
    funds = datasets["funds"]
    nav = datasets["nav_history"]

    perf = fund_performance(nav).merge(funds, on="FundID", how="left")

    result: dict[str, object] = {}
    if not perf.empty:
        best = perf.loc[perf["ReturnPct"].idxmax()]
        worst = perf.loc[perf["ReturnPct"].idxmin()]
        result["best_performing_fund"] = {
            "FundID": best["FundID"],
            "FundName": best.get("FundName"),
            "ReturnPct": round(float(best["ReturnPct"]), 2),
        }
        result["worst_performing_fund"] = {
            "FundID": worst["FundID"],
            "FundName": worst.get("FundName"),
            "ReturnPct": round(float(worst["ReturnPct"]), 2),
        }

    # Highest expense ratio (only if the column is present).
    if "ExpenseRatio" in funds.columns:
        hi_exp = funds.loc[funds["ExpenseRatio"].idxmax()]
        result["highest_expense_ratio"] = {
            "FundID": hi_exp["FundID"],
            "FundName": hi_exp["FundName"],
            "ExpenseRatio": float(hi_exp["ExpenseRatio"]),
        }
    else:
        result["highest_expense_ratio"] = "N/A (ExpenseRatio not in dataset)"
        logger.warning("ExpenseRatio column absent — highest expense ratio unavailable.")

    # Highest AUM = current market value of net units held per fund.
    hold = holdings(master)
    aum = hold.groupby("FundID")["CurrentValue"].sum().rename("AUM")
    aum = aum.reset_index().merge(funds[["FundID", "FundName"]], on="FundID", how="left")
    if not aum.empty:
        top_aum = aum.loc[aum["AUM"].idxmax()]
        result["highest_aum_fund"] = {
            "FundID": top_aum["FundID"],
            "FundName": top_aum.get("FundName"),
            "AUM": round(float(top_aum["AUM"]), 2),
        }

    # Most popular fund = most transactions.
    popularity = master.groupby("FundID").size().rename("TxCount")
    popularity = popularity.reset_index().merge(
        funds[["FundID", "FundName"]], on="FundID", how="left"
    )
    if not popularity.empty:
        top_pop = popularity.loc[popularity["TxCount"].idxmax()]
        result["most_popular_fund"] = {
            "FundID": top_pop["FundID"],
            "FundName": top_pop.get("FundName"),
            "TransactionCount": int(top_pop["TxCount"]),
        }

    logger.info("Completed fund analysis (best/worst/expense/AUM/popularity).")
    return result
