#!/usr/bin/env python3
"""Deterministically build Play Store graphics from real app captures.

This script never draws or reconstructs application UI. It composes only real
captures, real app outputs, original sample photography, and rasterized copy.
Chromium is used for complex-script shaping; Pillow normalizes every PNG to sRGB.
"""

from __future__ import annotations

import argparse
import csv
import html
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

from PIL import Image, ImageCms, ImageDraw, ImageOps


ROOT = Path(__file__).resolve().parents[1]
REPO = ROOT.parent
SOURCE = ROOT / "source"
CAPTURES = SOURCE / "captures"
SAMPLES = SOURCE / "sample-images"
RESULTS = SAMPLES / "real-app-results"
TEMPLATES = SOURCE / "templates"
HTML_OUT = TEMPLATES / "rendered-html"
CROPS = TEMPLATES / "generated-crops"
FONTS = SOURCE / "fonts"
COPY_DECK = ROOT / "qa" / "copy-deck.csv"

PHONE_SIZE = (1080, 1920)
FEATURE_SIZE = (1024, 500)
LARGE_SIZE = (1920, 1080)

TOKENS = {
    "{ORIGINAL_SIZE}": "2.68 MB",
    "{PROCESSED_SIZE}": "236 kB",
    "{PERCENT_SAVED}": "91.2",
    "{COMPRESSION_RATIO}": "11.38",
}

PHONE_NAMES = {
    "phone-01-compression": "01-compression.png",
    "phone-02-background-removal": "02-background-removal.png",
    "phone-03-solid-color": "03-solid-color-replacement.png",
    "phone-04-batch": "04-batch-compression.png",
    "phone-05-resize": "05-resizing.png",
    "phone-06-format": "06-format-conversion.png",
    "phone-07-comparison": "07-comparison.png",
    "phone-08-trust": "08-trust.png",
}

APP_IMAGES = {
    "home": CAPTURES / "phone/01-home-three-selected-raw.png",
    "resize_format": CAPTURES / "phone/03-editor-resize-format-raw.png",
    "custom_resize": CAPTURES / "phone/04-editor-custom-resize-raw.png",
    "batch_progress": CAPTURES / "phone/05-batch-progress-raw.png",
    "batch_done": CAPTURES / "phone/06-batch-summary-raw.png",
    "comparison": CAPTURES / "phone/07-result-comparison-raw.png",
    "batch_results": CAPTURES / "phone/08-result-batch-list-raw.png",
    "stats_actions": CAPTURES / "phone/09-result-stats-actions-raw.png",
    "cutout_ui": CAPTURES / "phone/11-background-removal-result-raw.png",
    "white_ui": CAPTURES / "phone/14-background-white-preview-raw.png",
    "blue_ui": CAPTURES / "phone/15-background-blue-preview-raw.png",
    "red_ui": CAPTURES / "phone/16-background-red-preview-raw.png",
    "green_ui": CAPTURES / "phone/17-background-green-preview-raw.png",
}

ORIGINAL = SAMPLES / "portrait-curly-hair-original.png"
COMPRESSED = RESULTS / "compression/portrait-compressed.jpg"
CUTOUT = RESULTS / "background/portrait-transparent.png"


def fail(message: str) -> None:
    raise SystemExit(f"ERROR: {message}")


def require_inputs() -> None:
    required = [
        COPY_DECK,
        ORIGINAL,
        COMPRESSED,
        CUTOUT,
        FONTS / "NotoSans-Variable.ttf",
        FONTS / "NotoSansDevanagari-Variable.ttf",
        FONTS / "NotoSansGujarati-Variable.ttf",
        REPO / "play-store/assets/app-icon-512.png",
        REPO / "app/src/main/res/drawable-nodpi/ic_image_compressor_logo.png",
        *APP_IMAGES.values(),
    ]
    for device in ("tablet-7", "tablet-10", "chromebook"):
        for index in range(1, 5):
            name = {
                1: "01-batch-selected-raw.png",
                2: "02-batch-summary-raw.png",
                3: "03-background-removal-raw.png",
                4: "04-comparison-raw.png",
            }[index]
            required.append(CAPTURES / device / name)
        required.append(CAPTURES / device / "03-background-removal-clean-raw.png")
    missing = [str(path.relative_to(ROOT)) for path in required if not path.is_file()]
    if missing:
        fail("missing required real inputs:\n  " + "\n  ".join(missing))


def chrome_binary() -> Path:
    candidates = [
        os.environ.get("CHROME_BIN"),
        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
        shutil.which("google-chrome"),
        shutil.which("chromium"),
        shutil.which("chromium-browser"),
    ]
    for candidate in candidates:
        if candidate and Path(candidate).is_file():
            return Path(candidate)
    fail("Chromium/Google Chrome not found; set CHROME_BIN")
    raise AssertionError


def as_uri(path: Path) -> str:
    return path.resolve().as_uri()


def copy_rows() -> dict[str, dict[str, dict[str, str]]]:
    rows: dict[str, dict[str, dict[str, str]]] = {}
    with COPY_DECK.open(newline="", encoding="utf-8") as handle:
        for row in csv.DictReader(handle):
            for key in ("headline", "supporting_copy", "measurement_copy"):
                value = row[key]
                for token, replacement in TOKENS.items():
                    value = value.replace(token, replacement)
                row[key] = value
            rows.setdefault(row["locale"], {})[row["asset_id"]] = row
    return rows


def common_css(locale: str, width: int, height: int, headline_size: int) -> str:
    primary_font = {
        "hi-IN": "NotoDevanagari",
        "gu-IN": "NotoGujarati",
    }.get(locale, "NotoSans")
    localized_proof_css = "" if locale == "en-US" else ".label,.detail span,.metric span,.stats-bar span,.resize-chips,.aspect{display:none!important}.frame05{justify-content:center!important}.frame05 .quality-ui{height:520px!important}.frame05 .resize-ui{height:680px!important}"
    return f"""
@font-face {{font-family:NotoSans;src:url('{as_uri(FONTS / 'NotoSans-Variable.ttf')}') format('truetype');font-weight:100 900;}}
@font-face {{font-family:NotoDevanagari;src:url('{as_uri(FONTS / 'NotoSansDevanagari-Variable.ttf')}') format('truetype');font-weight:100 900;}}
@font-face {{font-family:NotoGujarati;src:url('{as_uri(FONTS / 'NotoSansGujarati-Variable.ttf')}') format('truetype');font-weight:100 900;}}
* {{box-sizing:border-box;}}
html,body {{margin:0;width:{width}px;height:{height}px;overflow:hidden;background:#F6F8FF;}}
body {{font-family:{primary_font},NotoSans,sans-serif;color:#0F172A;}}
.canvas {{position:relative;width:{width}px;height:{height}px;overflow:hidden;background:
 radial-gradient(circle at 86% 8%,rgba(37,99,235,.17),transparent 31%),
 radial-gradient(circle at 4% 94%,rgba(219,234,254,.88),transparent 39%),
 linear-gradient(150deg,#F6F8FF 0%,#EEF4FF 54%,#DBEAFE 100%);}}
.headline {{position:absolute;z-index:10;left:56px;right:56px;top:52px;text-align:center;font-weight:825;font-size:{headline_size}px;line-height:1.08;letter-spacing:-1.7px;text-wrap:balance;}}
.subhead {{position:absolute;z-index:10;left:72px;right:72px;top:220px;text-align:center;color:#475569;font-size:27px;line-height:1.25;font-weight:590;text-wrap:balance;}}
.body {{position:absolute;left:56px;right:56px;top:292px;bottom:54px;}}
.card {{background:rgba(255,255,255,.94);border:1px solid rgba(148,163,184,.22);border-radius:34px;overflow:hidden;box-shadow:0 24px 64px rgba(15,23,42,.12),0 5px 15px rgba(37,99,235,.06);}}
.label {{display:inline-flex;align-items:center;gap:8px;background:rgba(15,23,42,.78);color:#fff;border-radius:999px;padding:9px 17px;font-size:19px;font-weight:760;letter-spacing:.4px;}}
.label.blue {{background:#2563EB;}} .label.green {{background:#16A34A;}}
.metric-row {{display:grid;grid-template-columns:1fr 56px 1fr;align-items:center;gap:14px;}}
.metric {{background:#fff;border:1px solid rgba(37,99,235,.15);border-radius:24px;padding:22px;text-align:center;box-shadow:0 10px 24px rgba(15,23,42,.08);}}
.metric strong {{display:block;font-size:38px;line-height:1;color:#0F172A;}} .metric span {{display:block;margin-top:8px;color:#475569;font-size:20px;font-weight:650;}}
.arrow {{width:54px;height:54px;border-radius:50%;display:grid;place-items:center;background:#2563EB;color:white;font-size:32px;font-weight:900;box-shadow:0 8px 18px rgba(37,99,235,.25);}}
.proof {{position:absolute;z-index:30;left:0;right:0;bottom:0;height:42px;background:#991B1B;color:#fff;display:flex;align-items:center;justify-content:center;font:800 17px NotoSans,sans-serif;letter-spacing:1.1px;}}
.checker {{background-color:#fff;background-image:linear-gradient(45deg,#d9dce3 25%,transparent 25%),linear-gradient(-45deg,#d9dce3 25%,transparent 25%),linear-gradient(45deg,transparent 75%,#d9dce3 75%),linear-gradient(-45deg,transparent 75%,#d9dce3 75%);background-size:38px 38px;background-position:0 0,0 19px,19px -19px,-19px 0;}}
.ui-shot {{display:block;width:100%;height:100%;object-fit:cover;object-position:50% 25%;}}
{localized_proof_css}
"""


def proof(locale: str, omit_review_footer: bool) -> str:
    if locale == "en-US" or omit_review_footer:
        return ""
    return '<div class="proof">TRANSLATION REVIEW REQUIRED • DO NOT PUBLISH</div>'


def img(path: Path, klass: str = "", extra: str = "") -> str:
    return f'<img class="{klass}" src="{as_uri(path)}" {extra}>'


def phone_fragment(asset_id: str, row: dict[str, str]) -> str:
    support = html.escape(row["supporting_copy"])
    measure = html.escape(row["measurement_copy"])
    if asset_id == "phone-01-compression":
        return f"""
<div class="body frame01">
 <div class="card compare-photo">
  <div class="photo-block">{img(ORIGINAL)}<span class="label">ORIGINAL</span></div>
  <div class="photo-block">{img(COMPRESSED)}<span class="label green">COMPRESSED</span></div>
 </div>
 <div class="metric-row savings"><div class="metric"><strong>2.68 MB</strong><span>Original</span></div><div class="arrow">→</div><div class="metric"><strong>236 kB</strong><span>91.2% saved</span></div></div>
 <div class="detail-pair"><div class="card detail"><span>ORIGINAL • 100% DETAIL</span>{img(CROPS / 'portrait-original-100pct.png')}</div><div class="card detail"><span>COMPRESSED • 100% DETAIL</span>{img(CROPS / 'portrait-compressed-100pct.png')}</div></div>
 <div class="card evidence">{img(CROPS / 'result-metrics-crop.png','ui-shot')}</div>
</div>"""
    if asset_id == "phone-02-background-removal":
        return f"""
<div class="body frame02">
 <div class="transform">
  <div class="card portrait"><span class="label">BEFORE</span>{img(ORIGINAL)}</div>
  <div class="arrow big">→</div>
  <div class="card portrait checker"><span class="label blue">REAL APP RESULT</span>{img(CUTOUT)}</div>
 </div>
 <div class="card cutout-evidence">{img(APP_IMAGES['cutout_ui'],'ui-shot')}</div>
</div>"""
    if asset_id == "phone-03-solid-color":
        cards = []
        for label, key in (("WHITE", "white_ui"), ("BLUE", "blue_ui"), ("GREEN", "green_ui"), ("RED", "red_ui")):
            cards.append(f'<div class="card color"><span class="label">{label}</span>{img(APP_IMAGES[key],"ui-shot")}</div>')
        return f'<div class="body frame03"><div class="color-grid">{"".join(cards)}</div></div>'
    if asset_id == "phone-04-batch":
        return f"""
<div class="body frame04">
 <div class="card batch-selection">{img(CROPS / 'batch-selection-crop.png','ui-shot')}</div>
 <div class="batch-grid">
  <div class="card batch"><span class="label blue">REAL PROGRESS</span>{img(CROPS / 'batch-progress-crop.png','ui-shot')}</div>
  <div class="card batch"><span class="label green">COMPLETED</span>{img(CROPS / 'batch-summary-crop.png','ui-shot')}</div>
 </div>
 <div class="batch-proof"><strong>3</strong><i>→</i><strong>3</strong><i>•</i><strong>0</strong></div>
</div>"""
    if asset_id == "phone-05-resize":
        return f"""
<div class="body frame05">
 <div class="card quality-ui">{img(CROPS / 'quality-controls-crop.png','ui-shot')}</div>
 <div class="card resize-ui">{img(CROPS / 'resize-controls-crop.png','ui-shot')}</div>
 <div class="resize-chips"><b>25%</b><b>50%</b><b>75%</b><b>Custom width × height</b></div>
 <div class="aspect"><span>✓</span><strong>Maintain aspect ratio</strong></div>
</div>"""
    if asset_id == "phone-06-format":
        return f"""
<div class="body frame06">
 <div class="format-chips"><b>JPG</b><b>PNG</b><b>WEBP</b></div>
 <div class="card format-ui">{img(CROPS / 'format-controls-crop.png','ui-shot')}</div>
 <div class="card format-result">{img(CROPS / 'format-result-crop.png','ui-shot')}</div>
</div>"""
    if asset_id == "phone-07-comparison":
        return f"""
<div class="body frame07">
 <div class="compare-grid">
  <div class="card compare-ui">{img(CROPS / 'comparison-ui-crop.png','ui-shot')}</div>
  <div class="compare-side">
   <div class="card comparison-stats">{img(CROPS / 'result-metrics-crop.png','ui-shot')}</div>
   <div class="comparison-photos"><div class="card">{img(ORIGINAL)}</div><div class="card">{img(COMPRESSED)}</div></div>
   <div class="comparison-details"><div class="card">{img(CROPS / 'portrait-original-100pct.png')}</div><div class="card">{img(CROPS / 'portrait-compressed-100pct.png')}</div></div>
  </div>
 </div>
 <div class="stats-bar"><div><b>2.68 MB</b><span>Original</span></div><div><b>236 kB</b><span>Processed</span></div><div><b>91.2%</b><span>Saved</span></div><div><b>11.38:1</b><span>Ratio</span></div></div>
</div>"""
    if asset_id == "phone-08-trust":
        bullets = [part.strip() for part in row["supporting_copy"].split("•")]
        bullet_html = "".join(f'<div class="trust-item"><span>✓</span><b>{html.escape(part)}</b></div>' for part in bullets)
        return f"""
<div class="body frame08">
 <div class="trust-list">{bullet_html}</div>
 <div class="trust-grid"><div class="card trust-ui">{img(CROPS / 'home-content-crop.png','ui-shot')}</div><div class="card trust-ui actions">{img(CROPS / 'stats-actions-crop.png','ui-shot')}</div></div>
</div>"""
    fail(f"no phone layout for {asset_id}")
    return ""


PHONE_CSS = r"""
.frame01{display:flex;flex-direction:column;gap:20px}.compare-photo{height:565px;display:grid;grid-template-columns:1fr 1fr;gap:4px;padding:12px}.photo-block{position:relative;overflow:hidden;border-radius:25px}.photo-block img{width:100%;height:100%;object-fit:cover;object-position:50% 30%}.photo-block .label{position:absolute;left:18px;bottom:18px}.savings{height:118px}.detail-pair{display:grid;grid-template-columns:1fr 1fr;gap:18px;height:250px}.detail{position:relative;padding-top:43px}.detail span{position:absolute;top:12px;left:16px;color:#475569;font-size:16px;font-weight:780;letter-spacing:.4px}.detail img{width:300px;height:180px;display:block;margin:auto;object-fit:none}.evidence{height:405px;padding:10px}.evidence img{object-fit:contain;object-position:center}
.frame02{display:flex;flex-direction:column;gap:24px}.transform{height:900px;display:grid;grid-template-columns:1fr 72px 1fr;align-items:center}.portrait{position:relative;height:880px}.portrait img{width:100%;height:100%;object-fit:cover;object-position:50% 30%}.portrait.checker img{object-fit:contain;object-position:center bottom}.portrait .label{position:absolute;z-index:2;left:22px;top:22px}.arrow.big{width:66px;height:66px;font-size:38px}.cutout-evidence{height:585px}.cutout-evidence img{object-position:50% 22%}
.frame03{}.color-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px}.color{height:680px;position:relative}.color .label{display:none}.color img{object-position:50% 12%}
.frame04{display:flex;flex-direction:column;gap:22px}.batch-selection{height:475px;padding:10px}.batch-selection img{object-fit:contain;object-position:center}.batch-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px;height:675px}.batch{position:relative;padding:8px}.batch .label{position:absolute;z-index:3;left:18px;top:18px}.batch img{object-fit:contain;object-position:center;background:#F7FAFA}.batch-proof{height:110px;background:#0F172A;color:white;border-radius:28px;display:flex;align-items:center;justify-content:center;gap:24px;font-size:25px;box-shadow:0 18px 44px rgba(15,23,42,.18)}.batch-proof i{color:#93C5FD;font-size:32px;font-style:normal}
.frame05{display:flex;flex-direction:column;justify-content:center;gap:24px}.quality-ui{height:480px;padding:10px}.quality-ui img{object-fit:contain;object-position:center}.resize-ui{height:590px;padding:10px}.resize-ui img{object-fit:contain;object-position:center}.resize-chips{display:grid;grid-template-columns:1fr 1fr 1fr 2.2fr;gap:12px}.resize-chips b{background:#fff;border:2px solid #93C5FD;border-radius:22px;padding:24px 12px;text-align:center;font-size:23px;color:#1E3A8A}.aspect{display:flex;align-items:center;justify-content:center;gap:16px;background:#DBEAFE;border-radius:24px;padding:24px;font-size:25px}.aspect span{width:38px;height:38px;border-radius:50%;display:grid;place-items:center;background:#16A34A;color:#fff}
.frame06{display:flex;flex-direction:column;gap:22px}.format-chips{display:grid;grid-template-columns:repeat(3,1fr);gap:16px}.format-chips b{background:#0F172A;color:#fff;border-radius:26px;padding:25px;text-align:center;font-size:31px;letter-spacing:1px}.format-chips b:nth-child(2){background:#2563EB}.format-chips b:nth-child(3){background:#16A34A}.format-ui{height:610px;padding:12px}.format-ui img{object-fit:contain;object-position:center}.format-result{height:610px;padding:12px}.format-result img{object-fit:contain;object-position:center}
.frame07{display:flex;flex-direction:column;gap:22px}.compare-grid{display:grid;grid-template-columns:.95fr 1.05fr;gap:18px;height:1230px}.compare-ui{padding:8px}.compare-ui img{object-fit:contain;object-position:center}.compare-side{display:flex;flex-direction:column;gap:16px}.comparison-stats{height:350px;padding:8px}.comparison-stats img{object-fit:contain;object-position:center}.comparison-photos{display:grid;grid-template-columns:1fr 1fr;gap:12px;height:545px}.comparison-photos .card img{width:100%;height:100%;object-fit:cover;object-position:50% 30%}.comparison-details{display:grid;grid-template-columns:1fr 1fr;gap:12px;height:255px}.comparison-details .card{display:grid;place-items:center}.comparison-details img{width:300px;height:180px;object-fit:none;transform:scale(.72)}.stats-bar{display:grid;grid-template-columns:repeat(4,1fr);gap:10px}.stats-bar div{background:#0F172A;color:#fff;padding:22px 8px;border-radius:22px;text-align:center}.stats-bar b{display:block;font-size:27px}.stats-bar span{display:block;color:#BFDBFE;font-size:17px;margin-top:5px}
.frame08{display:flex;flex-direction:column;gap:22px}.trust-list{display:grid;grid-template-columns:1fr 1fr;gap:12px}.trust-item{display:flex;align-items:center;gap:12px;background:#fff;border-radius:22px;padding:18px 20px;color:#334155;box-shadow:0 8px 22px rgba(15,23,42,.07);font-size:19px}.trust-item span{flex:0 0 34px;width:34px;height:34px;border-radius:50%;display:grid;place-items:center;background:#DCFCE7;color:#16A34A;font-weight:900}.trust-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px;height:1170px}.trust-ui{padding:8px}.trust-ui img{object-fit:contain;object-position:center}.trust-ui.actions img{object-fit:contain;object-position:center}
"""


def phone_html(locale: str, asset_id: str, row: dict[str, str], omit_review_footer: bool, headline_override: str | None = None) -> str:
    headline = headline_override or row["headline"]
    size = 66
    if locale in ("hi-IN", "gu-IN"):
        size = 56
    elif locale == "ru-RU":
        size = 60
    if len(headline) > 38:
        size -= 6
    support = row["supporting_copy"]
    subhead = ""
    if asset_id in ("phone-03-solid-color", "phone-05-resize") and support:
        subhead = f'<div class="subhead">{html.escape(support)}</div>'
    return f"""<!doctype html><html lang="{html.escape(locale)}"><head><meta charset="utf-8"><style>{common_css(locale,*PHONE_SIZE,size)}{PHONE_CSS}</style></head><body><div class="canvas"><div class="headline">{html.escape(headline)}</div>{subhead}{phone_fragment(asset_id,row)}{proof(locale,omit_review_footer)}</div></body></html>"""


def feature_html(locale: str, row: dict[str, str], omit_review_footer: bool) -> str:
    size = 43 if locale == "en-US" else 37
    return f"""<!doctype html><html lang="{html.escape(locale)}"><head><meta charset="utf-8"><style>
{common_css(locale,*FEATURE_SIZE,size)}
.headline{{left:54px;right:555px;top:112px;text-align:left;letter-spacing:-1px;line-height:1.04}}.subhead{{left:56px;right:555px;top:275px;text-align:left;font-size:22px;color:#334155}}
.feature-visual{{position:absolute;right:43px;top:40px;width:510px;height:420px}}.before,.after{{position:absolute;width:238px;height:350px;top:30px}}.before{{left:0;transform:rotate(-2deg)}}.after{{right:0;transform:rotate(2deg)}}.before img,.after img{{width:100%;height:100%;object-fit:cover;object-position:50% 30%}}.after img{{object-fit:contain;object-position:center bottom}}.before .label,.after .label{{position:absolute;z-index:2;left:14px;top:14px;font-size:13px;padding:7px 11px}}.feature-arrow{{position:absolute;z-index:4;left:226px;top:175px;width:58px;height:58px;border-radius:50%;display:grid;place-items:center;background:#2563EB;color:#fff;font-size:34px;font-weight:900;box-shadow:0 10px 24px rgba(37,99,235,.3)}}.result-ui{{position:absolute;left:55px;top:310px;width:340px;height:150px;padding:7px}}.result-ui img{{width:100%;height:100%;object-fit:contain;object-position:center}}.proof{{height:30px;font-size:12px}}
</style></head><body><div class="canvas"><div class="headline">{html.escape(row['headline'])}</div><div class="subhead">{html.escape(row['supporting_copy'])}</div><div class="card result-ui">{img(CROPS / 'result-metrics-crop.png')}</div><div class="feature-visual"><div class="card before"><span class="label">ORIGINAL</span>{img(ORIGINAL)}</div><div class="feature-arrow">→</div><div class="card after checker"><span class="label blue">REAL CUTOUT</span>{img(CUTOUT)}</div></div>{proof(locale,omit_review_footer)}</div></body></html>"""


def normalize_png(path: Path, mode: str = "RGB") -> None:
    srgb = ImageCms.ImageCmsProfile(ImageCms.createProfile("sRGB")).tobytes()
    with Image.open(path) as source:
        if mode == "RGB":
            image = source.convert("RGB")
        elif mode == "RGBA":
            image = source.convert("RGBA")
            alpha = Image.new("L", image.size, 255)
            image.putalpha(alpha)
        else:
            raise ValueError(mode)
        image.save(path, format="PNG", optimize=True, compress_level=9, icc_profile=srgb)


def render(chrome: Path, source_html: str, html_path: Path, output: Path, size: tuple[int, int]) -> None:
    html_path.parent.mkdir(parents=True, exist_ok=True)
    output.parent.mkdir(parents=True, exist_ok=True)
    html_path.write_text(source_html, encoding="utf-8")
    command = [
        str(chrome), "--headless=new", "--disable-gpu", "--hide-scrollbars",
        "--allow-file-access-from-files", "--run-all-compositor-stages-before-draw",
        "--virtual-time-budget=1800", "--force-device-scale-factor=1",
        f"--window-size={size[0]},{size[1]}", f"--screenshot={output.resolve()}",
        html_path.resolve().as_uri(),
    ]
    result = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if result.returncode:
        fail(f"Chrome failed for {html_path.name}: {result.stderr.strip()}")
    if not output.is_file():
        fail(f"Chrome produced no output for {html_path.name}")
    with Image.open(output) as image:
        if image.size != size:
            fail(f"unexpected render dimensions for {output}: {image.size}")
    normalize_png(output, "RGB")


def prepare_sources() -> None:
    crop_dir = CROPS
    crop_dir.mkdir(parents=True, exist_ok=True)
    crop_box = (300, 120, 600, 300)
    for source, name in ((ORIGINAL, "portrait-original-100pct.png"), (COMPRESSED, "portrait-compressed-100pct.png")):
        with Image.open(source) as image:
            image.convert("RGB").crop(crop_box).save(crop_dir / name, "PNG", optimize=True)
    capture_crops = [
        (APP_IMAGES["home"], (42, 328, 1038, 827), "batch-selection-crop.png"),
        (APP_IMAGES["batch_progress"], (42, 328, 1038, 1195), "batch-progress-crop.png"),
        (APP_IMAGES["batch_done"], (42, 328, 1038, 1251), "batch-summary-crop.png"),
        # The narrow phone layout naturally clips the trailing Custom preset.
        # Use the real custom fields and aspect controls, while the surrounding
        # template names the verified presets without reconstructing app UI.
        (APP_IMAGES["custom_resize"], (36, 576, 1044, 945), "quality-controls-crop.png"),
        (APP_IMAGES["custom_resize"], (36, 1220, 1044, 1715), "resize-controls-crop.png"),
        (APP_IMAGES["custom_resize"], (36, 1760, 1044, 2130), "format-controls-crop.png"),
        (APP_IMAGES["batch_results"], (36, 535, 1044, 1290), "format-result-crop.png"),
        (APP_IMAGES["comparison"], (36, 280, 1044, 2380), "comparison-ui-crop.png"),
        (APP_IMAGES["batch_results"], (36, 1600, 1044, 2180), "result-metrics-crop.png"),
        (APP_IMAGES["home"], (36, 328, 1044, 1889), "home-content-crop.png"),
        (APP_IMAGES["stats_actions"], (36, 340, 1044, 2265), "stats-actions-crop.png"),
    ]
    for source, box, name in capture_crops:
        with Image.open(source) as image:
            image.convert("RGB").crop(box).save(crop_dir / name, "PNG", optimize=True)
    icon_source_dir = SOURCE / "icon"
    icon_source_dir.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(REPO / "play-store/assets/app-icon-512.png", icon_source_dir / "store-icon-pixel-source-512.png")
    shutil.copyfile(REPO / "app/src/main/res/drawable-nodpi/ic_image_compressor_logo.png", icon_source_dir / "installed-brand-reference-1024.png")


def export_icon() -> None:
    output = ROOT / "common/icon/app-icon-512.png"
    output.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(REPO / "play-store/assets/app-icon-512.png", output)
    normalize_png(output, "RGBA")


def export_large_screens() -> None:
    names = {
        "01-batch-selected-raw.png": "01-batch-selection.png",
        "02-batch-summary-raw.png": "02-batch-summary.png",
        "03-background-removal-raw.png": "03-background-removal.png",
        "04-comparison-raw.png": "04-comparison.png",
    }
    for device in ("tablet-7", "tablet-10", "chromebook"):
        clean_path = CAPTURES / device / "03-background-removal-clean-raw.png"
        with Image.open(clean_path) as clean_source:
            clean = clean_source.convert("RGB")
            # Standardize only the captured Android System UI status region.
            # The original app captures remain untouched under source/captures.
            clean_status = clean.crop((0, 0, LARGE_SIZE[0], 48))
        for raw_name, final_name in names.items():
            output = ROOT / "en-US" / device / final_name
            output.parent.mkdir(parents=True, exist_ok=True)
            source_path = clean_path if raw_name == "03-background-removal-raw.png" else CAPTURES / device / raw_name
            with Image.open(source_path) as source:
                final = source.convert("RGB")
                if raw_name != "03-background-removal-raw.png":
                    final.paste(clean_status, (0, 0))
                final.save(output, "PNG")
            normalize_png(output, "RGB")


def contact_sheet(paths: list[Path], output: Path, columns: int, thumb: tuple[int, int], labels: bool = False) -> None:
    rows = (len(paths) + columns - 1) // columns
    label_h = 34 if labels else 0
    sheet = Image.new("RGB", (columns * thumb[0], rows * (thumb[1] + label_h)), "#E8EEF9")
    draw = ImageDraw.Draw(sheet)
    for index, path in enumerate(paths):
        with Image.open(path) as source:
            tile = ImageOps.fit(source.convert("RGB"), thumb, method=Image.Resampling.LANCZOS)
        x = index % columns * thumb[0]
        y = index // columns * (thumb[1] + label_h)
        sheet.paste(tile, (x, y))
        if labels:
            draw.text((x + 8, y + thumb[1] + 6), path.stem[:32], fill="#0F172A")
    output.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(output, "PNG", optimize=True)
    normalize_png(output, "RGB")


def make_contact_sheets() -> None:
    destination = ROOT / "qa/contact-sheets"
    for locale in ("en-US", "hi-IN", "gu-IN", "ru-RU"):
        paths = sorted((ROOT / locale / "phone").glob("*.png"))
        contact_sheet(paths, destination / f"{locale}-phone-contact-sheet.png", 4, (270, 480))
        contact_sheet(paths, destination / f"{locale}-phone-thumbnail-strip.png", 8, (135, 240))
    for device in ("tablet-7", "tablet-10", "chromebook"):
        paths = sorted((ROOT / "en-US" / device).glob("*.png"))
        contact_sheet(paths, destination / f"en-US-{device}-contact-sheet.png", 2, (960, 540))
    experiments = sorted((ROOT / "experiments").glob("*.png"))
    contact_sheet(experiments, destination / "experiment-contact-sheet.png", 3, (270, 480), labels=True)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--translations-reviewed", action="store_true", help="remove proof footer only after qualified native review")
    parser.add_argument(
        "--omit-review-footer",
        action="store_true",
        help="remove the visible review footer without changing translation review status",
    )
    args = parser.parse_args()
    require_inputs()
    chrome = chrome_binary()
    rows = copy_rows()
    if args.translations_reviewed:
        unapproved = sorted({
            locale
            for locale, locale_rows in rows.items()
            if locale != "en-US"
            and any(row["review_status"] != "TRANSLATION_APPROVED" for row in locale_rows.values())
        })
        if unapproved:
            fail(
                "--translations-reviewed is blocked until every copy-deck row for "
                + ", ".join(unapproved)
                + " has review_status TRANSLATION_APPROVED"
            )
        missing_records = []
        for locale in ("hi-IN", "gu-IN", "ru-RU"):
            review_file = ROOT / locale / "translation-review-required.md"
            review_text = review_file.read_text(encoding="utf-8") if review_file.is_file() else ""
            if "Reviewer / date / approval record:" not in review_text or "Reviewer / date / approval record: **pending**" in review_text:
                missing_records.append(locale)
        if missing_records:
            fail(
                "--translations-reviewed is blocked until reviewer/date approval records replace pending in "
                + ", ".join(missing_records)
            )
    render_policy = {
        "localized_copy_status": "TRANSLATION_APPROVED" if args.translations_reviewed else "TRANSLATION_REVIEW_REQUIRED_DO_NOT_PUBLISH",
        "visible_review_footer": not (args.translations_reviewed or args.omit_review_footer),
        "footer_suppressed_without_translation_approval": bool(args.omit_review_footer and not args.translations_reviewed),
    }
    (ROOT / "qa/render-policy.json").write_text(
        json.dumps(render_policy, indent=2, sort_keys=True) + "\n",
        encoding="utf-8",
    )
    prepare_sources()
    export_icon()

    for locale in ("en-US", "hi-IN", "gu-IN", "ru-RU"):
        reviewed = locale == "en-US" or args.translations_reviewed
        omit_review_footer = reviewed or args.omit_review_footer
        feature_row = rows[locale]["feature-graphic"]
        render(chrome, feature_html(locale, feature_row, omit_review_footer), HTML_OUT / f"{locale}-feature-graphic.html", ROOT / locale / "feature-graphic/feature-graphic-1024x500.png", FEATURE_SIZE)
        for asset_id, filename in PHONE_NAMES.items():
            row = rows[locale][asset_id]
            render(chrome, phone_html(locale, asset_id, row, omit_review_footer), HTML_OUT / f"{locale}-{asset_id}.html", ROOT / locale / "phone" / filename, PHONE_SIZE)

    export_large_screens()

    base_row = rows["en-US"]["phone-01-compression"]
    experiment_a = rows["en-US"]["experiment-01-a-savings"]["headline"]
    render(chrome, phone_html("en-US", "phone-01-compression", base_row, True, experiment_a), HTML_OUT / "experiment-01-a-storage.html", ROOT / "experiments/01-first-screenshot-a-storage.png", PHONE_SIZE)
    render(chrome, phone_html("en-US", "phone-01-compression", base_row, True), HTML_OUT / "experiment-01-b-quality.html", ROOT / "experiments/02-first-screenshot-b-quality.png", PHONE_SIZE)
    shutil.copyfile(ROOT / "en-US/phone/02-background-removal.png", ROOT / "experiments/03-background-removal-first.png")
    make_contact_sheets()
    print("Generated icon, 4 locale feature graphics, 32 phone frames, 12 large-screen captures, 3 experiment assets, and contact sheets.")


if __name__ == "__main__":
    main()
