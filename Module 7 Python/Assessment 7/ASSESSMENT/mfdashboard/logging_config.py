"""Centralised logging configuration for report generation and execution status."""

from __future__ import annotations

import logging
from pathlib import Path


def configure_logging(log_dir: str | Path = "logs", log_file: str = "dashboard.log") -> logging.Logger:
    """Configure and return the package logger.

    Logs are written both to the console and to a rotating-friendly log file so that
    execution status and report-generation steps are captured for audit purposes.
    """
    log_path = Path(log_dir)
    log_path.mkdir(parents=True, exist_ok=True)

    logger = logging.getLogger("mfdashboard")
    logger.setLevel(logging.INFO)

    # Avoid duplicate handlers when configure_logging is called more than once.
    if logger.handlers:
        return logger

    formatter = logging.Formatter(
        fmt="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
        datefmt="%Y-%m-%d %H:%M:%S",
    )

    file_handler = logging.FileHandler(log_path / log_file, mode="w", encoding="utf-8")
    file_handler.setFormatter(formatter)
    logger.addHandler(file_handler)

    console_handler = logging.StreamHandler()
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)

    return logger


def get_logger() -> logging.Logger:
    """Return the package logger (configuring a default if needed)."""
    logger = logging.getLogger("mfdashboard")
    if not logger.handlers:
        return configure_logging()
    return logger
