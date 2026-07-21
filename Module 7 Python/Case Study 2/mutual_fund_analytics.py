"""
Case Study 2: Mutual Fund Performance Analytics
ABC Asset Management Company (AMC)

Covers:
  Part 1  - Read Data
  Part 2  - Data Cleaning
  Part 3  - Merge Data
  Part 4  - Create New Columns
  Part 5  - NumPy Tasks
  Part 6  - Pandas Analysis
  Part 7  - GroupBy
  Part 8  - Detect Issues
  Part 9  - Finance Metrics
  Part 10 - Export Reports
"""

import os
import numpy as np
import pandas as pd

# Directory of this script so it can be run from anywhere.
BASE_DIR = os.path.dirname(os.path.abspath(__file__))

RISK_FREE_RATE = 6.0  # % (used for the Sharpe Ratio in Part 9)


def _path(filename):
    """Build an absolute path next to this script."""
    return os.path.join(BASE_DIR, filename)


def _banner(title):
    print("\n" + "=" * 70)
    print(title)
    print("=" * 70)


# ---------------------------------------------------------------------------
# Part 1 - Read Data
# ---------------------------------------------------------------------------
def read_data():
    """Read all four CSV files using Pandas with exception handling."""
    _banner("PART 1 - READ DATA")
    files = {
        "funds": "funds.csv",
        "investors": "investors.csv",
        "transactions": "transactions.csv",
        "nav_history": "nav_history.csv",
    }
    data = {}
    for key, name in files.items():
        try:
            df = pd.read_csv(_path(name))
            data[key] = df
            print(f"Loaded {name:<18} -> {df.shape[0]} rows, {df.shape[1]} columns")
        except FileNotFoundError:
            print(f"ERROR: {name} not found.")
            raise
        except pd.errors.EmptyDataError:
            print(f"ERROR: {name} is empty.")
            raise
    return data["funds"], data["investors"], data["transactions"], data["nav_history"]


# ---------------------------------------------------------------------------
# Part 2 - Data Cleaning
# ---------------------------------------------------------------------------
def clean_data(funds, investors, transactions, nav_history):
    """Remove duplicates, handle missing values and fix data types."""
    _banner("PART 2 - DATA CLEANING")

    # Strip stray whitespace/tabs from all text columns (e.g. "Corporate\t\t").
    for df in (funds, investors, transactions, nav_history):
        for col in df.select_dtypes(include="object").columns:
            df[col] = df[col].str.strip()

    # Remove duplicate rows from every dataset.
    funds = funds.drop_duplicates()
    investors = investors.drop_duplicates()
    transactions = transactions.drop_duplicates()
    nav_history = nav_history.drop_duplicates()

    # Check missing values (before cleaning).
    print("Missing values before cleaning:")
    for name, df in [("funds", funds), ("investors", investors),
                     ("transactions", transactions), ("nav_history", nav_history)]:
        print(f"  {name}: {int(df.isnull().sum().sum())} missing")

    # Convert Date columns into datetime format.
    nav_history["Date"] = pd.to_datetime(nav_history["Date"], errors="coerce")
    transactions["PurchaseDate"] = pd.to_datetime(
        transactions["PurchaseDate"], errors="coerce"
    )

    # Fill missing NAV using Forward Fill (per fund, ordered by date).
    nav_history = nav_history.sort_values(["FundID", "Date"])
    nav_history["NAV"] = nav_history.groupby("FundID")["NAV"].ffill()
    nav_history["NAV"] = nav_history["NAV"].ffill()  # safety for leading gaps

    # Replace missing InvestorType with "Retail" (also treat blanks as missing).
    investors["InvestorType"] = investors["InvestorType"].replace("", np.nan).fillna("Retail")

    # Remove rows having negative NAV.
    before = len(nav_history)
    nav_history = nav_history[nav_history["NAV"] >= 0]
    print(f"Removed {before - len(nav_history)} row(s) with negative NAV.")

    print("Missing values after cleaning:")
    for name, df in [("funds", funds), ("investors", investors),
                     ("transactions", transactions), ("nav_history", nav_history)]:
        print(f"  {name}: {int(df.isnull().sum().sum())} missing")

    return funds, investors, transactions, nav_history


def latest_nav_table(nav_history):
    """Return the most recent NAV per fund."""
    idx = nav_history.groupby("FundID")["Date"].idxmax()
    latest = nav_history.loc[idx, ["FundID", "NAV"]].rename(columns={"NAV": "LatestNAV"})
    return latest.reset_index(drop=True)


# ---------------------------------------------------------------------------
# Part 3 - Merge Data
# ---------------------------------------------------------------------------
def merge_data(funds, investors, transactions, nav_history):
    """Merge all four datasets into one DataFrame."""
    _banner("PART 3 - MERGE DATA")

    latest = latest_nav_table(nav_history)

    df = (
        transactions
        .merge(investors, on="InvestorID", how="left")
        .merge(funds, on="FundID", how="left")
        .merge(latest, on="FundID", how="left")
    )

    df = df.rename(columns={
        "InvestorName": "Investor Name",
        "FundName": "Fund Name",
        "UnitsPurchased": "Units Purchased",
        "PurchaseNAV": "Purchase NAV",
        "LatestNAV": "Latest NAV",
    })

    required = ["Investor Name", "Fund Name", "Category", "AMC", "State",
                "Units Purchased", "Purchase NAV", "Latest NAV"]
    print("Merged DataFrame shape:", df.shape)
    print(df[required].head(10).to_string(index=False))
    return df


# ---------------------------------------------------------------------------
# Part 4 - Create New Columns
# ---------------------------------------------------------------------------
def add_calculated_columns(df):
    """Add Investment Amount, Current Value, Profit and ROI %."""
    _banner("PART 4 - CREATE NEW COLUMNS")

    df["Investment Amount"] = df["Units Purchased"] * df["Purchase NAV"]
    df["Current Value"] = df["Units Purchased"] * df["Latest NAV"]
    df["Profit"] = df["Current Value"] - df["Investment Amount"]
    df["ROI %"] = (df["Profit"] / df["Investment Amount"]) * 100

    cols = ["Investor Name", "Fund Name", "Investment Amount",
            "Current Value", "Profit", "ROI %"]
    print(df[cols].head(10).round(2).to_string(index=False))
    return df


# ---------------------------------------------------------------------------
# Part 5 - NumPy Tasks
# ---------------------------------------------------------------------------
def numpy_tasks(nav_history):
    """Statistics on NAV using NumPy."""
    _banner("PART 5 - NUMPY TASKS")

    nav = nav_history["NAV"].to_numpy(dtype=float)

    print(f"Average NAV           : {np.mean(nav):.4f}")
    print(f"Maximum NAV           : {np.max(nav):.4f}")
    print(f"Minimum NAV           : {np.min(nav):.4f}")
    print(f"Variance of NAV       : {np.var(nav):.4f}")
    print(f"Standard Deviation NAV: {np.std(nav):.4f}")

    # Rolling average (window = 5) using NumPy convolution.
    window = 5
    if len(nav) >= window:
        rolling = np.convolve(nav, np.ones(window) / window, mode="valid")
        print(f"Rolling Average (w=5) : first 5 -> "
              f"{np.round(rolling[:5], 4).tolist()}")
    else:
        print("Not enough data points for a rolling average of window 5.")


# ---------------------------------------------------------------------------
# Part 6 - Pandas Analysis
# ---------------------------------------------------------------------------
def pandas_analysis(df):
    """Top investors, funds and best/worst NAV analysis."""
    _banner("PART 6 - PANDAS ANALYSIS")

    top_investors = (
        df.groupby("Investor Name")["Investment Amount"].sum()
        .sort_values(ascending=False).head(5)
    )
    print("Top 5 investors by investment amount:")
    print(top_investors.round(2).to_string())

    top_funds = (
        df.groupby("Fund Name")["Profit"].sum()
        .sort_values(ascending=False).head(5)
    )
    print("\nTop 5 profitable funds:")
    print(top_funds.round(2).to_string())

    fund_profit = df.groupby("Fund Name")["Profit"].sum()
    print(f"\nWorst performing fund : {fund_profit.idxmin()} "
          f"({fund_profit.min():.2f})")

    fund_nav = df.groupby("Fund Name")["Latest NAV"].mean()
    print(f"Highest NAV fund      : {fund_nav.idxmax()} ({fund_nav.max():.2f})")
    print(f"Lowest NAV fund       : {fund_nav.idxmin()} ({fund_nav.min():.2f})")

    return top_investors, top_funds


# ---------------------------------------------------------------------------
# Part 7 - GroupBy
# ---------------------------------------------------------------------------
def groupby_analysis(df):
    """Aggregations grouped by Category, AMC, State and Investor Type."""
    _banner("PART 7 - GROUPBY")

    by_category = df.groupby("Category").agg(
        Average_ROI=("ROI %", "mean"),
        Average_NAV=("Latest NAV", "mean"),
        Total_Investment=("Investment Amount", "sum"),
    ).round(2)
    print("Group by Category:")
    print(by_category.to_string())

    by_amc = df.groupby("AMC").agg(
        Number_of_Funds=("Fund Name", "nunique"),
        Average_NAV=("Latest NAV", "mean"),
        Total_Investment=("Investment Amount", "sum"),
    ).round(2)
    print("\nGroup by AMC:")
    print(by_amc.to_string())

    by_state = df.groupby("State").agg(
        Number_of_Investors=("Investor Name", "nunique"),
        Total_Investment=("Investment Amount", "sum"),
        Average_ROI=("ROI %", "mean"),
    ).round(2)
    print("\nGroup by State:")
    print(by_state.to_string())

    by_type = df.groupby("InvestorType").agg(
        Total_Investment=("Investment Amount", "sum"),
        Average_Profit=("Profit", "mean"),
    ).round(2)
    print("\nGroup by Investor Type:")
    print(by_type.to_string())

    return by_category, by_amc, by_state, by_type


# ---------------------------------------------------------------------------
# Part 8 - Detect Issues
# ---------------------------------------------------------------------------
def detect_issues(funds, investors, transactions, nav_history):
    """Data-quality checks across the raw datasets."""
    _banner("PART 8 - DETECT ISSUES")

    dup_nav = nav_history[nav_history.duplicated(subset=["FundID", "Date"], keep=False)]
    print(f"Duplicate NAV records : {len(dup_nav)}")

    neg_nav = nav_history[nav_history["NAV"] < 0]
    print(f"Negative NAV records  : {len(neg_nav)}")

    today = pd.Timestamp.now().normalize()
    future_nav = nav_history[nav_history["Date"] > today]
    future_txn = transactions[transactions["PurchaseDate"] > today]
    print(f"Future dates (NAV)    : {len(future_nav)}")
    print(f"Future dates (Txn)    : {len(future_txn)}")

    valid_fund_ids = set(funds["FundID"])
    valid_investor_ids = set(investors["InvestorID"])
    missing_funds = transactions[~transactions["FundID"].isin(valid_fund_ids)]
    missing_investors = transactions[~transactions["InvestorID"].isin(valid_investor_ids)]
    print(f"Missing Fund IDs      : {len(missing_funds)}")
    print(f"Missing Investor IDs  : {len(missing_investors)}")

    invalid_purchase = transactions[transactions["PurchaseNAV"] < 0]
    print(f"Invalid Purchase NAV  : {len(invalid_purchase)}")


# ---------------------------------------------------------------------------
# Part 9 - Finance Metrics
# ---------------------------------------------------------------------------
def finance_metrics(df, nav_history):
    """Per-transaction financial metrics plus Sharpe Ratio."""
    _banner("PART 9 - FINANCE METRICS")

    df["ROI"] = (df["Current Value"] - df["Investment Amount"]) / df["Investment Amount"] * 100
    df["Absolute Return"] = df["Current Value"] - df["Investment Amount"]
    # Holding period assumed to be 1 year, so annual return equals ROI.
    df["Annual Return"] = df["ROI"]

    # Volatility from NAV using NumPy standard deviation.
    volatility = np.std(nav_history["NAV"].to_numpy(dtype=float))
    avg_return = df["ROI"].mean()
    sharpe = (avg_return - RISK_FREE_RATE) / volatility if volatility else np.nan

    print(f"Average ROI           : {avg_return:.2f}%")
    print(f"Volatility (std NAV)  : {volatility:.4f}")
    print(f"Risk Free Rate        : {RISK_FREE_RATE:.2f}%")
    print(f"Sharpe Ratio          : {sharpe:.4f}")
    print("\nSample finance metrics:")
    print(df[["Investor Name", "Fund Name", "ROI", "Absolute Return",
              "Annual Return"]].head(10).round(2).to_string(index=False))
    return df


# ---------------------------------------------------------------------------
# Part 10 - Export Reports
# ---------------------------------------------------------------------------
def export_reports(df, by_category):
    """Generate the required Excel and CSV reports."""
    _banner("PART 10 - EXPORT REPORTS")

    # TopFunds.xlsx - funds ranked by ROI, Profit and NAV.
    top_funds = df.groupby("Fund Name").agg(
        Category=("Category", "first"),
        AMC=("AMC", "first"),
        Total_Investment=("Investment Amount", "sum"),
        Total_Profit=("Profit", "sum"),
        Average_ROI=("ROI %", "mean"),
        Latest_NAV=("Latest NAV", "mean"),
    ).round(2).sort_values("Average_ROI", ascending=False).reset_index()
    top_funds.to_excel(_path("TopFunds.xlsx"), index=False)
    print("Wrote TopFunds.xlsx")

    # InvestorSummary.xlsx - one row per investor.
    investor_summary = df.groupby("Investor Name").agg(
        State=("State", "first"),
        InvestorType=("InvestorType", "first"),
        Total_Investment=("Investment Amount", "sum"),
        Current_Value=("Current Value", "sum"),
        Total_Profit=("Profit", "sum"),
        Average_ROI=("ROI %", "mean"),
    ).round(2).sort_values("Total_Investment", ascending=False).reset_index()
    investor_summary.to_excel(_path("InvestorSummary.xlsx"), index=False)
    print("Wrote InvestorSummary.xlsx")

    # CategorySummary.csv
    by_category.to_csv(_path("CategorySummary.csv"))
    print("Wrote CategorySummary.csv")


# ---------------------------------------------------------------------------
# Expected Outputs summary
# ---------------------------------------------------------------------------
def expected_outputs(df):
    _banner("EXPECTED OUTPUTS")

    fund = df.groupby("Fund Name").agg(
        ROI=("ROI %", "mean"),
        Profit=("Profit", "sum"),
        NAV=("Latest NAV", "mean"),
    )
    print(f"Top Performing Fund (Highest ROI)   : {fund['ROI'].idxmax()} "
          f"({fund['ROI'].max():.2f}%)")
    print(f"Top Performing Fund (Highest Profit): {fund['Profit'].idxmax()} "
          f"({fund['Profit'].max():.2f})")
    print(f"Top Performing Fund (Highest NAV)   : {fund['NAV'].idxmax()} "
          f"({fund['NAV'].max():.2f})")
    print(f"Worst Performing Fund (Lowest ROI)  : {fund['ROI'].idxmin()} "
          f"({fund['ROI'].min():.2f}%)")

    print("\nState-wise Investment:")
    print(df.groupby("State")["Investment Amount"].sum().round(2).to_string())
    print("\nAMC-wise Investment:")
    print(df.groupby("AMC")["Investment Amount"].sum().round(2).to_string())
    print("\nCategory-wise ROI:")
    print(df.groupby("Category")["ROI %"].mean().round(2).to_string())


def main():
    funds, investors, transactions, nav_history = read_data()
    funds, investors, transactions, nav_history = clean_data(
        funds, investors, transactions, nav_history
    )
    df = merge_data(funds, investors, transactions, nav_history)
    df = add_calculated_columns(df)
    numpy_tasks(nav_history)
    pandas_analysis(df)
    by_category, _, _, _ = groupby_analysis(df)
    detect_issues(funds, investors, transactions, nav_history)
    df = finance_metrics(df, nav_history)
    export_reports(df, by_category)
    expected_outputs(df)
    _banner("DONE")


if __name__ == "__main__":
    main()
