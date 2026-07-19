#!/usr/bin/env python3
"""Fetch and verify the pinned U2-NetP ONNX release used by the app.

The model is downloaded from the public rembg GitHub release only. The SHA-256
is checked before the destination is replaced, so a changed remote artifact
cannot silently enter a release build.
"""

from __future__ import annotations

import argparse
import hashlib
import os
import tempfile
import urllib.request
from pathlib import Path


MODEL_URL = "https://github.com/danielgatis/rembg/releases/download/v0.0.0/u2netp.onnx"
EXPECTED_SHA256 = "309c8469258dda742793dce0ebea8e6dd393174f89934733ecc8b14c76f4ddd8"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def verify(path: Path) -> None:
    actual = sha256(path)
    if actual != EXPECTED_SHA256:
        raise RuntimeError(
            f"Unexpected U2-NetP SHA-256 for {path}: {actual}; "
            f"expected {EXPECTED_SHA256}",
        )


def fetch(destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    file_descriptor, temporary_name = tempfile.mkstemp(
        prefix="u2netp-",
        suffix=".onnx",
        dir=destination.parent,
    )
    os.close(file_descriptor)
    temporary_path = Path(temporary_name)
    try:
        with urllib.request.urlopen(MODEL_URL, timeout=120) as response:
            temporary_path.write_bytes(response.read())
        verify(temporary_path)
        temporary_path.replace(destination)
    finally:
        temporary_path.unlink(missing_ok=True)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--output",
        default="app/src/main/assets/models/u2netp.onnx",
        help="Destination model path.",
    )
    parser.add_argument(
        "--verify-only",
        action="store_true",
        help="Verify the existing destination without downloading it.",
    )
    args = parser.parse_args()
    output = Path(args.output)

    if args.verify_only:
        verify(output)
    else:
        fetch(output)
    print(f"Verified {output}: {EXPECTED_SHA256}")


if __name__ == "__main__":
    main()
