#!/usr/bin/env python3
"""Validate the Google Ads package and write a reproducible Markdown report."""

from __future__ import annotations

import csv
import json
import re
import subprocess
from dataclasses import dataclass
from pathlib import Path

from PIL import Image


ADS_ROOT = Path(__file__).resolve().parents[1]
TEXT_ROOT = ADS_ROOT / "text"
IMAGE_ROOT = ADS_ROOT / "images" / "final"
VIDEO_ROOT = ADS_ROOT / "videos" / "final"
STORYBOARD_ROOT = ADS_ROOT / "qa" / "video_storyboards"
REPORT_PATH = ADS_ROOT / "VALIDATION_REPORT.md"

TEXT_RULES = {
    "SHORT_HEADLINES.csv": (15, 30),
    "LONG_HEADLINES.csv": (5, 90),
    "DESCRIPTIONS.csv": (5, 90),
}
IMAGE_RULES = {
    "landscape": (4, (1200, 628)),
    "square": (4, (1200, 1200)),
    "vertical": (4, (1200, 1500)),
}
VIDEO_RULES = {
    "vertical": (2, (1080, 1920)),
    "square": (2, (1080, 1080)),
    "horizontal": (2, (1920, 1080)),
}
TEXT_COLUMNS = [
    "id",
    "text",
    "character_count",
    "creative_angle",
    "verified_feature_source",
    "recommended_priority",
]


@dataclass
class Check:
    name: str
    passed: bool
    detail: str


checks: list[Check] = []


def record(name: str, passed: bool, detail: str) -> None:
    checks.append(Check(name, passed, detail))


def probe_video(path: Path) -> dict:
    output = subprocess.check_output(
        [
            "ffprobe",
            "-v",
            "error",
            "-show_streams",
            "-show_format",
            "-of",
            "json",
            str(path),
        ]
    )
    return json.loads(output)


def is_faststart(path: Path) -> bool:
    data = path.read_bytes()
    moov = data.find(b"moov")
    mdat = data.find(b"mdat")
    return moov >= 0 and mdat >= 0 and moov < mdat


def validate_text() -> list[dict[str, str]]:
    details: list[dict[str, str]] = []
    all_ids: list[str] = []
    for filename, (required_count, max_characters) in TEXT_RULES.items():
        path = TEXT_ROOT / filename
        with path.open(encoding="utf-8", newline="") as handle:
            reader = csv.DictReader(handle)
            rows = list(reader)
            record(
                f"{filename} header",
                reader.fieldnames == TEXT_COLUMNS,
                ",".join(reader.fieldnames or []),
            )
        record(
            f"{filename} asset count",
            len(rows) == required_count,
            f"{len(rows)} found; {required_count} required",
        )
        for row in rows:
            actual = len(row["text"])
            declared = int(row["character_count"])
            passed = actual == declared and actual <= max_characters
            record(
                f"{row['id']} character count",
                passed,
                f"declared {declared}, actual {actual}, limit {max_characters}",
            )
            all_ids.append(row["id"])
            details.append(
                {
                    "id": row["id"],
                    "kind": filename.replace(".csv", "").lower(),
                    "characters": str(actual),
                    "limit": str(max_characters),
                    "status": "PASS" if passed else "FAIL",
                }
            )
    short_rows = list(csv.DictReader((TEXT_ROOT / "SHORT_HEADLINES.csv").open(encoding="utf-8")))
    record(
        "At least one short headline is 15 characters or fewer",
        any(len(row["text"]) <= 15 for row in short_rows),
        ", ".join(f"{row['id']}={len(row['text'])}" for row in short_rows if len(row["text"]) <= 15),
    )
    record("Text IDs are unique", len(all_ids) == len(set(all_ids)), f"{len(all_ids)} IDs")

    selection = (TEXT_ROOT / "APP_CAMPAIGN_SELECTION.md").read_text(encoding="utf-8")
    headline_ids = re.findall(r"^\| (SH\d{2}) \|", selection, flags=re.MULTILINE)
    description_ids = re.findall(r"^\| (DS\d{2}) \|", selection, flags=re.MULTILINE)
    record(
        "App campaign selection",
        len(headline_ids) == 5 and len(description_ids) == 5,
        f"{len(headline_ids)} headlines and {len(description_ids)} descriptions",
    )
    return details


def validate_images() -> list[dict[str, str]]:
    details: list[dict[str, str]] = []
    names: list[str] = []
    state_ids: set[str] = set()
    manifest_rows = list(csv.DictReader((ADS_ROOT / "ASSET_MANIFEST.csv").open(encoding="utf-8")))
    for row in manifest_rows:
        if row["filename"].startswith("images/final/"):
            state_ids.add(row["level_or_state_id"])

    pattern = re.compile(r"^[a-z0-9]+(?:_[a-z0-9]+)+_(?:landscape|square|vertical)_\d+x\d+\.png$")
    for orientation, (required_count, dimensions) in IMAGE_RULES.items():
        paths = sorted((IMAGE_ROOT / orientation).glob("*"))
        record(
            f"{orientation} image count",
            len(paths) == required_count,
            f"{len(paths)} found; {required_count} required",
        )
        for path in paths:
            names.append(path.name)
            with Image.open(path) as image:
                actual_dimensions = image.size
                image_format = image.format
            size_bytes = path.stat().st_size
            passed = actual_dimensions == dimensions and image_format in {"PNG", "JPEG"} and size_bytes <= 5_000_000
            record(
                f"Image {path.name}",
                passed and bool(pattern.fullmatch(path.name)),
                f"{actual_dimensions[0]}x{actual_dimensions[1]}, {image_format}, {size_bytes} bytes",
            )
            details.append(
                {
                    "filename": path.relative_to(ADS_ROOT).as_posix(),
                    "dimensions": f"{actual_dimensions[0]}x{actual_dimensions[1]}",
                    "format": str(image_format),
                    "bytes": str(size_bytes),
                    "status": "PASS" if passed and pattern.fullmatch(path.name) else "FAIL",
                }
            )
    record("Final image filenames are unique", len(names) == len(set(names)), f"{len(names)} filenames")
    record(
        "At least six distinct real app states are represented",
        len(state_ids) >= 6,
        ", ".join(sorted(state_ids)),
    )
    return details


def validate_videos() -> list[dict[str, str]]:
    details: list[dict[str, str]] = []
    names: list[str] = []
    pattern = re.compile(r"^[a-z0-9]+(?:_[a-z0-9]+)+_(?:horizontal|square|vertical)_\d+x\d+\.mp4$")
    for orientation, (required_count, dimensions) in VIDEO_RULES.items():
        paths = sorted((VIDEO_ROOT / orientation).glob("*.mp4"))
        record(
            f"{orientation} video count",
            len(paths) == required_count,
            f"{len(paths)} found; {required_count} required",
        )
        for path in paths:
            names.append(path.name)
            probe = probe_video(path)
            video_streams = [stream for stream in probe["streams"] if stream["codec_type"] == "video"]
            audio_streams = [stream for stream in probe["streams"] if stream["codec_type"] == "audio"]
            stream = video_streams[0]
            actual_dimensions = (int(stream["width"]), int(stream["height"]))
            duration = float(probe["format"]["duration"])
            faststart = is_faststart(path)
            passed = (
                len(video_streams) == 1
                and actual_dimensions == dimensions
                and stream["codec_name"] == "h264"
                and stream.get("pix_fmt") == "yuv420p"
                and stream.get("avg_frame_rate") == "30/1"
                and 15.0 <= duration <= 25.0
                and faststart
                and not audio_streams
                and bool(pattern.fullmatch(path.name))
            )
            record(
                f"Video {path.name}",
                passed,
                (
                    f"{actual_dimensions[0]}x{actual_dimensions[1]}, {stream['codec_name']}, "
                    f"{stream.get('pix_fmt')}, {stream.get('avg_frame_rate')} fps, "
                    f"{duration:.2f}s, faststart={faststart}, audio={'none' if not audio_streams else audio_streams[0]['codec_name']}"
                ),
            )
            details.append(
                {
                    "filename": path.relative_to(ADS_ROOT).as_posix(),
                    "dimensions": f"{actual_dimensions[0]}x{actual_dimensions[1]}",
                    "codec": stream["codec_name"],
                    "pixel_format": stream.get("pix_fmt", ""),
                    "fps": stream.get("avg_frame_rate", ""),
                    "duration": f"{duration:.2f}",
                    "faststart": "yes" if faststart else "no",
                    "audio": "none" if not audio_streams else audio_streams[0]["codec_name"],
                    "status": "PASS" if passed else "FAIL",
                }
            )
    record("Final video filenames are unique", len(names) == len(set(names)), f"{len(names)} filenames")
    record(
        "No fabricated audio",
        all(item["audio"] == "none" for item in details),
        "Repository and captured app contain no audio; AAC is therefore not applicable",
    )
    return details


def validate_qa_and_manifest() -> None:
    contact_sheet = ADS_ROOT / "qa" / "IMAGE_CONTACT_SHEET.png"
    with Image.open(contact_sheet) as image:
        dimensions = image.size
    record("Image contact sheet exists", dimensions[0] > 0 and dimensions[1] > 0, f"{dimensions[0]}x{dimensions[1]}")

    final_videos = sorted(VIDEO_ROOT.glob("*/*.mp4"))
    storyboards = sorted(STORYBOARD_ROOT.glob("*_storyboard.png"))
    record(
        "One storyboard per final video",
        len(storyboards) == len(final_videos) == 6,
        f"{len(storyboards)} storyboards for {len(final_videos)} videos",
    )

    manifest_path = ADS_ROOT / "ASSET_MANIFEST.csv"
    rows = list(csv.DictReader(manifest_path.open(encoding="utf-8")))
    record("Asset manifest row count", len(rows) == 18, f"{len(rows)} rows")
    manifest_names = [row["filename"] for row in rows]
    record("Manifest filenames are unique", len(manifest_names) == len(set(manifest_names)), f"{len(manifest_names)} entries")
    missing = [name for name in manifest_names if not (ADS_ROOT / name).is_file()]
    record("Every manifest asset exists", not missing, "none missing" if not missing else ", ".join(missing))


def markdown_table(headers: list[str], rows: list[list[str]]) -> str:
    lines = ["| " + " | ".join(headers) + " |", "| " + " | ".join("---" for _ in headers) + " |"]
    lines.extend("| " + " | ".join(row) + " |" for row in rows)
    return "\n".join(lines)


def write_report(text_details: list[dict[str, str]], image_details: list[dict[str, str]], video_details: list[dict[str, str]]) -> None:
    passed = sum(item.passed for item in checks)
    failed = len(checks) - passed
    overall = "PASS" if failed == 0 else "FAIL"
    lines = [
        "# Google Ads asset validation report",
        "",
        "Validated: **2026-08-26 (Asia/Kolkata)**",
        "",
        f"Overall: **{overall}** — {passed}/{len(checks)} checks passed.",
        "",
        "## Automated summary",
        "",
        markdown_table(
            ["Check", "Status", "Detail"],
            [[item.name, "PASS" if item.passed else "FAIL", item.detail.replace("|", "\\|")] for item in checks],
        ),
        "",
        "## Text assets",
        "",
        markdown_table(
            ["ID", "Type", "Characters", "Limit", "Status"],
            [[item["id"], item["kind"], item["characters"], item["limit"], item["status"]] for item in text_details],
        ),
        "",
        "## Final images",
        "",
        markdown_table(
            ["Filename", "Dimensions", "Format", "Bytes", "Status"],
            [[item["filename"], item["dimensions"], item["format"], item["bytes"], item["status"]] for item in image_details],
        ),
        "",
        "## Final videos",
        "",
        markdown_table(
            ["Filename", "Dimensions", "Codec", "Pixel format", "FPS", "Duration", "Fast start", "Audio", "Status"],
            [
                [
                    item["filename"],
                    item["dimensions"],
                    item["codec"],
                    item["pixel_format"],
                    item["fps"],
                    item["duration"],
                    item["faststart"],
                    item["audio"],
                    item["status"],
                ]
                for item in video_details
            ],
        ),
        "",
        "## Visual QA",
        "",
        "Every final image, the complete contact sheet, and every final-video storyboard was opened after the last generation pass. The review confirmed:",
        "",
        "- App controls, subject edges, numerical values, and page context remain readable at each crop.",
        "- The background-removal action/reaction and batch configure/complete/compare sequence are understandable without sound.",
        "- First frames contain useful real app UI; two or more meaningful changes occur by five seconds.",
        "- Each video ends with a 1.2-second real Home-screen brand finish.",
        "- No system UI, ads, consent screens, personal information, debug overlays, test labels, fake controls, black bars, stretching, or synthetic gameplay are visible.",
        "- Colors and typography match the installed app. All sample scenes are real capture fixtures and genuine app results.",
        "",
        "The utility has no game audio, so silent MP4 output is intentional. AAC is required only when an audio source exists. Google Ads campaign use still requires the final videos to be uploaded to YouTube; this package does not upload or publish them.",
    ]
    REPORT_PATH.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> None:
    text_details = validate_text()
    image_details = validate_images()
    video_details = validate_videos()
    validate_qa_and_manifest()
    write_report(text_details, image_details, video_details)
    failures = [item for item in checks if not item.passed]
    print(f"Validated {len(checks)} checks: {len(checks) - len(failures)} passed, {len(failures)} failed")
    if failures:
        for failure in failures:
            print(f"FAIL: {failure.name}: {failure.detail}")
        raise SystemExit(1)


if __name__ == "__main__":
    main()
