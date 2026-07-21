"""Automated Mutual Fund Performance Dashboard — main entry point.

Pipeline:
    1. Read all CSV files (with exception handling)
    2. Clean missing values, normalise IDs, remove duplicates & outliers
    3. Compute NumPy statistical metrics
    4. Merge datasets and run Pandas analysis (top/high-value investors, fund analysis)
    5. Compute portfolio finance metrics via the FundPortfolio OOP class
    6. Generate charts
    7. Export reports
    8. Log execution status throughout
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

from mfdashboard import analysis_pandas as ap
from mfdashboard import metrics_numpy, visualization
from mfdashboard.cleaning import clean_pipeline
from mfdashboard.data_loader import load_all
from mfdashboard.logging_config import configure_logging
from mfdashboard.portfolio import FundPortfolio
from mfdashboard.reporting import export_dataframe, write_summary_report


def run(
    data_dir: str,
    out_dir: str,
    min_investment: float = 1_000_000,
    min_transactions: int = 10,
    min_income: float = 1_500_000,
    fallback_top_n: int = 10,
) -> int:
    logger = configure_logging(log_dir=str(Path(out_dir) / "logs"))
    logger.info("=== Mutual Fund Performance Dashboard: START ===")

    reports_dir = Path(out_dir) / "reports"
    charts_dir = Path(out_dir) / "charts"

    try:
        # 1. Load.
        datasets = load_all(data_dir)

        # 2. Clean.
        datasets = clean_pipeline(datasets)

        # 3. NumPy metrics.
        numpy_metrics = metrics_numpy.compute_all(datasets)

        # 4. Pandas analysis.
        portfolio = FundPortfolio(datasets)
        master = portfolio.master

        top20 = ap.top_investors(master, datasets["investors"], n=20)
        high_value = ap.high_value_investors(
            master,
            datasets["investors"],
            min_investment=min_investment,
            min_transactions=min_transactions,
            min_income=min_income,
            fallback_top_n=fallback_top_n,
        )
        fund_insights = ap.fund_analysis(datasets, master)

        # 5. Finance metrics via OOP class.
        finance_metrics = portfolio.summary()
        allocation = portfolio.fund_allocation_pct()
        category = portfolio.category_investment_pct()
        pnl = portfolio.investor_profit_loss()

        # 6. Charts.
        visualization.generate_all(portfolio, datasets, charts_dir)

        # 7. Export reports.
        export_dataframe(top20, reports_dir, "top20_investors.csv")
        export_dataframe(high_value, reports_dir, "high_value_investors.csv")
        export_dataframe(allocation, reports_dir, "fund_allocation.csv")
        export_dataframe(category, reports_dir, "category_investment.csv")
        export_dataframe(pnl, reports_dir, "investor_profit_loss.csv")
        write_summary_report(numpy_metrics, finance_metrics, fund_insights, reports_dir)

        # 8. Console summary.
        _print_console_summary(numpy_metrics, finance_metrics, fund_insights, top20, high_value)

        logger.info("=== Mutual Fund Performance Dashboard: SUCCESS ===")
        return 0
    except Exception as exc:  # noqa: BLE001 - top-level guard logs any failure.
        logger.exception("Dashboard failed: %s", exc)
        logger.info("=== Mutual Fund Performance Dashboard: FAILED ===")
        return 1


def _print_console_summary(numpy_metrics, finance_metrics, fund_insights, top20, high_value) -> None:
    print("\n" + "=" * 60)
    print(" MUTUAL FUND PERFORMANCE DASHBOARD — SUMMARY")
    print("=" * 60)

    print("\n[NumPy Statistical Metrics]")
    for key, value in numpy_metrics.items():
        print(f"  {key:<32}: {value:,.4f}" if isinstance(value, float) else f"  {key:<32}: {value}")

    print("\n[Portfolio Finance Metrics]")
    for key, value in finance_metrics.items():
        print(f"  {key:<32}: {value:,.4f}" if isinstance(value, float) else f"  {key:<32}: {value}")

    print("\n[Fund Analysis]")
    for key, value in fund_insights.items():
        print(f"  {key:<24}: {value}")

    print(f"\n[Top 5 Investors by Portfolio Value] (of {len(top20)})")
    for _, row in top20.head(5).iterrows():
        print(f"  {row['InvestorID']:<8} {str(row.get('InvestorName')):<20} INR {row['PortfolioValue']:,.2f}")

    qualification = ""
    if len(high_value) and "Qualification" in high_value.columns:
        qualification = f" ({high_value['Qualification'].iloc[0]})"
    print(f"\n[High-Value Investors]: {len(high_value)} listed{qualification}")
    print("=" * 60 + "\n")


def main() -> None:
    parser = argparse.ArgumentParser(description="Automated Mutual Fund Performance Dashboard")
    parser.add_argument("--data-dir", default=".", help="Directory containing the input CSV files")
    parser.add_argument("--out-dir", default="output", help="Directory for generated reports/charts/logs")
    parser.add_argument("--min-investment", type=float, default=1_000_000,
                        help="High-value threshold for total investment (default ₹10 Lakhs)")
    parser.add_argument("--min-transactions", type=int, default=10,
                        help="High-value threshold for transaction count (default 10)")
    parser.add_argument("--min-income", type=float, default=1_500_000,
                        help="High-value threshold for annual income (default ₹15 Lakhs)")
    parser.add_argument("--fallback-top-n", type=int, default=10,
                        help="Investors to list by investment when none meet strict criteria")
    args = parser.parse_args()
    sys.exit(run(
        args.data_dir,
        args.out_dir,
        min_investment=args.min_investment,
        min_transactions=args.min_transactions,
        min_income=args.min_income,
        fallback_top_n=args.fallback_top_n,
    ))


if __name__ == "__main__":
    main()
