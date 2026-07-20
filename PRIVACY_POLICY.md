# Privacy Policy

Effective date: July 20, 2026

Last updated: July 20, 2026

Application: Photo Compressor & BG Remover

## Developer and Contact

Photo Compressor & BG Remover is developed and published by Naimish Gupta.

For privacy questions, concerns, or requests, contact:

Email: naimish.app@gmail.com

## Summary

Photo Compressor & BG Remover supports image selection, compression, resizing,
format conversion, background removal, background replacement, History, saving,
opening, and sharing. Image processing occurs on the device, and background
removal uses an on-device model. The developer does not operate a backend that
receives images. The app does not require user accounts. Internet access is used
for Google advertising and consent services.

## Images and File Access

Images are selected using Android's system Photo Picker. The app accesses only
images selected by the user and reads the selected content URI and local
metadata needed to display and process each image, including its display name,
media type, file size, dimensions, format, transparency information, and EXIF
orientation. When the content provider permits it, the app attempts to retain a
read permission so local History can reopen the selected source.

Selected and generated images are processed locally. App code does not upload
selected or generated images, filenames, content URIs, EXIF metadata, or
image-processing results to a developer-operated server or to Google Mobile
Ads.

Saving is initiated by the user and uses Android MediaStore where supported.
Saved images remain in the user's public device gallery. On Android 9 (API 28)
and earlier only, a user-initiated save may request the legacy shared-storage
write permission required to save to the public Pictures collection. That
permission is not requested for saving on Android 10 or later.

The app does not request device location permission. Google advertising
services may infer approximate location from the device's IP address as
described below.

When a user chooses Share or Open, Android may provide the selected result to
another application chosen by the user. Information received by that
application is governed by that application's privacy practices. Photo
Compressor & BG Remover does not automatically share generated images without a
user-initiated action. Temporary read access to the selected result is granted
to a receiving application for that action.

## Local History, Retention and Deletion

Photo Compressor & BG Remover keeps up to 200 History records in app-private
Android DataStore. A record can include source and output references or paths,
display names, media types and formats, file sizes, dimensions, transparency
information, operation and compression details, result or warning information,
a saved-output reference, and a timestamp. The configured History DataStore is
excluded from cloud backup and device transfer.

Temporary compression and background-removal outputs are stored in app-private
cache directories. Removing a visible History item or using Clear History
removes the corresponding local metadata and attempts to delete temporary
output files referenced by those records when the files are owned by the app.
These actions do not release persisted read permissions for selected source
images and do not guarantee deletion of orphaned or otherwise unreferenced cache
files. Records whose temporary output no longer exists can be hidden from the
History screen without being removed from DataStore.

Android may evict cached files. Clearing the app's cache through Android
settings can remove temporary outputs without removing every stored History
record. Clearing the app's storage removes app-local History and cache data.
Uninstalling the app also removes its app-private data, subject to Android's
platform behavior.

Clear History and clearing app-private data do not delete images that the user
previously saved in the public device gallery. Saved gallery images must be
deleted through the gallery or file manager.

There is no user account and therefore no account-deletion process.

## Advertising and Consent

Photo Compressor & BG Remover uses Google AdMob to display banner, native,
interstitial, and app-open advertisements at appropriate points in the user
experience.

Core image-processing functionality remains available if an advertisement is
unavailable, fails to load, the device is offline, or personalized-ad consent is
not provided. Advertising failures do not prevent users from accessing
completed image results.

The app uses Google's User Messaging Platform to request current consent
information, display privacy messages when required, and provide access to
privacy options where required. Google manages the consent form, participating
advertising partners, and resulting consent signals.

## Data Automatically Collected and Shared by Google Mobile Ads

According to Google's Mobile Ads disclosure, Google Mobile Ads may
automatically collect and share:

- **Approximate location:** derived from the device's IP address.
- **User product interactions:** such as app launches, taps, and ad or video
  views.
- **Diagnostic and performance information:** such as app launch time, hang
  rate, energy usage, and related SDK or app performance information.
- **Device or other identifiers:** including the Android Advertising ID, App
  Set ID, and, where applicable, other identifiers related to accounts signed
  in on the device.

Google states that these data may be used for advertising or marketing,
analytics, and fraud prevention, security, and compliance. Google states that
data transmitted by the Mobile Ads SDK is encrypted in transit using Transport
Layer Security (TLS). Collection can depend on region, consent choices, device
settings, and SDK or account configuration.

Google controls retention and deletion for data handled by its advertising and
consent services, subject to Google's policies and the controls Google makes
available. The developer cannot delete data controlled solely by Google.

For more information, see:

- https://policies.google.com/privacy
- https://developers.google.com/admob/android/privacy/play-data-disclosure
- https://developers.google.com/admob/android/privacy

## Your Choices and Controls

Users control which images the app can access by selecting them through
Android's system Photo Picker.

When entries are visible in History, users can remove individual entries or use
Clear History. Users can also clear the application's cache or storage through
Android settings. Clearing storage is the reliable way to remove all app-local
History metadata when records are hidden because their temporary outputs no
longer exist. Images saved in the public device gallery must be deleted using
the gallery or file manager.

Where required, users can review advertising consent choices through the
Privacy Options entry inside the app. Android device settings may also allow
users to reset or delete the Advertising ID and manage advertising
personalization.

Users may contact Naimish Gupta at naimish.app@gmail.com regarding this policy
or privacy-related questions.

## Data Security

Photo Compressor & BG Remover uses Android scoped-storage and content-URI
mechanisms and requests access only to images selected by the user. Local
History and temporary processing files are stored in app-private storage.
Temporary URI access is granted to receiving applications only when the user
chooses to open or share an image.

The developer does not operate a backend for image processing. Google states
that information transmitted by its advertising and consent services is
protected in transit using TLS.

No method of electronic storage or transmission can be guaranteed to be
completely secure. Security also depends on the user's device, operating
system, installed applications, backups, and device configuration.

## On-Device Background Removal

Background removal uses a U2-NetP ONNX model with ONNX Runtime on the device.
The model is packaged with the app. No remote model is required, and no image is
uploaded for background removal.

## Third-Party Services and Components

The privacy-affecting third-party services and components used by the app are:

- **Google Mobile Ads SDK:** displays advertising and handles the Google data
  described above.
- **Google User Messaging Platform:** requests consent information and presents
  Google-managed privacy messages and privacy options when required.
- **ONNX Runtime:** runs background-removal inference locally on the device.
- **U2-NetP background-removal model:** is packaged with the app and processes
  image data locally through ONNX Runtime.

The release source does not include a separate developer-integrated analytics
or crash-reporting service. Google Mobile Ads can still collect the interaction
and diagnostic information described above.

## Children and Teen Users

Photo Compressor & BG Remover is a general-purpose image utility intended for
users aged 13 and older. It is not designed or marketed primarily to children
under 13.

The app does not create user accounts or ask users to provide their age.
Selected images and generated results are processed on the device and are not
uploaded to a developer-operated server.

The app uses Google Mobile Ads and Google User Messaging Platform. Google may
process advertising-related information as described in the Advertising and
Consent and Data Automatically Collected and Shared by Google Mobile Ads
sections.

Parents, guardians, and users may contact Naimish Gupta at
naimish.app@gmail.com with privacy-related questions.

## Changes to This Policy

This Privacy Policy may be updated to reflect changes to the application,
applicable law, third-party services, or advertising configuration. The
effective date and last-updated date will be revised when changes are made.

The privacy policy linked in Google Play and the policy accessible inside the
application will contain the same current disclosures.

## Contact Us

For questions, concerns, or requests regarding this Privacy Policy, contact:

Naimish Gupta

Email: naimish.app@gmail.com
