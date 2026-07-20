# Real UI capture record

Capture date: **2026-07-20 (Asia/Kolkata)**  
Application: **Photo Compressor & BG Remover**  
Actual application ID: `com.rameshta.photocompressor`

The PNGs in this directory are retained, uncomposed screenshots of the real
debug APK. Final Store graphics may crop or place these captures in cards, but
must not redraw, recolor or invent application controls and results.

## Capture environment

- Build: repository debug APK, built from the inspected working tree;
  `app/build/outputs/apk/debug/app-debug.apk`, 149,076,303 bytes, SHA-256
  `d857a4e4492a7e9c4b5bb834ab9d3240e7737c308140d5336308de17ebd4335a`.
- Emulator: isolated Android 37 Pixel 7a-based AVD data directory, model
  reported by Android as `sdk_gphone16k_arm64`.
- Theme and state: light theme, animations disabled, clean app state and no
  personal notifications.
- Inputs: only the three generated samples registered in
  `../licenses.md`; no personal gallery media was used.
- Advertising: the debug build uses Google's sample/test advertising setup.
  Network access was disabled for the retained marketing-capture session. No
  ad was tapped, and no ad, ad placeholder or consent dialog is present in the
  retained captures.
- Privacy: captures contain no account, credential, signing detail, device
  owner name, personal notification, API key, advertising identifier or user
  data.
- Image processing: the app itself produced the compressed files, transparent
  cutout, solid-color previews and measurements shown in the UI. No editor was
  used to recreate or cosmetically improve those results.

The app was also checked with Save, Open and Share during the verification run.
System receiving-app screens are not marketing assets and are not retained in
the central carousel.

## Viewport profiles

All profiles rendered the installed APK at their stated window constraints.
The 1920×1080 files are fresh app renders, **not resized phone screenshots**.
They were produced on the isolated Android emulator by changing the display
size and density before capture, then navigating the real app in that layout.

| Directory | Captured pixels | Display density | Approximate app viewport | Evidence status |
| --- | ---: | ---: | ---: | --- |
| `phone/` | 1080×2400 | 420 dpi | about 411×914 dp | Primary supported phone capture. |
| `tablet-7/` | 1920×1080 | 288 dpi | about 1067×600 dp | Manifest/build-compatible large-window render; Play Console availability remains unverified. |
| `tablet-10/` | 1920×1080 | 240 dpi | 1280×720 dp | Manifest/build-compatible large-window render; Play Console availability remains unverified. |
| `chromebook/` | 1920×1080 | 160 dpi | 1920×1080 dp | Manifest/build-compatible desktop-scale render; Play Console availability remains unverified. |

These profiles provide truthful responsive rendering evidence but are not a
substitute for Play Console Device Catalog results or dedicated hardware QA.
Before uploading any tablet or Chromebook set, confirm that the exact release
AAB is distributed to the corresponding category and check representative
devices for layout, input and system-inset behavior. If Play Console excludes a
category, omit that screenshot slot rather than treating manifest
installability as distribution proof.

## Folder/state guide

### Phone

The numbered raw captures cover:

1. home with three selected samples;
2. editor target-size and quality controls;
3. percentage resize and format controls;
4. custom pixel sizing and aspect-ratio control;
5. batch completion/progress state;
6. batch summary;
7. result comparison;
8. batch result selector;
9. measured statistics plus result actions;
10. background-removal processing;
11. genuine transparent removal result;
12–17. solid-color preview and export-control states; and
18. History containing real results.

The first retained `05-batch-progress-raw.png` capture completed faster than the
screenshot action and should be interpreted by its visible UI, not its
filename. Final copy must not claim that it depicts an in-progress state unless
that state is visibly present.

### Tablet and Chromebook

Numbered captures are separate renders of selected states at each profile.
Some screens remain single-column because the production app has no dedicated
tablet resource layout; the result comparison changes to side-by-side above
the implementation's 700 dp breakpoint. This package preserves that real
behavior and does not manufacture a tablet-specific UI.

Large-screen source raws are preserved unchanged. The final exports normalize
only rows 0–47 of the Android System UI by copying a same-profile, genuine demo-
mode status strip from `03-background-removal-clean-raw.png`: fixed 10:00, one
full Wi-Fi indicator, full battery, and no notifications. The earliest app UI
begins below row 71 in every profile, so this operation does not cover, redraw,
or change application UI. Phone compositions use content crops below their
status bars; raw phone timestamps remain preserved as capture evidence.

## Capture handling rules

- Keep raw files immutable and separate from generated Store compositions.
- Preserve the real app's teal Material UI colors inside crops.
- Cropping, rounded masks and layout shadows may be applied around a capture;
  do not stretch, blur, sharpen, perspective-warp or rewrite the UI.
- Any numerical claim must trace to the same run's input file, output file and
  visible result statistics.
- A 100% detail crop may be made from the real input and output files, but it
  must not be represented as an in-app zoom feature.
- Solid-color previews may be used only as actual app previews. An output that
  fails visual QA must be excluded even if the source file is retained for
  diagnostic evidence.
- The QA manifest and report, when complete, are the authority for final-file
  dimensions, alpha, color mode, copy, duplicates and upload readiness.
