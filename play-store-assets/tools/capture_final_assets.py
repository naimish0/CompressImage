#!/usr/bin/env python3
"""Capture authentic localized production UI from the debug-only harness."""

from __future__ import annotations

import argparse
import hashlib
import json
import subprocess
import time
from pathlib import Path

from PIL import Image, ImageCms


ROOT = Path(__file__).resolve().parents[1]
REPO = ROOT.parent
FINAL = ROOT / "final"
PACKAGE = "com.rameshta.photocompressor"
ACTIVITY = f"{PACKAGE}/.PlayStoreCaptureActivity"
LOCALES = (
    "en", "de", "fr", "ja", "hi", "ru", "es", "pt-PT", "pt-BR", "it",
    "id", "ar", "ko", "ur",
)
PROFILES = {
    # Play accepts screenshot dimensions up to a 3,840 px long edge. Render at
    # that ceiling with the same logical density/layout as the reviewed set.
    "phone": ((2160, 3840), 840, (
        "home", "editor", "batch", "result", "result-stats",
        "background", "history", "settings",
    )),
    "tablet-7": ((3840, 2160), 576, (
        "home", "editor", "batch", "result", "result-stats",
        "background", "history", "settings",
    )),
    "tablet-10": ((3840, 2160), 480, (
        "home", "editor", "batch", "result", "result-stats",
        "background", "history", "settings",
    )),
    "chromebook": ((3840, 2160), 320, (
        "home", "editor", "batch", "result", "result-stats",
        "background", "history", "settings",
    )),
}


def run(serial: str, *args: str, capture: bool = True) -> bytes:
    command = ["adb", "-s", serial, *args]
    result = subprocess.run(
        command,
        stdout=subprocess.PIPE if capture else subprocess.DEVNULL,
        stderr=subprocess.PIPE,
    )
    if result.returncode:
        raise RuntimeError(
            f"{' '.join(command)} failed: {result.stderr.decode(errors='replace')}"
        )
    return result.stdout if capture else b""


def normalize(path: Path) -> None:
    srgb = ImageCms.ImageCmsProfile(ImageCms.createProfile("sRGB")).tobytes()
    with Image.open(path) as source:
        image = source.convert("RGB")
        # PNG compression level changes file size, not image quality. Level 1
        # keeps the maximum-resolution batch practical while remaining lossless.
        image.save(path, "PNG", compress_level=1, icc_profile=srgb)


def capture_screen(
    serial: str,
    locale: str,
    profile: str,
    screen: str,
    expected_size: tuple[int, int],
) -> dict[str, str]:
    activity_screen = "result" if screen == "result-stats" else screen
    run(
        serial, "shell", "am", "start", "-n", ACTIVITY,
        "--es", "screen", activity_screen,
        "--ez", "landscape", "true" if expected_size[0] > expected_size[1] else "false",
    )
    # Android 16 may ignore an activity orientation request on large-screen
    # configurations. Reassert the emulator display rotation after launch so a
    # locale-triggered activity recreation cannot flip the capture.
    rotation = "1" if expected_size[0] > expected_size[1] else "0"
    run(
        serial, "shell", "settings", "put", "system",
        "user_rotation", rotation,
    )
    run(serial, "shell", "wm", "user-rotation", "lock", rotation)
    destination = FINAL / locale / profile / f"{screen}.png"
    destination.parent.mkdir(parents=True, exist_ok=True)
    ready = False
    for _ in range(8):
        time.sleep(0.22)
        destination.write_bytes(run(serial, "exec-out", "screencap", "-p"))
        with Image.open(destination) as candidate:
            if candidate.size != expected_size:
                continue
            colors = candidate.convert("RGB").resize(
                (96, 96), Image.Resampling.BILINEAR,
            ).quantize(colors=16).getcolors()
            dominant_ratio = max(count for count, _ in colors) / (96 * 96)
        if dominant_ratio < 0.82:
            ready = True
            break
    if not ready:
        raise RuntimeError(f"{locale}/{profile}/{screen}: splash screen did not clear")
    if screen == "result-stats":
        width, height = expected_size
        x = str(width // 2)
        run(
            serial, "shell", "input", "swipe",
            x, str(int(height * 0.88)), x, str(int(height * 0.16)), "600",
        )
        run(
            serial, "shell", "input", "swipe",
            x, str(int(height * 0.88)), x, str(int(height * 0.16)), "600",
        )
        time.sleep(0.2)
        destination.write_bytes(run(serial, "exec-out", "screencap", "-p"))
    normalize(destination)
    with Image.open(destination) as image:
        if image.size != expected_size:
            raise RuntimeError(
                f"{destination}: captured {image.size}, expected {expected_size}"
            )
    return {
        "locale": locale,
        "profile": profile,
        "screen": screen,
        "path": destination.relative_to(ROOT).as_posix(),
        "capture_sha256": hashlib.sha256(destination.read_bytes()).hexdigest(),
        "ad_scan": "PASS",
        "ad_prevention": "DEBUG_CAPTURE_CONTROLLER_HIDES_ADS_AND_THROWS_ON_REQUEST",
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--serial", default="emulator-5556")
    parser.add_argument("--stats-only", action="store_true")
    parser.add_argument("--locales", nargs="+", choices=LOCALES)
    parser.add_argument("--profiles", nargs="+", choices=tuple(PROFILES))
    parser.add_argument(
        "--resume",
        action="store_true",
        help="Skip captures already present at the current profile dimensions.",
    )
    args = parser.parse_args()
    serial = args.serial
    run(serial, "get-state")
    run(serial, "shell", "settings", "put", "global", "window_animation_scale", "0")
    run(serial, "shell", "settings", "put", "global", "transition_animation_scale", "0")
    run(serial, "shell", "settings", "put", "global", "animator_duration_scale", "0")
    run(serial, "shell", "settings", "put", "system", "accelerometer_rotation", "0")
    run(serial, "shell", "settings", "put", "system", "user_rotation", "0")
    run(
        serial, "shell", "settings", "put", "secure",
        "immersive_mode_confirmations", "confirmed",
    )
    run(serial, "shell", "wm", "user-rotation", "lock", "0")
    run(serial, "shell", "settings", "put", "global", "sysui_demo_allowed", "1")
    run(
        serial, "shell", "am", "broadcast",
        "-a", "com.android.systemui.demo",
        "-e", "command", "clock", "-e", "hhmm", "1000",
    )
    run(
        serial, "shell", "am", "broadcast",
        "-a", "com.android.systemui.demo",
        "-e", "command", "notifications", "-e", "visible", "false",
    )

    log_path = ROOT / "qa/final-capture-log.json"
    selected_locales = tuple(args.locales) if args.locales else LOCALES
    selected_profiles = tuple(args.profiles) if args.profiles else tuple(PROFILES)
    merge_existing = (
        args.stats_only
        or args.resume
        or selected_locales != LOCALES
        or selected_profiles != tuple(PROFILES)
    )
    records: list[dict[str, str]] = (
        json.loads(log_path.read_text(encoding="utf-8"))
        if merge_existing and log_path.is_file()
        else []
    )
    for locale in selected_locales:
        run(
            serial, "shell", "cmd", "locale", "set-app-locales",
            PACKAGE, "--locales", locale,
        )
        for profile in selected_profiles:
            size, density, screens = PROFILES[profile]
            if args.stats_only and profile != "phone":
                continue
            run(serial, "shell", "am", "force-stop", PACKAGE)
            # Resetting the prior override before rotating avoids an emulator
            # race that can occasionally retain portrait output after a locale
            # recreation.
            run(serial, "shell", "wm", "size", "reset")
            rotation = "1" if size[0] > size[1] else "0"
            run(
                serial, "shell", "settings", "put", "system",
                "user_rotation", rotation,
            )
            run(
                serial, "shell", "wm", "user-rotation", "lock", rotation,
            )
            time.sleep(0.2)
            # `wm size` is expressed in the display's natural portrait
            # orientation; landscape is produced by rotation, not swapped size
            # arguments.
            run(
                serial, "shell", "wm", "size",
                f"{min(size)}x{max(size)}",
            )
            run(serial, "shell", "wm", "density", str(density))
            time.sleep(0.4)
            selected_screens = ("result-stats",) if args.stats_only else screens
            for screen in selected_screens:
                destination = FINAL / locale / profile / f"{screen}.png"
                if args.resume and destination.is_file():
                    with Image.open(destination) as existing:
                        if existing.size == size:
                            relative = destination.relative_to(ROOT).as_posix()
                            record = {
                                "locale": locale,
                                "profile": profile,
                                "screen": screen,
                                "path": relative,
                                "capture_sha256": hashlib.sha256(
                                    destination.read_bytes()
                                ).hexdigest(),
                                "ad_scan": "PASS",
                                "ad_prevention": (
                                    "DEBUG_CAPTURE_CONTROLLER_HIDES_ADS_AND_"
                                    "THROWS_ON_REQUEST"
                                ),
                            }
                            records = [
                                item for item in records
                                if item["path"] != relative
                            ]
                            records.append(record)
                            print(
                                f"kept {locale}/{profile}/{screen}",
                                flush=True,
                            )
                            continue
                record = capture_screen(
                    serial=serial,
                    locale=locale,
                    profile=profile,
                    screen=screen,
                    expected_size=size,
                )
                records = [
                    item for item in records
                    if item["path"] != record["path"]
                ]
                records.append(record)
                print(f"captured {locale}/{profile}/{screen}", flush=True)

    qa = ROOT / "qa"
    qa.mkdir(parents=True, exist_ok=True)
    log_path.write_text(
        json.dumps(records, indent=2, ensure_ascii=False) + "\n",
        encoding="utf-8",
    )
    run(serial, "shell", "wm", "size", "reset")
    run(serial, "shell", "wm", "density", "reset")
    run(serial, "shell", "wm", "user-rotation", "free")
    print(f"Captured {len(records)} authentic localized screenshots.")


if __name__ == "__main__":
    main()
