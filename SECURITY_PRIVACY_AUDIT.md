# Security And Privacy Audit

Review date: **2026-07-19**

App/package: **Photo Compressor — `com.rameshta.photocompressor`**

Scope: current Android source, manifests, local persistence and sharing, image/background-removal paths, Google Mobile Ads/UMP integration, legal/privacy documents, and the latest locally generated release artifact and reports.

This is the current audit and supersedes the former historical snapshot. Google Play publication readiness is tracked in `PLAY_STORE_RELEASE_AUDIT.md`.

## Verdict

**Security/privacy implementation: conditionally acceptable for continued testing.**

**Production publication: blocked.**

No developer backend, credential leak, developer analytics integration, or app-code image upload was found. Image compression, conversion, resizing, comparison, and ONNX background removal operate locally. Important release gaps remain: incomplete publisher identity/contact/hosting, policy-sensitive pre-action interstitials, URI/cache cleanup limitations, missing app-open frequency enforcement, incomplete localization coverage, and an unsigned/stale AAB.

## Status Summary

### Blockers

| ID | Finding | Required resolution |
| --- | --- | --- |
| B-01 | `PRIVACY_POLICY.md` intentionally lacks the verified legal publisher/entity name, direct monitored privacy contact, and public HTTPS policy URL. | Publisher supplies all three values and hosts the final policy as an active, public, non-geofenced, non-editable HTTPS webpage (not a PDF), then uses the same URL/copy in Play Console and in app. |
| B-02 | Save, Share, and Open are performed only after an optional interstitial finishes. | Move full-screen ads away from direct user actions to a genuine natural break and re-review against Google Play Ads policy. |
| B-03 | The available release AAB is unsigned and older than current source. | Build after all changes, sign with the publisher upload key, and reinspect the exact candidate. |

### Warnings

| ID | Finding | Risk/recommendation |
| --- | --- | --- |
| W-01 | App-open manager records but does not enforce cold-start, minimum-background-duration, cooldown, or per-session-limit values. | Add explicit guards. Interstitial limits do not protect app-open frequency. |
| W-02 | Persisted source URI read grants are never released when History is removed/cleared. | Track reference use and call `releasePersistableUriPermission` when no longer needed. |
| W-03 | History deletion removes only referenced app-owned temp outputs; orphaned/unrecorded/evicted cache files can remain. | Add a canonical-path constrained orphan sweep and retention schedule. |
| W-04 | Several content screens can combine multiple banner/native positions. | Review density, accidental-click separation, small screens, accessibility, RTL, and no-fill/failure states using test ads. |
| W-05 | Only a subset of advertised locale choices has dedicated translations; some localized policy strings can lag the authoritative English implementation disclosure. | Offer only completed translations or finish/review every advertised locale, including legal/privacy text. |
| W-06 | Static 16 KB ELF alignment passes, but only a 4 KB runtime device was tested. | Exercise all ONNX/image/export paths on a 16 KB page-size device with the final candidate. |

### Passes

- App-owned image processing is on-device; no app network/backend client or image-upload endpoint was found.
- Android Photo Picker is used for user-selected input; no broad photo-read permission is declared.
- Legacy shared-gallery writes are limited to `WRITE_EXTERNAL_STORAGE` on API 28 and earlier.
- Ad requests are gated by Google UMP/consent readiness and Google Mobile Ads initialization.
- App History DataStore is excluded from cloud backup and device transfer.
- FileProvider exposes only `cache/processed/` and `cache/background_removal/`; no root or external-root path is configured.
- Stored **output** content URIs are restricted to MediaStore or the app FileProvider before use. Original Photo Picker URI authorities are not subject to this output validation.
- Share/open intents use user-triggered Android intent flows and grant URI access rather than exposing raw private paths.
- Output filenames are sanitized and app-owned temp deletion performs canonical containment checks.
- Pinned U2-NetP model provenance, hash, public distribution chain, and license notices are documented and packaged.
- Production AdMob identifiers are present; they are public identifiers rather than secret credentials.
- All native libraries in the inspected AAB use 16 KB ELF `LOAD` alignment.
- Prepared Play listing text, icon, feature graphic, and eight upload screenshots pass the documented character, dimension, and color-mode checks; see `PLAY_STORE_RELEASE_AUDIT.md`.

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

- Adaptive banners: eligible top, bottom, Home empty-space, and Result empty-space placements.
- Native ads: Home, batch Progress, Result, and Background flows.
- Interstitials: History-open opportunity and immediately before Save, Share, and Open actions.
- App-open ads: considered on eligible foreground returns after consent/initialization and load.

Interstitial policy defaults are a zero successful-action threshold, three-minute global minimum interval, maximum three impressions per app-process session, and no first-opportunity suppression. `recordSuccessfulAction()` does not create an effective prerequisite while the threshold is zero.

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

### Direct-action ads policy risk

`PhotoCompressorApp.kt` defers Save, Share, and Open until the optional interstitial's completion callback. These are clear user-requested actions, not post-task natural breaks. This is a production policy blocker even though the app reliably continues after dismissal/failure. Official policy: <https://support.google.com/googleplay/android-developer/answer/9857753>.

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

No broad `READ_EXTERNAL_STORAGE`, `READ_MEDIA_IMAGES`, camera, microphone, contacts, location, phone, SMS, or account permission is declared by app source. The release merged manifest was regenerated from the current tree on 2026-07-19; reinspect the manifest packaged in the final signed bundle.

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

The authoritative English repository/in-app copy now covers:

- local image processing and no developer backend/account;
- Photo Picker, metadata access, persisted read grants, and the legacy API 28 save permission;
- History fields, 200-item cap, backup exclusion, cache retention, deletion limits, and gallery deletion;
- banner/native/interstitial/app-open formats and actual interstitial threshold/caps;
- GMA 25.4 automatic collection/sharing, purposes, TLS, and UMP 4.0;
- bundled on-device U2-NetP/ONNX Runtime behavior;
- children/target-audience consistency.

Remaining publication fields are intentionally explicit placeholders: verified publisher name, direct monitored privacy contact, and hosted HTTPS URL. Complete them before release and keep every translated in-app policy synchronized. Google Play User Data policy: <https://support.google.com/googleplay/android-developer/answer/10144311>.

## Model And Supply-Chain Evidence

Packaged model: `app/src/main/assets/models/u2netp.onnx`

Size: 4,574,861 bytes

SHA-256: `309c8469258dda742793dce0ebea8e6dd393174f89934733ecc8b14c76f4ddd8`

`legal/MODEL_PROVENANCE.md` pins the rembg release artifact, records its byte-identical Apache-2.0-declared model-repository copy and upstream U2-Net lineage, and includes U2-Net/rembg/ONNX Runtime licenses/notices. The inspected AAB contains the same model hash and notices. This resolves the prior undocumented locally converted model finding, subject to the publisher's own legal review.

No private keys, signing keystores, passwords, service-account files, or secret tokens were identified in the reviewed source. AdMob IDs are intentionally public identifiers. Release signing credentials must remain outside Git.

## Artifact, Test, Lint, And 16 KB Evidence

- Existing AAB: `app/build/outputs/bundle/release/app-release.aab`, 58,453,852 bytes, generated 2026-07-19 14:36 local time.
- Locale task: `:app:generateDebugLocaleConfig` passes after pseudo locales were separated from production locale filters; reproduced 2026-07-19. The custom localization-content checker fails with 77 errors: 61 long strings identical to English (primarily accurate but untranslated privacy/ad fallbacks), 14 protected-token changes in Kannada/Malayalam, and missing Odia/Urdu resource files.
- Signing: `jarsigner -verify` reports `jar is unsigned`.
- Freshness: the AAB predates current Kotlin/resources/Gradle changes.
- Unit report: 51 tests passed with zero failures/errors against the current tree on 2026-07-19.
- Connected report: one screenshot-capture test passed on an SM-S928B; it predates current changes and is not a complete security/privacy suite.
- Release lint report: passed with 37 warnings on an isolated `--no-daemon` retry on 2026-07-19. An earlier combined-run attempt crashed inside Android Lint/UAST's service layer rather than reporting an app finding.
- Static 16 KB check: all 16 native libraries in the old AAB have `LOAD` alignment `2**14`.
- Runtime page size: attached SM-S928B reports `4096`; no final-candidate 16 KB runtime result exists.

## Required Verification

After closing the production blockers and localization-content errors:

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

Do not change this audit to production-ready until the exact source passes build/test/lint, the exact AAB is signed and re-audited, direct-action ad placements are remediated, privacy publisher fields and hosting are complete, residual-retention behavior is accepted or fixed, and Play Console/AdMob declarations have publisher-owned evidence.
