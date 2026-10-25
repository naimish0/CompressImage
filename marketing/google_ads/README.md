# Google Ads creative package

Prepared and validated: **2026-08-26 (Asia/Kolkata)**

## Product detected

- **Name:** Photo Compressor & BG Remover
- **Application ID:** `com.rameshta.photocompressor`
- **Positioning:** an Android utility for single or batch photo compression toward a KB/MB target, resizing, JPG/PNG/WEBP conversion, before/after comparison, and on-device background removal/replacement.
- **Audience:** ages 13+; everyday photo cleanup, smaller sharing files, product photos, documents, and social posts.
- **Network wording:** image processing runs on-device and images are not uploaded for processing. The app uses internet access for Google ads and consent, so the package makes no whole-app offline claim.

The supplied repository is not a puzzle game. It contains no levels, boards, moves, lives, timer, hints, boosters, coins, streaks, game rewards, or gameplay audio. The package therefore uses real app workflow captures and does not invent puzzle creatives. See [RESEARCH.md](RESEARCH.md) for the product audit, current Google requirements, sources, and market analysis.

## Research summary

Official Google documentation was verified live. App campaign text limits are 30 characters for headlines and 90 for descriptions; the selected image formats are 1200×628, 1200×1200, and 1200×1500 with a 5 MB maximum; and campaign videos must be YouTube-hosted. Performance Max supports the 15/5/5 text mix produced here and benefits from custom video in all three orientations.

Direct photo-compressor competitors foreground KB/MB targets, batch handling, and numerical proof. High-install puzzle benchmarks demonstrate a transferable pattern—show one understandable action and reaction immediately—but their mechanics and layouts are not copied. The clearest distinct opportunity for this app is its teal/yellow Material 3 UI, transparent-background reveal, target-size controls, and real result proof.

## Creative strategy

The six captured angles are:

1. Understand the objective instantly with three selected images.
2. Show the signature on-device background-removal capability.
3. Present target, quality, resize, and format controls as a dense but readable task.
4. Make the 205 kB/quality/resize decision meaningful without guaranteeing an exact result.
5. Show real cause and effect through the transparent reveal and original/processed comparison.
6. Show workflow completion through the 100% batch summary and local History—not game progression.

The six deterministic real-app states, reproduction steps, capture moments, and crop risks are documented in [CAPTURE_PLAN.md](CAPTURE_PLAN.md).

## Text assets

### Short headlines

| ID | Text | Characters |
| --- | --- | ---: |
| SH01 | Compress photos | 15 |
| SH02 | Set a target size | 17 |
| SH03 | Resize with control | 19 |
| SH04 | Remove photo backgrounds | 24 |
| SH05 | Compare before you save | 23 |
| SH06 | Batch-compress your photos | 26 |
| SH07 | Choose JPG, PNG or WEBP | 23 |
| SH08 | Keep photo work on-device | 25 |
| SH09 | Photo Compressor & BG Remover | 29 |
| SH10 | Balance quality and size | 24 |
| SH11 | Resize by percent or pixels | 27 |
| SH12 | See the size you saved | 22 |
| SH13 | Convert image formats | 21 |
| SH14 | Make sharing files smaller | 26 |
| SH15 | Process up to 50 photos | 23 |

### Long headlines

| ID | Text | Characters |
| --- | --- | ---: |
| LH01 | Compress photos toward a KB or MB target, then compare before you save. | 71 |
| LH02 | Resize photos by percentage or custom dimensions and keep the aspect ratio when needed. | 87 |
| LH03 | Remove or replace photo backgrounds on your device without uploading your images. | 81 |
| LH04 | Process one photo or a batch of up to 50 and export as JPG, PNG, or WEBP. | 73 |
| LH05 | Photo Compressor & BG Remover combines compression, resizing, conversion, and cleanup. | 86 |

### Descriptions

| ID | Text | Characters |
| --- | --- | ---: |
| DS01 | Set a KB or MB target and choose the balance between quality and file size. | 75 |
| DS02 | Compare original and processed images before saving, sharing, or opening. | 73 |
| DS03 | Resize by percent or custom pixels, then export as JPG, PNG, or WEBP. | 69 |
| DS04 | Remove or replace photo backgrounds on your device with a bundled model. | 72 |
| DS05 | Select one image or batch-process up to 50 with the same compression settings. | 78 |

The upload-ready CSV files, including feature evidence and priority columns, are in `text/`. The recommended five App campaign headlines and five descriptions are in [APP_CAMPAIGN_SELECTION.md](text/APP_CAMPAIGN_SELECTION.md).

## Image inventory

All 12 final files are PNG, under 5 MB, visually inspected, and derived by cropping/reframing untouched real-app captures without stretching or synthetic content.

| File | State | Angle |
| --- | --- | --- |
| `images/final/landscape/editor_target_size_landscape_1200x628.png` | CI-02 | Meaningful size decision |
| `images/final/landscape/background_result_landscape_1200x628.png` | CI-05 | On-device background removal |
| `images/final/landscape/compare_result_landscape_1200x628.png` | CI-04 | Before/after proof |
| `images/final/landscape/batch_complete_landscape_1200x628.png` | CI-03 | Batch completion |
| `images/final/square/home_batch_square_1200x1200.png` | CI-01 | Objective instantly |
| `images/final/square/history_outputs_square_1200x1200.png` | CI-06 | Completed outputs |
| `images/final/square/background_result_square_1200x1200.png` | CI-05 | On-device background removal |
| `images/final/square/compare_result_square_1200x1200.png` | CI-04 | Before/after proof |
| `images/final/vertical/home_batch_vertical_1200x1500.png` | CI-01 | Objective instantly |
| `images/final/vertical/editor_resize_preview_vertical_1200x1500.png` | CI-02 | Resize and output information |
| `images/final/vertical/batch_complete_vertical_1200x1500.png` | CI-03 | Batch completion |
| `images/final/vertical/background_result_vertical_1200x1500.png` | CI-05 | On-device background removal |

Untouched screenshot sources are retained in `images/raw/`. The complete visual overview is [IMAGE_CONTACT_SHEET.png](qa/IMAGE_CONTACT_SHEET.png).

## Video inventory

Concept A shows configuration, the real Remove background action/reaction, a genuine transparent result, and a 1.2-second Home finish. Concept B opens on three selected images, shows the real configuration and compression flow, reaches the 100% batch summary and comparison result, and uses the same short genuine finish.

| File | Dimensions | Duration | Concept |
| --- | ---: | ---: | --- |
| `videos/final/vertical/signature_background_removal_vertical_1080x1920.mp4` | 1080×1920 | 16.03 s | A — signature capability |
| `videos/final/square/signature_background_removal_square_1080x1080.mp4` | 1080×1080 | 16.03 s | A — signature capability |
| `videos/final/horizontal/signature_background_removal_horizontal_1920x1080.mp4` | 1920×1080 | 16.03 s | A — signature capability |
| `videos/final/vertical/batch_challenge_result_vertical_1080x1920.mp4` | 1080×1920 | 21.00 s | B — challenge and satisfaction |
| `videos/final/square/batch_challenge_result_square_1080x1080.mp4` | 1080×1080 | 21.00 s | B — challenge and satisfaction |
| `videos/final/horizontal/batch_challenge_result_horizontal_1920x1080.mp4` | 1920×1080 | 21.00 s | B — challenge and satisfaction |

All are MP4/H.264, 30 FPS, `yuv420p`, and fast-start enabled. The app and repository have no audio, so silent output is intentional and no music or sound was fabricated. Untouched recordings are in `videos/raw/`; the six inspected storyboards are in `qa/video_storyboards/`.

**Google Ads requires YouTube-hosted videos for campaign use. These files were not uploaded or published.**

## Validation and build results

- `python3 marketing/google_ads/tools/validate_assets.py`: **PASS**, 67/67 checks.
- Text: 25/25 within declared limits; SH01 is 15 characters.
- Images: 12/12 at exact required dimensions/ratios, valid PNG, each under 5 MB; six distinct deterministic app states represented.
- Videos: 6/6 at exact dimensions, H.264, `yuv420p`, CFR 30 FPS, 16.03–21.00 seconds, fast-start; no source audio exists.
- Manifest: 18/18 final visual assets exist with unique filenames; one storyboard per video and the image contact sheet exist.
- Visual QA: every final image, the contact sheet, and every final storyboard was opened after final generation and passed readability, crop, authenticity, muted pacing, first-frame, brand-finish, and no-ad/debug/system-UI checks.
- `./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`: **BUILD SUCCESSFUL** (60 tasks; unit tests and debug lint passed).
- `python3 tools/check_localizations.py`: **PASS** for 13 locales, 265 translated strings, and one plural resource.

The detailed per-file result is [VALIDATION_REPORT.md](VALIDATION_REPORT.md); campaign mapping is in [ASSET_MANIFEST.csv](ASSET_MANIFEST.csv). Regenerate with `python3 marketing/google_ads/tools/build_assets.py`, then revalidate.

## Build and capture environment

- Normal debug workflow: `./gradlew :app:assembleDebug`.
- Installed APK: `app/build/outputs/apk/debug/app-debug.apk`.
- Capture target: isolated Pixel 7a Android Emulator, API 37.
- Source frames: 1080×1920 portrait and 1920×1080 landscape.
- Capture activity: existing debug-only `PlayStoreCaptureActivity`, using production composables, deterministic rights-cleared sample inputs, and genuine app results.
- Ads: the existing debug capture contract excluded ad surfaces. Production IDs, release ads, monetization, consent, analytics, and production UI were not modified.

## Recommended first five App campaign visual assets

1. `videos/final/square/signature_background_removal_square_1080x1080.mp4`
2. `images/final/square/background_result_square_1200x1200.png`
3. `videos/final/vertical/batch_challenge_result_vertical_1080x1920.mp4`
4. `images/final/landscape/editor_target_size_landscape_1200x628.png`
5. `images/final/vertical/batch_complete_vertical_1200x1500.png`

Start these with the selected App campaign copy in `text/APP_CAMPAIGN_SELECTION.md`. The set covers a visual transformation, target-size control, batch completion, square reach, and full-screen vertical reach without duplicating the same moment.

## Recommended first Performance Max asset group

Theme: **On-device photo control**.

- Supply all 15 short headlines, five long headlines, and five descriptions so Google has distinct objective, control, batch, comparison, format, and background-removal angles.
- Supply all four landscape, four square, and four vertical images; use the background result as the lead square, target-size control as the lead landscape, and batch completion as the lead vertical.
- Supply both video concepts in all three orientations to avoid relying on auto-generated video and to cover transformation plus batch/result intent.
- Keep business name/icon fields tied to the verified Play listing and installed app; do not reuse the older feature graphic that still carries the former shorter brand.

Campaign performance cannot be predicted from local QA. Evaluate combinations through Google Ads asset reporting, replace consistently low-rated assets, and refresh with new truthful real-app states rather than unsupported claims.

## Limitations and blockers

- The requested puzzle-game scenes are inapplicable because the repository is a photo utility. No fake puzzle content was created.
- Capture used an emulator rather than a physical phone. The source resolution meets the requested 1080×1920 preference, and both orientations rendered from the real build.
- Videos are silent because the installed app has no sound assets or playback. Advertising music rights therefore do not arise.
- YouTube upload, Google Ads upload, publication, and campaign creation were intentionally not performed.
- Dynamic competitor ad libraries do not provide stable evidence URLs for every transient creative; current Play presentations and durable official examples are cited in `RESEARCH.md`.

No production source behavior or ads architecture was changed for this package. Nothing was committed, pushed, uploaded, or published.
