#!/usr/bin/env python3
"""Create localized feature graphics and visual-review contact sheets."""

from __future__ import annotations

import html
import os
import shutil
import subprocess
import xml.etree.ElementTree as ET
from pathlib import Path

from PIL import Image, ImageCms, ImageDraw, ImageOps


ROOT = Path(__file__).resolve().parents[1]
REPO = ROOT.parent
FINAL = ROOT / "final"
HTML = ROOT / "source/templates/final-html"
LOCALE_RESOURCES = {
    "en": "values",
    "de": "values-de",
    "fr": "values-fr",
    "ja": "values-ja",
    "hi": "values-hi",
    "ru": "values-ru",
    "es": "values-es",
    "pt-PT": "values-pt-rPT",
    "pt-BR": "values-pt-rBR",
    "it": "values-it",
    "id": "values-id",
    "ar": "values-ar",
    "ko": "values-ko",
    "ur": "values-ur",
}
ORIGINAL = ROOT / "source/sample-images/portrait-curly-hair-original.png"
CUTOUT = ROOT / "source/sample-images/real-app-results/background/portrait-transparent.png"
ICON = REPO / "play-store/assets/app-icon-512.png"


def chrome() -> str:
    candidates = (
        os.environ.get("CHROME_BIN"),
        "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
        shutil.which("google-chrome"),
        shutil.which("chromium"),
    )
    for candidate in candidates:
        if candidate and Path(candidate).is_file():
            return str(candidate)
    raise RuntimeError("Chrome/Chromium not found")


def strings(locale: str) -> dict[str, str]:
    directory = LOCALE_RESOURCES[locale]
    path = REPO / "app/src/main/res" / directory / "strings.xml"
    root = ET.parse(path).getroot()
    return {
        element.attrib["name"]: "".join(element.itertext()).replace("\\'", "'")
        for element in root
        if element.tag == "string" and "name" in element.attrib
    }


def uri(path: Path) -> str:
    return path.resolve().as_uri()


def normalize(path: Path, mode: str = "RGB") -> None:
    srgb = ImageCms.ImageCmsProfile(ImageCms.createProfile("sRGB")).tobytes()
    with Image.open(path) as source:
        image = source.convert(mode)
        if mode == "RGBA":
            image.putalpha(255)
        image.save(path, "PNG", optimize=True, compress_level=9, icc_profile=srgb)


def render_feature(locale: str) -> None:
    catalog = strings(locale)
    direction = "rtl" if locale in {"ar", "ur"} else "ltr"
    result_capture = FINAL / locale / "phone/result-stats.png"
    output = FINAL / locale / "feature-graphic/feature-graphic-1024x500.png"
    source = HTML / f"{locale}-feature.html"
    output.parent.mkdir(parents=True, exist_ok=True)
    source.parent.mkdir(parents=True, exist_ok=True)
    headline = html.escape(catalog["home_intro"])
    supporting = html.escape(catalog["home_formats_privacy"])
    original = html.escape(catalog["original"])
    processed = html.escape(catalog["history_operation_background_removed"])
    source.write_text(
        f"""<!doctype html><html lang="{html.escape(locale)}" dir="{direction}">
<head><meta charset="utf-8"><style>
*{{box-sizing:border-box}}html,body{{margin:0;width:1024px;height:500px;overflow:hidden}}
body{{font-family:"Noto Sans",Arial,sans-serif;color:#12212A;background:
radial-gradient(circle at 8% 10%,#DDF8F1 0,transparent 32%),
linear-gradient(140deg,#F7FAFA 0%,#E8F2F4 58%,#DCE6FA 100%)}}
.copy{{position:absolute;left:42px;top:40px;width:445px;z-index:3}}
h1{{font-size:43px;line-height:1.05;margin:0 0 17px;font-weight:850;letter-spacing:-1px}}
p{{font-size:19px;line-height:1.34;margin:0;color:#3E525D;font-weight:560}}
.brand{{position:absolute;left:42px;bottom:28px;display:flex;align-items:center;gap:12px;
font-size:16px;font-weight:760;color:#1B3843}}.brand img{{width:48px;height:48px;border-radius:11px}}
.visual{{position:absolute;right:32px;top:25px;width:500px;height:450px}}
.card{{position:absolute;top:18px;width:230px;height:335px;border-radius:25px;overflow:hidden;
background:#fff;box-shadow:0 18px 42px rgba(25,53,75,.19);border:1px solid #D4E2E7}}
.before{{left:5px;transform:rotate(-2deg)}}.after{{right:5px;transform:rotate(2deg);
background-image:linear-gradient(45deg,#D8DEE3 25%,transparent 25%),
linear-gradient(-45deg,#D8DEE3 25%,transparent 25%),
linear-gradient(45deg,transparent 75%,#D8DEE3 75%),
linear-gradient(-45deg,transparent 75%,#D8DEE3 75%);background-size:28px 28px}}
.card img{{width:100%;height:100%;object-fit:cover;object-position:center 30%}}
.after img{{object-fit:contain;object-position:center bottom}}
.label{{position:absolute;z-index:2;left:12px;top:12px;background:rgba(15,35,45,.86);color:#fff;
border-radius:999px;padding:7px 11px;font-size:12px;font-weight:800;max-width:200px}}
.after .label{{background:#087F5B}}.arrow{{position:absolute;z-index:4;left:222px;top:150px;width:58px;height:58px;
border-radius:50%;background:#087F5B;color:white;display:grid;place-items:center;font-size:34px;font-weight:900;
box-shadow:0 9px 20px rgba(8,127,91,.28)}}
.proof{{position:absolute;right:74px;bottom:0;width:350px;height:118px;border-radius:18px 18px 0 0;
overflow:hidden;box-shadow:0 -6px 26px rgba(25,53,75,.16);border:1px solid #C9D8DD}}
.proof img{{width:100%;height:100%;object-fit:cover;object-position:center 78%}}
[dir=rtl] .copy{{text-align:right}}[dir=rtl] h1{{font-size:40px}}
</style></head><body>
<section class="copy"><h1>{headline}</h1><p>{supporting}</p></section>
<div class="brand"><img src="{uri(ICON)}">Photo Compressor &amp; BG Remover</div>
<section class="visual">
 <div class="card before"><span class="label">{original}</span><img src="{uri(ORIGINAL)}"></div>
 <div class="arrow">→</div>
 <div class="card after"><span class="label">{processed}</span><img src="{uri(CUTOUT)}"></div>
 <div class="proof"><img src="{uri(result_capture)}"></div>
</section></body></html>""",
        encoding="utf-8",
    )
    command = [
        chrome(), "--headless=new", "--disable-gpu", "--hide-scrollbars",
        "--allow-file-access-from-files", "--run-all-compositor-stages-before-draw",
        "--virtual-time-budget=1600", "--force-device-scale-factor=1",
        "--window-size=1024,500", f"--screenshot={output.resolve()}",
        source.resolve().as_uri(),
    ]
    result = subprocess.run(command, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if result.returncode:
        raise RuntimeError(result.stderr.decode(errors="replace"))
    normalize(output)


def contact_sheet(
    paths: list[Path],
    output: Path,
    columns: int,
    thumb: tuple[int, int],
    labels: bool = True,
) -> None:
    label_height = 26 if labels else 0
    rows = (len(paths) + columns - 1) // columns
    sheet = Image.new(
        "RGB",
        (columns * thumb[0], rows * (thumb[1] + label_height)),
        "#E7EEF2",
    )
    draw = ImageDraw.Draw(sheet)
    for index, path in enumerate(paths):
        with Image.open(path) as source:
            tile = ImageOps.contain(source.convert("RGB"), thumb, Image.Resampling.LANCZOS)
        x = (index % columns) * thumb[0] + (thumb[0] - tile.width) // 2
        y = (index // columns) * (thumb[1] + label_height)
        sheet.paste(tile, (x, y))
        if labels:
            draw.text(
                ((index % columns) * thumb[0] + 5, y + thumb[1] + 5),
                f"{path.parent.name}/{path.stem}"[:36],
                fill="#10242E",
            )
    output.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(output, "PNG")
    normalize(output)


def make_review_sheets() -> None:
    review = ROOT / "qa/final-contact-sheets"
    locales = tuple(LOCALE_RESOURCES)
    for locale in locales:
        overview = FINAL / locale / "all-features-3840x2160.png"
        contact_sheet(
            [FINAL / locale / "feature-graphic/feature-graphic-1024x500.png"]
            + [
                FINAL / locale / "tablet-10" / f"{screen}.png"
                for screen in (
                    "home", "editor", "batch", "result",
                    "result-stats", "background", "history", "settings",
                )
            ],
            overview,
            columns=3,
            thumb=(1280, 720),
            labels=False,
        )
        paths = [FINAL / locale / "feature-graphic/feature-graphic-1024x500.png"]
        paths.append(overview)
        paths += sorted((FINAL / locale / "phone").glob("*.png"))
        for profile in ("tablet-7", "tablet-10", "chromebook"):
            paths += sorted((FINAL / locale / profile).glob("*.png"))
        contact_sheet(
            paths,
            review / f"{locale}-all-assets.png",
            columns=5,
            thumb=(320, 220),
        )
    contact_sheet(
        [FINAL / locale / "feature-graphic/feature-graphic-1024x500.png" for locale in locales],
        review / "all-feature-graphics.png",
        columns=2,
        thumb=(512, 250),
    )
    contact_sheet(
        [FINAL / locale / "all-features-3840x2160.png" for locale in locales],
        review / "all-common-sheets.png",
        columns=2,
        thumb=(640, 360),
    )
    for profile in ("phone", "tablet-7", "tablet-10", "chromebook"):
        paths = [
            path
            for locale in locales
            for path in sorted((FINAL / locale / profile).glob("*.png"))
        ]
        thumb = (135, 240) if profile == "phone" else (320, 180)
        columns = 8 if profile == "phone" else 4
        contact_sheet(
            paths,
            review / f"all-{profile}.png",
            columns=columns,
            thumb=thumb,
        )


def main() -> None:
    icon_output = FINAL / "common/icon/app-icon-512.png"
    icon_output.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(ICON, icon_output)
    normalize(icon_output, "RGBA")
    for locale in LOCALE_RESOURCES:
        render_feature(locale)
        print(f"rendered {locale} feature graphic", flush=True)
    make_review_sheets()
    print(
        "Generated 14 localized feature graphics, 14 all-feature overview "
        "sheets, and final visual-review sheets."
    )


if __name__ == "__main__":
    main()
