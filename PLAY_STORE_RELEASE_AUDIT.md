# Play Store Release Audit

Audit date: 2026-07-17  
Project: Photo Compressor  
Module audited: `:app`  
Audit scope: Google Play production gate after adding Google AdMob banner and interstitial ads.

## Executive Summary

Final verdict: **Not ready**

The codebase now contains a centralized Google AdMob and Google User Messaging Platform integration using only official Google SDKs. Image processing remains on-device by app code, and ads are gated behind UMP consent readiness. The latest placement request adds reusable top and bottom anchored adaptive banners across all app-owned screens, safe empty-space banners on Home/Result, inline History ads after every five content items, a History-navigation interstitial, and a Save-click interstitial. History is now persisted through DataStore and records compressed, background-removed, and saved outputs so items remain visible after process death/app restart when the underlying output still exists.

This requested ad density and the interstitial on History navigation/Save click require manual policy review before production. History navigation interstitials may be considered disruptive, Save-click interstitials may be considered interruption of a direct user action because they appear before MediaStore save begins, and top plus bottom plus inline banners can create accidental-click/ad-density concerns. The app no longer claims to be fully offline; documentation now states that image processing is local and that internet access may be used for ads and consent information.

The generated release APK and AAB were built and inspected. The release artifact is not publishable yet because production AdMob IDs are not configured, the AAB/APK are unsigned, external Play/AdMob/Firebase registration for `com.rameshta.photocompressor` still needs to be completed, a public privacy policy and Play/AdMob console configuration are missing, and the bundled U2-NetP pretrained weights still need independently verified commercial redistribution evidence.

Official sources reviewed:

- Google Play target API policy: https://support.google.com/googleplay/android-developer/answer/11926878
- Google Play ads policy: https://support.google.com/googleplay/android-developer/answer/9857753
- Google AdMob Android interstitial guide: https://developers.google.com/admob/android/interstitial
- Google AdMob anchored adaptive banner guide: https://developers.google.com/admob/android/banner/anchored-adaptive
- Google UMP privacy and consent guide: https://developers.google.com/admob/android/privacy
- Google Mobile Ads SDK Play Data Safety disclosure guide: https://developers.google.com/admob/android/privacy/play-data-disclosure
- Android 16 KB page-size guidance: https://developer.android.com/guide/practices/page-sizes

## Final Verdict

**Not ready for Google Play production upload.**

The release build succeeds and the AAB is generated, but the current AAB is unsigned and has release ads disabled because production IDs were not supplied. It is suitable for technical inspection, not for Play submission.

Finding counts:

| Severity | Count | Fixed in this pass | Remaining |
|---|---:|---:|---:|
| Blocker | 5 | 1 | 4 |
| High | 4 | 1 | 3 |
| Medium | 6 | 0 | 6 |
| Low | 4 | 0 | 4 |

## Application Modules And Release Configuration

- Modules: one Android application module, `:app`.
- Repository instructions found: `README.md`; no `AGENTS.md`, debug manifest, product-flavor manifest, or separate release manifest was found.
- Build variants: `debug`, `release`.
- Release build type: R8 minification enabled, resource shrinking enabled.
- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`
- Merged release manifest: `app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml`
- Existing dirty worktree state was preserved. `.idea/gradle.xml` and `.idea/misc.xml` were already dirty before this ads pass and were not reverted.

Final artifact identifiers:

```text
applicationId com.rameshta.photocompressor
versionCode 1
versionName 1.0
minSdk 24
targetSdk 36
compileSdk 36
```

Toolchain:

- Android Gradle Plugin: 9.1.1
- Gradle wrapper: 9.3.1
- Kotlin plugin: 2.3.10
- Java source/target compatibility: 11
- Compose BOM: 2024.09.00
- Google Mobile Ads SDK: 25.4.0
- Google User Messaging Platform SDK: 4.0.0

## Concise Audit Plan

1. Read project guidance and preserve existing worktree changes.
2. Inspect module structure, navigation, manifests, dependencies, and existing ads/consent stubs.
3. Review current official Play, AdMob, UMP, Data Safety, and 16 KB page-size documentation.
4. Design a policy-safe ad placement matrix.
5. Implement centralized consent, ads initialization, banner, interstitial, and placement architecture.
6. Build debug and release artifacts using official Google test ads for development.
7. Inspect the merged release manifest, APK, AAB, permissions, native libraries, and signing state.
8. Run lint, unit tests, release build, bundle build, and connected instrumentation tests.
9. Update compliance documentation and record remaining manual Play/AdMob actions.

## Blockers

| ID | Evidence | File or artifact | Why it matters | Required fix | Fixed | Verification performed |
|---|---|---|---|---|---|---|
| B-01 | Release `BuildConfig` has `ADS_ENABLED=false`, blank ad-unit IDs, and the merged release manifest contains the disabled placeholder app ID `ca-app-pub-0000000000000000~0000000000`. | `app/build.gradle.kts`; generated release `BuildConfig`; merged release manifest | The generated AAB does not represent a production ad configuration and must not be uploaded as the final ad-supported release. | Supply real `PHOTO_COMPRESSOR_ADMOB_APP_ID`, `PHOTO_COMPRESSOR_ADMOB_TOP_BANNER_ID`, `PHOTO_COMPRESSOR_ADMOB_BOTTOM_BANNER_ID`, `PHOTO_COMPRESSOR_ADMOB_INLINE_ID`, `PHOTO_COMPRESSOR_ADMOB_HISTORY_INTERSTITIAL_ID`, and `PHOTO_COMPRESSOR_ADMOB_SAVE_INTERSTITIAL_ID` through secure Gradle/CI configuration, rebuild, and reinspect the merged manifest/AAB. Short property aliases `ADMOB_APP_ID`, `TOP_BANNER_AD_UNIT_ID`, `BOTTOM_BANNER_AD_UNIT_ID`, `INLINE_AD_UNIT_ID`, `HISTORY_INTERSTITIAL_AD_UNIT_ID`, and `SAVE_INTERSTITIAL_AD_UNIT_ID` are also supported. | No | Grep of generated `BuildConfig` and merged release manifest. |
| B-02 | `apksigner verify` reports `DOES NOT VERIFY`; `jarsigner` reports the AAB is unsigned. | `app-release-unsigned.apk`; `app-release.aab` | Google Play requires a signed upload artifact. Signing cannot be generated or replaced without developer authorization. | Configure upload signing with Play App Signing using secure local/CI secrets. Do not commit keystores or passwords. | No | `/Users/naimishgupta/Library/Android/sdk/build-tools/36.1.0/apksigner verify`; `jarsigner -verify`. |
| B-03 | APK badging reports `package: name='com.rameshta.photocompressor'`. | `app/build.gradle.kts`; APK badging | The Play package name is permanent after first publish and must match external store/service registrations. | Create the Play app and related external registrations for this final reverse-DNS application ID before first upload. | Yes in source; external registration still required | `aapt dump badging`. |
| B-04 | Privacy policy is only a draft with placeholders for legal developer name, contact, effective date, and child-audience statement. No public HTTPS privacy-policy URL is configured. | `PRIVACY_POLICY.md`; `README.md`; Settings source | Ads and Data Safety disclosures require a public, accurate privacy policy. Developer identity and contact cannot be invented in code. | Publish a public HTTPS privacy policy and link the same policy from Play Console and in-app legal/privacy UI. Fill developer identity, contact, date, target audience, and advertising disclosures. | No | Source and docs audit. |
| B-05 | The app packages `assets/models/u2netp.onnx`; repository evidence documents Apache-2.0 source code but not independently verified commercial redistribution terms for the exact pretrained weights. | `app/src/main/assets/models/u2netp.onnx`; `legal/MODEL_PROVENANCE.md`; AAB asset list | User requirements state unknown or non-commercial model provenance is a release blocker. | Obtain rights-holder evidence covering commercial use, redistribution, ONNX conversion, and Play distribution, or replace with a verified commercial-use model. | No | Packaged model SHA-256 previously recorded as `2db478c3e56cc19f8076b5bc12f0725716fc82d5b9a19e554815cac1150c476b`; legal docs inspected. |

## High-Severity Findings

| ID | Evidence | File or artifact | Why it matters | Required fix | Fixed | Verification performed |
|---|---|---|---|---|---|---|
| H-01 | Centralized UMP consent, gated Mobile Ads initialization, shared banner/interstitial managers, debug test IDs, and release safeguards against sample IDs are implemented. The requested placement strategy now includes History-navigation and Save-click interstitials plus top/bottom/inline banners. | `ads/`; `di/AdsModule.kt`; `PhotoCompressorApp.kt`; `app/build.gradle.kts`; Compose screens | Direct SDK calls scattered through screens or aggressive ad placement would create Play Ads and invalid-traffic risk. The requested History and Save interstitials remain policy-sensitive even though they are centralized and optional. | Keep all ad requests behind consent/initialization and placement policy. Do not add interstitials to Back, picker, processing, failure, cancellation, Open, or exit flows. Complete manual policy review for History/Save interstitials and top+bottom+inline banner density before production. | Partially | Unit tests, source inspection, debug/release compilation. |
| H-02 | Final permissions include AdMob/AdServices identifiers and WorkManager-transitive `WAKE_LOCK`/`FOREGROUND_SERVICE`. | Merged release manifest; `aapt dump permissions`; dependency tree | These change Play disclosures, Data Safety answers, and possibly foreground-service declarations. Removing SDK-transitive permissions without runtime proof could break the official SDK. | Complete Play Console declarations and AdMob privacy setup. If the developer wants to remove WorkManager permissions, validate with Google SDK guidance and full ad runtime testing first. | No | Manifest blame shows AdServices permissions from `play-services-ads-api:25.4.0`; `WAKE_LOCK`/`FOREGROUND_SERVICE` from `androidx.work:work-runtime:2.7.0`. |
| H-03 | `minSdk=24`; final manifest has no `WRITE_EXTERNAL_STORAGE`; saving uses MediaStore. | `app/build.gradle.kts`; image repository; final permission dump | On Android API 24-28, gallery writes may fail without legacy write permission/runtime handling. Ads work did not change this product behavior. | Decide whether to support API 24-28 gallery save with a `maxSdkVersion=28` permission and runtime flow, or raise support expectations after product review. | No | Source review and final `aapt dump permissions`. |
| H-04 | Target audience and Families configuration cannot be verified from code. | Play Console manual state; `PRIVACY_POLICY.md` placeholder | If children are included, AdMob serving, consent, content rating, and Families policy requirements change materially. | Complete Play Console target-audience declaration. If children are included, configure AdMob child-directed/family-safe ad behavior and update privacy policy. | No | No Play Console access; docs/source review only. |

## Medium-Severity Findings

| ID | Evidence | File or artifact | Why it matters | Required fix | Fixed | Verification performed |
|---|---|---|---|---|---|---|
| M-01 | Release production ads were not runtime-tested because production IDs are absent and release signing is unavailable. | Current AAB/APK; generated release config | Debug test ads prove code paths compile, but not the exact production ad-enabled release behavior. | After production IDs and signing are configured, install the signed release/internal-app-sharing build and test consent, banners, and interstitials on real devices. | No | Release BuildConfig inspection; connected tests ran debug. |
| M-02 | Artifact-level 16 KB checks passed, but the attached device reports page size `4096`. | APK native libs; connected device | ONNX native runtime should be exercised on a 16 KB page-size environment when available. | Run background removal and export on a 16 KB Android emulator/device. | No | `zipalign -P 16` pass; ELF LOAD segments all `align 2**14`; `adb shell getconf PAGE_SIZE` returned `4096`. |
| M-03 | Full manual ad layout matrix was not completed on every small/large screen, font scale, orientation, light/dark, no-fill, and consent state. | Compose screens; connected test report | Accidental-click and layout-jump risk depends on runtime visuals. | Perform manual QA with test ads only before production IDs are enabled. | No | Connected tests passed; source reserves banner space and avoids editing canvases/dialogs. |
| M-04 | Cache cleanup for processed and background-removal files remains limited. | Image repositories; cache paths | Temporary image outputs can remain in app cache longer than user expectations. | Add startup/session cache cleanup covering processed and background-removal cache directories. | No | Source search for cleanup call sites. |
| M-05 | Backup/data extraction rules are broad and do not explicitly exclude persisted History URI metadata. | `backup_rules.xml`; `data_extraction_rules.xml`; DataStore History store | History now stores output URI/display metadata. That should not be backed up unintentionally without a product/privacy decision. | Explicitly exclude image temp state, URI/history metadata, and other private processing state from cloud backup/device transfer. | No | Manifest and XML audit. |
| M-06 | Accessibility and UX policy validation was source-level only. | Compose UI; connected tests | TalkBack order, contrast, dynamic type, RTL, and banner spacing need human/device review. | Run manual accessibility QA with banners loaded, failed, and hidden. | No | Source includes many content descriptions; no full manual accessibility report produced. |

## Low-Severity Findings

| ID | Evidence | File or artifact | Why it matters | Required fix | Fixed | Verification performed |
|---|---|---|---|---|---|---|
| L-01 | AAB includes `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`; ONNX libs dominate size. | AAB/APK native inventory | Play splits by ABI, but bundle and universal APK are large. | Keep all ABIs if intentional; otherwise evaluate ABI filters after device-support review. | No | AAB listing and native inventory. |
| L-02 | `DebugProbesKt.bin` remains packaged in release. | APK listing | Usually harmless from coroutines, but debug-oriented assets are worth reviewing. | Optionally exclude if confirmed safe with coroutine diagnostics and tests. | No | APK zip listing. |
| L-03 | `TEMPLATE_LIST_INLINE` is modeled but there is no separate template-list/collage route in the current navigation graph. | `AdPlacementPolicy.kt`; `Routes.kt`; navigation/source audit | No current behavior risk; it preserves the requested placement map for a future route without placing ads on a non-existent screen. | Wire only after a verified template/collage list screen exists and spacing is reviewed. | No | Source inspection. |
| L-04 | `versionCode=1`; prior Play release history is unknown. | APK badging; `app/build.gradle.kts` | Upload will fail if any prior track already used versionCode 1. | Verify Play Console version history before upload. | No | `aapt dump badging`. |

## Issues Fixed

- Added official Google Mobile Ads SDK `25.4.0` and Google UMP SDK `4.0.0`.
- Added only explicit app-requested ad permissions: `INTERNET` and `ACCESS_NETWORK_STATE`.
- Added AdMob app ID manifest placeholder and centralized Gradle configuration.
- Debug builds use official Google sample IDs only.
- Release builds reject Google sample IDs and disable ads when production IDs are missing.
- Implemented `AdsConfiguration`, `ConsentManager`, `AdsInitializer`, `BannerAdController`, `InterstitialAdManager`, and `AdPlacementPolicy`.
- Implemented UMP launch consent update, required consent form display, `canRequestAds()` gating, and Settings privacy-options entry when required.
- Implemented a reusable Compose anchored adaptive banner with lifecycle pause/resume/destroy and stable reserved top/bottom/inline areas.
- Added `AdScreenScaffold` and applied top/bottom banners to every app-owned route in the current navigation graph: Home, Editor/configuration, Progress, Result, Background, History, and Settings.
- Added safe empty-space banners on Home and Result and inline History banners after every five real history items.
- Implemented requested History-open interstitial using explicit pending navigation; it navigates exactly once after dismissal/failure or immediately when unavailable.
- Implemented requested Save-click interstitial using explicit pending save request IDs. Save validates output first, optionally shows an already-loaded ad, then saves through MediaStore, records History, verifies the URI, and posts the Toast.
- Deduplicated Save requests while a Save ad/save flow is pending or active.
- Removed Share interstitial behavior; Share and Open now execute direct user actions with secure `content://` URIs.
- Fixed missing History persistence by adding a DataStore-backed History repository and recording compressed, background-removed, and saved outputs.
- Added fake ads/consent managers for deterministic tests.
- Updated README, third-party notices, and privacy-policy draft for ad-supported behavior.

## Issues Remaining

- Configure production AdMob app ID and ad-unit IDs outside source control.
- Sign the release AAB with a production upload key.
- Confirm or change the production application ID.
- Publish a public HTTPS privacy policy with legal identity, contact, target-audience statement, and ad disclosures.
- Complete Play Console and AdMob manual setup.
- Verify U2-NetP pretrained-weight commercial redistribution rights.
- Decide API 24-28 gallery-save behavior.
- Run production-ID signed-release QA with test devices/test mode and no production ad clicks.
- Run 16 KB page-size runtime testing when available.
- Manually review the requested History-open interstitial, Save-click interstitial, and top+bottom+inline banner density against Google Play Ads policy and Better Ads Standards before enabling production ads.

## History Root Cause And Fix

Root cause:

- History was kept only in `PhotoCompressorUiState.history`, so it was lost after process death/app restart.
- Compression results were inserted into that in-memory list when processing completed, but not through a persistent repository.
- Background-removal outputs were exposed through `BackgroundUiState.Success` and result state, but were not consistently inserted into History.
- Saved MediaStore outputs were not recorded as History entries with their durable `content://` output URIs.

Fix implemented:

- Added `HistoryRepository` and `DataStoreHistoryRepository` backed by `processed_image_history`.
- Added `HistoryOperationType` fields for compressed, background-removed, resized, format-converted, enhanced, and collage outputs.
- Records compressed outputs after successful compression and background-removed outputs after successful ONNX/background export.
- On Save, validates a result, optionally runs the requested `SAVE_CLICKED` interstitial, saves through MediaStore, verifies a nonblank saved URI, writes the saved `content://` output to History, and only then shows `Image saved successfully`.
- History observes repository Flow state through the ViewModel, updates reactively, and de-duplicates saved outputs by stable output ID.
- History uses a mixed lazy list for inline ads so ads do not count as History items and do not change Open/Remove identity.

Limitations:

- Existing old missing entries cannot be reliably reconstructed without scanning user media or importing unrelated images. The fix records future outputs and saved outputs.
- History stores metadata and output URIs, not full-resolution bitmaps.

## Target, Compile, And Min SDK Status

Google Play policy reviewed on 2026-07-17 states that starting August 31, 2026, new apps and updates must target Android 16/API 36. This project already builds with:

```text
compileSdk 36
targetSdk 36
minSdk 24
```

Compatibility notes:

- Edge-to-edge: `MainActivity` enables edge-to-edge and Compose scaffolds apply padding; manual visual QA remains required with banners.
- Photo/media access: Photo Picker remains permission-free; no broad media permissions are added.
- PendingIntent mutability: no unsafe app-created PendingIntent issue found in source audit.
- Exported components: launcher activity and SDK components declare exported state in the merged manifest.
- Foreground services: app code does not declare custom FGS, but WorkManager-transitive SDK components and `FOREGROUND_SERVICE` are present because of the Ads SDK dependency.
- Package visibility: Ads SDK contributes `queries`; the app did not add broad `QUERY_ALL_PACKAGES`.
- Native libraries: 16 KB artifact alignment passed; runtime on 16 KB page size remains untested.

## Permission Inventory

Final release APK permissions:

| Permission | Feature needing it | Android versions | Type | Play declaration impact | Permission-free alternative | Status |
|---|---|---|---|---|---|---|
| `android.permission.INTERNET` | Load AdMob ads and UMP consent information. Images are not uploaded by app code. | All supported versions | Normal | Supports "contains ads" and Data Safety ad disclosures. | Remove ads/consent network SDKs. | Kept |
| `android.permission.ACCESS_NETWORK_STATE` | Let ad/consent SDKs handle connectivity and request behavior. | All supported versions | Normal | No special declaration by itself; disclose ads/consent network use. | Remove ads/consent network SDKs. | Kept |
| `com.google.android.gms.permission.AD_ID` | Google Mobile Ads identifier/fraud/personalization use, depending on consent and AdMob configuration. | Android 13+ enforcement context; Play services ecosystem | Normal plus Google Play Advertising ID policy | Must be reflected in Data Safety if collected/shared; do not remove without selected ad configuration review. | Limited Ads or manifest removal may be possible only after policy/runtime review. | Kept |
| `android.permission.ACCESS_ADSERVICES_AD_ID` | Privacy Sandbox/AdServices ad identifier support from GMA SDK. | Newer Android versions | Normal/AdServices | Data Safety and ad disclosure impact. | Disable/remove ad SDK features only after Google guidance review. | Kept |
| `android.permission.ACCESS_ADSERVICES_ATTRIBUTION` | Ad attribution support from GMA SDK. | Newer Android versions | Normal/AdServices | Data Safety/ad attribution disclosure impact. | Remove ads/attribution SDK features. | Kept |
| `android.permission.ACCESS_ADSERVICES_TOPICS` | Topics API support from GMA SDK. | Newer Android versions | Normal/AdServices | May affect ad personalization disclosure. | Remove ads or configure ad behavior after policy review. | Kept |
| `android.permission.WAKE_LOCK` | WorkManager transitive dependency from GMA SDK. | All supported versions | Normal | Usually no special Play declaration; privacy/security review item. | Potential manifest removal only after confirming SDK will not need WorkManager wake locks. | Kept and documented |
| `android.permission.FOREGROUND_SERVICE` | WorkManager transitive dependency from GMA SDK. | Android 9+ | Normal but Play Console may require FGS disclosure if used | Possible Play Console FGS declaration risk. | Potential manifest removal only after confirming no SDK foreground-worker path is used. | Kept and documented |
| `com.rameshta.photocompressor.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | AndroidX dynamic receiver protection. | All supported versions | Signature app-defined | No special Play declaration. | Not applicable. | Kept |

Permissions explicitly absent:

- `READ_MEDIA_IMAGES`
- `READ_MEDIA_VIDEO`
- `READ_MEDIA_VISUAL_USER_SELECTED`
- `READ_EXTERNAL_STORAGE`
- `WRITE_EXTERNAL_STORAGE`
- `MANAGE_EXTERNAL_STORAGE`
- `CAMERA`
- `POST_NOTIFICATIONS`
- `QUERY_ALL_PACKAGES`
- `REQUEST_INSTALL_PACKAGES`
- `SYSTEM_ALERT_WINDOW`
- `SCHEDULE_EXACT_ALARM`
- `USE_EXACT_ALARM`
- `RECEIVE_BOOT_COMPLETED`

## Photo Picker And URI Handling

Photo access remains based on Android Photo Picker:

- `ActivityResultContracts.PickMultipleVisualMedia(maxItems = 50)`
- `PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)`
- Content is read with `ContentResolver` streams/descriptors and `OpenableColumns.DISPLAY_NAME/SIZE`.
- No broad media permission is used to display a runtime permission dialog.
- No `_data` column query or content-URI-to-file-path conversion was found.
- Large bitmaps are not passed through navigation arguments.
- Ads are not shown before opening the picker or during picker flow.

## Merged Release-Manifest Findings

Merged manifest inspected: `app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml`

Important findings:

- Package: `com.rameshta.photocompressor`
- `MainActivity`: exported `true`, launcher only.
- `FileProvider`: authority `com.rameshta.photocompressor.fileprovider`, exported `false`, grants URI permissions.
- AdMob metadata exists: `com.google.android.gms.ads.APPLICATION_ID`.
- Current release metadata value is the disabled placeholder because production IDs are missing.
- SDK components include `com.google.android.gms.ads.AdActivity`, `MobileAdsInitProvider`, `AdService`, `OutOfContextTestingActivity`, `NotificationHandlerActivity`, `GoogleApiActivity`, `HsdpShimActivity`, WorkManager components, and ProfileInstaller receiver.
- `android.ext.adservices` optional uses-library is present.
- `debuggable` is not present in the release manifest.
- `testOnly` is not present in the release manifest.
- No app-declared `usesCleartextTraffic` override or network security config was found.
- No `READ_MEDIA_*`, `MANAGE_EXTERNAL_STORAGE`, `QUERY_ALL_PACKAGES`, or `REQUEST_INSTALL_PACKAGES` was found.

## Banner Placement Matrix

| Placement | Implemented | Policy decision | Notes |
|---|---:|---|---|
| `TOP` | Yes | Policy-sensitive due density | Shared top anchored adaptive banner below each app bar on all app-owned routes. Hidden while a full-screen ad is visible. |
| `BOTTOM` | Yes | Policy-sensitive due density | Shared bottom anchored adaptive banner above system navigation and above persistent bottom actions where present. Hidden while a full-screen ad is visible. |
| `HOME_EMPTY_SPACE` | Yes | Requires manual layout QA | Extra adaptive banner in empty Home content only when the screen is tall enough and ads are eligible. |
| `RESULT_EMPTY_SPACE` | Yes | Requires manual layout QA | Extra adaptive banner in Result content between details and save/share controls, not over image previews. |
| `HISTORY_INLINE` | Yes | Requires manual density QA | Inserted after every five real History items, never before the first item and never as a trailing empty-looking row. Content item keys/actions remain stable. |
| `TEMPLATE_LIST_INLINE` | No current route | Deferred | Placement enum exists, but no separate template-list route exists in the current navigation graph. |
| Splash/startup | No | Prohibited | No banner on splash. |
| Android Photo Picker | No | Prohibited | System UI cannot contain app banner. |
| Dialogs/snackbars | No | Prohibited | No banner in transient UI. |
| Crop/refinement canvas | No direct overlay | Prohibited over controls/images | Banner is not constructed in editing canvas area. |
| Processing/progress content area | Top/bottom only | Allowed with QA | Compression and background-removal screens can show top/bottom scaffold banners, but no banner overlays the progress indicator, preview, or Cancel button. |

## Interstitial Placement Matrix

| Placement | Implemented trigger | Policy decision | Notes |
|---|---|---|---|
| `HISTORY_OPENED` | User deliberately taps the History destination from Home | Policy-sensitive | Pending navigation is preserved. If an already-loaded ad can show, History opens after dismissal/failure; otherwise History opens immediately. No ad is shown on state restoration, already-current History, Back, app foreground return, or external viewer return. |
| `SAVE_CLICKED` | User taps Save/Save all with a valid processed output | Policy-sensitive | Pending save request ID is preserved. If an already-loaded ad can show, MediaStore save starts after dismissal/failure; otherwise save starts immediately. Duplicate Save taps are ignored while a Save ad/save flow is pending or active. |
| Image Open | No enum and no trigger | Prohibited | Open is direct user navigation to an external viewer and runs immediately with a secure `content://` URI. |
| Image Share | No enum and no trigger | Direct user action | Share validates output and opens the Android Share Sheet immediately; no interstitial is shown. |
| App launch/splash | None | Prohibited | Not implemented. |
| Ordinary navigation/screen open | None except requested History tap | Prohibited | No interstitial is shown on ordinary screen opens or buttons other than the explicit History tap. |
| Back/exit | None | Prohibited | Not implemented. |
| Before Photo Picker | None | Prohibited | Not implemented. |
| Before/during compression, export, or ONNX inference | None | Prohibited | Not implemented. |
| Failure/cancellation | None | Prohibited | Not implemented. |

Frequency behavior:

- The requested implementation attempts an interstitial only when one is already loaded and consent/initialization allow ads.
- It never waits for an ad load after the user taps History or Save.
- It never displays from an ad-load callback.
- The current requested flow does not include the previous conservative every-3-actions frequency cap; this increases policy/density risk and must be manually reviewed before production.

Save sequence verified in source:

1. User taps Save.
2. ViewModel validates that a selected result exists and ignores duplicate taps while a pending ad/save flow is active.
3. A stable request ID is stored in `PendingAdAction.SaveResult`.
4. If an already-loaded `SAVE_CLICKED` interstitial can show, the ad is shown.
5. On dismissal, show failure, unavailable ad, unavailable Activity, or missing consent, `performPendingSave(requestId)` runs exactly once.
6. Image is saved through `SaveImagesUseCase`/MediaStore.
7. A nonblank saved URI is required.
8. History is updated with the saved `content://` output URI.
9. Success Toast text is `Image saved successfully` only after MediaStore and History insertion succeed.
10. Failure Toast text is `Couldn't save image. Please try again.` if MediaStore save or URI verification fails.

History sequence verified in source:

1. User taps the History icon.
2. ViewModel stores `PendingAdAction.OpenHistory`.
3. If already on History, the pending action is consumed without an ad.
4. If an already-loaded `HISTORY_OPENED` interstitial can show, the ad is shown.
5. On dismissal, show failure, unavailable ad, unavailable Activity, or missing consent, navigation to History executes exactly once.

Open sequence verified in source:

- Open never queues an interstitial.
- It creates an `Intent.ACTION_VIEW` with `setDataAndType(uri, mimeType)` and `FLAG_GRANT_READ_URI_PERMISSION`.
- If no compatible viewer exists or the output is unavailable, a user-visible error is shown without displaying an ad.

## Consent Implementation

Implemented behavior:

- UMP consent information is requested on app launch.
- Required consent forms are loaded/displayed before ads are requested.
- `canRequestAds()` gates Mobile Ads initialization and ad loading.
- Initialization is idempotent and not duplicated across recomposition.
- Privacy options are exposed from Settings when UMP reports they are required.
- Consent errors do not block image selection, processing, editing, or export.
- Ads are optional; ad load/show failures do not surface user-facing errors.

Manual verification still needed:

- EEA/GDPR required consent state.
- Consent not required state.
- Consent declined state.
- US state privacy messages.
- Privacy options reopening after production AdMob message setup.

## Data Safety Recommendation

Adding AdMob changes the previous privacy posture. The app should not be described as fully offline. Accurate wording:

> Image processing happens entirely on your device. An internet connection may be used to load advertisements and consent information.

Based on Google Mobile Ads SDK data disclosure documentation for SDK `25.4.0`, Data Safety should be reviewed for:

- IP address, including approximate location inferred from IP.
- User product interactions related to ad serving.
- Diagnostics and SDK/app performance data.
- Device/account identifiers, including Advertising ID where permitted.
- Advertising data and fraud-prevention signals.
- Consent choices processed through UMP.

Image data recommendation:

- User-selected images, generated images, filenames, content URIs, EXIF metadata, model tensors, and image-processing outputs are not sent to AdMob by app code.
- Background removal uses a bundled ONNX model and no remote model download was found.
- Outputs saved to MediaStore are user-controlled device files.

Proposed Play Console direction, subject to developer confirmation against actual Play/AdMob configuration:

- Mark **Yes** for ads.
- Disclose advertising-related data collection/sharing according to Google Mobile Ads SDK documentation.
- Do not declare app collection/sharing of user images unless another non-repository service exists.
- Declare that image processing is local and images are not transmitted by app code.
- Reconcile this with any AdMob personalized/non-personalized/Limited Ads configuration selected in AdMob.

## Privacy-Policy Requirements

`PRIVACY_POLICY.md` was created as a draft source, not a publishable Play URL. Before release, publish a policy that is:

- Public HTTPS.
- Viewable without login.
- Not visitor-editable.
- Not a local file.
- Not PDF-only.
- Consistent with the Play Data Safety form and app behavior.

Required content:

- Developer/app legal identity.
- Support email or contact page.
- App name and package.
- User-controlled Photo Picker access.
- Local image processing and no app-code upload of images.
- Temporary cache behavior and saved MediaStore outputs.
- Google AdMob and UMP usage.
- Advertising-related data handled by Google and partners.
- Consent/privacy choices where required.
- Children/target-audience statement after Play Console configuration is final.
- Data retention and deletion instructions.
- Policy update date.

## Dependency And Licence Inventory

Release direct dependencies:

| Dependency | Version | Purpose | Licence/source | Native code | Permissions/components contributed | Commercial restriction notes |
|---|---:|---|---|---|---|---|
| `androidx.core:core-ktx` | 1.17.0 | Android Kotlin APIs | AndroidX / Apache-2.0 | No direct | AndroidX support metadata | Maintained AndroidX |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.9.4 | Lifecycle/coroutines | AndroidX / Apache-2.0 | No | Lifecycle components transitively | Maintained AndroidX |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.9.4 | Compose ViewModel integration | AndroidX / Apache-2.0 | No | None notable | Maintained AndroidX |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.9.4 | Compose lifecycle state | AndroidX / Apache-2.0 | No | None notable | Maintained AndroidX |
| `androidx.activity:activity-compose` | 1.11.0 | Activity/Photo Picker Compose host | AndroidX / Apache-2.0 | No | Activity support | Maintained AndroidX |
| Compose BOM and Compose UI/Graphics/Material3/Icons | BOM 2024.09.00 | UI | AndroidX / Apache-2.0 | Transitive `androidx.graphics.path` native lib | None notable | Maintained AndroidX |
| `androidx.navigation:navigation-compose` | 2.9.5 | Navigation | AndroidX / Apache-2.0 | No | None notable | Maintained AndroidX |
| `androidx.hilt:hilt-navigation-compose` | 1.3.0 | DI navigation helpers | AndroidX / Apache-2.0 | No | None notable | Maintained AndroidX |
| `androidx.exifinterface:exifinterface` | 1.4.1 | EXIF orientation/metadata support | AndroidX / Apache-2.0 | No | None notable | Maintained AndroidX |
| `androidx.datastore:datastore-preferences` | 1.1.7 | Persistent History state/preferences | AndroidX / Apache-2.0 | `libdatastore_shared_counter.so` | None notable | Maintained AndroidX |
| `io.coil-kt:coil-compose` | 2.7.0 | Image loading/display | Coil / Apache-2.0 | No direct | OkHttp/Okio transitively; no network usage found in app code for selected URIs | Maintained |
| `com.google.dagger:hilt-android` | 2.60.1 | Dependency injection | Dagger/Hilt / Apache-2.0 | No | Hilt generated code | Maintained Google |
| `com.microsoft.onnxruntime:onnxruntime-android` | 1.27.0 | On-device background removal inference | ONNX Runtime / MIT | Yes, ONNX Runtime `.so` files | Native libraries, no permissions | Maintained Microsoft; size impact high |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.10.2 | Coroutines | Kotlin / Apache-2.0 | No | `DebugProbesKt.bin` resource | Maintained JetBrains |
| `com.google.android.gms:play-services-ads` | 25.4.0 | AdMob ads | Google Mobile Ads SDK terms/docs | No app-visible `.so` | Ad permissions, activities, services, provider, queries, UMP, WorkManager transitives | Official Google SDK; requires Play/Data Safety disclosures |
| `com.google.android.ump:user-messaging-platform` | 4.0.0 | Consent forms/privacy choices | Google UMP docs | No | UMP resources/classes | Official Google SDK |

Relevant transitives:

- `com.google.android.gms:play-services-ads-api:25.4.0`
- `com.google.android.gms:play-services-ads-identifier:18.0.0`
- `com.google.android.gms:play-services-measurement-sdk-api:20.1.2`
- `androidx.work:work-runtime:2.7.0`
- `androidx.privacysandbox.ads:ads-adservices:1.0.0-beta05`
- `androidx.privacysandbox.ads:ads-adservices-java:1.0.0-beta05`

No GPL/AGPL direct dependency, dynamic version, snapshot dependency, mediation SDK, or non-official ad network was added.

## U2-Net/ONNX Model Provenance

- Runtime: ONNX Runtime Android `1.27.0`.
- Runtime licence: MIT; notice files are packaged as app assets.
- Model asset: `app/src/main/assets/models/u2netp.onnx`.
- Model size: 4.4 MB on disk, 4,603,519 bytes in AAB.
- Model checksum recorded previously: `2db478c3e56cc19f8076b5bc12f0725716fc82d5b9a19e554815cac1150c476b`.
- Expected source direction: official U2-Net/U2-NetP repository source is Apache-2.0.
- Commercial-use conclusion: **not cleared for production** until the exact pretrained weight redistribution and conversion rights are documented.

## 16 KB Page-Size Result

Artifact checks:

- `zipalign -v -c -P 16 4 app-release-unsigned.apk`: passed.
- ELF LOAD segment alignment check with `llvm-objdump`: no non-`align 2**14` LOAD segments found.
- `extractNativeLibs=false` in final manifest.

Runtime check:

- Attached device page size: `4096`.
- No 16 KB page-size emulator/device runtime test was completed.

Result: **Artifact compatibility passed; runtime 16 KB validation remains medium risk.**

## Native ABI Inventory

Final APK native libraries:

| ABI | Native libraries |
|---|---|
| `arm64-v8a` | `libandroidx.graphics.path.so` 12 KB, `libdatastore_shared_counter.so` 8 KB, `libonnxruntime.so` 27 MB, `libonnxruntime4j_jni.so` 112 KB |
| `armeabi-v7a` | `libandroidx.graphics.path.so` 8 KB, `libdatastore_shared_counter.so` 8 KB, `libonnxruntime.so` 19 MB, `libonnxruntime4j_jni.so` 80 KB |
| `x86` | `libandroidx.graphics.path.so` 12 KB, `libdatastore_shared_counter.so` 8 KB, `libonnxruntime.so` 32 MB, `libonnxruntime4j_jni.so` 92 KB |
| `x86_64` | `libandroidx.graphics.path.so` 12 KB, `libdatastore_shared_counter.so` 8 KB, `libonnxruntime.so` 32 MB, `libonnxruntime4j_jni.so` 100 KB |

## Release APK/AAB Analysis

- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`, 120 MB.
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`, 56 MB.
- Release APK signing: unsigned.
- Release AAB signing: unsigned.
- Debuggable: false/not present in release manifest.
- Test-only: false/not present in release manifest.
- Cleartext override: none found.
- Network security config: none found.
- FileProvider paths: scoped app cache/files paths, not root filesystem or broad external storage.
- Source maps: R8 mapping is packaged in AAB metadata (`BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map`, 60.65 MB).
- Model file packaged: `base/assets/models/u2netp.onnx`.
- Licence files packaged: `MODEL_PROVENANCE.md`, ONNX Runtime license/notices, U2-Net license, third-party notices.

Largest AAB entries:

| Entry | Size |
|---|---:|
| R8 mapping metadata | 60.65 MB |
| `base/lib/x86_64/libonnxruntime.so` | 33.98 MB |
| `base/lib/x86/libonnxruntime.so` | 33.96 MB |
| `base/lib/arm64-v8a/libonnxruntime.so` | 27.99 MB |
| `base/lib/armeabi-v7a/libonnxruntime.so` | 20.01 MB |
| `base/dex/classes.dex` | 6.38 MB |
| `base/assets/models/u2netp.onnx` | 4.60 MB |
| `base/res/drawable/ic_image_compressor_logo.png` | 777 KB |

Play Asset Delivery is not recommended automatically. The model is required for offline background removal and should remain locally available unless a separately reviewed asset-delivery strategy preserves offline behavior.

## Security Findings

- No app WebView, Retrofit, Ktor, Volley, Firebase, Crashlytics, Remote Config, telemetry SDK, or hard-coded app network URL was found.
- Google Ads SDK resources contain ad/offline advertising strings and SDK components; this is expected from the official SDK and must be disclosed.
- No app code uploads image data.
- No unsafe FileProvider root or broad external-storage path was found.
- No app code shares `file://` URIs; sharing uses FileProvider/content URIs.
- No app component accepts broad untrusted deep links.
- No production ad IDs are logged by app code.
- Debug-only ad logging is guarded by `BuildConfig.DEBUG`.
- Persisted History stores metadata and output URIs only; it does not store full-resolution bitmaps or raw image bytes.

## Offline And Network Findings

The final ad-supported app must not be described as fully offline. Accurate behavior:

- Core image processing, background removal inference, compression, resize, conversion, crop, collage, save, and share run on-device by app code.
- Internet may be used by UMP and Google Mobile Ads SDK for consent and ad loading.
- If the device is offline, consent/ad failures do not block core image workflows.
- No app network client for image upload was found.
- The final merged manifest contains `INTERNET`, so the release is not a no-network artifact.

Airplane/offline manual matrix remains to be completed after production IDs/signing using test devices and without clicking production ads.

## Performance And Memory Findings

- Interstitials are not shown during compression, ONNX inference, export encoding, or MediaStore pending transactions.
- Interstitials are preloaded only after consent/SDK initialization and are optional.
- Top/bottom banners are owned by shared scaffold instances and do not reload because progress state changes.
- Inline History ads use stable mixed-list keys and do not count as History items.
- Banners are reused across recompositions and destroyed with lifecycle cleanup.
- No ad load waits block image-processing flows.
- Source inspection found no ad component retaining full-resolution bitmaps or storing an Activity reference.
- Automated tests verified core flows still compile and run; no low-memory, slow-network, no-fill, 12 MP, batch-compression, or background-removal timing benchmark was completed in this environment.
- Performance comparison result: no automated processing-time regression was detected by existing tests, but a real ads-enabled/ads-disabled timing and memory benchmark remains a manual release task.

## Accessibility Findings

- Banners are placed in scaffold slots or mixed History list rows, not over image content, controls, dialogs, snackbars, or system navigation.
- Stable banner height is reserved while loading for top/bottom placements to reduce layout jumps.
- Top+bottom+inline ad density still needs manual TalkBack/small-screen/large-font review before production.
- Settings exposes privacy options only when UMP reports they are required.
- Full TalkBack, dynamic font, RTL, contrast, and small-screen ad-placement QA remains manual.

## Tests And Commands Executed

Successful:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugKotlin :app:compileReleaseKotlin
./gradlew :app:lintDebug :app:lintRelease
./gradlew :app:processReleaseMainManifest :app:assembleRelease :app:bundleRelease
./gradlew :app:connectedDebugAndroidTest
./gradlew :app:testDebugUnitTest
/Users/naimishgupta/Library/Android/sdk/build-tools/36.1.0/aapt dump badging app/build/outputs/apk/release/app-release-unsigned.apk
/Users/naimishgupta/Library/Android/sdk/build-tools/36.1.0/aapt dump permissions app/build/outputs/apk/release/app-release-unsigned.apk
/Users/naimishgupta/Library/Android/sdk/build-tools/36.1.0/zipalign -v -c -P 16 4 app/build/outputs/apk/release/app-release-unsigned.apk
xcrun llvm-objdump LOAD alignment audit over extracted release APK native libraries
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
/Users/naimishgupta/Library/Android/sdk/build-tools/36.1.0/apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release-unsigned.apk
```

Expected negative/signing results:

- `apksigner verify`: failed because the APK is unsigned.
- `jarsigner`: reported the AAB is unsigned.

Other command notes:

- Connected instrumentation tests passed: 3 tests on SM-S928B. The AndroidX test services `appops` warning about `MANAGE_EXTERNAL_STORAGE` had no test failure.
- Lint reports were generated at `app/build/reports/lint-results-debug.html` and `app/build/reports/lint-results-release.html`.
- Release `BuildConfig` inspection confirmed `ADS_ENABLED=false` and blank ad-unit IDs because production AdMob IDs were not supplied.
- Release APK size: 120 MB. Release AAB size: 56 MB.
- 16 KB ZIP alignment passed and no native ELF LOAD segment with non-`2**14` alignment was found.

## Store-Listing Checklist

Developer must verify manually:

- App name matches installed label `Photo Compressor`.
- App icon, adaptive icon, round icon, and screenshots match the current UI.
- Listing says the app contains ads.
- Description does not claim the full app is offline.
- Description says image processing happens on device and internet may be used for ads/consent.
- Description does not claim lossless compression unless technically true.
- Background-removal limitations are accurately described.
- Privacy claims match code and Google SDK behavior.
- Data Safety answers match AdMob/UMP configuration.
- Content rating includes ads.
- Category, support email, and privacy URL are valid.
- Target audience is accurate.
- No misleading before/after images, copyrighted images, or unauthorized trademarks are used.

## Manual Steps Remaining In Play Console And AdMob

- Mark **Yes, this app contains ads** in Play Console.
- Complete/update Data Safety for Google Mobile Ads SDK and UMP.
- Add a valid public HTTPS privacy-policy URL.
- Complete Target Audience declaration.
- Complete content rating with advertising included.
- Configure GDPR/European regulations messages in AdMob.
- Configure applicable US state privacy messages.
- Add and verify `app-ads.txt`.
- Link the AdMob app to the Play Store listing.
- Confirm production AdMob app ID and ad units.
- Configure ad content blocking controls.
- Confirm personalized, non-personalized, or Limited Ads strategy.
- Verify seller-information visibility where required.
- Review whether Play Console requires a foreground-service declaration because `FOREGROUND_SERVICE` is present from the Ads SDK WorkManager transitive dependency.
- Rebuild with production IDs, sign the AAB, inspect the final merged manifest, and run release QA with test device configuration.

## Final Go/No-Go Recommendation

**No-go for production upload today.**

The code implementation is centralized and technically controlled: consent is centralized, debug builds use test ads, production sample IDs are blocked, banners are shared and lifecycle-aware, Open/Share never show an interstitial, image data is not uploaded by app code, and History now persists compressed/background-removed/saved outputs. Production release approval still requires the remaining blockers to be closed, a final signed production-configured AAB to be rebuilt and inspected, and a manual Google Play Ads policy review of the requested History-open interstitial, Save-click interstitial, and top+bottom+inline banner density.

Production ads were not clicked during testing. The current debug configuration uses Google test ads, and the current release artifact has ads disabled because production identifiers were not provided.
