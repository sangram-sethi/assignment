"""FundPortfolio OOP class encapsulating finance metrics for the whole book."""

from __future__ import annotations

from dataclasses import dataclass, field

import numpy as np
import pandas as pd

from . import analysis_pandas as ap
from .logging_config import get_logger

logger = get_logger()

RISK_FREE_RATE = 6.0  # % annual, simplified assumption for Sharpe ratio.
TRADING_DAYS_PER_YEAR = 252


@dataclass
class FundPortfolio:
    """Encapsulates a merged mutual-fund portfolio and its performance metrics.

    The class is constructed from the cleaned datasets and exposes reusable methods
    for every finance metric required by the case study.
    """

    datasets: dict[str, pd.DataFrame]
    master: pd.DataFrame = field(init=False)
    _holdings: pd.DataFrame = field(init=False)

    def __post_init__(self) -> None:
        self.master = ap.build_master(self.datasets)
        self._holdings = ap.holdings(self.master)
        logger.info("FundPortfolio initialised with %d holdings.", len(self._holdings))

    # --- core aggregates -------------------------------------------------
    def net_invested(self) -> float:
        """Net capital deployed (inflows minus redemptions)."""
        return float(self.master["SignedAmount"].sum())

    def total_portfolio_value(self) -> float:
        """Current market value of all holdings."""
        return float(self._holdings["CurrentValue"].sum())

    def absolute_return(self) -> float:
        """Current value minus net invested capital."""
        return self.total_portfolio_value() - self.net_invested()

    def portfolio_return_pct(self) -> float:
        """Absolute return as a percentage of net invested capital."""
        invested = self.net_invested()
        if invested == 0:
            return float("nan")
        return self.absolute_return() / invested * 100

    # --- time-based metrics ---------------------------------------------
    def _investment_span_years(self) -> float:
        """Years between first transaction and latest NAV date."""
        dates = pd.to_datetime(self.master["TransactionDate"], errors="coerce")
        nav_dates = pd.to_datetime(self.datasets["nav_history"]["Date"], errors="coerce")
        start = dates.min()
        end = nav_dates.max()
        if pd.isna(start) or pd.isna(end) or end <= start:
            return float("nan")
        return (end - start).days / 365.25

    def cagr(self) -> float:
        """Compound Annual Growth Rate (%)."""
        invested = self.net_invested()
        value = self.total_portfolio_value()
        years = self._investment_span_years()
        if invested <= 0 or value <= 0 or not years or np.isnan(years) or years <= 0:
            return float("nan")
        return ((value / invested) ** (1 / years) - 1) * 100

    def annualized_return(self) -> float:
        """Annualised simple return (%)."""
        years = self._investment_span_years()
        if not years or np.isnan(years) or years <= 0:
            return float("nan")
        return self.portfolio_return_pct() / years

    def average_holding_period(self) -> float:
        """Average holding period in days (transaction date to latest NAV date)."""
        dates = pd.to_datetime(self.master["TransactionDate"], errors="coerce")
        end = pd.to_datetime(self.datasets["nav_history"]["Date"], errors="coerce").max()
        if pd.isna(end):
            return float("nan")
        held_days = (end - dates).dt.days.dropna()
        if held_days.empty:
            return float("nan")
        return float(held_days.mean())

    # --- allocation & diversification -----------------------------------
    def fund_allocation_pct(self) -> pd.DataFrame:
        """Current value share per fund (%)."""
        alloc = (
            self._holdings.groupby("FundID")["CurrentValue"].sum().rename("Value").reset_index()
        )
        total = alloc["Value"].sum()
        alloc["AllocationPct"] = (alloc["Value"] / total * 100) if total else 0.0
        funds = self.datasets["funds"][["FundID", "FundName", "Category"]]
        alloc = alloc.merge(funds, on="FundID", how="left")
        return alloc.sort_values("AllocationPct", ascending=False).reset_index(drop=True)

    def category_investment_pct(self) -> pd.DataFrame:
        """Invested amount share per fund category (%)."""
        # master already carries the fund Category column from build_master().
        cat = self.master.groupby("Category")["Amount"].sum().rename("Investment").reset_index()
        total = cat["Investment"].sum()
        cat["InvestmentPct"] = (cat["Investment"] / total * 100) if total else 0.0
        return cat.sort_values("InvestmentPct", ascending=False).reset_index(drop=True)

    def diversification_score(self) -> float:
        """Simpson diversification score = 1 - sum(weight^2) over fund allocation.

        Ranges 0 (fully concentrated) to ~1 (highly diversified).
        """
        alloc = self.fund_allocation_pct()
        weights = (alloc["AllocationPct"] / 100).to_numpy(dtype=float)
        weights = weights[~np.isnan(weights)]
        if weights.size == 0:
            return float("nan")
        return float(1 - np.sum(weights ** 2))

    # --- cost & risk-adjusted metrics -----------------------------------
    def expense_ratio_impact(self) -> float:
        """Annual cost (₹) implied by value-weighted expense ratios.

        Returns NaN if the funds dataset has no ExpenseRatio column.
        """
        funds_df = self.datasets["funds"]
        if "ExpenseRatio" not in funds_df.columns:
            logger.warning("ExpenseRatio column absent — expense ratio impact unavailable.")
            return float("nan")
        funds = funds_df[["FundID", "ExpenseRatio"]]
        alloc = self.fund_allocation_pct().merge(funds, on="FundID", how="left")
        # ExpenseRatio expressed as % of value.
        return float((alloc["Value"] * alloc["ExpenseRatio"] / 100).sum())

    def sharpe_ratio(self) -> float:
        """Simplified Sharpe ratio using per-fund NAV daily returns as volatility."""
        nav = self.datasets["nav_history"].sort_values(["FundID", "Date"]).copy()
        nav["DailyRet"] = nav.groupby("FundID")["NAV"].pct_change()
        vol = nav["DailyRet"].std()
        if not vol or np.isnan(vol) or vol == 0:
            return float("nan")
        annual_vol = vol * np.sqrt(TRADING_DAYS_PER_YEAR) * 100
        excess = self.annualized_return() - RISK_FREE_RATE
        if np.isnan(excess) or annual_vol == 0:
            return float("nan")
        return excess / annual_vol

    def investor_profit_loss(self) -> pd.DataFrame:
        """Per-investor current value, invested capital and profit/loss."""
        invested = (
            self.master.groupby("InvestorID")["SignedAmount"].sum().rename("NetInvested")
        )
        value = (
            self._holdings.groupby("InvestorID")["CurrentValue"].sum().rename("CurrentValue")
        )
        pnl = pd.concat([invested, value], axis=1).fillna(0)
        pnl["ProfitLoss"] = pnl["CurrentValue"] - pnl["NetInvested"]
        pnl["ReturnPct"] = np.where(
            pnl["NetInvested"] != 0,
            pnl["ProfitLoss"] / pnl["NetInvested"] * 100,
            np.nan,
        )
        return pnl.sort_values("ProfitLoss", ascending=False).reset_index()

    # --- convenience -----------------------------------------------------
    def summary(self) -> dict[str, float]:
        """Return all scalar finance metrics as a dict."""
        metrics = {
            "total_portfolio_value": round(self.total_portfolio_value(), 2),
            "net_invested": round(self.net_invested(), 2),
            "absolute_return": round(self.absolute_return(), 2),
            "portfolio_return_pct": round(self.portfolio_return_pct(), 2),
            "cagr_pct": round(self.cagr(), 2),
            "annualized_return_pct": round(self.annualized_return(), 2),
            "diversification_score": round(self.diversification_score(), 4),
            "average_holding_period_days": round(self.average_holding_period(), 1),
            "expense_ratio_impact": round(self.expense_ratio_impact(), 2),
            "sharpe_ratio": round(self.sharpe_ratio(), 4),
        }
        logger.info("Computed portfolio finance metrics summary.")
        return metrics
