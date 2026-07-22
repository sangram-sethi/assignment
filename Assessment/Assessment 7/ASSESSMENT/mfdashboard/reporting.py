"""Report export helpers: write metrics and tables to disk."""

from __future__ import annotations

import json
from datetime import datetime
from pathlib import Path

import pandas as pd

from .logging_config import get_logger

logger = get_logger()


def export_dataframe(df: pd.DataFrame, out_dir: Path, name: str) -> Path:
    out_dir.mkdir(parents=True, exist_ok=True)
    path = out_dir / name
    df.to_csv(path, index=False)
    logger.info("Exported report: %s (%d rows)", path, len(df))
    return path


def write_summary_report(
    numpy_metrics: dict,
    finance_metrics: dict,
    fund_insights: dict,
    out_dir: Path,
) -> Path:
    """Write a human-readable Markdown summary of the key results."""
    out_dir.mkdir(parents=True, exist_ok=True)
    path = out_dir / "summary_report.md"

    def fmt(value) -> str:
        if isinstance(value, float):
            return f"{value:,.2f}"
        return str(value)

    lines: list[str] = []
    lines.append("# Mutual Fund Portfolio Performance & Risk Report")
    lines.append("")
    lines.append(f"_Generated: {datetime.now():%Y-%m-%d %H:%M:%S}_")
    lines.append("")

    lines.append("## Statistical Metrics (NumPy)")
    lines.append("")
    lines.append("| Metric | Value |")
    lines.append("| --- | ---: |")
    for key, value in numpy_metrics.items():
        lines.append(f"| {key.replace('_', ' ').title()} | {fmt(value)} |")
    lines.append("")

    lines.append("## Portfolio Finance Metrics")
    lines.append("")
    lines.append("| Metric | Value |")
    lines.append("| --- | ---: |")
    for key, value in finance_metrics.items():
        lines.append(f"| {key.replace('_', ' ').title()} | {fmt(value)} |")
    lines.append("")

    lines.append("## Fund Analysis")
    lines.append("")
    for key, value in fund_insights.items():
        lines.append(f"- **{key.replace('_', ' ').title()}**: {value}")
    lines.append("")

    path.write_text("\n".join(lines), encoding="utf-8")
    logger.info("Wrote summary report: %s", path)

    # Also persist machine-readable JSON.
    json_path = out_dir / "summary_metrics.json"
    json_path.write_text(
        json.dumps(
            {
                "numpy_metrics": numpy_metrics,
                "finance_metrics": finance_metrics,
                "fund_insights": fund_insights,
            },
            indent=2,
            default=str,
        ),
        encoding="utf-8",
    )
    logger.info("Wrote metrics JSON: %s", json_path)
    return path
