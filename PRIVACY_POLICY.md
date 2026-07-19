# Privacy Policy

Effective date: July 20, 2026
App: Photo Compressor

> **Publisher action required before release**
>
> - **Developer/publisher:** Replace this item with the exact legal developer or
>   entity name used for the Google Play listing.
> - **Privacy contact:** Add a direct, monitored privacy email address or other
>   direct contact method. A reference to the Play listing alone is not enough.
> - **Public policy URL:** Host the completed policy as an active, publicly
>   accessible, non-geofenced, non-editable HTTPS webpage (not a PDF), and enter
>   that URL in Google Play Console. Keep the same current policy in Play and in
>   the app.

The publication fields above are intentionally not populated because the
repository does not contain verified publisher identity, contact, or hosting
information. They must be completed before this policy or the app is published.

## Summary

Photo Compressor is an image utility for selecting, compressing, resizing,
converting, saving, opening, sharing, and removing or replacing image
backgrounds. Image processing, including ONNX background removal, happens on
the device. The app does not create user accounts and does not use a developer
backend to upload or process images. Internet access is used by Google
advertising and advertising-consent services.

## Images And File Access

The app uses Android's system Photo Picker so the user chooses the specific
images the app can access. It reads each selected content URI and local metadata
needed to display and process the image, including the display name, media type,
file size, dimensions, format, transparency information, and EXIF orientation.
When the content provider permits it, the app takes a persisted read permission
so a source referenced by local History can be opened again. The current
implementation does not release that persisted permission when History is
cleared.

App code does not upload selected images, generated images, filenames, content
URIs, EXIF metadata, or image-processing data to Google Mobile Ads or another
server. This statement concerns image handling by app code; Google's automatic
advertising-SDK data practices are disclosed separately below.

Processing creates temporary output files in the app cache. A generated image
is opened in another app or shared through Android intents only after the user
chooses the corresponding action. When the user chooses Save, the app writes
the output through Android MediaStore. Saved images are placed in
`Pictures/Photo Compressor` and remain under the user's control in the device
gallery or file manager.

On Android 9 (API 28) and earlier only, a user-initiated Save requests the
legacy `WRITE_EXTERNAL_STORAGE` permission so the output can be written to the
public Pictures folder. The manifest limits that permission to API 28 and
earlier; it is not requested for Save on Android 10 or later.

## Local History, Retention, And Deletion

The app keeps up to 200 History records in app-private Android DataStore. A
record can include source and output content references or paths, display names,
MIME types and formats, file sizes, dimensions, transparency information,
operation and compression details, result or warning information, a saved
output reference, and a timestamp. History stores metadata and references; the
actual temporary output image file is stored separately in the app cache. History
DataStore is excluded from the app's configured cloud-backup and device-transfer
rules.

Temporary processing outputs may remain in the app cache until Android evicts
them, the user clears the app cache or storage, the app is uninstalled, or a
referenced file is successfully deleted by the History cleanup described below.
Files that are no longer referenced by History may remain in cache until one of
the system-level cleanup events occurs.

- Removing one History item or using Clear History removes the corresponding
  local record and attempts to delete only the app-owned temporary output file
  referenced by that record.
- Clear History does not delete images already saved in the device gallery,
  does not guarantee deletion of orphaned or otherwise unreferenced cache files,
  and does not release persisted read permissions for selected source images.
- Clearing the app's storage through Android settings removes app-local History
  and cache data. Clearing only the cache can remove temporary outputs without
  clearing every stored History record.
- Saved gallery images must be deleted through the gallery or file manager.

There is no app account and therefore no account-deletion process.

## Advertising And Consent

The release configuration uses Google AdMob through Google Mobile Ads SDK
25.4.0. The app implements banner, native, interstitial, and app-open ad formats.

- Banner-ad opportunities may appear at both the top and bottom of Home, batch
  progress, result, background-removal, and History screens. The editor and
  Settings screens can show a bottom banner. Home and result screens can also
  show an additional inline banner opportunity. One inline native-ad
  opportunity can appear on Home, one can appear in History after the first
  item or below the empty state, and one can appear as the final inline item
  after the substantive per-image results in an eligible completed batch
  summary. That placement requires a stable, non-cancelled summary with at
  least three result rows and at least one successful item. It is excluded from
  active, short, all-failed, and cancelled batches and is hidden while a
  full-screen ad is visible. The custom native layout displays localized ad
  attribution and AdChoices.
- Interstitial ads may appear when the user presses Back from loaded, non-empty
  History content after engaging with that session by scrolling the list or
  interacting with a History item, and at completed-workflow transitions after
  a successful result is available. An eligible loaded History interstitial is
  shown before pop/navigation while History remains visible, and Back
  navigation continues from the ad's completion callback. History entry,
  populated enter-and-immediate-Back, and empty, loading, or error exits do not
  offer this interstitial. Open, Share, Remove, Clear History, and Save actions
  themselves remain direct and are not gated by interstitial ads.
- Once an interstitial reports that it has been shown, another interstitial is
  blocked for at least three minutes. No more than three interstitials are shown
  per app-process session across the History-exit and workflow-completion
  placements.
- App-open ads may appear on a foreground return after an ad has loaded and the
  current consent and full-screen eligibility checks pass. The three-minute and
  three-per-session interstitial limits do not apply to app-open ads. The
  current implementation does not impose a separate minimum background time,
  app-open cooldown, or per-session app-open cap.
- History Back navigation and result navigation continue if their optional
  interstitial is unavailable or fails to display. Full-screen ads are
  suppressed while the app marks image processing as active.

Core image processing remains usable if ads fail to load, the device is
offline, or personalized-ad consent is not given.

The app uses Google User Messaging Platform SDK 4.0.0 to request current consent
information, display a consent form when Google reports that one is required,
and offer a privacy-options form when required. Google handles the consent form,
participating advertising partners, and the resulting consent or privacy-choice
signals.

### Data automatically collected and shared by Google Mobile Ads

According to Google's disclosure for Mobile Ads SDK 25.4.0, the SDK
automatically collects and shares these categories when used:

- **Approximate location:** derived from the device IP address.
- **App interactions:** user product interactions such as app launches, taps,
  and ad or video views.
- **Diagnostics:** SDK and app performance information such as app launch time,
  hang rate, and energy usage.
- **Device or other identifiers:** the Android Advertising ID, App Set ID, and,
  when applicable, other identifiers related to accounts signed in on the
  device.

Google states that these data are used for advertising or marketing, analytics,
and fraud prevention, security, and compliance, and that data transmitted by
the SDK is encrypted in transit using TLS. Collection of particular identifiers
and the ads delivered can depend on device settings, region, consent choices,
and Google configuration.

Google's applicable information is available at:

- https://policies.google.com/privacy
- https://developers.google.com/admob/android/privacy/play-data-disclosure
- https://developers.google.com/admob/android/privacy

Google controls retention and deletion for data handled by its advertising and
consent services, subject to Google's policies and the privacy choices it makes
available.

## Data Security

Local History and configuration are stored in the app's private Android storage,
while saved images are stored in user-accessible device media storage. Security
of device-local and saved media also depends on the device, operating system,
backups, and user settings. As noted above, Google states that Mobile Ads SDK
network data is encrypted in transit using TLS.

## On-Device Background Removal

Background removal uses a bundled U2-NetP ONNX model and ONNX Runtime on the
device. The model is packaged with the app. App code does not download a remote
model or upload an image for this feature.

## Third-Party Components

The app uses AndroidX, Jetpack Compose, Kotlin coroutines, Hilt/Dagger, Coil,
OkHttp/Okio transitively, ONNX Runtime Android, Google Mobile Ads SDK, and Google
User Messaging Platform SDK. Focused notices for key privacy and model
components are maintained in `legal/THIRD_PARTY_NOTICES.md` and the other files
in `legal/`; that focused notice is not an exhaustive dependency inventory.

## Children

Photo Compressor is a general-purpose utility and is not directed to children
under 13. This statement does not treat every person aged 13 or older as an
adult: the definition of a child varies by country, region, and applicable law,
and Google Play target-age selections can trigger Families requirements. The
automatic Google advertising-SDK practices described above apply whenever the
SDK is allowed to request ads. The publisher must configure the Google Play
target-audience and advertising settings consistently with the actual intended
audience and must reassess the implementation and policy before including any
audience treated as children under applicable law or serving ads to children or
users of unknown age in a mixed-audience app. The current implementation does
not provide a neutral age screen or child-directed/under-age ad-request
treatment and includes app-open ads, so it must not be represented as ready for
a child or mixed audience without a separate Families-compliance review and
implementation update. In particular, an eligible completed Progress screen
can contain top and bottom banners plus the final inline native placement;
Families treatment prohibits multiple ad placements on a page when ads are
served to children or users of unknown age. The publisher must confirm the
target-audience selection and resulting ad treatment before release.

## Changes

This policy may be updated to reflect changes to the app, law, or advertising
configuration. The effective date will be changed when the policy is updated.
The hosted Google Play privacy-policy URL and the in-app policy must contain the
same current disclosures.
