"""
Case Study 1: Credit Risk & Loan Portfolio Analysis
=====================================================

End-to-end pipeline that:
  1. Reads the three source CSV files with exception handling for corrupted files.
  2. Computes portfolio statistics with NumPy.
  3. Merges the datasets, cleans missing values, and removes outliers with Pandas.
  4. Identifies the top risky customers.
  5. Calculates finance/risk metrics.
  6. Exports risk_report.xlsx, high_risk_customers.csv and summary.json.

Run:
    python credit_risk_analysis.py
"""

from __future__ import annotations

import json
import os
from dataclasses import dataclass, field

import numpy as np
import pandas as pd

# --------------------------------------------------------------------------- #
# Configuration
# --------------------------------------------------------------------------- #
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

CUSTOMERS_FILE = os.path.join(BASE_DIR, "customers.csv")
LOANS_FILE = os.path.join(BASE_DIR, "loans.csv")
CREDIT_SCORES_FILE = os.path.join(BASE_DIR, "credit_scores.csv")

RISK_REPORT_FILE = os.path.join(BASE_DIR, "risk_report.xlsx")
HIGH_RISK_FILE = os.path.join(BASE_DIR, "high_risk_customers.csv")
SUMMARY_FILE = os.path.join(BASE_DIR, "summary.json")

# Assumptions used by the finance metrics.
LGD = 0.45            # Loss Given Default (industry standard baseline)
SALARY_IS_ANNUAL = True  # Salary column is treated as an annual figure.


# --------------------------------------------------------------------------- #
# OOP: Loan class
# --------------------------------------------------------------------------- #
@dataclass
class Loan:
    """Represents a single loan and exposes derived risk calculations."""

    loan_id: str
    customer_id: int
    loan_amount: float
    interest_rate: float
    tenure: int
    emi: float
    paid_emis: int
    default_flag: int
    salary: float = 0.0
    credit_score: float = 0.0
    _monthly_income: float = field(init=False, default=0.0)

    def __post_init__(self) -> None:
        self._monthly_income = (self.salary / 12.0) if SALARY_IS_ANNUAL else self.salary

    @property
    def outstanding_emis(self) -> int:
        """Number of EMIs still to be paid."""
        return max(self.tenure - self.paid_emis, 0)

    @property
    def outstanding_balance(self) -> float:
        """Approximate remaining principal exposure (EAD)."""
        return self.emi * self.outstanding_emis

    def debt_to_income(self) -> float:
        """Monthly EMI as a fraction of monthly income."""
        if self._monthly_income <= 0:
            return np.nan
        return self.emi / self._monthly_income

    def loan_utilization(self) -> float:
        """Fraction of the loan tenure that is still outstanding."""
        if self.tenure <= 0:
            return np.nan
        return self.outstanding_emis / self.tenure

    def expected_loss(self, lgd: float = LGD) -> float:
        """Expected Loss = PD x LGD x EAD (PD proxied by the default flag)."""
        pd_ = 1.0 if self.default_flag == 1 else 0.0
        return pd_ * lgd * self.outstanding_balance

    def is_high_risk(self) -> bool:
        """Business rule for a high-risk customer."""
        return (
            self.credit_score < 650
            and self.salary < 60000
            and self.loan_amount > 1_000_000
            and self.default_flag == 1
        )


# --------------------------------------------------------------------------- #
# Data loading with exception handling
# --------------------------------------------------------------------------- #
def read_csv_safe(path: str, required_columns: list[str]) -> pd.DataFrame:
    """Read a CSV file, guarding against missing/corrupted files."""
    if not os.path.exists(path):
        raise FileNotFoundError(f"Input file not found: {path}")

    try:
        df = pd.read_csv(path)
    except pd.errors.EmptyDataError as exc:
        raise ValueError(f"File is empty or corrupted: {path}") from exc
    except pd.errors.ParserError as exc:
        raise ValueError(f"File could not be parsed (corrupted): {path}") from exc
    except UnicodeDecodeError as exc:
        raise ValueError(f"File has an invalid encoding: {path}") from exc

    missing = [c for c in required_columns if c not in df.columns]
    if missing:
        raise ValueError(f"{os.path.basename(path)} is missing columns: {missing}")

    return df


def load_data() -> tuple[pd.DataFrame, pd.DataFrame, pd.DataFrame]:
    """Load the three source datasets."""
    customers = read_csv_safe(CUSTOMERS_FILE, ["CustomerID", "Age", "Salary", "City"])
    loans = read_csv_safe(
        LOANS_FILE,
        ["LoanID", "CustomerID", "LoanAmount", "InterestRate",
         "Tenure", "EMI", "PaidEMIs", "DefaultFlag"],
    )
    credit_scores = read_csv_safe(CREDIT_SCORES_FILE, ["CustomerID", "CreditScore"])
    return customers, loans, credit_scores


# --------------------------------------------------------------------------- #
# NumPy statistics
# --------------------------------------------------------------------------- #
def compute_numpy_stats(df: pd.DataFrame) -> dict:
    """Portfolio statistics computed with NumPy."""
    loan_amount = df["LoanAmount"].to_numpy(dtype=float)
    salary = df["Salary"].to_numpy(dtype=float)
    interest_rate = df["InterestRate"].to_numpy(dtype=float)

    return {
        "mean_loan_amount": float(np.mean(loan_amount)),
        "median_salary": float(np.median(salary)),
        "interest_rate_25th_percentile": float(np.percentile(interest_rate, 25)),
        "interest_rate_50th_percentile": float(np.percentile(interest_rate, 50)),
        "interest_rate_75th_percentile": float(np.percentile(interest_rate, 75)),
        "interest_rate_90th_percentile": float(np.percentile(interest_rate, 90)),
        "salary_loan_correlation": float(np.corrcoef(salary, loan_amount)[0, 1]),
        "loan_amount_std": float(np.std(loan_amount)),
        "salary_std": float(np.std(salary)),
    }


# --------------------------------------------------------------------------- #
# Pandas: merge, clean, outliers
# --------------------------------------------------------------------------- #
def merge_datasets(
    customers: pd.DataFrame, loans: pd.DataFrame, credit_scores: pd.DataFrame
) -> pd.DataFrame:
    """Merge customers + loans + credit scores on CustomerID."""
    df = loans.merge(customers, on="CustomerID", how="left")
    df = df.merge(credit_scores, on="CustomerID", how="left")
    return df


def handle_missing_data(df: pd.DataFrame) -> pd.DataFrame:
    """Replace missing values per the case-study rules."""
    df = df.copy()
    # Salary -> median
    df["Salary"] = df["Salary"].fillna(df["Salary"].median())
    # Credit Score -> mean
    df["CreditScore"] = df["CreditScore"].fillna(df["CreditScore"].mean())
    # Interest Rate -> previous value (forward fill, then back fill for a leading gap)
    df["InterestRate"] = df["InterestRate"].ffill().bfill()
    return df


def remove_outliers(df: pd.DataFrame) -> pd.DataFrame:
    """Remove loans whose amount exceeds the 99th percentile."""
    threshold = np.percentile(df["LoanAmount"].to_numpy(dtype=float), 99)
    return df[df["LoanAmount"] <= threshold].copy()


# --------------------------------------------------------------------------- #
# Risk metrics on the merged frame
# --------------------------------------------------------------------------- #
def build_loans(df: pd.DataFrame) -> list[Loan]:
    """Instantiate Loan objects from the cleaned dataframe."""
    loans: list[Loan] = []
    for row in df.itertuples(index=False):
        loans.append(
            Loan(
                loan_id=row.LoanID,
                customer_id=int(row.CustomerID),
                loan_amount=float(row.LoanAmount),
                interest_rate=float(row.InterestRate),
                tenure=int(row.Tenure),
                emi=float(row.EMI),
                paid_emis=int(row.PaidEMIs),
                default_flag=int(row.DefaultFlag),
                salary=float(row.Salary),
                credit_score=float(row.CreditScore),
            )
        )
    return loans


def add_finance_metrics(df: pd.DataFrame) -> pd.DataFrame:
    """Add per-loan finance metrics using the Loan class."""
    df = df.copy()
    loans = build_loans(df)

    df["DebtToIncome"] = [ln.debt_to_income() for ln in loans]
    df["LoanUtilization"] = [ln.loan_utilization() for ln in loans]
    df["OutstandingBalance"] = [ln.outstanding_balance for ln in loans]
    df["ExpectedLoss"] = [ln.expected_loss() for ln in loans]
    df["IsHighRisk"] = [ln.is_high_risk() for ln in loans]
    return df


def compute_portfolio_metrics(df: pd.DataFrame) -> dict:
    """Aggregate portfolio-level finance metrics."""
    total_loans = len(df)
    total_exposure = float(df["LoanAmount"].sum())

    default_count = int((df["DefaultFlag"] == 1).sum())
    default_pct = (default_count / total_loans * 100.0) if total_loans else 0.0

    # NPA % = share of loan value that is in default.
    npa_amount = float(df.loc[df["DefaultFlag"] == 1, "LoanAmount"].sum())
    npa_pct = (npa_amount / total_exposure * 100.0) if total_exposure else 0.0

    return {
        "total_loans": total_loans,
        "total_exposure": total_exposure,
        "average_dti": float(df["DebtToIncome"].mean()),
        "average_loan_utilization": float(df["LoanUtilization"].mean()),
        "default_pct": default_pct,
        "npa_pct": npa_pct,
        "average_emi": float(df["EMI"].mean()),
        "total_expected_loss": float(df["ExpectedLoss"].sum()),
        "average_expected_loss": float(df["ExpectedLoss"].mean()),
    }


def find_top_risky(df: pd.DataFrame, top_n: int = 20) -> pd.DataFrame:
    """Customers matching the high-risk rule, ranked by expected loss."""
    risky = df[
        (df["CreditScore"] < 650)
        & (df["Salary"] < 60000)
        & (df["LoanAmount"] > 1_000_000)
        & (df["DefaultFlag"] == 1)
    ].copy()

    # If the strict rule is too narrow, fall back to a composite risk ranking.
    if len(risky) < top_n:
        scored = df.copy()
        scored["RiskScore"] = (
            (scored["CreditScore"] < 650).astype(int)
            + (scored["Salary"] < 60000).astype(int)
            + (scored["LoanAmount"] > 1_000_000).astype(int)
            + (scored["DefaultFlag"] == 1).astype(int) * 2
        )
        risky = scored.sort_values(
            ["RiskScore", "ExpectedLoss"], ascending=False
        ).head(top_n)
    else:
        risky = risky.sort_values("ExpectedLoss", ascending=False).head(top_n)

    return risky


# --------------------------------------------------------------------------- #
# Output automation
# --------------------------------------------------------------------------- #
def export_outputs(
    portfolio: pd.DataFrame,
    top_risky: pd.DataFrame,
    numpy_stats: dict,
    portfolio_metrics: dict,
) -> None:
    """Write risk_report.xlsx, high_risk_customers.csv and summary.json."""
    # 1. Excel report with multiple sheets.
    with pd.ExcelWriter(RISK_REPORT_FILE, engine="openpyxl") as writer:
        portfolio.to_excel(writer, sheet_name="Portfolio", index=False)
        top_risky.to_excel(writer, sheet_name="Top20_Risky", index=False)

        stats_df = pd.DataFrame(
            list(numpy_stats.items()), columns=["Metric", "Value"]
        )
        stats_df.to_excel(writer, sheet_name="NumPy_Stats", index=False)

        metrics_df = pd.DataFrame(
            list(portfolio_metrics.items()), columns=["Metric", "Value"]
        )
        metrics_df.to_excel(writer, sheet_name="Finance_Metrics", index=False)

    # 2. High-risk customers CSV.
    top_risky.to_csv(HIGH_RISK_FILE, index=False)

    # 3. JSON summary.
    summary = {
        "numpy_statistics": numpy_stats,
        "finance_metrics": portfolio_metrics,
        "high_risk_customer_count": int(len(top_risky)),
        "high_risk_customer_ids": top_risky["CustomerID"].tolist(),
    }
    with open(SUMMARY_FILE, "w", encoding="utf-8") as fh:
        json.dump(summary, fh, indent=2)


# --------------------------------------------------------------------------- #
# Orchestration
# --------------------------------------------------------------------------- #
def main() -> None:
    print("Loading data...")
    customers, loans, credit_scores = load_data()

    print("Merging datasets...")
    merged = merge_datasets(customers, loans, credit_scores)

    print("Handling missing data...")
    merged = handle_missing_data(merged)

    print("Removing outliers (LoanAmount > 99th percentile)...")
    before = len(merged)
    merged = remove_outliers(merged)
    print(f"  Removed {before - len(merged)} outlier rows.")

    print("Computing NumPy statistics...")
    numpy_stats = compute_numpy_stats(merged)

    print("Computing finance metrics...")
    portfolio = add_finance_metrics(merged)
    portfolio_metrics = compute_portfolio_metrics(portfolio)

    print("Identifying top 20 risky customers...")
    top_risky = find_top_risky(portfolio, top_n=20)

    print("Exporting outputs...")
    export_outputs(portfolio, top_risky, numpy_stats, portfolio_metrics)

    print("\n=== NumPy Statistics ===")
    for k, v in numpy_stats.items():
        print(f"  {k}: {v:,.4f}")

    print("\n=== Finance Metrics ===")
    for k, v in portfolio_metrics.items():
        print(f"  {k}: {v:,.4f}")

    print("\nDone. Generated:")
    print(f"  - {RISK_REPORT_FILE}")
    print(f"  - {HIGH_RISK_FILE}")
    print(f"  - {SUMMARY_FILE}")


if __name__ == "__main__":
    try:
        main()
    except (FileNotFoundError, ValueError) as exc:
        print(f"[ERROR] {exc}")
        raise
