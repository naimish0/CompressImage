# Google Play Release Audit

Review date: **2026-07-19**

App: **Photo Compressor**

Package: `com.rameshta.photocompressor`

Scope: current source tree, publishing documents and assets, the latest locally generated manifests/reports, and `app/build/outputs/bundle/release/app-release.aab`.

This document supersedes the earlier historical audit. It records the state visible on the review date; it is not proof of any setting in Play Console, AdMob, DNS, a hosting provider, or the publisher's signing system.

## Verdict

**Not ready for production upload.**

The store listing and graphics are prepared, target SDK and model provenance are in good shape, production AdMob identifiers are configured, and the app's own image processing remains on-device. Publication is still blocked by the items below.

| Status | Finding |
| --- | --- |
| **Blocker** | Interstitials are invoked immediately before Save, Share, and Open. These direct-action interruptions carry a material Google Play Ads policy risk and should not ship without changing the placements. |
| **Blocker** | The privacy policy still needs the publisher's verified legal/entity name, a direct monitored privacy contact, and an active, public, non-geofenced, non-editable HTTPS webpage URL (not a PDF). |
| **Blocker** | The existing AAB is unsigned and older than the current source. It is not the upload artifact for this implementation. |
| **Publisher action** | Play Console, AdMob, developer identity/package verification, signing, testing-track eligibility, consent messages, and app-ads.txt cannot be completed or inferred from the repository. |

## Current Build Configuration

Verified from `app/build.gradle.kts` and `gradle/libs.versions.toml`:

| Item | Current value |
| --- | --- |
| Namespace/application ID | `com.rameshta.photocompressor` |
| Version | `versionCode 1`, `versionName 1.0` |
| SDK | `minSdk 24`, `targetSdk 36`, `compileSdk 36` with minor API level 1 |
| Release optimization | R8 minification and resource shrinking enabled |
| Google Mobile Ads | `com.google.android.gms:play-services-ads:25.4.0` |
| Google UMP | `com.google.android.ump:user-messaging-platform:4.0.0` |
| ONNX Runtime | `com.microsoft.onnxruntime:onnxruntime-android:1.27.0` |

Release ads are enabled and production App ID/banner/native/interstitial/app-open identifiers are populated in `app/build.gradle.kts`. The identifiers are public routing identifiers, not signing credentials or private API secrets. Debug builds use Google's sample identifiers and test mode; release builds reject Google's sample identifiers and set test mode to false.

`targetSdk 36` meets the standard mobile-app requirement that applies to new apps and updates starting **August 31, 2026**. Play Console remains the authority for the exact account/app state: <https://support.google.com/googleplay/android-developer/answer/11926878>.

## Current Advertising Behavior

Advertising requests are gated through Google UMP state and Mobile Ads initialization. If no ad is ready, the requested app action proceeds.

| Format | Current implementation |
| --- | --- |
| Adaptive banners | Top, bottom, Home empty-space, and Result empty-space placements are eligible. Home, Progress, Result, and Background can show both top and bottom banners; other app screens use a smaller subset. |
| Native | Inline native ads are used on Home, Progress, Result, and Background screens. |
| Interstitial | Eligible when History is opened and before Save, Share, or Open, including relevant History-item actions. All placements share a global minimum interval of three minutes and maximum of three shown interstitials per app-process session. |
| App-open | Loaded after consent/SDK readiness and considered on foreground returns, with suppression around active processing, another full-screen ad, picker/share/open/privacy/locale transitions, and recent full-screen-ad return. |

The current `InterstitialPolicyConfig` has `successfulActionsRequired = 0` and `suppressFirstSessionAd = false`. Therefore an already loaded interstitial can appear at the first eligible opportunity; a previously completed workflow is not required.

### Advertising blockers and warnings

1. **Blocker — direct action interruption.** `PhotoCompressorApp.kt` calls the interstitial flow before Save, Share, and Open perform the action selected by the user. This is materially exposed to the Play rule against unexpected full-screen ads while a user is performing a task. Move these ads to a genuine natural break and let direct Save/Share/Open actions execute immediately. Official policy: <https://support.google.com/googleplay/android-developer/answer/9857753>.

2. **Warning — app-open limits are not implemented.** `AppOpenAdManager.kt` records `coldStart`, `backgroundedAt`, `lastShownAtMs`, and `adsShownThisSession`, but its eligibility path does not use those values. It does not enforce a minimum background duration, a cooldown between app-open impressions, a per-session app-open cap, or an explicit cold-start guard. The three-minute/three-per-session rules apply only to interstitials. Current privacy copy accurately discloses this distinction; the implementation should still add deliberate app-open limits before release.

3. **Warning — dense screens need device review.** Some screens can combine top and bottom banners, an additional empty-space banner, and a native placement. Test small screens, large font scale, gesture/three-button navigation, landscape, RTL, no-fill, load failure, and full-screen-ad transitions for content obstruction and accidental-click risk.

## Permissions And Manifest Surface

The app manifest directly declares:

- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`
- `android.permission.WRITE_EXTERNAL_STORAGE` with `maxSdkVersion="28"`, requested only for a user-initiated shared-Pictures save on Android 9 and earlier

The latest generated merged manifests also contain SDK/transitive entries:

- `com.google.android.gms.permission.AD_ID`
- `android.permission.ACCESS_ADSERVICES_AD_ID`
- `android.permission.ACCESS_ADSERVICES_ATTRIBUTION`
- `android.permission.ACCESS_ADSERVICES_TOPICS`
- `android.permission.WAKE_LOCK`
- `android.permission.FOREGROUND_SERVICE`
- the app-scoped signature-level `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`

The AdServices and Advertising ID permissions originate from Google Mobile Ads. `WAKE_LOCK` is merged by Google measurement/WorkManager dependencies and `FOREGROUND_SERVICE` by WorkManager. The release merged manifest was regenerated from the current tree during validation on 2026-07-19; reinspect the manifest packaged in the final signed bundle. The app does not declare broad photo-read permission and uses Android's Photo Picker for input.

## Data Safety Working Answers

These are repository-grounded preparation notes, not a submitted Play Console form. Reconfirm them against the exact signed AAB and final AdMob/UMP configuration.

| Play data category | Collected | Shared | Basis/purposes |
| --- | --- | --- | --- |
| Approximate location | Yes | Yes | Google Mobile Ads automatically handles IP-derived approximate location when the SDK is used. |
| App activity → App interactions | Yes | Yes | App launches, taps, and video views handled by Google Mobile Ads. |
| App info and performance → Diagnostics | Yes | Yes | App launch time, hang rate, energy usage, and related SDK/app diagnostics. |
| Device or other IDs | Yes | Yes | Advertising ID, App Set ID, and applicable account/device identifiers. |
| Photos and videos | No, based on current implementation | No | App code processes selected/generated images locally and does not pass image content, filenames, EXIF, or content URIs to the ad SDK or a developer backend. |

Google describes the automatic collection and sharing of these four categories collectively as supporting advertising, analytics, and fraud-prevention purposes. Map those to the Play form's available purpose labels and account for the final app/AdMob/UMP configuration. Google states that Mobile Ads data is encrypted in transit using TLS. Collection of particular identifiers and the ads delivered can depend on device, region, consent, and Google settings. Do not mark the app as collecting no data merely because it has no developer backend. Official references:

- <https://developers.google.com/admob/android/privacy/play-data-disclosure>
- <https://support.google.com/googleplay/android-developer/answer/10787469>

Additional form facts visible in source:

- The app has no account creation or login.
- App History is local; its DataStore is excluded from configured cloud backup and device transfer.
- Users can clear individual/all History records, clear Android app storage/cache, and delete saved gallery files through the gallery/file manager. Those actions do not control Google's retention.
- No independent security-review certification is present in the repository.
- The app contains ads and uses Advertising ID-related SDK permissions.

## Privacy And Local-Data Findings

### Blocker — publisher details and hosting

`PRIVACY_POLICY.md` and the in-app policy now describe the current implementation, GMA 25.4 automatic collection/sharing, TLS, UMP 4.0, the four ad formats, current interstitial rules, local History/cache behavior, persisted URI access, legacy API 28 save permission, the on-device model, and the lack of accounts. Before publication, the publisher must still add:

1. exact legal publisher/developer or entity name;
2. direct monitored privacy contact;
3. an active, public, non-geofenced, non-editable HTTPS policy webpage URL (not a PDF) used in Play Console.

Keep the hosted policy, repository policy, in-app policy, Data Safety answers, and actual SDK behavior synchronized. User Data policy: <https://support.google.com/googleplay/android-developer/answer/10144311>.

### Warning — residual URI and cache retention

- Selected content URIs may receive persisted read grants through `takePersistableUriPermission`; no `releasePersistableUriPermission` call exists when History is cleared.
- Removing a History record attempts to delete only its referenced app-owned temporary output under `cache/processed` or `cache/background_removal`.
- Files dropped when History exceeds 200 entries, files from failed/unrecorded workflows, and other orphaned cache files are not swept by Clear History.
- Saved MediaStore/gallery images are intentionally not deleted by Clear History.

The policy now discloses these limits. A stronger release should release no-longer-needed URI grants and sweep verified app-owned orphan cache files.

## Release Artifact And Verification Freshness

| Evidence | Current result | Freshness |
| --- | --- | --- |
| Locale generation | `:app:generateDebugLocaleConfig` passes | Reproduced 2026-07-19 after pseudo locales were moved out of production locale filters |
| `app-release.aab` | 58,453,852 bytes; `jarsigner -verify` reports `jar is unsigned` | Generated 2026-07-19 14:36 local time and older than current source/configuration; not an upload artifact |
| Unit tests | 51 tests, 0 failures/errors | Reproduced 2026-07-19 against the current tree |
| Connected tests | Last available report: one screenshot-capture test passed on an SM-S928B | Predates current source and is not full functional release coverage |
| Release lint | Passed with 37 warnings | Reproduced 2026-07-19 with an isolated `--no-daemon` run; an earlier combined-run attempt hit a transient Android Lint/UAST service crash |
| Release merged manifest | Available for inspection | Regenerated from the current tree on 2026-07-19; recheck the manifest in the final signed bundle |

These passing checks do not make the old unsigned AAB release-ready or replace final-device and Play Console verification.

## 16 KB Page-Size Status

**Static pass on the existing AAB:** all 16 packaged native libraries across `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64` have ELF `LOAD` alignment `2**14` (16 KB). This includes ONNX Runtime, its JNI bridge, DataStore's shared-counter library, and AndroidX graphics path.

**Runtime still required:** the attached SM-S928B reports `PAGE_SIZE=4096`. Run background removal, preview, export, save, and reopen on a real or emulated 16 KB page-size device using the fresh signed candidate. Official guidance: <https://developer.android.com/guide/practices/page-sizes>.

## Model Provenance And Notices

**Repository evidence passes the current audit.** The packaged U2-NetP file is 4,574,861 bytes with SHA-256 `309c8469258dda742793dce0ebea8e6dd393174f89934733ecc8b14c76f4ddd8`. `legal/MODEL_PROVENANCE.md` records a pinned public artifact, byte-identical cross-check, source/distribution chain, and Apache-2.0/MIT notices. The existing AAB contains the same hash and packages the model/license notices.

This resolves the earlier undocumented-local-conversion finding. It is repository provenance evidence, not independent legal advice; the publisher remains responsible for confirming distribution rights.

## Store Listing And Asset Status

**Pass:** `play-store/store-listing.md` contains a 16/30-character title, 66/80-character short description, a full description within the 4,000-character limit, accurate on-device/ads/legacy-storage disclosures, and a publisher-owned field checklist.

**Pass:** prepared upload graphics have valid dimensions and color modes:

- app icon: 512×512 RGBA PNG;
- feature graphic: 1024×500 RGB PNG;
- eight phone screenshots: 1560×3120 RGB PNG, 1:2 ratio.

Upload the files in `play-store/upload-phone-screenshots/` in filename order and use `play-store/screenshots/captions.md` as the mapping source.

### Localization warning

The selector and production Gradle filters advertise many locale tags, but only a subset currently has dedicated translated `strings.xml`; other choices fall back to default English. Debug pseudo locales are enabled separately and locale-config generation now passes. On 2026-07-19, the repository localization checker failed with 77 errors: 61 long strings identical to English (primarily accurate but untranslated privacy/ad fallbacks), 14 protected-token changes across Kannada and Malayalam, and missing Odia and Urdu resource files. Publish localized store listings only for translations reviewed against actually packaged resources, and re-run the checker against the final tree.

## Publisher-Owned Play And AdMob Checklist

All items below remain unchecked unless the publisher supplies external evidence.

- [ ] Replace the privacy-policy publisher/contact placeholders and host the policy as an active, public, non-geofenced, non-editable HTTPS webpage (not a PDF); enter the same URL in Play Console.
- [ ] Complete the Data Safety form using the final signed bundle and GMA/UMP disclosures above.
- [ ] Declare **Contains ads: Yes**.
- [ ] Complete the Advertising ID declaration for the Google Mobile Ads use case.
- [ ] Declare app access accurately; current source has no login or restricted feature path.
- [ ] Select target age groups. The current policy says the app is general-purpose and not directed to children under 13; if the chosen audience changes, reassess Families requirements, ad serving, consent, listing, and privacy copy.
- [ ] Complete the IARC content-rating questionnaire with advertising, user-selected media, sharing, and external open/share behavior represented accurately.
- [ ] Complete the current Health apps, financial features, news, government, and other App content declarations with truthful “not applicable/no” answers only after the publisher verifies them.
- [ ] Complete the COVID-19 contact-tracing/status App content declaration. Current source has no such feature; select **No** only after verifying the final build: <https://support.google.com/googleplay/android-developer/answer/9859455>.
- [ ] Confirm Play's photo/video permission form does not require a declaration for this final manifest; input uses Photo Picker and no broad read permission is declared.
- [ ] Review any foreground-service/permission declaration prompts generated from the final merged manifest; do not describe an app-owned FGS unless the final implementation actually uses one.
- [ ] Register/link `com.rameshta.photocompressor` in Play Console and AdMob; verify every production ad unit belongs to the intended publisher account.
- [ ] Configure and publish required European-regulations and applicable US-state privacy messages in AdMob/UMP.
- [ ] Publish and verify `app-ads.txt` on the final developer website; review AdMob seller-information visibility.
- [ ] Add only test devices/test ads during QA; do not click production ads.
- [ ] Create/confirm Play App Signing and an upload key; keep keystore/passwords out of Git and CI logs.
- [ ] Confirm `versionCode 1` is unused for this Play app, then build and sign a fresh AAB after all source changes.
- [ ] Add release notes for the exact uploaded version and choose countries/regions, pricing, category, tags, and contact details.
- [ ] Complete the testing track and production-access requirements shown for this developer account; requirements can differ by account type and creation date.
- [ ] For a new personal account, complete physical-device verification in the Play Console mobile app on an eligible physical, non-rooted Android 10-or-later device if Play Console requires it: <https://support.google.com/googleplay/android-developer/answer/14316361>.
- [ ] Run Play pre-launch report, device-catalog review, automated policy checks, and staged rollout monitoring.
- [ ] Complete the Play developer-account/profile requirements: verify the contact email and phone and the public developer email. For an organization account, also verify the public developer phone, organization phone and website, D-U-N-S number, and linked payments profile where applicable: <https://support.google.com/googleplay/android-developer/answer/10841920>.
- [ ] Complete Android developer verification for `com.rameshta.photocompressor`, including publisher identity, package registration, and signing-key ownership. The March 2026 guide describes the September 2026 certified-device rollout beginning in Singapore, Thailand, Brazil, and Indonesia: <https://developer.android.com/developer-verification/guides/pdf-guides/pdc-guide.pdf>.

## Rebuild And Verification Commands

Run these after fixing ad placements, completing final documents/translations, and clearing the localization checker:

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:lintRelease
./gradlew :app:assembleDebug :app:bundleRelease
./gradlew :app:connectedDebugAndroidTest

jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
shasum -a 256 app/build/outputs/bundle/release/app-release.aab
```

Then inspect the final generated manifest and bundle contents:

```bash
rg -n "uses-permission|AD_ID|AD_SERVICES|WAKE_LOCK|FOREGROUND_SERVICE" \
  app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml

unzip -l app/build/outputs/bundle/release/app-release.aab \
  'base/lib/*/*.so' 'base/assets/models/*' 'base/assets/*LICENSE*' 'base/assets/*NOTICE*'

unzip -p app/build/outputs/bundle/release/app-release.aab \
  base/assets/models/u2netp.onnx | shasum -a 256

adb shell getconf PAGE_SIZE
```

For the final signed candidate, also use the current official `bundletool` to validate the bundle/build device APKs, verify APK signing with `apksigner`, check 16 KB ZIP alignment with `zipalign -P 16`, inspect every native library's ELF `LOAD` alignment, and execute the complete image/background/ad/consent test matrix on 4 KB and 16 KB devices.

## Production Gate

Change the verdict only after all blockers are closed, a fresh bundle is built and signed from the reviewed source, automated/manual verification is current, and every publisher-owned Console/AdMob declaration has evidence. Until then, use the build only for development or test tracks configured with safe ad testing.
