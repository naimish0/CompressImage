# Security And Privacy Audit

Audit date: 2026-07-17  
Project: Photo Compressor  
Scope: Android application security/privacy audit for Google Play publication.

> Historical snapshot: this audit predates the 2026-07-18 remediation pass.
> Recheck the current source and `legal/MODEL_PROVENANCE.md` for the updated
> model artifact, privacy UI, and storage behavior. Signing and publisher-owned
> identity/hosting remain external by design.

## Executive Verdict

**Release recommendation: Not ready for Play production upload.**

No confidential credentials, private keys, signing passwords, service-account
files, OAuth secrets, backend tokens, or image-upload paths were found in the
current scanned repository or Git history. The audit found and fixed
repo-controlled privacy/security issues around backup of History metadata,
stored content-URI validation, app-owned temporary output cleanup, filename
sanitization, and future credential-file ignores.

The remaining release blockers are external or policy/configuration items:
production signing is not configured, release AdMob production IDs are absent
so release ads are disabled, the Play/AdMob/Firebase registrations need to be
created or updated for `com.rameshta.photocompressor`, the public privacy
policy still needs real developer identity/contact/URL, and the bundled U2-NetP
pretrained-weight redistribution rights remain unverified in official source
material.

Official references reviewed:

- Android backup rules and privacy: https://developer.android.com/privacy-and-security/risks/backup-best-practices
- Android FileProvider security: https://developer.android.com/privacy-and-security/risks/file-providers
- Android secure file sharing: https://developer.android.com/training/secure-file-sharing
- Android application/data-extraction manifest rules: https://developer.android.com/guide/topics/manifest/application-element
- Android network security guidance: https://developer.android.com/privacy-and-security/security-ssl
- Google Play Data Safety: https://support.google.com/googleplay/android-developer/answer/10787469
- Google Mobile Ads SDK Data Safety disclosure: https://developers.google.com/admob/android/privacy/play-data-disclosure
- Google Play Advertising ID policy: https://support.google.com/googleplay/android-developer/answer/6048248

## Finding Counts

| Severity | Count | Fixed | Remaining |
|---|---:|---:|---:|
| Critical | 0 | 0 | 0 |
| High | 4 | 0 | 4 |
| Medium | 6 | 5 | 1 |
| Low | 4 | 3 | 1 |
| Informational | 5 | 0 | 5 |

## Critical Findings

None found.

## High Findings

### H-01: Release artifacts are unsigned

- **Severity:** High
- **File/location:** `app/build/outputs/apk/release/app-release-unsigned.apk`; `app/build/outputs/bundle/release/app-release.aab`
- **Evidence:** `apksigner verify` reports `DOES NOT VERIFY`; `jarsigner -verify` reports `jar is unsigned`.
- **Impact:** The local release artifacts are not publishable upload artifacts.
- **Fix:** Configure Play App Signing/upload signing through local or CI secrets. Do not commit keystores or passwords.
- **Verification:** Release APK/AAB rebuilt and signing verification rerun.
- **Fixed:** No, requires developer signing material.
- **External action required:** Create/configure upload key, store credentials outside Git, rebuild signed AAB.

### H-02: Release ads disabled because production AdMob IDs are not configured

- **Severity:** High
- **File/location:** `app/build.gradle.kts`; generated release `BuildConfig`; merged release manifest.
- **Evidence:** Release `BuildConfig` has `ADS_ENABLED=false` and blank ad unit IDs. Merged release manifest uses disabled placeholder app ID `ca-app...0000`.
- **Impact:** The generated release does not represent a production ad-supported build.
- **Fix:** Supply production AdMob IDs through Gradle/CI properties, rebuild, and reinspect release artifacts.
- **Verification:** Debug `BuildConfig` uses Google sample IDs; release `BuildConfig` is disabled when IDs are missing.
- **Fixed:** No, requires production AdMob configuration.
- **External action required:** Configure AdMob app and ad-unit IDs, GDPR/US consent messages, app-ads.txt, and Play Console “Contains ads: Yes”.

### H-03: U2-NetP pretrained-weight redistribution rights remain externally unverified

- **Severity:** High
- **File/location:** `app/src/main/assets/models/u2netp.onnx`; `legal/MODEL_PROVENANCE.md`
- **Evidence:** The legal record documents Apache-2.0 repository source code, but states the exact pretrained weight redistribution rights were not independently found in official files.
- **Impact:** Unknown model-weight redistribution rights can create commercial publication/licensing risk.
- **Fix:** Obtain explicit rights-holder evidence for commercial use, ONNX conversion, redistribution, and Google Play distribution, or replace the model with verified rights.
- **Verification:** Model asset is packaged in APK/AAB; model provenance document inspected.
- **Fixed:** No.
- **External action required:** Legal/provenance confirmation before production.

### H-04: Privacy policy is still a draft with placeholders

- **Severity:** High
- **File/location:** `PRIVACY_POLICY.md`; app Settings legal text.
- **Evidence:** Policy contains placeholders for effective date, legal developer name, contact, and target-audience/children statement.
- **Impact:** Google Play requires accurate public privacy disclosure, especially with AdMob/UMP and personal image processing.
- **Fix:** Publish a public HTTPS privacy policy and point both app/legal UI and Play Console to the same policy.
- **Verification:** Privacy policy source inspected.
- **Fixed:** No.
- **External action required:** Developer must provide legal identity, contact, URL, and final target-audience declaration.

## Medium Findings

### M-01: History metadata was eligible for Android backup/device transfer

- **Severity:** Medium
- **File/location:** `app/src/main/res/xml/backup_rules.xml`; `app/src/main/res/xml/data_extraction_rules.xml`; `DataStoreHistoryRepository`
- **Evidence:** Backup rules were broad. History DataStore stores output URI, display name, MIME type, size/dimensions, operation type, timestamps, and original image info.
- **Impact:** User image metadata and content URI references could migrate through backup/transfer without a deliberate product/privacy decision.
- **Fix:** Exclude `datastore/processed_image_history.preferences_pb` from cloud backup and device transfer.
- **Verification:** Unit test `privacySensitiveXmlRulesAreScoped`; release manifest references both backup/data-extraction XML resources; AAB includes the XML resources.
- **Fixed:** Yes.
- **External action required:** Keep privacy policy consistent with local History retention.

### M-02: Stored content URIs were accepted for sharing/opening without authority validation

- **Severity:** Medium
- **File/location:** `app/src/main/java/com/rameshta/photocompressor/data/storage/ImageShareController.kt`
- **Evidence:** Any persisted `content://` in `ProcessedImage.filePath` was returned for share/open intents.
- **Impact:** If app-private History metadata were corrupted or restored maliciously, the app could proxy an unintended content URI.
- **Fix:** Accept only MediaStore authority and this app’s FileProvider authority for stored output content URIs.
- **Verification:** Source inspection; unit/build/lint pass; release rebuilt.
- **Fixed:** Yes.
- **External action required:** None.

### M-03: Clear/remove History left app-owned temporary outputs behind

- **Severity:** Medium
- **File/location:** `app/src/main/java/com/rameshta/photocompressor/data/storage/DataStoreHistoryRepository.kt`
- **Evidence:** `remove()` and `clear()` only removed DataStore metadata.
- **Impact:** App-owned processed/background-removal temp files could remain after users cleared History.
- **Fix:** Delete only app-owned temp output files under `cache/processed` and `cache/background_removal` when corresponding History entries are removed or cleared. Content URI/gallery outputs are not deleted.
- **Verification:** Source inspection and full test/build pass.
- **Fixed:** Yes.
- **External action required:** None.

### M-04: Manual output filename sanitization allowed path-looking names

- **Severity:** Medium
- **File/location:** `app/src/main/java/com/rameshta/photocompressor/util/OutputFilenameGenerator.kt`; `AndroidImageRepository.saveImage`
- **Evidence:** Unsafe characters were replaced, but names like `../secret name` could retain leading dot/underscore patterns.
- **Impact:** MediaStore display names do not directly create filesystem traversal here, but path-looking user-provided names are avoidable defense-in-depth risk.
- **Fix:** Trim leading/trailing `.`, `_`, and `-`, cap generated base names, and keep extension enforcement.
- **Verification:** Unit test added for path-looking names; full tests pass.
- **Fixed:** Yes.
- **External action required:** None.

### M-05: Production package name migrated to owned namespace

- **Severity:** Medium
- **File/location:** `app/build.gradle.kts`; APK badging.
- **Evidence:** Release package is `com.rameshta.photocompressor`.
- **Impact:** Package name is permanent after first Play upload and should be a developer-owned namespace.
- **Fix:** Changed the Gradle namespace/application ID and source package to the intended final package before first upload.
- **Verification:** `aapt dump badging`.
- **Fixed:** Yes.
- **External action required:** Create or update external service registrations for this package before production.

### M-06: History still stores original source URI metadata locally

- **Severity:** Medium
- **File/location:** `DataStoreHistoryRepository.encodeImageInfo`
- **Evidence:** History entries include original `uriString` to support existing comparison/result behavior.
- **Impact:** App-private local metadata can include user-selected source URI/display name. This is not uploaded by app code and is now excluded from backup, but it remains local app data.
- **Fix:** No code change in this pass to avoid changing History/comparison behavior. Revisit only with a product decision.
- **Verification:** Source inspection; backup exclusions added.
- **Fixed:** No.
- **External action required:** Decide whether History should retain original source references or store only output metadata.

## Low Findings

### L-01: `.gitignore` did not cover common secret/signing artifacts

- **Severity:** Low
- **File/location:** `.gitignore`
- **Evidence:** Patterns for keystores, `.env`, service-account JSON, private keys, and Firebase config were absent.
- **Impact:** Higher chance of accidentally staging future credentials.
- **Fix:** Added ignore patterns for keystores, key/cert containers, `.env*`, `google-services.json`, and service-account JSON names.
- **Verification:** `git status`; file-pattern scan found no such current files outside ignored build/venv folders.
- **Fixed:** Yes.
- **External action required:** None.

### L-02: FileProvider scope should stay narrow

- **Severity:** Low
- **File/location:** `app/src/main/res/xml/file_paths.xml`
- **Evidence:** Current scope is limited to `cache-path` entries `processed/` and `background_removal/`.
- **Impact:** Good current posture; future broad paths could expose app/private files.
- **Fix:** Added unit test asserting no `<root-path>` or `<external-path>` and required scoped cache paths.
- **Verification:** Unit test `privacySensitiveXmlRulesAreScoped`.
- **Fixed:** Yes.
- **External action required:** Keep future FileProvider additions reviewed.

### L-03: Release contains `DebugProbesKt.bin`

- **Severity:** Low
- **File/location:** Release APK/AAB root.
- **Evidence:** APK listing includes `DebugProbesKt.bin`, commonly packaged by coroutines.
- **Impact:** No direct secret exposure found, but it is debug-oriented packaging noise.
- **Fix:** Not changed; excluding it should be tested against coroutine tooling/runtime expectations.
- **Verification:** APK/AAB listing.
- **Fixed:** No.
- **External action required:** Optional packaging review.

### L-04: Lint warnings remain

- **Severity:** Low
- **File/location:** `app/build/reports/lint-results-debug.xml`; `app/build/reports/lint-results-release.xml`
- **Evidence:** 23 warnings per variant: dependency/tool newer versions, icon shape/location, unused string, and banner `Configuration.screenHeightDp` guidance.
- **Impact:** No direct credential or image-leak issue found, but production polish and future maintenance risk remain.
- **Fix:** Not changed in this security pass to avoid broad upgrades/refactors.
- **Verification:** `lintDebug` and `lintRelease` pass with warnings.
- **Fixed:** No.
- **External action required:** Address in a separate maintenance pass.

## Informational Findings

### I-01: AdMob identifiers are public identifiers, not secrets

- **Evidence:** Current and history scans find only redacted AdMob public IDs such as `ca-app...1713`.
- **Impact:** These values are extractable from APK/AAB by design. Debug/release separation matters for traffic control, not secrecy.
- **Verification:** Current scan found 5 AdMob public ID hits; Git history scan found 55 AdMob public ID hits across 14 commits.

### I-02: No confidential credentials found in current repository or Git history

- **Evidence:** Pattern scans covered tracked/untracked nonignored text files, `local.properties`, `gradle.properties`, and `git rev-list --all`.
- **Impact:** No immediate credential rotation was indicated by repository evidence.
- **Limitations:** `gitleaks` and `trufflehog` were not installed; pattern scan was used instead.

### I-03: Image processing data flow is local by app code

- **Evidence:** No Retrofit, Ktor, Volley, Firebase, Crashlytics, analytics event upload, custom `HttpURLConnection`, WebView upload, or image-upload endpoint found. ONNX Runtime uses bundled `assets/models/u2netp.onnx`.
- **Impact:** App code does not upload selected images, generated images, filenames, content URIs, or EXIF data. AdMob/UMP may use network for ads and consent.
- **Verification:** Source/dependency scan and release artifact inspection.

### I-04: EXIF handling reads orientation but does not copy GPS metadata

- **Evidence:** `ExifInterface` is used to read orientation for display/processing. Outputs are encoded from `Bitmap.compress`; no `setAttribute()` or `saveAttributes()` copy path was found.
- **Impact:** GPS/device EXIF metadata is not intentionally preserved in processed outputs by app code. Orientation is applied to pixels.
- **Verification:** Source search for EXIF write APIs and output encoding flow.

### I-05: No WebView code found

- **Evidence:** No `WebView`, `loadUrl`, `addJavascriptInterface`, SSL-error bypass, trust-all TLS, or hostname-verifier bypass found in app code.
- **Impact:** WebView-specific attack paths are not present in the app-owned codebase.
- **Verification:** Source search.

## Secret Scan Summary

- Dedicated tools: `gitleaks`/`trufflehog` were not installed in this environment.
- Current repository scan: 5 findings, all AdMob public IDs.
- Git history scan: 14 commits scanned via `git rev-list --all`; 55 findings, all AdMob public IDs.
- File artifact scan: no `.jks`, `.keystore`, `.p12`, `.pfx`, `.pem`, `.key`, `google-services.json`, service-account JSON, or `.env*` files found outside ignored build/venv directories.
- Confirmed confidential credentials: none found.
- Git-history exposure status: no confidential credentials found; AdMob public identifiers appeared in history and do not require rotation as secrets.

## Build Configuration Assessment

- `compileSdk`: 36.1
- `targetSdk`: 36
- `minSdk`: 24
- AGP: 9.1.1
- Gradle wrapper: 9.3.1
- Kotlin: 2.3.10
- R8/minify/resource shrinking: enabled for release
- Debug ads: Google official sample IDs only, `ADS_TEST_MODE=true`
- Release ads: production IDs required via Gradle properties; missing IDs produce `ADS_ENABLED=false`
- Release signing: not configured; local APK/AAB unsigned
- Release `debuggable`: absent/false in merged release manifest
- Release `testOnly`: absent in merged release manifest

## Merged Release Manifest And Permissions

Release APK permissions:

- `android.permission.INTERNET`: app manifest and Google ads; required for AdMob/UMP.
- `android.permission.ACCESS_NETWORK_STATE`: app manifest and Google ads/WorkManager; required for ad/network state.
- `com.google.android.gms.permission.AD_ID`: Google Mobile Ads/ads identifier.
- `android.permission.ACCESS_ADSERVICES_AD_ID`: Google Mobile Ads Privacy Sandbox/AdServices.
- `android.permission.ACCESS_ADSERVICES_ATTRIBUTION`: Google Mobile Ads Privacy Sandbox/AdServices.
- `android.permission.ACCESS_ADSERVICES_TOPICS`: Google Mobile Ads Privacy Sandbox/AdServices.
- `android.permission.WAKE_LOCK`: Google Play services measurement/WorkManager transitive dependency.
- `android.permission.FOREGROUND_SERVICE`: WorkManager transitive dependency.
- `com.rameshta.photocompressor.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`: AndroidX dynamic receiver protection.

Not present in final release permission dump:

- `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE`, `CAMERA`, `POST_NOTIFICATIONS`, `QUERY_ALL_PACKAGES`, `REQUEST_INSTALL_PACKAGES`, `SYSTEM_ALERT_WINDOW`, exact alarm permissions.

Exported components reviewed:

- App `MainActivity` is exported for launcher only.
- FileProvider is `exported=false` and `grantUriPermissions=true`.
- Google/AndroidX exported components are SDK/platform components protected by `BIND_JOB_SERVICE` or `android.permission.DUMP` where applicable.

Network security:

- No app `usesCleartextTraffic=true`.
- No app network-security config trusting user/debug CAs.
- No custom trust-all TLS or hostname bypass code found.

Backup:

- `allowBackup=true` remains enabled.
- History DataStore file is now excluded from cloud backup and device transfer.

## Release Artifact Assessment

- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`
- APK size: 120 MB
- AAB size: 55 MB
- APK SHA-256 prefix: `d64ef41a3d1f...`
- AAB SHA-256 prefix: `2f747d4b4381...`
- App ID: `com.rameshta.photocompressor`
- Version: `versionCode=1`, `versionName=1.0`
- Native ABIs: `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`
- Largest packaged files: ONNX Runtime native libraries, `classes.dex`, `assets/models/u2netp.onnx`
- ZIP alignment: `zipalign -v -c -P 16 4` passed.
- ELF segment inspection: not completed because no `readelf`/`llvm-readelf` tool was available in the Android SDK/local shell.

## Image Data Flow Summary

1. User selects images through Android Photo Picker (`PickMultipleVisualMedia`, image-only).
2. App reads selected `content://` URIs through `ContentResolver`.
3. App detects image format from headers and reads EXIF orientation.
4. Compression/resizing/format conversion/background removal run on-device.
5. Intermediate outputs are stored in app-private cache under scoped directories.
6. Save writes through MediaStore to `Pictures/Photo Compressor`.
7. Share/Open use secure `content://` URIs with temporary read grants.
8. History stores metadata and URI references, not full-resolution bitmaps.
9. Ad requests use `AdRequest.Builder().build()` without image filenames, URIs, EXIF, or custom targeting.

## Third-Party SDK And Network Summary

Direct notable dependencies:

- Google Mobile Ads SDK `25.4.0`: networked ads, Advertising ID/AdServices data handling.
- Google User Messaging Platform `4.0.0`: consent info/forms/privacy options.
- ONNX Runtime Android `1.27.0`: native local inference.
- Coil `2.7.0`: image loading; transitive OkHttp/Okio present, but no app remote URL loading path was found.
- AndroidX DataStore `1.1.7`: local History persistence.
- Hilt, Compose, Navigation, ExifInterface, coroutines.

No Firebase Analytics, Crashlytics, Remote Config, Retrofit, Ktor, Volley, or custom backend client was found.

## AdMob And Consent Assessment

- UMP consent info is requested on app launch before ad requests.
- Ads initialize only when consent state allows `canRequestAds()`.
- Privacy options form entry exists in Settings when required.
- Debug uses Google sample test IDs.
- Release disables ads safely if production IDs are missing and rejects Google sample IDs in release properties.
- AdMob App ID and ad-unit IDs are public identifiers, not secrets.
- App code does not pass image data, filenames, content URIs, EXIF, or processing metadata to ad requests.
- Data Safety and privacy policy must disclose Google Mobile Ads/UMP data collection and sharing.

## Proposed Play Data Safety Summary

Developer must reconcile this with the final Play Console setup and Google’s current SDK disclosure page.

- Images/files: user-selected and generated images are processed locally by app code; not uploaded by app code.
- Files and docs/media: app accesses selected photos and saved output files as user-controlled processing data.
- Data collected/shared by app-owned backend: no backend found.
- Data collected/shared by third-party SDKs: Google Mobile Ads/UMP may collect/share advertising-related data, including IP address, approximate location derived from IP, device or other identifiers including Advertising ID where permitted, app interactions, diagnostics, and advertising/consent signals.
- Accounts: no user account feature found.
- Analytics/crash reporting: no Firebase/Crashlytics/analytics SDK found.
- Retention/deletion: app-local History metadata persists until History/app data is cleared; app-owned temp outputs are removed on History clear/remove where applicable; user-saved gallery outputs remain under user control.

## Commands Executed

- `git status --short --branch`
- `rg --files`; targeted `rg` searches for credentials, logs, network clients, EXIF writes, WebView/TLS bypasses, URI sharing, Photo Picker usage.
- Custom redacted current-repo secret scan.
- Custom redacted Git-history secret scan over `git rev-list --all`.
- `find` checks for keystores, key files, service-account JSON, `.env*`, and Google service files.
- `./gradlew :app:testDebugUnitTest --tests com.rameshta.photocompressor.util.UtilityTest`
- `./gradlew :app:testDebugUnitTest :app:lintDebug :app:lintRelease :app:assembleDebug :app:assembleRelease :app:bundleRelease :app:processReleaseMainManifest`
- `./gradlew :app:lintDebug :app:lintRelease`
- `./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:bundleRelease`
- `./gradlew :app:connectedDebugAndroidTest`
- `aapt dump permissions`
- `aapt dump badging`
- `apksigner verify --verbose`
- `jarsigner -verify -verbose -certs`
- `zipalign -v -c -P 16 4`
- `./gradlew :app:dependencies --configuration releaseRuntimeClasspath`

Results:

- Unit tests: passed.
- Lint: passed with 23 warnings per variant.
- Debug build: passed.
- Release APK build: passed.
- Release AAB build: passed.
- Connected instrumentation: passed, 3 tests on Android 16 device. The Android test-services appops warning for `MANAGE_EXTERNAL_STORAGE` appeared, but the task succeeded and the app does not request that permission.
- Combined lint/build/connected command initially hit an Android lint worker/task-graph bug; rerunning lint separately passed.

## Files Changed

- `.gitignore`
- `app/src/main/res/xml/backup_rules.xml`
- `app/src/main/res/xml/data_extraction_rules.xml`
- `app/src/main/java/com/rameshta/photocompressor/data/storage/DataStoreHistoryRepository.kt`
- `app/src/main/java/com/rameshta/photocompressor/data/storage/ImageShareController.kt`
- `app/src/main/java/com/rameshta/photocompressor/util/OutputFilenameGenerator.kt`
- `app/src/test/java/com/rameshta/photocompressor/util/UtilityTest.kt`
- `SECURITY_PRIVACY_AUDIT.md`

Pre-existing worktree state preserved:

- `.idea/vcs.xml` was already deleted before this audit and was not restored.

## Required External Actions

1. Configure production signing securely; do not commit keystores or passwords.
2. Create or update Play, AdMob, Firebase, API-key, OAuth, and deep-link registrations for `com.rameshta.photocompressor` before first Play upload.
3. Provide production AdMob IDs through local/CI Gradle properties and rebuild.
4. Publish the final HTTPS privacy-policy URL and update Play Console.
5. Complete Play Console Data Safety, Advertising ID, “Contains ads”, target audience, content rating, and app category declarations.
6. Configure AdMob GDPR/EEA messages, US state privacy settings, ad-content blocking controls, app-ads.txt, and Play Store linking.
7. Obtain explicit commercial redistribution evidence for the bundled U2-NetP pretrained weights.
8. Run signed release QA with production configuration using test devices/test mode only; do not click live ads.
9. Run a true 16 KB page-size runtime test if a suitable emulator/device is available.

## Residual Risk

No audit can prove that no data leak is possible. This pass verifies that no
obvious app-controlled image upload path, secret embedding, broad FileProvider
scope, unsafe TLS/WebView path, release private logging, or broad storage
permission was found. Residual risk remains in third-party SDK behavior,
Play/AdMob console configuration, model licensing, production signing
operations, and any future code paths added after this audit.
