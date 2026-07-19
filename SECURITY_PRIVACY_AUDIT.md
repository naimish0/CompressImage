# Security And Privacy Audit

Review date: **2026-07-20**

App/package: **Photo Compressor — `com.rameshta.photocompressor`**

Proposed Play Store title: **Photo Compressor & BG Remover**

Scope: current Android source, manifests, local persistence and sharing, image/background-removal paths, Google Mobile Ads/UMP integration, legal/privacy documents, generated reports, and a separate inspection of the available local release artifact. The available AAB predates the latest working-tree changes and is not treated as the release candidate.

This is the current audit and supersedes the former historical snapshot. Google Play publication readiness is tracked in `PLAY_STORE_RELEASE_AUDIT.md`.

## Verdict

**Security/privacy implementation: conditionally acceptable for continued testing.**

**Production publication: blocked.**

No developer backend, credential leak, developer analytics integration, or app-code image upload was found. Image compression, conversion, resizing, comparison, and ONNX background removal operate locally. Direct result/History item actions remain ungated, native ad attribution has been restored, the History interstitial has been moved from entry to a qualifying user-initiated Back break, and the completed-batch native is isolated from active processing and full-screen ads. These placement changes improve the general unexpected-ad, obstruction, and accidental-click posture but still require rendered-flow validation and do not guarantee Play approval. Important release gaps remain: multi-placement screens require a conditional Families-policy decision based on the publisher's truthful target-audience selection; publisher identity/contact/hosting is incomplete; URI/cache cleanup and app-open frequency limitations remain; localized copy still requires native-speaker review; and the available AAB is stale and unsigned.

## Status Summary

### Blockers and conditional blockers

| ID | Finding | Required resolution |
| --- | --- | --- |
| B-01 | `PRIVACY_POLICY.md` intentionally lacks the verified legal publisher/entity name, direct monitored privacy contact, and public HTTPS policy URL. | Publisher supplies all three values and hosts the final policy as an active, public, non-geofenced, non-editable HTTPS webpage (not a PDF), then uses the same URL/copy in Play Console and in app. |
| B-02 | Home, Progress, Result, Background, and History can render multiple ad placements on one page; eligible completed Progress can combine top and bottom banners with a final native item. | This is a conditional **Families** ad-format blocker, not an unconditional general Made for Ads rule. The publisher must select the target audience truthfully under applicable local laws. If ads are served to children or users of unknown age, the completed Progress top/bottom/native combination and the other multi-placement pages are prohibited and require different Families-compliant ad treatment. |
| B-04 | The available release AAB predates later Kotlin/resource changes and is unsigned. | Build a fresh bundle from the final reviewed tree, sign it with the publisher upload key, and reinspect the exact candidate. |
| B-05 | If the target audience includes children, the current ad implementation lacks a neutral age screen and child/unknown-age request treatment, retains Advertising ID access, and includes app-open ads. | Treat the target-audience decision as a whole-app conditional blocker. A child or mixed audience requires Families-compliant age screening, identifier handling, ad requests/content/sources/formats, and layout; AdMob says Families-compliant apps are ineligible for app-open ads. |

### Resolved since the previous audit

- Open, Share, Remove, Clear History, and Save actions run directly without interstitial gating.
- The native layout now includes localized ad attribution and AdChoices.
- The History interstitial is no longer offered on entry. Only Back from nonempty `HistoryUiState.Content` after a list scroll or History-item interaction can offer it before pop/navigation while History remains visible; navigation continues from the completion callback, and populated enter-and-immediate-Back plus empty/loading/error exits bypass it.
- The completed-batch native is a final inline item after substantive per-image results and requires a stable, non-cancelled summary with at least three rows and one success. Active, short, all-failed, and cancelled batches are excluded, and it is hidden while a full-screen ad is visible.
- The authoritative English and translated ad disclosures now match the current placement behavior and pass repository validation. Native-speaker review remains required.

### Warnings

| ID | Finding | Risk/recommendation |
| --- | --- | --- |
| W-01 | App-open manager records but does not enforce cold-start, minimum-background-duration, cooldown, or per-session-limit values. | Add explicit guards. Interstitial limits do not protect app-open frequency. |
| W-02 | Persisted source URI read grants are never released when History is removed/cleared. | Track reference use and call `releasePersistableUriPermission` when no longer needed. |
| W-03 | History deletion removes only referenced app-owned temp outputs; orphaned/unrecorded/evicted cache files can remain. | Add a canonical-path constrained orphan sweep and retention schedule. |
| W-04 | Home can combine top/bottom and empty-space banners with its native placement; Result can combine top/bottom and empty-space banners; History can combine top/bottom banners with a native ad; and eligible completed Progress can combine top/bottom banners with its final native item. | Regardless of target audience, review density, obstruction, deceptive presentation, accidental-click separation, small screens, accessibility, RTL, scrolling, active/completed states, and no-fill/failure/full-screen transitions using test ads. |
| W-05 | Static 16 KB ELF alignment passes, but only a 4 KB runtime device was tested. | Exercise all ONNX/image/export paths on a 16 KB page-size device with the final candidate. |

### Passes

- App-owned image processing is on-device; no app network/backend client or image-upload endpoint was found.
- Android Photo Picker is used for user-selected input; no broad photo-read permission is declared.
- Legacy shared-gallery writes are limited to `WRITE_EXTERNAL_STORAGE` on API 28 and earlier.
- Ad requests are gated by Google UMP/consent readiness and Google Mobile Ads initialization.
- Qualifying History-Back and completed-workflow interstitials share the global cooldown/session cap; item and file actions remain direct.
- Native ads render localized ad attribution plus AdChoices; the completed-batch opportunity is separated from active progress and full-screen ads.
- App History DataStore is excluded from cloud backup and device transfer.
- FileProvider exposes only `cache/processed/` and `cache/background_removal/`; no root or external-root path is configured.
- Stored **output** content URIs are restricted to MediaStore or the app FileProvider before use. Original Photo Picker URI authorities are not subject to this output validation.
- Share/open intents use user-triggered Android intent flows and grant URI access rather than exposing raw private paths.
- Output filenames are sanitized and app-owned temp deletion performs canonical containment checks.
- Pinned U2-NetP model provenance, hash, public distribution chain, and license notices are documented and packaged.
- Production AdMob identifiers are present; they are public identifiers rather than secret credentials.
- The searchable selector exposes 26 packaged language or regional options including English; current-tree repository validation passes all 25 non-English packs, with native-speaker review still required before localized publication.
- All native libraries in the inspected AAB use 16 KB ELF `LOAD` alignment.
- Prepared Play listing text, icon, feature graphic, and screenshot files pass character/dimension/color-mode checks; default Home screenshots 01–02 and History screenshot 08 require recapture. See `PLAY_STORE_RELEASE_AUDIT.md`.

## Data Flow Inventory

| Data/process | Source | Storage/recipient | Retention/deletion | Network behavior |
| --- | --- | --- | --- | --- |
| Selected image content | Android Photo Picker content URI | Read by app for local processing | Provider grant can persist; current code does not release it when History is cleared | No app-code upload found |
| Selected image metadata | ContentResolver/decoder/EXIF | In-memory and potentially local History | History item/all History, clear app storage; source grant can remain | No app-code upload found |
| Processed/background output | Local image engine and bundled ONNX model | App cache under `processed` or `background_removal` | Referenced temp file is attempted on History removal; OS/cache/storage clear/uninstall; orphans can remain | No app-code upload found |
| Saved image | User-initiated MediaStore save | `Pictures/Photo Compressor` / device gallery | User deletes through gallery/file manager | No app-code upload found |
| Shared/opened image | User action | Chosen external Android app | Controlled by receiving app after URI grant | External app's policy applies |
| History | App result records | App-private DataStore, up to 200 records | Remove one/Clear History/clear storage; excluded from configured backup/transfer | No app-code upload found |
| Background model | Bundled `u2netp.onnx` | App asset/ONNX Runtime | Removed with app | No runtime model download or image upload found |
| Advertising/consent | Google Mobile Ads 25.4.0 and UMP 4.0 | Google and advertising partners under final consent/configuration | Google policies/controls apply | Network required for ads/consent information |

History records may include source/output URIs or paths, names, MIME types/formats, size, dimensions, alpha information, operation/compression/result/warning fields, saved-output reference, and timestamp. The History DataStore stores metadata/references rather than full-resolution image bytes; temporary output bytes are separate cache files.

## Advertising And Consent Review

### Formats and placements

- Adaptive banners: eligible top, bottom, Home empty-space, and Result empty-space placements. Home, Progress, Result, Background, and History enable top and bottom banners.
- Native ads: one eligible inline opportunity on Home, one in History after the first item or below the empty state, and one final inline item after substantive per-image results in an eligible completed batch summary. The completed-batch placement requires a stable, non-cancelled summary with at least three result rows and at least one success; active, short, all-failed, and cancelled batches are excluded, and it is hidden during full-screen ads. The shared layout includes localized ad attribution and AdChoices.
- Interstitials: one opportunity when the user presses Back from nonempty `HistoryUiState.Content` after a list scroll or History-item interaction, and one at the completed-workflow transition after a successful result is available. A loaded eligible History ad appears before pop/navigation while History remains visible, and navigation continues from its completion callback; populated enter-and-immediate-Back and empty/loading/error exits bypass the ad. Open, Share, Remove, Clear History, and Save actions themselves run directly.
- App-open ads: considered on eligible foreground returns after consent/initialization and load.

Interstitial policy defaults are a zero successful-action threshold, three-minute global minimum interval, maximum three impressions per app-process session across both placements, and no first-opportunity suppression. The first qualifying History Back action or completed workflow can therefore be eligible, subject to ad readiness and the shared limits; the navigation layer guards the Back flow against duplicate requests and suppresses duplicate handling of the same result key.

### Consent and failure behavior

`GoogleConsentManager` requests UMP consent information, loads/shows a form when Google reports it is required, and exposes privacy options when required. Ads initialize/preload only when `canRequestAds()` is true. Core image operations do not depend on ad availability, and app actions continue when a full-screen ad is absent or fails.

This is sound failure isolation, but UMP integration is not by itself proof that AdMob privacy messages, partner lists, regional settings, or child-directed treatment are correctly configured; those settings are publisher-owned.

### App-open limiter gap

`AppOpenAdManager` suppresses ads during active processing, consent updates, another full-screen ad, a short return from a full-screen ad, and selected external/locale transitions. However, `coldStart`, `backgroundedAt`, `lastShownAtMs`, and `adsShownThisSession` are not checked in `showIfEligible()`. Consequently:

- there is no explicit cold-start eligibility rule;
- a minimum time in background is not enforced;
- there is no app-open cooldown;
- there is no app-open session cap.

The privacy policy accurately states that interstitial limits do not apply to app-open ads, but a deliberate cap is still recommended for predictable user experience and policy posture.

### Ad-placement policy analysis

`PhotoCompressorApp.kt` no longer offers an interstitial when History opens. It considers the opportunity only when the user presses Back from nonempty `HistoryUiState.Content` after actual session engagement through a list scroll or History-item interaction. If eligible and loaded, the ad appears before pop/navigation while History remains visible and Back navigation continues from its completion callback; populated enter-and-immediate-Back and empty, loading, or error exits continue without it. AdMob's disallowed-interstitial guidance says not to place more than one interstitial after every two user actions and explicitly applies the rule to Back; the engagement action plus Back supplies at least two actions before this opportunity, while the existing three-minute cooldown and session cap add broader frequency limits. This moves the opportunity to an end-of-content navigation break, avoids beginning-of-segment presentation, and follows the cited action-count rationale. The exact rendered flow still requires general Ads-policy and device validation; the implementation change is not a guarantee of Play acceptance: <https://support.google.com/admob/answer/6201362?hl=en>.

Separately, Home, Progress, Result, Background, and History can render multiple ad placements on a page. Eligible completed Progress can specifically combine top and bottom banners with the final native item. Google's prohibition on multiple ad placements on a page is in the **Families Ads and Monetization** requirements; it is not an unconditional general Made for Ads rule. It applies to a children-only audience and, for a mixed audience, when serving ads to children or users of unknown age. The repository cannot establish the Play Console target-audience selection. If the publisher truthfully targets only age groups that do not include children under applicable local laws, this specific Families format rule does not apply, although general density, obstruction, deceptive-ad, and accidental-click requirements still do. If ads are served to children or users of unknown age, the completed Progress top/bottom/native combination and the other multi-placement screens are prohibited and require different Families-compliant ad treatment before production.

The conditional Families gap extends beyond multi-placement layout. No neutral age screen is implemented; `AdsInitializer` applies an empty `RequestConfiguration` without child-directed or under-age tags/content-rating treatment; Advertising ID permissions remain in the merged manifest; and app-open ads remain enabled. A child or mixed audience would therefore require appropriate neutral age screening, prevention of prohibited identifier transmission from child/unknown-age users, non-personalized age-appropriate ads from eligible self-certified SDK sources, content-rating controls, and compliant ad formats. AdMob states that apps complying with Google Play's Families Policy are not eligible for app-open ads. The current ad inventory can remain unchanged only if the publisher's truthful target audience excludes children under applicable local laws.

- <https://support.google.com/googleplay/android-developer/answer/9893335>
- <https://support.google.com/googleplay/android-developer/answer/9867159>
- <https://support.google.com/googleplay/android-developer/answer/11043825>
- <https://support.google.com/googleplay/android-developer/answer/12955712>
- <https://support.google.com/admob/answer/9620632>
- <https://support.google.com/googleplay/android-developer/answer/9857753>
- <https://support.google.com/googleplay/android-developer/answer/12271244>

### Native attribution and direct-action remediation

`NativeAdvancedAd.kt` renders both `AdChoicesView` and the localized `ad_label` badge with a 15 dp minimum dimension. The completed-batch instance is a final inline item after substantive per-image results and requires a stable, non-cancelled summary with at least three rows and one success. Active, short, all-failed, and cancelled batches are excluded, and the item is hidden while a full-screen ad is visible. Validate Home, History, and completed-batch states using live test ads and Google's native-ad validator: <https://support.google.com/admob/answer/6239795> and <https://developers.google.com/admob/android/native/advanced>.

Open, Share, Remove, Clear History, and Save execute directly. Only a qualifying user-initiated History Back action can offer the History interstitial; Back navigation continues from its completion callback or immediately when no ad is shown. `WORKFLOW_COMPLETED` remains after a successful result becomes available. This keeps file/item actions failure-isolated while retaining the History interstitial at a natural navigation break, subject to final rendered-flow validation.

## Google Mobile Ads Data Disclosure

Google's GMA 25.4 disclosure says the SDK automatically collects and shares the following when used:

| Play Data Safety category | Collection/sharing guidance | Examples from Google's GMA 25.4 disclosure |
| --- | --- | --- |
| Location → Approximate location | Mark collected and shared | IP address, which may estimate general device location |
| App activity → App interactions | Mark collected and shared | App launches, taps, and video views |
| App info and performance → Diagnostics | Mark collected and shared | App launch time, hang rate, and energy usage |
| Device or other IDs | Mark collected and shared | Advertising ID, App Set ID, and applicable signed-in-account identifiers |

Google describes this automatic collection and sharing collectively as supporting advertising, analytics, and fraud-prevention purposes. Map those to the Play form's available purpose labels and also account for the final app/AdMob/UMP configuration. Google states that SDK data is encrypted in transit using TLS. Device, region, consent, Google configuration, and identifier availability can affect particular data, but the Data Safety form must still include SDK behavior. References:

- <https://developers.google.com/admob/android/privacy/play-data-disclosure>
- <https://support.google.com/googleplay/android-developer/answer/10787469>

Based on current app code, do not mark user photos/videos as collected or shared merely because they are selected and processed locally. Revisit that answer if any future SDK/backend receives image content, image-derived content, filenames, EXIF, or content URIs.

## Manifest And Permission Review

### App-declared

| Permission | Purpose/status |
| --- | --- |
| `INTERNET` | Google ads and consent network access |
| `ACCESS_NETWORK_STATE` | Ad/network availability |
| `WRITE_EXTERNAL_STORAGE` with `maxSdkVersion=28` | Runtime-requested only for user-initiated save to shared Pictures on Android 9 and earlier |

### Merged from SDKs/dependencies

| Permission | Source/impact |
| --- | --- |
| `com.google.android.gms.permission.AD_ID` | Google Mobile Ads/identifier dependency; requires accurate Advertising ID declaration |
| `ACCESS_ADSERVICES_AD_ID` | Google Mobile Ads AdServices integration |
| `ACCESS_ADSERVICES_ATTRIBUTION` | Google Mobile Ads AdServices integration |
| `ACCESS_ADSERVICES_TOPICS` | Google Mobile Ads AdServices integration |
| `WAKE_LOCK` | Google measurement and WorkManager merge |
| `FOREGROUND_SERVICE` | WorkManager merge; reinspect any Play declaration prompt from final bundle |
| App `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | Signature-level compatibility permission for non-exported dynamic receivers |

No broad `READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`, camera, microphone, contacts, location, phone, SMS, or account permission is declared by app source. The release merged manifest was validated with the current tree on 2026-07-20; reinspect the manifest packaged in the final signed bundle.

## Retention, Deletion, Backup, And URI Findings

### What Clear History currently does

`DataStoreHistoryRepository.remove()`/`clear()` remove record metadata and attempt to delete the referenced output only when it resolves to a regular file canonically inside the app's two approved cache directories. They intentionally do not delete a saved MediaStore item.

### Residual data

- `recordSuccessfulOutput()` retains at most 200 entries but does not delete a temp file when an older entry falls out of the list.
- Failed, cancelled, unrecorded, or otherwise orphaned cache outputs are not enumerated by Clear History.
- Missing-output History entries are filtered from the emitted flow but are not necessarily pruned from the persisted JSON.
- `takePersistableUriPermission()` is attempted for selected content URIs; there is no corresponding release call.

The policy now discloses these limitations. Recommended remediation is a conservative app-owned cache sweep with canonical containment/age checks plus reference-counted release of persisted URI grants.

### Backup and provider passes

- `backup_rules.xml` excludes `datastore/processed_image_history.preferences_pb` from cloud backup.
- `data_extraction_rules.xml` excludes it from cloud backup and device transfer.
- FileProvider exposes only `processed/` and `background_removal/` cache paths.
- Stored output content URI validation permits MediaStore and `${BuildConfig.APPLICATION_ID}.fileprovider` only; it does not restrict original user-selected Photo Picker URI authorities.

## Privacy-Document Consistency

The authoritative English repository/in-app copy covers:

- local image processing and no developer backend/account;
- Photo Picker, metadata access, persisted read grants, and the legacy API 28 save permission;
- History fields, 200-item cap, backup exclusion, cache retention, deletion limits, and gallery deletion;
- Home/History/completed-batch native placements, History top/bottom banners, History-Back and completed-workflow interstitials, app-open behavior, and actual shared interstitial threshold/caps;
- GMA 25.4 automatic collection/sharing, purposes, TLS, and UMP 4.0;
- bundled on-device U2-NetP/ONNX Runtime behavior;
- children/target-audience consistency.

Remaining publication fields are intentionally explicit placeholders: verified publisher name, direct monitored privacy contact, and hosted HTTPS URL. Complete them before release. The current `tools/check_localizations.py` run passes all 25 non-English packs after synchronizing the placement disclosures; rerun it after future catalog changes and obtain native-speaker review. Google Play User Data policy: <https://support.google.com/googleplay/android-developer/answer/10144311>.

## Model And Supply-Chain Evidence

Packaged model: `app/src/main/assets/models/u2netp.onnx`

Size: 4,574,861 bytes

SHA-256: `309c8469258dda742793dce0ebea8e6dd393174f89934733ecc8b14c76f4ddd8`

`legal/MODEL_PROVENANCE.md` pins the rembg release artifact, records its byte-identical Apache-2.0-declared model-repository copy and upstream U2-Net lineage, and includes U2-Net/rembg/ONNX Runtime licenses/notices. The inspected stale AAB contains the same model hash and notices; confirm them again in the final signed candidate. This resolves the prior undocumented locally converted model finding, subject to the publisher's own legal review.

No private keys, signing keystores, passwords, service-account files, or secret tokens were identified in the reviewed source. AdMob IDs are intentionally public identifiers. Release signing credentials must remain outside Git.

## Artifact, Test, Lint, And 16 KB Evidence

- Inspected AAB: `app/build/outputs/bundle/release/app-release.aab`, 59,406,038 bytes, generated 2026-07-20 01:25 local time; SHA-256 `0e37a7b19ccfd3ca099416d6231d012df675b2cd294b86aa0481488aa042fae9`. It predates later History, base-string, and native-ad source changes and is therefore stale.
- Locale validation: fresh `:app:generateDebugLocaleConfig` and `tools/check_localizations.py` runs passed on the current tree and reported 25 non-English locales, 253 translated strings, and one plural resource; all 27 base/localized XML catalogs parsed, and `values-id`/`values-in` are byte-identical.
- Signing: `jarsigner -verify` reports `jar is unsigned`.
- Unit report: a fresh forced run passed 64 tests with zero failures/errors/skips, including the current History-Back and completed-batch-native policy helpers; debug Android-test Kotlin compilation also passed.
- Connected report: four `HistoryScreenTest` tests passed on an SM-S928B. The report predates a subsequent `HistoryScreenTest.kt` edit, is partial, and is not a Hindi Home screenshot-capture run or a complete security/privacy suite.
- Debug and release lint reports: fresh forced current-tree runs completed with 38 warnings each and zero errors.
- Static 16 KB check: all 16 native libraries in the inspected stale AAB have ELF `LOAD` alignment `2**14`; reproduced with `llvm-objdump` on 2026-07-20 and requiring repetition on the final signed candidate.
- Runtime page size: attached SM-S928B reports `4096`; no final-candidate 16 KB runtime result exists.

## Required Verification

After closing the production blockers/publisher decisions and completing final publisher documents:

```bash
./gradlew :app:testDebugUnitTest :app:lintDebug :app:lintRelease
./gradlew :app:assembleDebug :app:bundleRelease
./gradlew :app:connectedDebugAndroidTest

jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
shasum -a 256 app/build/outputs/bundle/release/app-release.aab

rg -n "uses-permission|AD_ID|AD_SERVICES|WAKE_LOCK|FOREGROUND_SERVICE" \
  app/build/intermediates/merged_manifest/release/processReleaseMainManifest/AndroidManifest.xml

unzip -p app/build/outputs/bundle/release/app-release.aab \
  base/assets/models/u2netp.onnx | shasum -a 256

adb shell getconf PAGE_SIZE
```

For the exact signed candidate, additionally validate with current `bundletool`, verify derived APK signatures with `apksigner`, check ZIP alignment with `zipalign -P 16`, inspect ELF `LOAD` alignment for every `.so`, and run on-device tests for Photo Picker grants, API 24–28 save permission/denial, MediaStore save, share/open grants, History remove/clear, process restart, cache loss, consent states, no-fill/offline ads, full-screen-ad concurrency, locale recreation, and background removal on both 4 KB and 16 KB devices.

## Release Decision Rule

Do not change this audit to production-ready until the target-audience/Families decision is evidenced, the revised History Back and completed-batch native flows pass policy/device validation, localized disclosures pass verification, native rendering is validated, a fresh exact AAB is signed and re-audited, privacy publisher fields and hosting are complete, residual-retention behavior is accepted or fixed, and Play Console/AdMob declarations have publisher-owned evidence.
