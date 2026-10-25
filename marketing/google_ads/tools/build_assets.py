#!/usr/bin/env python3
"""Build truthful Google Ads assets from deterministic real-app captures."""

from __future__ import annotations

import csv
import json
import shutil
import subprocess
import tempfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageOps


GOOGLE_ADS = Path(__file__).resolve().parents[1]
REPO_ROOT = Path(__file__).resolve().parents[3]
RAW_IMAGES = GOOGLE_ADS / "images" / "raw"
FINAL_IMAGES = GOOGLE_ADS / "images" / "final"
RAW_VIDEOS = GOOGLE_ADS / "videos" / "raw"
FINAL_VIDEOS = GOOGLE_ADS / "videos" / "final"
TEXT_DIR = GOOGLE_ADS / "text"
QA_DIR = GOOGLE_ADS / "qa"


TEXT_COLUMNS = [
    "id",
    "text",
    "character_count",
    "creative_angle",
    "verified_feature_source",
    "recommended_priority",
]

SHORT_HEADLINES = [
    ("SH01", "Compress photos", "Core compression", "README.md:3", "high"),
    ("SH02", "Set a target size", "Target-size control", "CompressionConfig.kt:5-30", "high"),
    ("SH03", "Resize with control", "Resizing", "CompressionConfig.kt:32-38", "medium"),
    ("SH04", "Remove photo backgrounds", "Background removal", "README.md:40-45", "high"),
    ("SH05", "Compare before you save", "Before-and-after comparison", "play-store/store-listing.md:52", "high"),
    ("SH06", "Batch-compress your photos", "Batch processing", "play-store/store-listing.md:56", "high"),
    ("SH07", "Choose JPG, PNG or WEBP", "Format conversion", "play-store/store-listing.md:59", "medium"),
    ("SH08", "Keep photo work on-device", "On-device processing", "README.md:3", "high"),
    ("SH09", "Photo Compressor & BG Remover", "Brand", "app/src/main/res/values/strings.xml:4", "medium"),
    ("SH10", "Balance quality and size", "Quality modes", "EditorScreen.kt:quality_mode", "medium"),
    ("SH11", "Resize by percent or pixels", "Flexible resizing", "play-store/store-listing.md:58", "medium"),
    ("SH12", "See the size you saved", "Measurable result", "ResultScreen.kt:compression_stats", "medium"),
    ("SH13", "Convert image formats", "Format conversion", "play-store/store-listing.md:59", "medium"),
    ("SH14", "Make sharing files smaller", "Smaller sharing files", "play-store/store-listing.md:73", "medium"),
    ("SH15", "Process up to 50 photos", "Batch capacity", "play-store/store-listing.md:56", "medium"),
]

LONG_HEADLINES = [
    (
        "LH01",
        "Compress photos toward a KB or MB target, then compare before you save.",
        "Target-size compression",
        "play-store/store-listing.md:52",
        "high",
    ),
    (
        "LH02",
        "Resize photos by percentage or custom dimensions and keep the aspect ratio when needed.",
        "Flexible resizing",
        "play-store/store-listing.md:58",
        "medium",
    ),
    (
        "LH03",
        "Remove or replace photo backgrounds on your device without uploading your images.",
        "On-device background removal",
        "README.md:40-45",
        "high",
    ),
    (
        "LH04",
        "Process one photo or a batch of up to 50 and export as JPG, PNG, or WEBP.",
        "Batch and formats",
        "play-store/store-listing.md:56-59",
        "high",
    ),
    (
        "LH05",
        "Photo Compressor & BG Remover combines compression, resizing, conversion, and cleanup.",
        "All-in-one workflow",
        "README.md:3",
        "medium",
    ),
]

DESCRIPTIONS = [
    (
        "DS01",
        "Set a KB or MB target and choose the balance between quality and file size.",
        "Target-size control",
        "CompressionConfig.kt:5-51",
        "high",
    ),
    (
        "DS02",
        "Compare original and processed images before saving, sharing, or opening.",
        "Before-and-after comparison",
        "play-store/store-listing.md:52",
        "high",
    ),
    (
        "DS03",
        "Resize by percent or custom pixels, then export as JPG, PNG, or WEBP.",
        "Resize and convert",
        "play-store/store-listing.md:58-59",
        "medium",
    ),
    (
        "DS04",
        "Remove or replace photo backgrounds on your device with a bundled model.",
        "On-device background removal",
        "README.md:40-45",
        "high",
    ),
    (
        "DS05",
        "Select one image or batch-process up to 50 with the same compression settings.",
        "Batch processing",
        "play-store/store-listing.md:52-56",
        "high",
    ),
]


IMAGE_SPECS = [
    (
        "landscape/editor_target_size_landscape_1200x628.png",
        "editor_portrait_1080x1920.png",
        (1200, 628),
        (0.5, 0.92),
        "Meaningful size decision",
        "CI-02",
        "Performance Max and App campaign",
    ),
    (
        "landscape/background_result_landscape_1200x628.png",
        "background_landscape_1920x1080.png",
        (1200, 628),
        (0.5, 0.37),
        "On-device background removal",
        "CI-05",
        "Performance Max and App campaign",
    ),
    (
        "landscape/compare_result_landscape_1200x628.png",
        "result_landscape_1920x1080.png",
        (1200, 628),
        (0.5, 0.38),
        "Before-and-after proof",
        "CI-04",
        "Performance Max and App campaign",
    ),
    (
        "landscape/batch_complete_landscape_1200x628.png",
        "batch_landscape_1920x1080.png",
        (1200, 628),
        (0.5, 0.35),
        "Batch completion",
        "CI-03",
        "Performance Max and App campaign",
    ),
    (
        "square/home_batch_square_1200x1200.png",
        "home_portrait_1080x1920.png",
        (1200, 1200),
        (0.5, 0.0),
        "Objective instantly",
        "CI-01",
        "Performance Max and App campaign",
    ),
    (
        "square/history_outputs_square_1200x1200.png",
        "history_portrait_1080x1920.png",
        (1200, 1200),
        (0.5, 0.0),
        "Completed outputs",
        "CI-06",
        "Performance Max and App campaign",
    ),
    (
        "square/background_result_square_1200x1200.png",
        "background_portrait_1080x1920.png",
        (1200, 1200),
        (0.5, 0.0),
        "On-device background removal",
        "CI-05",
        "Performance Max and App campaign",
    ),
    (
        "square/compare_result_square_1200x1200.png",
        "result_portrait_1080x1920.png",
        (1200, 1200),
        (0.5, 0.0),
        "Before-and-after proof",
        "CI-04",
        "Performance Max and App campaign",
    ),
    (
        "vertical/home_batch_vertical_1200x1500.png",
        "home_portrait_1080x1920.png",
        (1200, 1500),
        (0.5, 0.0),
        "Objective instantly",
        "CI-01",
        "Performance Max and App campaign",
    ),
    (
        "vertical/editor_resize_preview_vertical_1200x1500.png",
        "editor_portrait_1080x1920.png",
        (1200, 1500),
        (0.5, 0.53),
        "Resize and output information",
        "CI-02",
        "Performance Max and App campaign",
    ),
    (
        "vertical/batch_complete_vertical_1200x1500.png",
        "batch_portrait_1080x1920.png",
        (1200, 1500),
        (0.5, 0.0),
        "Batch completion",
        "CI-03",
        "Performance Max and App campaign",
    ),
    (
        "vertical/background_result_vertical_1200x1500.png",
        "background_portrait_1080x1920.png",
        (1200, 1500),
        (0.5, 0.0),
        "On-device background removal",
        "CI-05",
        "Performance Max and App campaign",
    ),
]


VIDEO_METADATA = {
    "concept_a": {
        "angle": "On-device background removal",
        "state_id": "CI-02>CI-05>CI-01",
    },
    "concept_b": {
        "angle": "Batch challenge and satisfying result",
        "state_id": "CI-01>CI-02>CI-03>CI-04",
    },
}


def run(command: list[str]) -> None:
    subprocess.run(command, cwd=REPO_ROOT, check=True)


def write_csv(path: Path, rows: list[tuple[str, str, str, str, str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=TEXT_COLUMNS)
        writer.writeheader()
        for asset_id, text, angle, source, priority in rows:
            writer.writerow(
                {
                    "id": asset_id,
                    "text": text,
                    "character_count": len(text),
                    "creative_angle": angle,
                    "verified_feature_source": source,
                    "recommended_priority": priority,
                }
            )


def build_text_assets() -> None:
    write_csv(TEXT_DIR / "SHORT_HEADLINES.csv", SHORT_HEADLINES)
    write_csv(TEXT_DIR / "LONG_HEADLINES.csv", LONG_HEADLINES)
    write_csv(TEXT_DIR / "DESCRIPTIONS.csv", DESCRIPTIONS)


def build_images() -> list[dict[str, str]]:
    manifest_rows: list[dict[str, str]] = []
    for relative_output, source_name, size, centering, angle, state_id, use in IMAGE_SPECS:
        source = RAW_IMAGES / source_name
        output = FINAL_IMAGES / relative_output
        output.parent.mkdir(parents=True, exist_ok=True)
        with Image.open(source) as image:
            image = image.convert("RGB")
            fitted = ImageOps.fit(
                image,
                size,
                method=Image.Resampling.LANCZOS,
                centering=centering,
            )
            fitted.save(output, format="PNG", optimize=True, compress_level=9)
        orientation = output.parent.name
        manifest_rows.append(
            {
                "filename": output.relative_to(GOOGLE_ADS).as_posix(),
                "creative_angle": angle,
                "level_or_state_id": state_id,
                "orientation": orientation,
                "dimensions": f"{size[0]}x{size[1]}",
                "duration_seconds": "",
                "intended_campaign_use": use,
            }
        )
    return manifest_rows


def encode_video(input_path: Path, output_path: Path, video_filter: str | None = None, duration: float | None = None) -> None:
    output_path.parent.mkdir(parents=True, exist_ok=True)
    command = ["ffmpeg", "-y", "-loglevel", "error", "-i", str(input_path)]
    if duration is not None:
        command += ["-t", f"{duration:.3f}"]
    if video_filter:
        command += ["-vf", video_filter]
    command += [
        "-an",
        "-c:v",
        "libx264",
        "-profile:v",
        "high",
        "-level",
        "4.1",
        "-preset",
        "medium",
        "-crf",
        "18",
        "-r",
        "30",
        "-fps_mode",
        "cfr",
        "-pix_fmt",
        "yuv420p",
        "-movflags",
        "+faststart",
        str(output_path),
    ]
    run(command)


def build_concept_a_master(temp_dir: Path) -> Path:
    raw = RAW_VIDEOS / "concept_a_background_portrait_1080x1920.mp4"
    still = temp_dir / "concept_a_background_hold.png"
    home = RAW_IMAGES / "home_portrait_1080x1920.png"
    master = temp_dir / "concept_a_master.mp4"
    run(
        [
            "ffmpeg",
            "-y",
            "-loglevel",
            "error",
            "-ss",
            "10.5",
            "-i",
            str(raw),
            "-frames:v",
            "1",
            str(still),
        ]
    )
    filter_graph = (
        "[0:v]trim=start=0:end=10.8,setpts=PTS-STARTPTS,fps=30[v0];"
        "[1:v]trim=duration=4,setpts=PTS-STARTPTS,fps=30[v1];"
        "[2:v]trim=duration=1.2,setpts=PTS-STARTPTS,fps=30[v2];"
        "[v0][v1][v2]concat=n=3:v=1:a=0,format=yuv420p[v]"
    )
    run(
        [
            "ffmpeg",
            "-y",
            "-loglevel",
            "error",
            "-i",
            str(raw),
            "-loop",
            "1",
            "-t",
            "4",
            "-i",
            str(still),
            "-loop",
            "1",
            "-t",
            "1.2",
            "-i",
            str(home),
            "-filter_complex",
            filter_graph,
            "-map",
            "[v]",
            "-an",
            "-c:v",
            "libx264",
            "-profile:v",
            "high",
            "-level",
            "4.1",
            "-preset",
            "medium",
            "-crf",
            "18",
            "-r",
            "30",
            "-fps_mode",
            "cfr",
            "-pix_fmt",
            "yuv420p",
            "-movflags",
            "+faststart",
            str(master),
        ]
    )
    return master


def build_concept_b_master(temp_dir: Path) -> Path:
    raw = RAW_VIDEOS / "concept_b_batch_portrait_1080x1920.mp4"
    home = RAW_IMAGES / "home_portrait_1080x1920.png"
    workflow = temp_dir / "concept_b_workflow.mp4"
    master = temp_dir / "concept_b_master.mp4"
    encode_video(raw, workflow, "fps=30,format=yuv420p", duration=19.8)
    filter_graph = (
        "[0:v]setpts=PTS-STARTPTS,fps=30[v0];"
        "[1:v]trim=duration=1.2,setpts=PTS-STARTPTS,fps=30[v1];"
        "[v0][v1]concat=n=2:v=1:a=0,format=yuv420p[v]"
    )
    run(
        [
            "ffmpeg",
            "-y",
            "-loglevel",
            "error",
            "-i",
            str(workflow),
            "-loop",
            "1",
            "-t",
            "1.2",
            "-i",
            str(home),
            "-filter_complex",
            filter_graph,
            "-map",
            "[v]",
            "-an",
            "-c:v",
            "libx264",
            "-profile:v",
            "high",
            "-level",
            "4.1",
            "-preset",
            "medium",
            "-crf",
            "18",
            "-r",
            "30",
            "-fps_mode",
            "cfr",
            "-pix_fmt",
            "yuv420p",
            "-movflags",
            "+faststart",
            str(master),
        ]
    )
    return master


def ffprobe(path: Path) -> dict:
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
        ],
        cwd=REPO_ROOT,
    )
    return json.loads(output)


def build_videos() -> list[dict[str, str]]:
    manifest_rows: list[dict[str, str]] = []
    with tempfile.TemporaryDirectory(prefix="compressimage-google-ads-") as directory:
        temp_dir = Path(directory)
        masters = {
            "concept_a": build_concept_a_master(temp_dir),
            "concept_b": build_concept_b_master(temp_dir),
        }
        for concept, master in masters.items():
            if concept == "concept_a":
                square_y = "if(lt(t,2),120,if(lt(t,5),650,if(lt(t,14.8),180,0)))"
                horizontal_y = "if(lt(t,2),250,if(lt(t,5),1150,if(lt(t,14.8),180,0)))"
                stem = "signature_background_removal"
            else:
                square_y = "if(lt(t,2),0,if(lt(t,5),260,if(lt(t,7.5),650,if(lt(t,13),0,0))))"
                horizontal_y = "if(lt(t,2),0,if(lt(t,5),320,if(lt(t,7.5),1100,if(lt(t,13),0,if(lt(t,19.8),100,0)))))"
                stem = "batch_challenge_result"

            outputs = [
                (
                    "vertical",
                    FINAL_VIDEOS / "vertical" / f"{stem}_vertical_1080x1920.mp4",
                    "fps=30,format=yuv420p",
                    (1080, 1920),
                ),
                (
                    "square",
                    FINAL_VIDEOS / "square" / f"{stem}_square_1080x1080.mp4",
                    f"crop=1080:1080:0:'{square_y}',scale=1080:1080:flags=lanczos,fps=30,format=yuv420p",
                    (1080, 1080),
                ),
                (
                    "horizontal",
                    FINAL_VIDEOS / "horizontal" / f"{stem}_horizontal_1920x1080.mp4",
                    f"crop=1080:608:0:'{horizontal_y}',scale=1920:1080:flags=lanczos,fps=30,format=yuv420p",
                    (1920, 1080),
                ),
            ]
            for orientation, output, video_filter, dimensions in outputs:
                encode_video(master, output, video_filter)
                probe = ffprobe(output)
                duration = float(probe["format"]["duration"])
                manifest_rows.append(
                    {
                        "filename": output.relative_to(GOOGLE_ADS).as_posix(),
                        "creative_angle": VIDEO_METADATA[concept]["angle"],
                        "level_or_state_id": VIDEO_METADATA[concept]["state_id"],
                        "orientation": orientation,
                        "dimensions": f"{dimensions[0]}x{dimensions[1]}",
                        "duration_seconds": f"{duration:.2f}",
                        "intended_campaign_use": "Performance Max and App campaign; upload to YouTube first",
                    }
                )
    return manifest_rows


def load_font(size: int, bold: bool = False) -> ImageFont.ImageFont:
    candidates = [
        Path("/System/Library/Fonts/Supplemental/Arial Bold.ttf" if bold else "/System/Library/Fonts/Supplemental/Arial.ttf"),
        Path("/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf" if bold else "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
    ]
    for path in candidates:
        if path.is_file():
            return ImageFont.truetype(str(path), size=size)
    return ImageFont.load_default()


def build_contact_sheet() -> None:
    paths = [FINAL_IMAGES / spec[0] for spec in IMAGE_SPECS]
    columns = 3
    tile_w, tile_h = 420, 360
    rows = (len(paths) + columns - 1) // columns
    sheet = Image.new("RGB", (columns * tile_w, rows * tile_h), "#F7FAF9")
    draw = ImageDraw.Draw(sheet)
    title_font = load_font(18, bold=True)
    meta_font = load_font(14)
    for index, path in enumerate(paths):
        row, column = divmod(index, columns)
        x, y = column * tile_w, row * tile_h
        with Image.open(path) as image:
            thumb = ImageOps.contain(image.convert("RGB"), (390, 275), Image.Resampling.LANCZOS)
        thumb_x = x + (tile_w - thumb.width) // 2
        thumb_y = y + 12
        sheet.paste(thumb, (thumb_x, thumb_y))
        draw.text((x + 15, y + 296), path.name, font=title_font, fill="#171D1D")
        draw.text((x + 15, y + 326), f"{Image.open(path).size[0]} x {Image.open(path).size[1]}", font=meta_font, fill="#3F4948")
    output = QA_DIR / "IMAGE_CONTACT_SHEET.png"
    output.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(output, format="PNG", optimize=True)


def extract_video_frame(video: Path, timestamp: float, output: Path) -> None:
    run(
        [
            "ffmpeg",
            "-y",
            "-loglevel",
            "error",
            "-ss",
            f"{timestamp:.3f}",
            "-i",
            str(video),
            "-frames:v",
            "1",
            str(output),
        ]
    )


def build_storyboards() -> None:
    output_dir = QA_DIR / "video_storyboards"
    output_dir.mkdir(parents=True, exist_ok=True)
    font = load_font(18, bold=True)
    for video in sorted((FINAL_VIDEOS).glob("*/*.mp4")):
        duration = float(ffprobe(video)["format"]["duration"])
        timestamps = [0.0, 2.0, 5.0, 10.0]
        if duration > 19.5:
            timestamps.append(min(18.5, duration - 2.0))
        timestamps.append(max(duration - 1.0, 0.0))
        with tempfile.TemporaryDirectory(prefix="compressimage-storyboard-") as directory:
            frame_dir = Path(directory)
            frames: list[Image.Image] = []
            for index, timestamp in enumerate(timestamps):
                frame_path = frame_dir / f"frame-{index}.png"
                extract_video_frame(video, timestamp, frame_path)
                with Image.open(frame_path) as frame:
                    contained = ImageOps.contain(frame.convert("RGB"), (236, 236), Image.Resampling.LANCZOS)
                panel = Image.new("RGB", (250, 290), "white")
                panel.paste(contained, ((250 - contained.width) // 2, 8 + (236 - contained.height) // 2))
                ImageDraw.Draw(panel).text(
                    (12, 254),
                    f"{timestamp:.1f}s",
                    font=font,
                    fill="#171D1D",
                )
                frames.append(panel)
            storyboard = Image.new("RGB", (250 * len(frames), 330), "#F7FAF9")
            draw = ImageDraw.Draw(storyboard)
            for index, panel in enumerate(frames):
                storyboard.paste(panel, (index * 250, 40))
            draw.text((12, 10), video.name, font=font, fill="#006D77")
            storyboard.save(output_dir / f"{video.stem}_storyboard.png", optimize=True)


def write_manifest(rows: list[dict[str, str]]) -> None:
    columns = [
        "filename",
        "creative_angle",
        "level_or_state_id",
        "orientation",
        "dimensions",
        "duration_seconds",
        "intended_campaign_use",
    ]
    with (GOOGLE_ADS / "ASSET_MANIFEST.csv").open("w", encoding="utf-8", newline="") as handle:
        writer = csv.DictWriter(handle, fieldnames=columns)
        writer.writeheader()
        writer.writerows(rows)


def main() -> None:
    build_text_assets()
    image_rows = build_images()
    video_rows = build_videos()
    build_contact_sheet()
    build_storyboards()
    write_manifest(image_rows + video_rows)
    print(f"Built {len(SHORT_HEADLINES) + len(LONG_HEADLINES) + len(DESCRIPTIONS)} text assets")
    print(f"Built {len(image_rows)} final images")
    print(f"Built {len(video_rows)} final videos")


if __name__ == "__main__":
    main()
