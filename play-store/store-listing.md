# Play Store Listing

## Default English Metadata

Play Store title (29/30 characters):
`Photo Compressor & BG Remover`

Launcher and in-app label:
`Photo Compressor & BG Remover`

The Play Store title and current Android application label now use the same
verified application name.

Short description (74/80 characters):
`Compress photos to KB/MB, resize, convert formats, and remove backgrounds.`

The title and short-description counts above were verified as Unicode character counts.

## Assets

App icon:
`play-store/assets/app-icon-512.png` — 512 x 512, 32-bit RGBA PNG.

Feature graphic:
`play-store/assets/feature-graphic-1024x500.png` — 1024 x 500, 24-bit RGB PNG.
It still displays the former shorter `Photo Compressor` brand. Replace or
recapture this graphic with the verified application name before publication;
the existing asset is retained in this repository update because changing
store artwork is outside the privacy-policy task.

Candidate phone screenshots:
`play-store/upload-phone-screenshots/` — eight 1560 x 3120, 24-bit RGB PNGs
with a 1:2 aspect ratio. Screenshots 03–07 match the current UI. Screenshots
01–02 contain the previous Home intro copy, and screenshot 08 predates the new
History ad layout. Recapture all three from the final build before upload.

Additional current local source captures (currently untracked):

- `play-store/screenshots/11-language-screen.png` — English language selector;
  not yet included in the eight-file upload set.
- `play-store/screenshots/12-home-hindi.png` — Hindi Home screen; reserve for a
  Hindi localized listing rather than mixing it into the default English set.

Add these two supplemental captures to version control if they are retained as
release evidence or listing sources. Do not rely on untracked local files in a
release handoff.

## Full Description

Photo Compressor & BG Remover helps you reduce image size, resize photos, convert image formats, and remove or replace backgrounds from selected images.

Select one image or batch-process up to 50 images. Pick a target size in KB or MB, choose a quality mode, resize by percentage or custom dimensions, keep the aspect ratio when needed, and export as JPG, PNG, or WEBP. The result screen lets you compare the original and processed image before saving, sharing, or opening the final file.

Key features:

- Compress a single image or a batch of up to 50 images.
- Set custom target size in KB or MB.
- Resize by preset percentage or custom width and height, with aspect-ratio and upscaling controls.
- Convert output format to JPG, PNG, or WEBP.
- Compare original and processed results.
- Save, share, or open processed images.
- Remove image backgrounds on your device.
- Export transparent PNG or WEBP images, or add a color and export as PNG, WEBP, or JPG.
- View recent compressed and background-removed images in History.
- Search and choose from 26 packaged language or regional options, or follow the system default.

Your selected images and generated results are processed on your device by app code. The app does not upload image content for compression, conversion, resizing, comparison, or background removal. Android's system photo picker lets you choose specific images without giving the app read access to your complete photo library.

On Android 9 and earlier, Android may ask for storage write access when you save a generated image to the shared Pictures collection. Android 10 and later use the system media collection without that broad storage permission.

This app contains ads. When consent and device settings permit ad requests, Google Mobile Ads may display top, bottom, and eligible inline banner ads within app screens; inline native ads on Home, in History, and as the final item in a stable, non-cancelled batch summary with at least three results and one success; interstitial ads when leaving an engaged, loaded non-empty History view with Back and at eligible completed-workflow transitions; and app-open ads on eligible returns from the background. The completed-batch native ad is excluded from active, short, all-failed, and cancelled batches and is hidden during full-screen ads. A History interstitial is considered only after the user scrolled the non-empty History list or interacted with an item. A loaded eligible ad appears before the app leaves History, with navigation continuing after the ad; History entry, populated enter-and-immediate-Back, and empty, loading, or error exits do not offer it. Open, Share, Remove, Clear History, and Save actions themselves are not gated by interstitial ads. An internet connection may be used to retrieve advertisements and advertising consent information. Core image selection and processing work on the device.

Photo Compressor & BG Remover is built for quick everyday image cleanup, smaller sharing files, product photos, documents, social posts, and background removal workflows.

Full description character count: **3,044/4,000**.

## Version 1.0 Release Notes

`Compress single photos or batches to a target KB/MB size, resize and convert JPG, PNG, or WEBP, compare results, and remove or replace backgrounds on-device. Includes local History and an in-app selector with 26 language or locale options.`

Use this text only for a version 1.0 bundle that contains the features above.
Recheck it against the exact signed artifact before rollout.

## Privacy Policy

The authoritative policy text is `PRIVACY_POLICY.md`, and the synchronized
hosted-page source is `docs/privacy-policy.html`. They contain the verified
publisher name, privacy contact, application name, and 13+ intended audience.
Host the HTML page as an active, publicly accessible, non-geofenced,
non-editable HTTPS webpage (not a PDF), and paste that URL into Play Console.
Keep the hosted copy, repository copy, in-app policy, Data safety form, and
actual SDK behavior consistent.

## Publisher-Owned Fields

Do not submit placeholders for these values:

| Play field | Required value |
| --- | --- |
| Legal publisher/developer name | **Naimish Gupta** |
| Support email | **naimish.app@gmail.com** |
| Website | **TODO — publisher must provide the final public website URL.** |
| Privacy policy URL | **TODO — publisher must host the finalized policy as an active, publicly accessible, non-geofenced, non-editable HTTPS webpage (not a PDF).** |
| App category and tags | **TODO — publisher must select the accurate category and tags in Play Console.** |
| Target audience | **Ages 13–15; Ages 16–17; Ages 18 and over.** The current ad implementation must be reviewed and updated before this declaration is used in production wherever these groups are treated as children under applicable law or Google Play policy. |

## Play Console Publication Checklist

- [ ] **Data safety:** account for Google Mobile Ads collection and sharing of approximate location, app interactions, diagnostics, and device or other identifiers. Declare the applicable advertising or marketing, analytics, and fraud prevention, security, and compliance purposes, and indicate encryption in transit. Verify retention and deletion answers against Google and the final privacy policy. Do not declare that no data is collected merely because image processing remains on-device.
- [ ] **App access:** verify that the submitted build has no login or restricted areas, then complete the App access declaration and add instructions if any restricted path is introduced.
- [ ] **Contains ads:** declare **Yes**; release builds enable Google Mobile Ads.
- [ ] **Advertising ID:** complete the Advertising ID declaration for the Google Mobile Ads integration and confirm the merged release manifest before upload.
- [ ] **Target audience and content:** configure Ages 13–15, Ages 16–17, and Ages 18 and over only after resolving the current advertising mismatch. Ages 13–15 and 16–17 can be treated as children in some locales. The current build has no neutral age screen or child/teen ad-request treatment, retains advertising-identifier access, uses multiple ad placements on some screens, and includes app-open ads. Apply all required Families/age treatment before production and answer every resulting Families/ads question truthfully.
- [ ] **Content rating:** submit a new questionnaire that describes image processing, user-selected media, sharing, and advertising accurately.
- [ ] **Health apps declaration:** verify the release has no health features, then submit the applicable no-health-features declaration.
- [ ] **COVID-19 declaration:** verify the release has no COVID-19 contact-tracing or status functionality, then submit the applicable no-functionality declaration.
- [ ] **Release notes:** write version-specific notes for the exact signed bundle being uploaded; do not reuse implementation notes from another build.
- [ ] **AdMob and app-ads.txt:** configure required privacy messages, publish app-ads.txt on the final developer website, and verify its status in AdMob.
- [ ] **Signing and artifact currency:** the currently generated AAB predates newer working-tree source and resource changes and is unsigned, so it is not the final release candidate. Build a fresh AAB after all source changes, sign it with the production upload key, and verify that exact artifact before upload.
- [ ] **Play developer account/profile:** verify the Play contact email and phone and the public developer email. For an organization account, also verify the public developer phone, organization phone and website, D-U-N-S number, and linked payments-profile details where applicable.
- [ ] **Android developer verification:** verify the developer identity and check the package-registration status for `com.rameshta.photocompressor` on the Android developer verification page in Play Console. Google attempts to auto-register eligible existing and new Play apps; if this package is shown as successfully auto-registered, no further package-registration action is required. Otherwise, complete manual registration and signing-key ownership proof when requested. Every Play package must be registered by **September 30, 2026**; an unregistered Play package is subject to removal after that deadline.
  Official reference: [Registering Play package names](https://support.google.com/googleplay/android-developer/answer/16984799?hl=en).
- [ ] **New-personal-account device verification:** if Play Console requires it for this account, use the Play Console mobile app to verify access to a non-rooted physical Android 10-or-later device.
- [ ] **Testing and production access:** complete the tests required for this developer-account type, including any closed-testing duration and tester-count requirement shown by Play Console, before applying for production access.
- [ ] **Store contact and URLs:** enter Naimish Gupta, naimish.app@gmail.com, the final website, and the compliant hosted privacy-policy URL from the table above.
- [ ] **Category and tags:** select and review the final category/tags before rollout.
- [ ] **Final verification evidence:** rerun unit tests, debug and release lint, the localization checker, and the relevant connected tests against the final source tree. Record the exact results and do not treat reports generated before later source changes as final-build evidence.
- [ ] **Localized listings:** all 25 non-English locale packs pass the repository localization checker on the current working tree. Rerun it against the final source tree, publish only store-listing translations that have been human-reviewed against the packaged UI, and provide language-matched screenshots when screenshots contain text.

## Phone Screenshots

Do not upload the complete eight-file candidate set until Home screenshots
01–02 and History screenshot 08 are recaptured from the current UI. The
canonical mapping, readiness state,
supplemental captures, and captions are maintained in
`play-store/screenshots/captions.md`.
