"""Matplotlib chart generation for the portfolio dashboard."""

from __future__ import annotations

from pathlib import Path

import matplotlib

matplotlib.use("Agg")  # non-interactive backend for automated report generation.
import matplotlib.pyplot as plt
import pandas as pd

from . import analysis_pandas as ap
from .logging_config import get_logger
from .portfolio import FundPortfolio

logger = get_logger()


def _save(fig, out_dir: Path, name: str) -> Path:
    out_dir.mkdir(parents=True, exist_ok=True)
    path = out_dir / name
    fig.tight_layout()
    fig.savefig(path, dpi=120)
    plt.close(fig)
    logger.info("Saved chart: %s", path)
    return path


def portfolio_allocation_pie(portfolio: FundPortfolio, out_dir: Path) -> Path:
    alloc = portfolio.fund_allocation_pct()
    alloc = alloc[alloc["Value"] > 0]
    fig, ax = plt.subplots(figsize=(7, 7))
    labels = alloc["FundName"].fillna(alloc["FundID"])
    ax.pie(alloc["Value"], labels=labels, autopct="%1.1f%%", startangle=90)
    ax.set_title("Portfolio Allocation by Fund")
    return _save(fig, out_dir, "portfolio_allocation_pie.png")


def fundwise_investment_bar(master: pd.DataFrame, funds: pd.DataFrame, out_dir: Path) -> Path:
    inv = master.groupby("FundID")["Amount"].sum().reset_index()
    inv = inv.merge(funds[["FundID", "FundName"]], on="FundID", how="left")
    inv = inv.sort_values("Amount", ascending=False)
    fig, ax = plt.subplots(figsize=(9, 5))
    ax.bar(inv["FundName"].fillna(inv["FundID"]), inv["Amount"], color="#4C72B0")
    ax.set_title("Fund-wise Investment")
    ax.set_ylabel("Investment Amount (₹)")
    ax.tick_params(axis="x", rotation=45)
    for lbl in ax.get_xticklabels():
        lbl.set_ha("right")
    return _save(fig, out_dir, "fundwise_investment_bar.png")


def monthly_investment_trend(master: pd.DataFrame, out_dir: Path) -> Path:
    tx = master.copy()
    tx["TransactionDate"] = pd.to_datetime(tx["TransactionDate"], errors="coerce")
    tx["Month"] = tx["TransactionDate"].dt.to_period("M").astype(str)
    trend = tx.groupby("Month")["Amount"].sum().reset_index()
    fig, ax = plt.subplots(figsize=(9, 5))
    ax.plot(trend["Month"], trend["Amount"], marker="o", color="#DD8452")
    ax.set_title("Monthly Investment Trend")
    ax.set_ylabel("Investment Amount (₹)")
    ax.set_xlabel("Month")
    ax.tick_params(axis="x", rotation=45)
    ax.grid(True, alpha=0.3)
    return _save(fig, out_dir, "monthly_investment_trend.png")


def daywise_investment_trend(master: pd.DataFrame, out_dir: Path) -> Path:
    tx = master.copy()
    tx["TransactionDate"] = pd.to_datetime(tx["TransactionDate"], errors="coerce")
    trend = tx.groupby(tx["TransactionDate"].dt.date)["Amount"].sum().reset_index()
    trend.columns = ["Date", "Amount"]
    trend = trend.sort_values("Date")
    fig, ax = plt.subplots(figsize=(10, 5))
    ax.plot(trend["Date"], trend["Amount"], marker="o", color="#8172B3")
    ax.set_title("Day-wise Investment Trend")
    ax.set_ylabel("Investment Amount (₹)")
    ax.set_xlabel("Date")
    ax.tick_params(axis="x", rotation=45)
    for lbl in ax.get_xticklabels():
        lbl.set_ha("right")
    ax.grid(True, alpha=0.3)
    return _save(fig, out_dir, "daywise_investment_trend.png")


def category_returns_bar(portfolio: FundPortfolio, out_dir: Path) -> Path:
    cat = portfolio.category_investment_pct()
    fig, ax = plt.subplots(figsize=(8, 5))
    ax.bar(cat["Category"], cat["InvestmentPct"], color="#55A868")
    ax.set_title("Category-wise Investment %")
    ax.set_ylabel("Investment Share (%)")
    ax.tick_params(axis="x", rotation=30)
    for lbl in ax.get_xticklabels():
        lbl.set_ha("right")
    return _save(fig, out_dir, "category_investment_bar.png")


def nav_movement_line(nav_history: pd.DataFrame, funds: pd.DataFrame, out_dir: Path) -> Path:
    nav = nav_history.copy()
    nav["Date"] = pd.to_datetime(nav["Date"], errors="coerce")
    fig, ax = plt.subplots(figsize=(10, 5))
    name_map = funds.set_index("FundID")["FundName"].to_dict()
    for fund_id, grp in nav.sort_values("Date").groupby("FundID"):
        ax.plot(grp["Date"], grp["NAV"], marker=".", label=name_map.get(fund_id, fund_id))
    ax.set_title("NAV Movement")
    ax.set_ylabel("NAV")
    ax.set_xlabel("Date")
    ax.legend(fontsize=8)
    ax.grid(True, alpha=0.3)
    return _save(fig, out_dir, "nav_movement_line.png")


def top_investors_barh(master: pd.DataFrame, investors: pd.DataFrame, out_dir: Path) -> Path:
    top = ap.top_investors(master, investors, n=10)
    top = top.sort_values("PortfolioValue")
    fig, ax = plt.subplots(figsize=(9, 6))
    labels = top["InvestorName"].fillna(top["InvestorID"])
    ax.barh(labels, top["PortfolioValue"], color="#C44E52")
    ax.set_title("Top 10 Investors by Portfolio Value")
    ax.set_xlabel("Portfolio Value (₹)")
    return _save(fig, out_dir, "top10_investors_barh.png")


def generate_all(
    portfolio: FundPortfolio, datasets: dict[str, pd.DataFrame], out_dir: str | Path
) -> list[Path]:
    """Generate every required chart, returning the list of saved paths."""
    out_dir = Path(out_dir)
    master = portfolio.master
    funds = datasets["funds"]
    investors = datasets["investors"]
    nav = datasets["nav_history"]

    charts = [
        portfolio_allocation_pie(portfolio, out_dir),
        fundwise_investment_bar(master, funds, out_dir),
        monthly_investment_trend(master, out_dir),
        daywise_investment_trend(master, out_dir),
        category_returns_bar(portfolio, out_dir),
        nav_movement_line(nav, funds, out_dir),
        top_investors_barh(master, investors, out_dir),
    ]
    logger.info("Generated %d charts.", len(charts))
    return charts
