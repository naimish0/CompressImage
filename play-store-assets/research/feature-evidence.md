# Feature and form-factor evidence

Audit date: **2026-07-20 (Asia/Kolkata)**  
Repository: `CompressImage`  
Application label: **Photo Compressor & BG Remover**

This file is the claim-control source for the Play Store artwork. A claim is not
approved merely because a control or preview exists: final artwork must use a
real input, a real result produced by the debug app, and the recorded values
from that same run.

## Audit scope and evidence level

- No `AGENTS.md` file exists in the repository or its searched parent path, so
  there were no additional repository-specific agent instructions to apply.
- The repository has one Android application module, `:app`.
- The actual namespace and application ID are both
  `com.rameshta.photocompressor` in `app/build.gradle.kts`. The mixed-case
  `com.Rameshta.photocompressor` supplied in the asset brief is not the package
  ID and must not be copied into metadata or artwork.
- `minSdk` is 24 and `targetSdk` is 36. The main activity has no fixed
  orientation and no source manifest restriction on resizing or screen size.
- `./gradlew :app:testDebugUnitTest :app:assembleDebug` passed on the current
  tree. The unit report contains **64 tests, 0 failures, 0 errors, 0 skipped**.
- The current debug APK was inspected with `aapt dump badging`. It advertises
  small, normal, large and xlarge screens, all densities, and ARM32, ARM64, x86
  and x86_64 native code. Its only derived required hardware feature is
  `android.hardware.faketouch`.
- The debug build is configured with Google's sample/test advertising setup;
  the release build uses a separate production advertising setup. IDs are
  intentionally not reproduced in this asset package.
- A targeted real-device instrumentation run was attempted on the connected
  SM-S928B without opening the app or an ad. Installation was safely stopped
  because a differently signed build of the same package was already installed;
  that app was not uninstalled and its data was not disturbed. Therefore the
  two device tests described below remain **test-source evidence**, not a new
  passing run for this audit. The final evidence instead comes from the isolated
  emulator run, retained raw captures, and app-created outputs listed below.

Evidence labels used below:

- **Implemented** — production source has an end-to-end code path and a real
  user-facing route.
- **Automated** — current unit tests passed or a feature-specific instrumented
  test exists. A test that merely exists is identified as such.
- **Runtime verified** — the isolated debug app completed the workflow and its
  source, output, UI capture, and measurements are retained in this package.
- **Capture required** — do not use a numerical or visual claim until the final
  debug workflow has produced raw capture and output evidence.
- **Unsupported** — do not advertise it.

## User-facing route inventory

`app/src/main/java/com/rameshta/photocompressor/ui/navigation/Routes.kt` and
`PhotoCompressorApp.kt` expose the following real screens:

| Route | Screen | User-visible capability |
| --- | --- | --- |
| `home` | `HomeScreen` | System Photo Picker, multi-image selection, selected-image list, History and Settings entry points. |
| `editor` | `EditorScreen` | Target size, quality mode, resizing, output format, compression action and single-image background-removal action. |
| `progress` | `BatchProgressScreen` | Total progress, per-image queued/running/success/failure state, cancel, retry failed and result navigation. |
| `result` | `ResultScreen` | Original/processed previews, measured statistics, output naming, save, share, open, save/share all and another-image action. |
| `background` | `BackgroundReplacementScreen` | Removal progress, transparent preview, palette/custom RGB replacement, format choice and export. |
| `history` | `HistoryScreen` | Loading/empty/error/content states plus open, share, remove and clear actions. |
| `settings` | `SettingsScreen` | Language, consent privacy options when required, privacy policy and model information. |
| `language` | `LanguageScreen` | Searchable packaged-language selector and System Default. |
| `privacy` | `PrivacyPolicyScreen` | In-app privacy-policy text. |

## Feature-evidence matrix

| Capability / proposed claim | Production implementation evidence | Real screen and automated evidence | Verdict and safe wording |
| --- | --- | --- | --- |
| Select one or several images | `HomeScreen.kt` uses Android's system `PickMultipleVisualMedia` with `ImageOnly`; `MAX_IMAGE_SELECTION` is 50. `PhotoCompressorViewModel.addImageUris()` loads each selected URI and de-duplicates the selection. | Home shows Select images/Add more and selected-image cards. Current `PhotoCompressorViewModelTest.addImagesAndCompressSuccessUpdatesResultsAndSummary` covers multi-selection through results. | **Implemented.** Safe: “Select one photo or a batch” and, if useful, “Batch-process up to 50 photos.” Do not imply access to the whole library. |
| Target-file-size compression | `CompressionConfig.kt` provides 100 KB, 200 KB, 500 KB, 1 MB and custom KB/MB targets. `AndroidImageRepository.compressImage()` encodes toward the target. `AdaptiveCompressionPlanner` enforces a quality floor and defines success as at most 5% over the requested target. The repository can return `targetReached=false`. | Editor exposes presets and custom KB/MB input. Result shows Requested target and either Reached within tolerance or Not safely achievable. Current unit tests cover target validation, tolerance and quality floors. `CompressionRepositoryInstrumentedTest` exists and runs the real Android encoder on a generated bitmap, but was not rerun in this audit. | **Implemented, not an exact-size guarantee.** Safe: “Choose a target size in KB or MB” or “Compress toward your target size.” Use an actual result's exact values only. Reject “always hits your target,” “exact size guaranteed,” and any invented before/after numbers. |
| Quality modes | `CompressionMode` has Best quality, Balanced and Smallest size policies with different default/minimum qualities and adaptive-resize limits. | Editor displays the three modes and their descriptions; Result records the selected mode and output encoder quality where applicable. Quality-policy unit tests pass. | **Implemented.** Safe to name the modes. Do not claim “lossless JPG/WEBP,” “zero quality loss,” or a universal quality level. |
| Before/after comparison | `ResultScreen.ComparisonPanel()` loads the original URI and processed file. It stacks them on narrower windows and places them side by side above 700 dp. | Result screen visibly labels Original and Processed. | **Implemented.** Safe: “Compare original and processed results” / “See the difference.” There is **no interactive comparison slider or zoom/detail control**. A marketing 100% crop may be made from the real source/result files, but it must not be presented as an in-app zoom feature. |
| File-size savings and compression ratio | `CompressionStatsCalculator` derives saved bytes, percentage saved and original-to-output ratio from the real `ImageInfo.sizeBytes` and output file length. `ResultScreen.StatsPanel()` displays original size, processed size, saved/increased size, percentage and ratio. | Current unit tests cover smaller and larger outputs. The retained run and UI show 2.68 MB → 236 kB, 91.2% saved and 11.38:1; `qa/measurements.csv` recomputes the values from the exact files. | **Runtime verified for this result pair.** Use only these retained same-run values; never reuse fixture or preview values. |
| Resize by 25%, 50%, 75% | `ResizeMode` and `ResizeCalculator` implement all three percentages. | Editor exposes 25%, 50%, 75% filter chips and previews the calculated output resolution. Unit tests verify percentage sizing. | **Implemented.** Requested headline “Resize by Percent or Pixels” is supported. |
| Custom pixel dimensions | `ResizeCalculator` accepts custom width/height, validates 16–12,000 px, supports proportional bounding-box sizing and optionally allows upscaling. ViewModel width/height editing updates the paired dimension when aspect locking is enabled. | Editor shows Width, Height, Maintain aspect ratio and Allow upscaling controls. Current tests cover locked aspect ratio, mixed-aspect batches, stretching with the lock off and upscaling validation. | **Implemented.** Show only these controls; there are no social-network, email, passport or platform presets. |
| JPG, PNG and WEBP conversion | `ImageFormat` defines JPEG/JPG, PNG and WEBP; `AndroidImageRepository.encodeBitmap()` maps them to Android encoders and labels a differing output format as `FORMAT_CONVERTED`. Header inspection accepts JPEG, PNG and WEBP inputs. | Editor offers JPG, PNG and WEBP chips; Result displays output format. Format mapping and transparency policy have passing unit coverage. | **Implemented.** Safe: “Convert JPG, PNG & WEBP.” This means conversion among supported image inputs/outputs, not arbitrary file formats. |
| Batch compression | Home allows up to 50 selections. `startCompressionForImages()` processes each image, keeps success/failure state, continues after individual failures, supports cancellation and retry, and publishes all successful results. | Progress shows total and per-item progress, summary, retry and Compare results. Result supports selecting batch outputs plus Save all/Share all. ViewModel tests cover multi-success, partial failure, cancellation, retry state and duplicate-tap protection. | **Implemented.** Safe: “Compress more photos at once.” A composed Store frame may combine separate real selection, in-progress and completed-state crops, but must not fabricate one impossible single app state. |
| Background removal | `RepositoryModule` binds `OnDeviceBackgroundRemovalRepository` and `OnnxBackgroundRemovalEngine`. The bundled `assets/models/u2netp.onnx` is loaded locally, produces an alpha mask and writes a transparent PNG. The repository may downscale very large images to a safe pixel budget and discloses that warning. | Editor enables Remove background only for exactly one image. Its normal action starts removal and navigates to Background, which shows real stages and a checkerboard transparent preview. Unit tests cover ViewModel success/cancel/export; mask math tests pass. `BackgroundRemovalEngineInstrumentedTest` exists for offline ONNX inference on a generated bitmap but was not rerun in this audit. | **Implemented.** Prefer “Remove Backgrounds Easily.” “In one tap” is technically one removal action after selecting one image, but the conservative wording avoids implying instant or perfect results. Do not promise flawless hair/edge quality. |
| Transparent background export | The removal repository creates an alpha PNG. A transparent export reuses that actual processed image. Background replacement prevents a non-alpha format for transparent export. | The real Background screen shows checkerboard transparency, and `source/sample-images/real-app-results/background/portrait-transparent.png` is the app-created output used in the assets. | **Runtime verified.** Safe: “Export a transparent PNG or WEBP.” Use only the retained app-created cutout. |
| Solid-color background replacement | `AndroidImageRepository.replaceBackground()` draws a selected ARGB color behind the actual transparent cutout and encodes PNG, WEBP or JPEG. | Real palette/custom-color previews were captured for white, blue, green and red. White/green exports passed visual inspection; blue/red export trials showed a damaged wedge and were excluded from final marketing assets. | **Runtime verified at the real preview level.** Clearer copy: “Replace with Any Solid Color.” The final frame uses genuine, visually correct in-app previews and does not imply arbitrary photo scenes. |
| History | `DataStoreHistoryRepository` stores up to 200 result records, filters records whose output is unavailable, and supports remove/clear. Successful compression results and background exports are recorded. History metadata is excluded from configured backup/device transfer. | History has loading/empty/error/content, open, share, remove and clear states. Current ViewModel tests cover persistence-facing state, open, clear/remove calls and error recovery; four History Compose tests exist in an earlier connected report. | **Implemented.** Safe: “Find recent results in History.” Avoid “permanent history,” “cloud backup,” or “never lose a result”: app cache can be cleared or evicted. Clearing History does not delete separately saved gallery copies. |
| Save | `AndroidImageRepository.saveImage()` copies an actual processed file into MediaStore; Android 10+ uses the Pictures collection and older supported Android versions require legacy write permission for user-initiated save. Save all maps the same operation across results. | Result has output filename, Save and Save all. ViewModel tests cover successful save/history update, duplicate taps and legacy permission denial. | **Implemented.** Safe: “Save results to your device.” Do not claim a specific gallery path on every device. |
| Open | `ImageShareController.openIntent()` creates an `ACTION_VIEW` intent with a narrowly granted content URI. | Result has Open; History opens the result screen and can then use the same action. Instrumented intent tests exist. | **Implemented, dependent on a compatible receiving app.** Safe: “Open your result.” The app already handles the no-compatible-app case. |
| Share one or many | `ImageShareController` creates `ACTION_SEND` and `ACTION_SEND_MULTIPLE` chooser intents with temporary read grants. | Result shows Share and, for a batch, Share all; History shows Share. Instrumented intent tests exist. | **Implemented.** Safe: “Save and share easily.” Do not show or name third-party services unless the app actually provides such presets (it does not). |
| On-device image processing | Compression, resizing, conversion, comparison input preparation, background removal and replacement use Android bitmap/encoder code plus the bundled ONNX model. No application networking client or developer image-upload endpoint was found. | Background explicitly says that it runs on this device and images are not uploaded. The in-app privacy policy distinguishes image processing from advertising. | **Implemented and supportable with precise wording.** Approved: “Image processing stays on your device” and “Images aren't uploaded for processing.” Do **not** say the whole app is offline or that no network/data collection occurs: Google Mobile Ads and consent services use the network. User-initiated Share/Open can also send a result to another chosen app. |
| No account required | No authentication dependency, account permission, sign-in route, registration screen or restricted feature path was found. | Every feature is reachable from the launcher flow without credentials. The privacy policy states that there is no user account. | **Implemented by absence of an account system.** Safe: “No account required.” Recheck the final release tree if authentication is ever added. |
| No watermark added | Compression writes the encoded bitmap directly. Background removal changes alpha; replacement only composites the selected color. No watermark overlay, watermark text, logo compositor or watermark setting was found. | No watermark UI exists; retained compression and background outputs were visually inspected and contain no app-added watermark. | **Runtime verified for retained outputs and supported by code inspection.** Safe: “No watermark added by the app.” Do not claim the source photo itself is watermark-free. |
| Location/privacy overclaim | `stripLocationMetadata` defaults true and bitmap re-encoding does not copy EXIF into the output, but the Store brief does not need a location-metadata claim. The app includes Google advertising/consent SDKs and network permissions. | Privacy policy describes local image handling and separate Google advertising data behavior. | Reject “collects no data,” “100% private,” “fully offline,” or “never communicates with the internet.” Use the narrower on-device-processing claim above. |

## Requested creative copy: disposition

| Requested wording | Decision | Reason / production-safe alternative |
| --- | --- | --- |
| “Smaller Photos. Still Sharp.” | **Conditional.** | Use only with an actual output and unaltered 100% source/result detail crops that visibly support the subjective quality message. Never pair it with a made-up size reduction. |
| “Remove Backgrounds in One Tap” | **Rewrite recommended.** | The primary removal action is a single tap after one image is selected, but processing is not instant and edge quality varies. Use “Remove Backgrounds Easily.” |
| “Replace Any Background Color” | **Supported but clarify.** | The app supports palette and custom RGB solid colors. Prefer “Replace with Any Solid Color.” |
| “Compress More Photos at Once” | **Approved.** | Real batch selection/progress/completion evidence is required. |
| “Resize by Percent or Pixels” | **Approved.** | 25%, 50%, 75%, custom width/height and aspect-ratio controls are real. |
| “Convert JPG, PNG & WEBP” | **Approved.** | All three are real supported output formats and supported image inputs. |
| “See the Difference” | **Approved.** | The UI shows labeled original and processed previews and measured statistics; do not add a fake slider or imply built-in zoom. |
| “Your Photos Stay on Your Device” | **Rewrite required.** | It is too absolute because users can intentionally Share/Open and ads/consent use the network. Use “Image Processing Stays on Device,” with “No account required” and “No watermark added” only as separately verified support points. |
| “Smaller Photos. Clean Backgrounds.” | **Approved as a feature-graphic headline.** | It accurately summarizes compression plus background-removal capability when the graphic uses a genuine result. |
| “Compress • Remove • Replace” | **Approved.** | All three operations exist. |

## Existing-artifact warning

The old `play-store/screenshots/` and `play-store/upload-phone-screenshots/`
files are **not measurement evidence for this package**. The repository also
contains `ScreenPreviews.kt`, whose preview-only `ImageInfo`, `ProcessedImage`,
file paths, file sizes, progress values and names are deliberately fabricated
for Compose rendering. Existing screenshot subjects/names overlap those
fixtures, and the old screenshots have no retained raw input/output pair,
checksum, capture script or rights record in the new package. Therefore:

- do not copy their numbers into the new artwork;
- do not use preview-only `/preview/...` or `content://preview/...` data;
- do not treat a Compose preview as proof that processing ran;
- generate each advertised result in the actual debug app;
- preserve the raw source, raw app output, raw UI capture and a measurement
  record together under `play-store-assets/source/`.

## Form-factor assessment

This is a **manifest/build-based** assessment, not Play Console Device Catalog
proof. Final distribution/availability must be confirmed in Play Console for
the exact uploaded AAB, track, countries and device exclusions.

| Form factor | Repository evidence | Support decision for assets |
| --- | --- | --- |
| Android phone | Launcher activity, phone-oriented Compose navigation, normal-screen support and existing phone workflow tests. | **Supported. Generate phone assets.** Final screenshots must be real debug-app captures. |
| 7-inch Android tablet | Current APK advertises large/xlarge screens; no fixed orientation/resizing restriction; all screens use scrollable Compose layouts. | **Manifest-compatible, not tablet-optimized. Generate only from a real 7-inch emulator capture and mark Play Console availability for confirmation.** Most screens remain full-width single-column. |
| 10-inch Android tablet | Same large/xlarge support. Result comparison alone becomes two-column above 700 dp. There are no tablet resource variants or window-size-class layouts. | **Manifest-compatible, not tablet-optimized. Generate only from a real 10-inch emulator capture and confirm Play Console availability.** Do not resize phone images. |
| Chromebook | No fixed orientation; target 36 behavior is resizable; APK contains x86 and x86_64 ONNX/native libraries in addition to ARM; no camera/telephony requirement. The built manifest derives `faketouch`. There is no Chromebook-specific keyboard, mouse, window-resize or landscape test suite. | **Build-compatible candidate, not ChromeOS-optimized or Device-Catalog-verified.** Create Chromebook assets only from a real ChromeOS/desktop profile after completing the workflows, and explicitly require Play Console device-catalog confirmation. |
| Wear OS | No Wear module, watch feature declaration, round-screen UI, wearable dependency or watch launcher. Phone layouts are not watch UI. | **Unsupported. Omit Wear assets.** |
| Android TV / Google TV | No `LEANBACK_LAUNCHER`, `android.software.leanback`, TV banner, D-pad/focus design or TV-specific UI. The implied `faketouch` requirement is also incompatible with a TV-only design. | **Unsupported. Omit TV screenshots and TV banner.** A sideloadable APK or a connected TV device does not establish Play TV support. |
| Android Automotive OS / Android Auto | No automotive app descriptor, car category, car app service, template API or parked-app declaration. | **Unsupported. Omit Automotive assets.** |
| Android XR | No XR manifest/category declaration, spatial UI, XR interaction work or XR testing. Generic 2D Android compatibility would not establish an XR product surface. | **Unsupported. Omit XR assets.** |

### Large-screen limitations relevant to honest screenshots

- `HomeScreen`, `EditorScreen`, `BatchProgressScreen`, `BackgroundReplacementScreen`
  and `HistoryScreen` remain essentially full-width single-column layouts.
- `ResultScreen` changes the original/processed panel to two columns only when
  the available width exceeds 700 dp; its action pair stacks only below 360 dp.
- Image preview components can become very large on wide screens because there
  is no shared content maximum width.
- There are no `sw600dp`/`sw720dp` resources, Window Size Class usage,
  foldable posture handling, keyboard shortcuts, mouse-specific affordances or
  large-screen automated tests.
- These limitations do not prove installation incompatibility, but they mean
  “optimized for tablets/Chromebook” is not an approved claim.

## Runtime evidence retained for the final export

The final package retains:

1. the rights-cleared source file and its checksum;
2. the exact debug build identifier/checksum and emulator/device profile;
3. the selected app settings;
4. the app-created raw output file and checksum;
5. byte-accurate source/output sizes and calculated percentage/ratio;
6. uncropped raw UI captures showing the relevant completed state;
7. a note confirming that no live ad, personal notification or personal media
   appears; and
8. for background work, the actual transparent app output reused for every
   replacement-color variant.

The source and app-output hashes are recorded in
`qa/source-evidence-manifest.csv`; exact compression arithmetic is in
`qa/measurements.csv`; raw UI is under `source/captures/`; capture conditions
and the exact debug APK hash are in `source/captures/README.md`; and rights/font provenance is in
`source/licenses.md`. The same-run evidence gate is therefore satisfied for the
English production-candidate assets. It does not turn one measured example into
a universal performance promise.
