# Google Play Release Audit

Review date: **2026-07-20**

App: **Photo Compressor**

Proposed Play Store title: **Photo Compressor & BG Remover**

Package: `com.rameshta.photocompressor`

Scope: current working tree, publishing documents and assets, regenerated manifests/reports, and a separate inspection of `app/build/outputs/bundle/release/app-release.aab`. The available AAB predates the latest working-tree changes and is not treated as the release candidate.

This document supersedes the earlier historical audit. It records the state visible on the review date; it is not proof of any setting in Play Console, AdMob, DNS, a hosting provider, or the publisher's signing system.

## Verdict

**Not ready for production upload.**

The direct Save, Share, Open, Remove, and Clear History actions remain ungated, and the custom native layout renders both localized ad attribution and AdChoices. The former History-entry interstitial has been moved to Back from a nonempty `HistoryUiState.Content` after actual session engagement through a list scroll or History-item interaction: an eligible loaded ad is shown before pop/navigation while History remains visible, then navigation continues from its completion callback; populated enter-and-immediate-Back and empty, loading, or error exits do not offer it. A stable, non-cancelled completed batch summary with at least three result rows and at least one success can now add a final inline native item after substantive per-image results; active, short, all-failed, and cancelled batches are excluded, and the placement is hidden during full-screen ads. These changes improve the general unexpected-ad, obstruction, and accidental-click posture but still require rendered-flow validation and are not a guarantee of Play approval. Multiple ad placements require a separate, conditional Families-policy analysis based on the publisher's truthful target-audience selection. Target SDK, model provenance, production AdMob identifiers, and on-device image processing remain in good shape; localization and native-speaker review must be current before publishing localized listings.

| Status | Finding |
| --- | --- |
| **Conditional blocker / publisher decision** | Home, Progress, Result, Background, and History can each render more than one ad placement on a page; eligible completed Progress can combine top and bottom banners with a final native item. Google's prohibition on multiple ad placements on a page is a **Families** ad-format requirement, not an unconditional general Made for Ads rule. It applies if the app targets children and, for a mixed audience, when ads are served to children or users of unknown age. The publisher must select the target audience truthfully and apply the corresponding Families ad-serving and layout treatment before production. |
| **Conditional blocker / publisher decision** | If any selected audience is treated as children under applicable law, the current build also lacks a neutral age screen and child-directed/under-age ad-request treatment, declares Advertising ID access, and includes app-open ads, which AdMob says are ineligible for Families-compliant apps. The target-audience decision therefore affects the entire ad implementation, not only banner density. |
| **Resolved / validate** | The History interstitial is no longer offered on entry. Back from nonempty `HistoryUiState.Content` can offer it only after the user scrolled the list or interacted with a History item; it appears before pop/navigation while History remains visible, with navigation continuing from the completion callback. Populated enter-and-immediate-Back and empty/loading/error exits bypass it. Validate both Back paths, engagement tracking, no-fill/failure callbacks, rotation, and rapid repeated input on devices before release. |
| **Resolved / validate** | The custom native-ad layout includes AdChoices and a visible localized “Ad” attribution badge with a 15 dp minimum size. Its new completed-batch opportunity is a final inline list item after substantive per-image results and requires a stable, non-cancelled summary with at least three rows and one success. Active, short, all-failed, and cancelled batches are excluded, and it is hidden during full-screen ads. Validate Home, History, and completed-batch renderings with live test ads and Google's native-ad validator. |
| **Blocker** | The privacy policy still needs the publisher's verified legal/entity name, a direct monitored privacy contact, and an active, public, non-geofenced, non-editable HTTPS webpage URL (not a PDF). |
| **Blocker** | The available local AAB predates later Kotlin/resource changes and is unsigned. It is not an upload artifact until a fresh bundle is built from the final reviewed tree, signed with the publisher's production upload key, and reverified. |
| **Asset action** | Default English Home screenshots 01–02 contain the previous Home copy, and History screenshot 08 predates the new ad layout. Recapture all three before upload. |
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
| Adaptive banners | Top, bottom, Home empty-space, and Result empty-space placements are eligible. Home, Progress, Result, Background, and History enable both top and bottom banners. Multiple placements are a conditional Families-policy blocker if the app targets children or ads are served to child/unknown-age users; they are not an unconditional general Made for Ads violation. |
| Native | One inline native-ad opportunity is used on Home. History adds one after its first item or below its empty state. A stable, non-cancelled completed batch with at least three result rows and at least one success adds one final inline native item after substantive per-image results; active, short, all-failed, and cancelled batches are excluded, and the item is hidden during a full-screen ad. The shared layout includes localized ad attribution and AdChoices. |
| Interstitial | Eligible when the user presses Back from a nonempty `HistoryUiState.Content` after scrolling the list or interacting with a History item, and at a completed-workflow transition after a successful result is available. The History ad is offered before pop/navigation while History remains visible and navigation continues from its completion callback; populated enter-and-immediate-Back and empty/loading/error exits bypass it. Open, Share, Remove, Clear History, and Save actions themselves run directly. Both placements share a global minimum interval of three minutes and maximum of three shown interstitials per app-process session. |
| App-open | Loaded after consent/SDK readiness and considered on foreground returns, with suppression around active processing, another full-screen ad, picker/share/open/privacy/locale transitions, and recent full-screen-ad return. |

The current `InterstitialPolicyConfig` has `successfulActionsRequired = 0` and `suppressFirstSessionAd = false`. The first qualifying History Back action or completed workflow can therefore be an eligible opportunity, subject to the shared cooldown/session cap and ad readiness. The navigation layer guards the History Back flow against duplicate requests and suppresses duplicate handling of the same workflow result key.

### Advertising status, blockers, and warnings

1. **Resolved / validate — History interstitial timing.** `PhotoCompressorApp.kt` no longer offers a full-screen ad when History opens. It considers the opportunity only when the user presses Back from a nonempty `HistoryUiState.Content` after actual session engagement through a list scroll or History-item interaction. If eligible and loaded, the ad appears before pop/navigation while the History content remains visible and Back navigation continues from the ad completion callback; populated enter-and-immediate-Back and empty, loading, or error exits continue without an ad. AdMob's disallowed-interstitial guidance says not to place more than one interstitial after every two user actions and explicitly applies that rule to Back; the engagement action plus Back provides at least two actions before this opportunity, in addition to the shared three-minute cooldown and session cap. This rationale improves the placement posture but does not establish approval. The change also places the opportunity at an end-of-content navigation break and avoids beginning-of-segment presentation, but final policy acceptance depends on the exact rendered flow. Test toolbar and system Back, both engagement paths, ad dismissal/failure/no-fill, rotation, process recreation, and rapid repeated input against the official guidance before production: <https://support.google.com/admob/answer/6201362?hl=en>, <https://support.google.com/googleplay/android-developer/answer/9857753>, and <https://support.google.com/googleplay/android-developer/answer/12271244>.

2. **Conditional Families blocker — multiple placements.** Home, Progress, Result, Background, and History can each render top and bottom banners; Home, Result, and History can add another inline/empty-space ad opportunity, and an eligible completed Progress summary can add the final native placement. Google's rule prohibiting multiple ad placements on a page is in the Families Ads and Monetization requirements. It applies to a children-only audience and, for a mixed audience, when serving ads to children or users of unknown age. The target-audience selection is not available in this repository. If the publisher truthfully targets only age groups that do not include children under applicable local laws, this specific Families format rule does not apply; the general density, obstruction, deceptive-ad, and accidental-click requirements still do. If children are included or child/unknown-age users are served ads, the top/bottom/native combination on completed Progress and the other multi-placement screens are prohibited and require a different Families-compliant ad treatment before production: <https://support.google.com/googleplay/android-developer/answer/9893335> and <https://support.google.com/googleplay/android-developer/answer/9867159>.

   The Families consequence is broader than layout. Current source has no neutral age screen and builds an untagged Google Mobile Ads `RequestConfiguration`; the merged manifest includes Advertising ID access, and the app retains app-open ads. For a mixed audience, child and unknown-age users require appropriate age screening, child-directed/under-age treatment, non-personalized and age-appropriate ads from eligible self-certified SDK sources, applicable content-rating controls, and prevention of prohibited identifier transmission. AdMob states that apps complying with Google Play's Families Policy are not eligible for app-open ads. Therefore the present ad inventory can remain unchanged only if the publisher's truthful target audience excludes children under applicable local laws; otherwise the current build needs a comprehensive Families implementation before submission. References: <https://support.google.com/googleplay/android-developer/answer/11043825>, <https://support.google.com/googleplay/android-developer/answer/12955712>, and <https://support.google.com/admob/answer/9620632>.

3. **Resolved / validate — native attribution and completed-batch placement.** `NativeAdvancedAd.kt` renders the localized `ad_label` badge at a minimum 15 dp width/height and retains `AdChoicesView`. The completed-batch instance is a final inline list item after substantive per-image results and requires a stable, non-cancelled summary with at least three rows and one success. Active, short, all-failed, and cancelled batches are excluded, and the item is hidden during a full-screen ad. Validate Home, History, and completed-batch states with live test ads, minimum contrast, asset registration, click regions, list scrolling, accessibility, and Google's native-ad validator. Official guidance: <https://support.google.com/admob/answer/6239795> and <https://developers.google.com/admob/android/native/advanced>.

4. **Resolved — direct item/action gating.** Open, Share, Remove, Clear History, and Save actions execute directly. Only a qualifying user-initiated History Back action can offer the History interstitial, and Back navigation continues from its completion callback or immediately when no ad is shown. `WORKFLOW_COMPLETED` remains after a successful result becomes available.

5. **Warning — app-open limits are not implemented.** `AppOpenAdManager.kt` records `coldStart`, `backgroundedAt`, `lastShownAtMs`, and `adsShownThisSession`, but its eligibility path does not use those values. It does not enforce a minimum background duration, a cooldown between app-open impressions, a per-session app-open cap, or an explicit cold-start guard. The three-minute/three-per-session rules apply only to interstitials. Current privacy copy accurately discloses this distinction; the implementation should still add deliberate app-open limits before release.

6. **Warning — dense screens need device review.** Home can combine top/bottom and empty-space banners with its native placement; Result can combine top/bottom and empty-space banners; History can combine top/bottom banners and a native ad; and an eligible completed Progress summary can combine top/bottom banners with its final inline native item. Regardless of target audience, test small screens, large font scale, gesture/three-button navigation, landscape, RTL, active/completed/empty/loading/error states, list scrolling, no-fill, load failure, and full-screen-ad transitions for content obstruction, deceptive presentation, and accidental-click risk.

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

The AdServices and Advertising ID permissions originate from Google Mobile Ads. `WAKE_LOCK` is merged by Google measurement/WorkManager dependencies and `FOREGROUND_SERVICE` by WorkManager. The release merged manifest was validated with the current tree on 2026-07-20; reinspect the manifest packaged in the final signed bundle. The app does not declare broad photo-read permission and uses Android's Photo Picker for input.

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

`PRIVACY_POLICY.md` describes the current implementation, GMA 25.4 automatic collection/sharing, TLS, UMP 4.0, the four ad formats, Home/History/completed-batch native placements, History top/bottom banners, both interstitial opportunities and their shared caps, local History/cache behavior, persisted URI access, legacy API 28 save permission, the on-device model, and the lack of accounts. The authoritative English in-app copy and all translated policy catalogs must be revalidated after the current placement-disclosure update; structural/content checks do not replace native-speaker review. Before publication, the publisher must still add:

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
| Locale validation | Fresh `:app:generateDebugLocaleConfig` and `tools/check_localizations.py` runs passed | Current-tree run reported 25 non-English locales, 253 translated strings, and one plural resource; all 27 base/localized XML catalogs parsed, and `values-id`/`values-in` are byte-identical |
| `app-release.aab` | 59,406,038 bytes; SHA-256 `0e37a7b19ccfd3ca099416d6231d012df675b2cd294b86aa0481488aa042fae9`; `jarsigner -verify` reports `jar is unsigned` | Generated 2026-07-20 01:25 local time; it predates later History, base-string, and native-ad source changes, so it is stale and not an upload artifact |
| Unit tests | Fresh forced run: 64 tests, 0 failures/errors/skips | Covers the current History-exit and completed-batch-native policy helpers; final-device behavior still requires instrumentation |
| Connected tests | Latest available report: four `HistoryScreenTest` tests passed on an SM-S928B | The report is partial and stale relative to a subsequent `HistoryScreenTest.kt` edit; it is not a Hindi Home screenshot run or full functional release coverage |
| Debug/release lint | Fresh forced runs passed with 38 warnings in each report and no errors | Current source/resources; repeat against the exact final release candidate if it changes |
| Release merged manifest | Available for inspection | Validated with the current tree on 2026-07-20; recheck the manifest in the final signed bundle |

These passing checks do not make the unsigned locally generated AAB release-ready or replace final-device and Play Console verification.

## 16 KB Page-Size Status

**Static pass for the inspected stale AAB only:** all 16 packaged native libraries across `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64` have ELF `LOAD` alignment `2**14` (16 KB). This includes ONNX Runtime, its JNI bridge, DataStore's shared-counter library, and AndroidX graphics path. Reproduced 2026-07-20 with `llvm-objdump`; repeat it on the fresh signed candidate.

**Runtime still required:** the attached SM-S928B reports `PAGE_SIZE=4096`. Run background removal, preview, export, save, and reopen on a real or emulated 16 KB page-size device using the fresh signed candidate. Official guidance: <https://developer.android.com/guide/practices/page-sizes>.

## Model Provenance And Notices

**Repository evidence passes the current audit.** The packaged U2-NetP file is 4,574,861 bytes with SHA-256 `309c8469258dda742793dce0ebea8e6dd393174f89934733ecc8b14c76f4ddd8`. `legal/MODEL_PROVENANCE.md` records a pinned public artifact, byte-identical cross-check, source/distribution chain, and Apache-2.0/MIT notices. The inspected stale AAB contains the same hash and packages the model/license notices; confirm the same evidence in the final signed candidate.

This resolves the earlier undocumented-local-conversion finding. It is repository provenance evidence, not independent legal advice; the publisher remains responsible for confirming distribution rights.

## Store Listing And Asset Status

**Pass:** `play-store/store-listing.md` contains the proposed 29/30-character Play title, the current shorter launcher label, a 74/80-character short description, a full description within the 4,000-character limit, version 1.0 release notes, accurate on-device/ads/legacy-storage disclosures, and a publisher-owned field checklist.

**Partial pass:** prepared graphics have valid dimensions and color modes:

- app icon: 512×512 RGBA PNG;
- feature graphic: 1024×500 RGB PNG;
- eight candidate phone screenshots: 1560×3120 RGB PNG, 1:2 ratio;
- two supplemental source captures (language selector and Hindi Home): 1560×3120 RGB PNG, 1:2 ratio; they are currently untracked and must be added to the release source set if retained in the documentation.

Do not upload the full default set yet: screenshots 01–02 contain the previous Home copy, and screenshot 08 predates the new History ad layout. The language-selector capture is not yet in the upload set, and the Hindi Home capture belongs in a Hindi localized listing. Use `play-store/screenshots/captions.md` as the readiness and mapping source.

### Localization status

The searchable in-app selector exposes 26 packaged language or regional options including English, plus System Default. Debug pseudo locales are enabled separately. The current-tree `tools/check_localizations.py` run passed all 25 non-English locale packs with 253 translated strings and one plural resource; all XML catalogs parsed, and the Indonesian alias remains byte-identical. Repeat it after every catalog change. This validates repository structure/content rules only; it is not human linguistic review. Publish localized store listings only after native-speaker review, and use language-matched screenshots when screenshots contain text.

## Production Play And AdMob Checklist

All items below remain unchecked unless the publisher supplies external evidence.

- [ ] Select target age groups truthfully, accounting for the definition of a child under applicable local laws. If children are included, the current build cannot be approved unchanged: implement a neutral age screen where required; apply child-directed/under-age request treatment, AAID restrictions, non-personalized age-appropriate ads, eligible self-certified SDK/source and content-rating controls; apply Families-compliant layout treatment to every multi-placement screen; and resolve AdMob's Families ineligibility for app-open ads. If children are excluded, retain evidence supporting the non-child audience selection. References: <https://support.google.com/googleplay/android-developer/answer/9893335>, <https://support.google.com/googleplay/android-developer/answer/11043825>, and <https://support.google.com/admob/answer/9620632>.
- [ ] Validate the revised History Back interstitial against the general Play Ads policy: nonempty `HistoryUiState.Content` plus a list scroll or History-item interaction, no entry/populated-immediate-Back/empty/loading/error opportunity, History visible while the ad is shown before pop/navigation, callback-driven navigation, and safe toolbar/system Back, no-fill, failure, rotation, and rapid-input behavior.
- [ ] Pass live test-ad/native-validator review for the localized attribution badge and AdChoices on Home, History, and the eligible completed-batch summary; confirm the Progress native is last after substantive per-image results, requires a stable non-cancelled summary with at least three rows and one success, excludes active/short/all-failed/cancelled batches, is hidden during full-screen ads, and is non-obstructive on supported devices.
- [ ] Recapture default English Home screenshots 01–02 and History screenshot 08 from the final UI; keep localized screenshots in matching localized listings.
- [ ] Replace the privacy-policy publisher/contact placeholders and host the policy as an active, public, non-geofenced, non-editable HTTPS webpage (not a PDF); enter the same URL in Play Console.
- [ ] Complete the Data Safety form using the final signed bundle and GMA/UMP disclosures above.
- [ ] Declare **Contains ads: Yes**.
- [ ] Complete the Advertising ID declaration for the Google Mobile Ads use case.
- [ ] Declare app access accurately; current source has no login or restricted feature path.
- [ ] Keep the target-audience selection, Families treatment, store listing, consent configuration, and privacy copy mutually consistent. If ads are served to children or users of unknown age, do not ship the completed Progress top/bottom/native combination or any other prohibited multi-placement page. The definition of a child varies by locale and applicable law, so “not directed to children under 13” does not by itself establish a globally non-child audience or replace Play Console's truthful age-group selection.
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
- [ ] Check Android developer-verification status for `com.rameshta.photocompressor` in Play Console. Google attempts to auto-register eligible new and existing Play apps under its package-name eligibility rules; if this package is not shown as successfully registered, complete the required manual flow. Every Play package must be registered by **September 30, 2026**, and manual registration may require proof of signing-key ownership: <https://developer.android.com/developer-verification/guides/google-play-console> and <https://support.google.com/googleplay/android-developer/answer/16984799>.

## Rebuild And Verification Commands

Run these after closing the ad-policy/publisher decisions and completing final publisher documents:

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

Change the verdict only after the target-audience/Families decision is evidenced, the revised History Back and completed-batch native flows pass policy/device validation, all remaining blockers are closed, Home screenshots 01–02 and History screenshot 08 are recaptured, a fresh bundle is built and signed from the reviewed source, automated/manual verification is current, and every publisher-owned Console/AdMob declaration has evidence. Until then, use the build only for development or internal testing with Google's test ads.
