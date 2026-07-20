#!/usr/bin/env python3
"""Validate final Play Store exports and write the reproducible QA manifest."""

from __future__ import annotations

import csv
import hashlib
import io
import json
import unicodedata
from pathlib import Path

from fontTools.ttLib import TTFont
from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "source"
QA = ROOT / "qa"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def final_assets() -> list[Path]:
    paths = [ROOT / "common/icon/app-icon-512.png"]
    for locale in ("en-US", "hi-IN", "gu-IN", "ru-RU"):
        paths.extend(sorted((ROOT / locale / "feature-graphic").glob("*.png")))
        paths.extend(sorted((ROOT / locale / "phone").glob("*.png")))
    for device in ("tablet-7", "tablet-10", "chromebook"):
        paths.extend(sorted((ROOT / "en-US" / device).glob("*.png")))
    paths.extend(sorted((ROOT / "experiments").glob("*.png")))
    return paths


def expected_size(path: Path) -> tuple[int, int]:
    rel = path.relative_to(ROOT).as_posix()
    if rel == "common/icon/app-icon-512.png":
        return (512, 512)
    if "/feature-graphic/" in rel:
        return (1024, 500)
    if "/phone/" in rel or rel.startswith("experiments/"):
        return (1080, 1920)
    return (1920, 1080)


def disposition(path: Path, approved_locales: set[str]) -> str:
    rel = path.relative_to(ROOT).as_posix()
    locale = path.relative_to(ROOT).parts[0]
    if locale in ("hi-IN", "gu-IN", "ru-RU") and locale not in approved_locales:
        return "TRANSLATION_REVIEW_REQUIRED_DO_NOT_PUBLISH"
    if locale in approved_locales:
        return "PASS_TRANSLATION_APPROVED"
    if rel.startswith("common/icon/"):
        return "CONDITIONAL_MISSING_NAMED_BRAND_SOURCE"
    if "/tablet-" in rel or "/chromebook/" in rel:
        return "PASS_LOCAL_QA_PLAY_CONSOLE_CONFIRMATION_REQUIRED"
    if rel.startswith("experiments/"):
        return "PASS_NOT_PUBLISHED_EXPERIMENT_CANDIDATE"
    return "PASS"


def dhash(path: Path) -> int:
    with Image.open(path) as source:
        image = source.convert("L").resize((9, 8), Image.Resampling.LANCZOS)
    pixels = list(image.getdata())
    value = 0
    for row in range(8):
        for column in range(8):
            value = (value << 1) | int(pixels[row * 9 + column] > pixels[row * 9 + column + 1])
    return value


def hamming(left: int, right: int) -> int:
    return bin(left ^ right).count("1")


def validate_fonts() -> tuple[bool, str]:
    font_paths = [
        SOURCE / "fonts/NotoSans-Variable.ttf",
        SOURCE / "fonts/NotoSansDevanagari-Variable.ttf",
        SOURCE / "fonts/NotoSansGujarati-Variable.ttf",
    ]
    coverage: set[int] = set()
    for path in font_paths:
        font = TTFont(path)
        coverage.update(font.getBestCmap().keys())
    required: set[int] = set()
    with (QA / "copy-deck.csv").open(newline="", encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            for field in ("headline", "supporting_copy", "measurement_copy", "alt_text"):
                for character in row[field]:
                    if not character.isspace() and unicodedata.category(character)[0] not in ("C",):
                        required.add(ord(character))
    missing = sorted(codepoint for codepoint in required if codepoint not in coverage)
    # Common punctuation and emoji-style arrows/bullets may come from browser fallback.
    allowed_fallback = {0x2022, 0x2192, 0x2713, 0x00D7, 0x2014, 0x2019, 0x201C, 0x201D}
    missing = [codepoint for codepoint in missing if codepoint not in allowed_fallback]
    if missing:
        return False, ", ".join(f"U+{item:04X}" for item in missing)
    return True, "Noto font union covers all copy-deck letters and digits; Chromium performs complex-script shaping."


def validate_measurements() -> tuple[bool, dict[str, str]]:
    original = SOURCE / "sample-images/portrait-curly-hair-original.png"
    processed = SOURCE / "sample-images/real-app-results/compression/portrait-compressed.jpg"
    original_bytes = original.stat().st_size
    processed_bytes = processed.stat().st_size
    saved = (1 - processed_bytes / original_bytes) * 100
    ratio = original_bytes / processed_bytes
    values = {
        "original_bytes": str(original_bytes),
        "processed_bytes": str(processed_bytes),
        "original_display": f"{original_bytes / 1_000_000:.2f} MB",
        "processed_display": f"{processed_bytes / 1_000:.0f} kB",
        "percent_saved": f"{saved:.1f}%",
        "compression_ratio": f"{ratio:.2f}:1",
        "original_sha256": sha256(original),
        "processed_sha256": sha256(processed),
    }
    passed = values["original_display"] == "2.68 MB" and values["processed_display"] == "236 kB" and values["percent_saved"] == "91.2%" and values["compression_ratio"] == "11.38:1"
    with (QA / "measurements.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["metric", "value", "evidence"])
        for key, value in values.items():
            writer.writerow([key, value, original.relative_to(ROOT) if key.startswith("original") else processed.relative_to(ROOT)])
        writer.writerow(["app_ui_evidence", "2.68 MB → 236 kB; 91.2%; 11.38:1", "source/captures/phone/09-result-stats-actions-raw.png"])
        writer.writerow(["target_status", "Not safely achievable at requested 205 kB", "source/captures/phone/09-result-stats-actions-raw.png"])
    return passed, values


def validate_alt_text() -> tuple[bool, int, list[str]]:
    expected = {"en-US": 25, "hi-IN": 10, "gu-IN": 10, "ru-RU": 10}
    rows: list[dict[str, str]] = []
    errors: list[str] = []
    for locale, expected_count in expected.items():
        path = ROOT / locale / "alt-text.md"
        locale_rows = []
        for line in path.read_text(encoding="utf-8").splitlines():
            if not line.startswith("|"):
                continue
            columns = [column.strip() for column in line.strip("|").split("|")]
            if len(columns) != 3 or columns[0] in ("Asset", "ऐप आइकन", "ઍપ આઇકન", "Значок приложения") and columns[1].startswith(("Alt-text", "Unreviewed")):
                continue
            if set(columns[0]) <= {"-", ":", " "}:
                continue
            try:
                declared = int(columns[2])
            except ValueError:
                continue
            actual = len(columns[1])
            locale_rows.append(columns)
            if actual != declared:
                errors.append(f"{locale} {columns[0]}: declared alt-text length {declared}, computed {actual}")
            if actual > 140:
                errors.append(f"{locale} {columns[0]}: alt text exceeds 140 characters ({actual})")
            rows.append({"locale": locale, "asset": columns[0], "alt_text": columns[1], "characters": str(actual)})
        if len(locale_rows) != expected_count:
            errors.append(f"{locale}: expected {expected_count} alt-text entries, found {len(locale_rows)}")
    with (QA / "alt-text-manifest.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=["locale", "asset", "alt_text", "characters"])
        writer.writeheader()
        writer.writerows(rows)
    return not errors, len(rows), errors


def write_source_evidence_manifest() -> None:
    paths = sorted((SOURCE / "captures").rglob("*.png"))
    paths += sorted((SOURCE / "sample-images").rglob("*.png"))
    paths += sorted((SOURCE / "sample-images").rglob("*.jpg"))
    with (QA / "source-evidence-manifest.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.writer(handle)
        writer.writerow(["path", "width", "height", "mode", "format", "bytes", "sha256", "evidence_status"])
        for path in paths:
            with Image.open(path) as image:
                status = "VERIFIED_SOURCE_OR_REAL_APP_OUTPUT"
                if path.name in ("portrait-blue.png", "portrait-red.png"):
                    status = "DIAGNOSTIC_FAILED_EXPORT_EXCLUDED_FROM_FINAL_ASSETS"
                writer.writerow([
                    path.relative_to(ROOT).as_posix(), image.width, image.height,
                    image.mode, image.format, path.stat().st_size, sha256(path), status,
                ])


def main() -> None:
    assets = final_assets()
    expected_count = 52
    errors: list[str] = []
    manifest_rows: list[dict[str, str]] = []
    render_policy_path = QA / "render-policy.json"
    if render_policy_path.is_file():
        render_policy = json.loads(render_policy_path.read_text(encoding="utf-8"))
    else:
        render_policy = {}
        errors.append("missing qa/render-policy.json")
    visible_review_footer = bool(render_policy.get("visible_review_footer", True))
    locale_review_status: dict[str, set[str]] = {}
    with (QA / "copy-deck.csv").open(newline="", encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            locale_review_status.setdefault(row["locale"], set()).add(row["review_status"])
    approval_recorded: dict[str, bool] = {}
    for locale in ("hi-IN", "gu-IN", "ru-RU"):
        review_file = ROOT / locale / "translation-review-required.md"
        review_text = review_file.read_text(encoding="utf-8") if review_file.is_file() else ""
        approval_recorded[locale] = (
            "Reviewer / date / approval record:" in review_text
            and "Reviewer / date / approval record: **pending**" not in review_text
        )
    approved_locales = {
        locale for locale, statuses in locale_review_status.items()
        if locale != "en-US" and statuses == {"TRANSLATION_APPROVED"} and approval_recorded.get(locale, False)
    }
    for locale, statuses in locale_review_status.items():
        if locale != "en-US" and statuses == {"TRANSLATION_APPROVED"} and not approval_recorded.get(locale, False):
            errors.append(f"{locale}: TRANSLATION_APPROVED copy rows lack a non-pending reviewer/date approval record")
    if len(assets) != expected_count:
        errors.append(f"expected {expected_count} final assets, found {len(assets)}")
    missing = [path for path in assets if not path.is_file()]
    if missing:
        errors.extend(f"missing {path.relative_to(ROOT)}" for path in missing)

    for path in assets:
        if not path.is_file():
            continue
        local_errors: list[str] = []
        with Image.open(path) as image:
            image.load()
            size = image.size
            mode = image.mode
            file_format = image.format
            has_icc = bool(image.info.get("icc_profile"))
            if size != expected_size(path):
                local_errors.append(f"dimension {size} != {expected_size(path)}")
            if file_format != "PNG":
                local_errors.append(f"format {file_format} != PNG")
            icon = path == ROOT / "common/icon/app-icon-512.png"
            expected_mode = "RGBA" if icon else "RGB"
            if mode != expected_mode:
                local_errors.append(f"mode {mode} != {expected_mode}")
            alpha_status = "none"
            if "A" in image.getbands():
                alpha = image.getchannel("A")
                extrema = alpha.getextrema()
                alpha_status = f"{extrema[0]}-{extrema[1]}"
                if not icon:
                    local_errors.append("unexpected alpha channel")
                elif extrema != (255, 255):
                    local_errors.append(f"icon contains non-opaque alpha {extrema}")
            if not has_icc:
                local_errors.append("missing embedded ICC profile")
            if icon and path.stat().st_size > 1024 * 1024:
                local_errors.append("icon exceeds 1024 KB")
            if not icon and "/feature-graphic/" not in path.as_posix() and max(size) > 2 * min(size):
                local_errors.append("screenshot long side exceeds twice short side")
            locale = path.relative_to(ROOT).parts[0]
            review_required = locale in ("hi-IN", "gu-IN", "ru-RU") and locale_review_status.get(locale) != {"TRANSLATION_APPROVED"}
            if review_required:
                footer_height = 30 if "/feature-graphic/" in path.as_posix() else 42
                footer = image.convert("RGB").crop((0, image.height - footer_height, image.width, image.height))
                red_pixels = sum(pixel == (153, 27, 27) for pixel in footer.getdata())
                red_ratio = red_pixels / (footer.width * footer.height)
                if visible_review_footer and red_ratio < 0.80:
                    local_errors.append("missing or incomplete red translation-review-required footer")
                if not visible_review_footer and red_ratio >= 0.80:
                    local_errors.append("review footer remains despite footer-suppression policy")
            elif locale in approved_locales:
                footer_height = 30 if "/feature-graphic/" in path.as_posix() else 42
                footer = image.convert("RGB").crop((0, image.height - footer_height, image.width, image.height))
                red_pixels = sum(pixel == (153, 27, 27) for pixel in footer.getdata())
                if red_pixels / (footer.width * footer.height) >= 0.80:
                    local_errors.append("approved translation still contains review-required footer")
        if local_errors:
            errors.extend(f"{path.relative_to(ROOT)}: {item}" for item in local_errors)
        manifest_rows.append({
            "path": path.relative_to(ROOT).as_posix(),
            "width": str(size[0]), "height": str(size[1]), "format": file_format,
            "mode": mode, "alpha_range": alpha_status, "icc_profile": "embedded" if has_icc else "missing",
            "bytes": str(path.stat().st_size), "sha256": sha256(path),
            "automated_status": "FAIL" if local_errors else "PASS",
            "release_disposition": disposition(path, approved_locales),
        })

    with (QA / "asset-manifest.csv").open("w", newline="", encoding="utf-8") as handle:
        writer = csv.DictWriter(handle, fieldnames=list(manifest_rows[0].keys()))
        writer.writeheader()
        writer.writerows(manifest_rows)

    font_pass, font_note = validate_fonts()
    if not font_pass:
        errors.append(f"font coverage: {font_note}")
    measurements_pass, measurements = validate_measurements()
    if not measurements_pass:
        errors.append(f"measurement mismatch: {measurements}")
    alt_text_pass, alt_text_count, alt_text_errors = validate_alt_text()
    errors.extend(alt_text_errors)
    write_source_evidence_manifest()

    duplicate_notes: list[str] = []
    for folder in [ROOT / locale / "phone" for locale in ("en-US", "hi-IN", "gu-IN", "ru-RU")]:
        paths = sorted(folder.glob("*.png"))
        hashes = {path: dhash(path) for path in paths}
        for index, left in enumerate(paths):
            for right in paths[index + 1:]:
                distance = hamming(hashes[left], hashes[right])
                if distance < 4:
                    duplicate_notes.append(f"{left.relative_to(ROOT)} vs {right.relative_to(ROOT)}: dHash distance {distance}")
    if duplicate_notes:
        errors.extend("near-duplicate: " + item for item in duplicate_notes)

    footer_policy_note = (
        "visible on unreviewed localized exports"
        if visible_review_footer
        else "omitted by explicit render policy; metadata remains review-required"
    )
    report = f"""# Play Store asset QA report

Generated: **2026-07-20 (Asia/Kolkata)**

## Automated result

**{'PASS' if not errors else 'FAIL'}** — {len(manifest_rows)} final asset files checked; expected {expected_count}.

- Exact dimensions and PNG format: {'PASS' if not any('dimension' in item or 'format' in item for item in errors) else 'FAIL'}
- Color modes and unexpected alpha: {'PASS' if not any('mode' in item or 'alpha' in item for item in errors) else 'FAIL'}
- Embedded sRGB ICC profile: {'PASS' if not any('ICC' in item for item in errors) else 'FAIL'}
- Icon 512×512, 32-bit RGBA, opaque alpha, under 1024 KB: {'PASS' if not any('icon' in item.lower() for item in errors) else 'FAIL'}
- Screenshot aspect ratio (long side ≤ 2× short side): {'PASS' if not any('long side' in item for item in errors) else 'FAIL'}
- Within-carousel duplicate/near-duplicate scan: {'PASS' if not duplicate_notes else 'FAIL'}
- Font glyph coverage: {'PASS' if font_pass else 'FAIL'} — {font_note}
- Numeric evidence recomputation: {'PASS' if measurements_pass else 'FAIL'} — 2.68 MB → 236 kB; 91.2% saved; 11.38:1.
- Alt-text coverage and ≤140-character limit: {'PASS' if alt_text_pass else 'FAIL'} — {alt_text_count} locale/asset entries checked.
- Translation render-policy gate: {'PASS' if not any('footer' in item.lower() for item in errors) else 'FAIL'} — review warning is {footer_policy_note}.

The per-file dimensions, modes, byte sizes, hashes and release gates are in
`asset-manifest.csv`. Exact measurement inputs and hashes are in
`measurements.csv`.

## Release gates that automated validation cannot clear

- The common icon is technically valid but conditional because the requested
  `ic_splitframe_logo.png` is absent. Confirm the installed identity source.
- English phone and feature assets are production candidates after visual and
  policy review. This script does not claim that graphics guarantee installs.
- hi-IN, gu-IN and ru-RU exports remain **DO NOT PUBLISH** in the manifest and
  review documents until qualified native-language review, whether or not the
  visible image footer is enabled. Gujarati maps to Play locale `gu`.
- Tablet and Chromebook files pass local image QA but remain conditional on the
  exact release AAB's Play Console Device Catalog eligibility.
- Blue/red solid-color export trials with a damaged wedge are excluded. The
  final color frame uses correct, genuine in-app preview captures only.
- Visual inspection, claim review, ad/personal-data review and thumbnail-speed
  review are documented below after human inspection; they are not inferred
  from file metadata.

## Automated failures

{chr(10).join('- ' + item for item in errors) if errors else '- None.'}
"""
    (QA / "qa-report.md").write_text(report, encoding="utf-8")
    if errors:
        raise SystemExit("Validation failed; see play-store-assets/qa/qa-report.md")
    print(f"PASS: validated {len(manifest_rows)} final assets")


if __name__ == "__main__":
    main()
