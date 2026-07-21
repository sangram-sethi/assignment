"""CSV data loading with exception handling for missing or corrupted files."""

from __future__ import annotations

from pathlib import Path

import pandas as pd

from .logging_config import get_logger

logger = get_logger()

# Expected columns per input file — used to detect corrupted / malformed files.
EXPECTED_COLUMNS: dict[str, set[str]] = {
    "investors": {"InvestorID", "InvestorName", "Age", "City", "AnnualIncome", "RiskProfile"},
    "funds": {"FundID", "FundName", "Category", "FundManager", "ExpenseRatio", "Benchmark"},
    "transactions": {
        "TransactionID", "InvestorID", "FundID", "TransactionDate",
        "TransactionType", "Units", "NAV", "Amount",
    },
    "nav_history": {"FundID", "Date", "NAV"},
}


def read_csv_file(path: str | Path, expected_columns: set[str] | None = None) -> pd.DataFrame:
    """Read a single CSV file with robust exception handling.

    Raises FileNotFoundError if the file is missing and ValueError if the file is
    empty or missing expected columns (treated as corrupted).
    """
    path = Path(path)
    try:
        df = pd.read_csv(path)
    except FileNotFoundError:
        logger.error("Missing file: %s", path)
        raise
    except pd.errors.EmptyDataError as exc:
        logger.error("Corrupted (empty) file: %s", path)
        raise ValueError(f"File is empty: {path}") from exc
    except pd.errors.ParserError as exc:
        logger.error("Corrupted (unparseable) file: %s (%s)", path, exc)
        raise ValueError(f"File could not be parsed: {path}") from exc

    if df.empty:
        logger.warning("File loaded but contains no rows: %s", path)

    if expected_columns is not None:
        missing = expected_columns - set(df.columns)
        if missing:
            logger.error("File %s missing expected columns: %s", path, sorted(missing))
            raise ValueError(f"File {path} missing columns: {sorted(missing)}")

    logger.info("Loaded %s (%d rows, %d cols)", path.name, len(df), df.shape[1])
    return df


def load_all(data_dir: str | Path = ".") -> dict[str, pd.DataFrame]:
    """Load all four input CSV files, returning a dict keyed by dataset name.

    Missing or corrupted files are logged; the loader continues where possible and
    returns whichever datasets were loaded successfully.
    """
    data_dir = Path(data_dir)
    files = {
        "investors": data_dir / "investors.csv",
        "funds": data_dir / "funds.csv",
        "transactions": data_dir / "transactions.csv",
        "nav_history": data_dir / "nav_history.csv",
    }

    datasets: dict[str, pd.DataFrame] = {}
    for name, path in files.items():
        try:
            datasets[name] = read_csv_file(path, EXPECTED_COLUMNS.get(name))
        except (FileNotFoundError, ValueError) as exc:
            logger.error("Skipping dataset '%s' due to error: %s", name, exc)

    if not datasets:
        raise RuntimeError("No input datasets could be loaded. Aborting.")

    logger.info("Successfully loaded %d/%d datasets.", len(datasets), len(files))
    return datasets
