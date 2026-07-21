"""
Case Study 3 - Loan Processing & Loan Repayment Analytics
=========================================================
ABC Bank - Loan Processing Department

This script implements Parts 1-10 of the case study end-to-end:
  1. Read data           6. Pandas analysis
  2. Data cleaning       7. GroupBy analysis
  3. Merge datasets      8. Business rules / flags
  4. New columns         9. Finance metrics
  5. NumPy tasks        10. Export reports

-----------------------------------------------------------------
ASSUMPTIONS / DATA NOTES (the provided CSVs differ from the PDF):
-----------------------------------------------------------------
1. Join keys are inconsistent across files, so they are normalised:
     - customers.CustomerID  = 'C101'  while loan_application.CustomerID = 101
       -> a numeric key is derived by stripping the leading 'C'.
     - loan_application.LoanID = 'L1001' while loan_payments.LoanID = 'L101'
       (a consistent offset of 900) -> a matching payment key is derived.
2. 'Credit Score' is NOT present in customers.csv. Because several parts of
   the case study require it, a reproducible synthetic Credit Score is
   generated (seed=42), including a few missing values so that the
   "replace missing Credit Score with mean" cleaning step is demonstrated.
3. 'Amount Paid' / 'Payment Status' are NOT present. They are derived from
   the EMI columns:
     - AmountPaid    = EMIAmount * PaidEMIs
     - PaymentStatus = Paid (0 pending) / Pending (0 paid) / Partial (both)
4. 'PaymentDate' maps to LastPaymentDate.
All derived/assumed fields are clearly commented below.
"""

import os
import numpy as np
import pandas as pd

# Resolve paths relative to this script so it runs from anywhere.
BASE_DIR = os.path.dirname(os.path.abspath(__file__))


def path(name: str) -> str:
    return os.path.join(BASE_DIR, name)


def section(title: str) -> None:
    print("\n" + "=" * 70)
    print(title)
    print("=" * 70)


# ============================================================
# PART 1 - READ DATA
# ============================================================
section("PART 1 - READ DATA")

customers = pd.read_csv(path("customers.csv"))
applications = pd.read_csv(path("loan_application.csv"))
payments = pd.read_csv(path("loan_payments.csv"))

print(f"customers.csv        : {customers.shape[0]} rows, {customers.shape[1]} cols")
print(f"loan_application.csv : {applications.shape[0]} rows, {applications.shape[1]} cols")
print(f"loan_payments.csv    : {payments.shape[0]} rows, {payments.shape[1]} cols")


# ============================================================
# PART 2 - DATA CLEANING
# ============================================================
section("PART 2 - DATA CLEANING")

# --- Remove duplicate records ---
before = len(customers) + len(applications) + len(payments)
customers = customers.drop_duplicates()
applications = applications.drop_duplicates()
payments = payments.drop_duplicates()

# --- Remove duplicate Loan IDs ---
applications = applications.drop_duplicates(subset="LoanID")
payments = payments.drop_duplicates(subset="LoanID")
after = len(customers) + len(applications) + len(payments)
print(f"Duplicate rows removed across all files : {before - after}")

# --- Synthesize Credit Score (absent in source) with some missing values ---
rng = np.random.default_rng(42)
credit_scores = rng.integers(600, 831, size=len(customers)).astype(float)
# Inject a few missing values to demonstrate the mean-fill cleaning step.
missing_idx = rng.choice(len(customers), size=3, replace=False)
credit_scores[missing_idx] = np.nan
customers["CreditScore"] = credit_scores

# --- Check missing values ---
print("\nMissing values per file:")
print("customers:\n", customers.isnull().sum().to_string())
print("applications:\n", applications.isnull().sum().to_string())
print("payments:\n", payments.isnull().sum().to_string())

# --- Replace missing Salary with median salary ---
median_salary = customers["Salary"].median()
customers["Salary"] = customers["Salary"].fillna(median_salary)

# --- Replace missing Credit Score with mean credit score ---
mean_credit = round(customers["CreditScore"].mean())
customers["CreditScore"] = customers["CreditScore"].fillna(mean_credit).astype(int)
print(f"\nMissing Salary filled with median   : {median_salary}")
print(f"Missing CreditScore filled with mean: {mean_credit}")

# --- Convert ApplicationDate and PaymentDate to datetime ---
applications["ApplicationDate"] = pd.to_datetime(
    applications["ApplicationDate"], errors="coerce"
)
# PaymentDate maps to LastPaymentDate in the provided data.
payments["LastPaymentDate"] = pd.to_datetime(
    payments["LastPaymentDate"], errors="coerce"
)

# --- Remove negative Loan Amounts ---
neg_loans = (applications["LoanAmount"] < 0).sum()
applications = applications[applications["LoanAmount"] >= 0]

# --- Remove invalid EMI Amounts (<= 0) ---
bad_emi = (payments["EMIAmount"] <= 0).sum()
payments = payments[payments["EMIAmount"] > 0]

# --- Remove future payment dates ---
today = pd.Timestamp.today().normalize()
future_dates = (payments["LastPaymentDate"] > today).sum()
payments = payments[payments["LastPaymentDate"] <= today]

print(f"Negative loan amounts removed       : {neg_loans}")
print(f"Invalid EMI amounts removed         : {bad_emi}")
print(f"Future payment dates removed        : {future_dates}")


# ============================================================
# PART 3 - MERGE DATASETS
# ============================================================
section("PART 3 - MERGE DATASETS")

# Normalise join keys.
# customers.CustomerID 'C101' -> 101 to match applications.CustomerID (int).
customers["CustKey"] = customers["CustomerID"].str.replace("C", "", regex=False).astype(int)

# applications.LoanID 'L1001' <-> payments.LoanID 'L101' (offset of 900).
applications["PayKey"] = "L" + (
    applications["LoanID"].str[1:].astype(int) - 900
).astype(str)

# Merge customers + applications on the numeric customer key.
merged = applications.merge(
    customers, left_on="CustomerID", right_on="CustKey", how="inner",
    suffixes=("_app", "_cust"),
)

# Merge in payments on the derived payment key.
merged = merged.merge(
    payments, left_on="PayKey", right_on="LoanID", how="inner",
    suffixes=("", "_pay"),
)

# Rename to clean, unambiguous names.
merged = merged.rename(columns={"LoanID": "LoanID"})

# Derived fields (absent in source) -------------------------------------
# Amount Paid so far = per-month EMI * number of EMIs already paid.
merged["AmountPaid"] = merged["EMIAmount"] * merged["PaidEMIs"]
# Payment Status derived from EMI progress.
def payment_status(row):
    if row["PendingEMIs"] == 0:
        return "Paid"
    if row["PaidEMIs"] == 0:
        return "Pending"
    return "Partial"
merged["PaymentStatus"] = merged.apply(payment_status, axis=1)

# Single dataframe with the case-study required columns.
loan_df = merged[[
    "LoanID", "CustomerName", "City", "LoanType", "LoanAmount",
    "CreditScore", "Salary", "LoanStatus", "EMIAmount", "PaidEMIs",
    "PendingEMIs", "AmountPaid", "PaymentStatus",
]].copy()

print(f"Merged dataset: {loan_df.shape[0]} rows, {loan_df.shape[1]} columns")
print("\nColumns:", ", ".join(loan_df.columns))
print("\nPreview:")
print(loan_df.head(10).to_string(index=False))


# ============================================================
# PART 4 - CREATE NEW COLUMNS
# ============================================================
section("PART 4 - CREATE NEW COLUMNS")

loan_df["MonthlyIncome"] = loan_df["Salary"] / 12
loan_df["DebtToIncomeRatio"] = loan_df["LoanAmount"] / loan_df["Salary"]
# EMI Due = outstanding EMI amount = per-month EMI * pending EMIs
# (adapted from the PDF's "EMI Amount - Amount Paid" to fit the EMI-count data).
loan_df["EMIDue"] = loan_df["EMIAmount"] * loan_df["PendingEMIs"]
# Payment Completion % = paid EMIs / total EMIs * 100
total_emis = loan_df["PaidEMIs"] + loan_df["PendingEMIs"]
loan_df["PaymentCompletionPct"] = (loan_df["PaidEMIs"] / total_emis * 100).round(2)

print(loan_df[[
    "LoanID", "MonthlyIncome", "DebtToIncomeRatio", "EMIDue", "PaymentCompletionPct",
]].head(10).to_string(index=False))


# ============================================================
# PART 5 - NUMPY TASKS
# ============================================================
section("PART 5 - NUMPY TASKS (Loan Amount statistics)")

amounts = loan_df["LoanAmount"].to_numpy()
print(f"Average Loan Amount     : {np.mean(amounts):,.2f}")
print(f"Median Loan Amount      : {np.median(amounts):,.2f}")
print(f"Maximum Loan Amount     : {np.max(amounts):,.2f}")
print(f"Minimum Loan Amount     : {np.min(amounts):,.2f}")
print(f"Standard Deviation      : {np.std(amounts):,.2f}")
print(f"Variance                : {np.var(amounts):,.2f}")
print(f"25th Percentile         : {np.percentile(amounts, 25):,.2f}")
print(f"75th Percentile         : {np.percentile(amounts, 75):,.2f}")


# ============================================================
# PART 6 - PANDAS ANALYSIS
# ============================================================
section("PART 6 - PANDAS ANALYSIS")

top10_loans = loan_df.nlargest(10, "LoanAmount")[["CustomerName", "City", "LoanType", "LoanAmount"]]
top10_salary = loan_df.nlargest(10, "Salary")[["CustomerName", "City", "Salary"]]
low_credit = loan_df[loan_df["CreditScore"] < 650][["CustomerName", "CreditScore", "LoanStatus"]]
big_loans = loan_df[loan_df["LoanAmount"] > 2_000_000][["CustomerName", "LoanType", "LoanAmount"]]
pending_payments = loan_df[loan_df["PendingEMIs"] > 0][["CustomerName", "LoanID", "EMIDue", "PaymentStatus"]]
fully_paid = loan_df[loan_df["PendingEMIs"] == 0][["CustomerName", "LoanID", "PaymentStatus"]]

print("\nTop 10 highest loan customers:")
print(top10_loans.to_string(index=False))
print("\nTop 10 customers by salary:")
print(top10_salary.to_string(index=False))
print(f"\nCustomers with Credit Score below 650: {len(low_credit)}")
print(low_credit.to_string(index=False))
print(f"\nCustomers with Loan Amount > Rs.20 Lakhs: {len(big_loans)}")
print(big_loans.to_string(index=False))
print(f"\nLoans with Pending Payments: {len(pending_payments)}")
print(f"Fully Paid Loans          : {len(fully_paid)}")


# ============================================================
# PART 7 - GROUPBY
# ============================================================
section("PART 7 - GROUPBY ANALYSIS")

city_summary = loan_df.groupby("City").agg(
    NumberOfCustomers=("CustomerName", "count"),
    AverageSalary=("Salary", "mean"),
    TotalLoanAmount=("LoanAmount", "sum"),
).round(2).sort_values("TotalLoanAmount", ascending=False)

loantype_summary = loan_df.groupby("LoanType").agg(
    NumberOfLoans=("LoanID", "count"),
    AverageLoanAmount=("LoanAmount", "mean"),
    TotalLoanAmount=("LoanAmount", "sum"),
).round(2).sort_values("TotalLoanAmount", ascending=False)

loanstatus_summary = loan_df.groupby("LoanStatus").agg(
    NumberOfLoans=("LoanID", "count"),
    TotalLoanAmount=("LoanAmount", "sum"),
)

paymentstatus_summary = loan_df.groupby("PaymentStatus").agg(
    Count=("LoanID", "count"),
    TotalAmountPaid=("AmountPaid", "sum"),
)

print("\nCity-wise summary:")
print(city_summary.to_string())
print("\nLoan Type summary:")
print(loantype_summary.to_string())
print("\nLoan Status summary (Approved / Pending / Rejected):")
print(loanstatus_summary.to_string())
print("\nPayment Status summary (Paid / Partial / Pending):")
print(paymentstatus_summary.to_string())


# ============================================================
# PART 8 - BUSINESS RULES / FLAGS
# ============================================================
section("PART 8 - BUSINESS RULES (Risk Flags)")

loan_df["Flag_HighLoan"] = loan_df["LoanAmount"] > 3_000_000
loan_df["Flag_LowCredit"] = loan_df["CreditScore"] < 650
loan_df["Flag_LowSalary"] = loan_df["Salary"] < 30_000
loan_df["Flag_HighDTI"] = loan_df["DebtToIncomeRatio"] > 5
loan_df["Flag_HighEMIDue"] = loan_df["EMIDue"] > 10_000
loan_df["Flag_PendingPayment"] = loan_df["PaymentStatus"] == "Pending"
loan_df["Flag_Rejected"] = loan_df["LoanStatus"] == "Rejected"

flag_cols = [c for c in loan_df.columns if c.startswith("Flag_")]
loan_df["RiskFlagCount"] = loan_df[flag_cols].sum(axis=1)

print("Flag counts:")
for c in flag_cols:
    print(f"  {c:<22}: {int(loan_df[c].sum())}")

print("\nHigh-risk loans (>=2 flags):")
high_risk = loan_df[loan_df["RiskFlagCount"] >= 2][
    ["CustomerName", "LoanType", "LoanAmount", "CreditScore", "RiskFlagCount"]
]
print(high_risk.to_string(index=False))


# ============================================================
# PART 9 - FINANCE METRICS
# ============================================================
section("PART 9 - FINANCE METRICS")

total_portfolio = loan_df["LoanAmount"].sum()
total_collected = loan_df["AmountPaid"].sum()
outstanding = (loan_df["LoanAmount"] - loan_df["AmountPaid"]).sum()
recovery_pct = total_collected / total_portfolio * 100
pending_loans = (loan_df["LoanStatus"] == "Pending").sum()
default_pct = pending_loans / len(loan_df) * 100
avg_emi = loan_df["EMIAmount"].mean()
avg_credit = loan_df["CreditScore"].mean()

metrics = {
    "Total Loan Portfolio": total_portfolio,
    "Total Amount Collected": total_collected,
    "Outstanding Amount": outstanding,
    "Loan Recovery %": round(recovery_pct, 2),
    "Default %": round(default_pct, 2),
    "Average EMI": round(avg_emi, 2),
    "Average Credit Score": round(avg_credit, 2),
}
for k, v in metrics.items():
    print(f"{k:<24}: {v:,.2f}" if isinstance(v, float) else f"{k:<24}: {v:,}")


# ============================================================
# PART 10 - EXPORT REPORTS
# ============================================================
section("PART 10 - EXPORT REPORTS")

# LoanSummary.xlsx -> aggregated summaries across multiple sheets.
loan_summary_path = path("LoanSummary.xlsx")
with pd.ExcelWriter(loan_summary_path, engine="openpyxl") as writer:
    city_summary.to_excel(writer, sheet_name="City Summary")
    loantype_summary.to_excel(writer, sheet_name="Loan Type Summary")
    loanstatus_summary.to_excel(writer, sheet_name="Loan Status Summary")
    paymentstatus_summary.to_excel(writer, sheet_name="Payment Status Summary")
    pd.DataFrame(list(metrics.items()), columns=["Metric", "Value"]).to_excel(
        writer, sheet_name="Finance Metrics", index=False
    )

# CustomerLoanReport.xlsx -> full per-loan detail.
customer_report_path = path("CustomerLoanReport.xlsx")
loan_df.to_excel(customer_report_path, sheet_name="Customer Loan Report", index=False)

# PendingPayments.csv -> all loans with outstanding EMIs.
pending_path = path("PendingPayments.csv")
pending_export = loan_df[loan_df["PendingEMIs"] > 0][[
    "LoanID", "CustomerName", "City", "LoanType", "LoanAmount",
    "EMIAmount", "PaidEMIs", "PendingEMIs", "EMIDue",
    "PaymentCompletionPct", "PaymentStatus",
]]
pending_export.to_csv(pending_path, index=False)

print(f"Created: {os.path.basename(loan_summary_path)}")
print(f"Created: {os.path.basename(customer_report_path)}")
print(f"Created: {os.path.basename(pending_path)}")


# ============================================================
# EXPECTED OUTPUTS (final display)
# ============================================================
section("EXPECTED OUTPUTS")

print("\n--- Top 10 Loan Customers ---")
print(top10_loans.to_string(index=False))
print("\n--- Customers with Low Credit Score (<650) ---")
print(low_credit.to_string(index=False))
print("\n--- Pending Loan Payments ---")
print(pending_export.head(10).to_string(index=False))
print("\n--- City-wise Loan Summary ---")
print(city_summary.to_string())
print("\n--- Loan Type Summary ---")
print(loantype_summary.to_string())
print("\n--- Loan Recovery Report ---")
print(f"Total Loan Portfolio : Rs. {total_portfolio:,.2f}")
print(f"Total Collected      : Rs. {total_collected:,.2f}")
print(f"Outstanding Amount   : Rs. {outstanding:,.2f}")
print(f"Loan Recovery %      : {recovery_pct:.2f}%")

print("\nDone. All parts completed successfully.")
