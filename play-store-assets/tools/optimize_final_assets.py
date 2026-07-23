#!/usr/bin/env python3
"""Losslessly recompress final PNGs to a conservative Play upload ceiling."""

from __future__ import annotations

import os
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
FINAL = ROOT / "final"
# Keep margin below the commonly enforced 8 MB screenshot upload ceiling.
TARGET_BYTES = 7_900_000


def main() -> None:
    optimized = 0
    for path in sorted(FINAL.rglob("*.png")):
        if path.stat().st_size <= TARGET_BYTES:
            continue
        temporary = path.with_suffix(".optimized.png")
        with Image.open(path) as source:
            image = source.convert("RGBA" if path.name == "app-icon-512.png" else "RGB")
            image.save(
                temporary,
                "PNG",
                optimize=True,
                compress_level=9,
                icc_profile=source.info.get("icc_profile"),
            )
        if temporary.stat().st_size >= path.stat().st_size:
            temporary.unlink()
            continue
        os.replace(temporary, path)
        optimized += 1
        print(f"losslessly optimized {path.relative_to(ROOT)}", flush=True)
    print(f"Losslessly optimized {optimized} final PNG(s).")


if __name__ == "__main__":
    main()
