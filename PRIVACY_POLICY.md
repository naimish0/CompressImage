# Privacy Policy Draft

Status: draft source. Replace bracketed placeholders before publishing.

Effective date: [DATE]  
Developer: [LEGAL DEVELOPER NAME]  
Contact: [SUPPORT EMAIL OR CONTACT URL]  
App: Photo Compressor

## Summary

Photo Compressor is an image utility for selecting, compressing, resizing,
converting, saving, opening, sharing, and removing backgrounds from images.
Image processing happens entirely on your device. An internet connection may be
used to load advertisements and advertising consent information.

## Images And Files

The app uses Android's system Photo Picker so you choose the specific images the
app can access. The app reads selected image content from Android content URIs
and processes it locally on the device.

App code does not upload user-selected images, generated images, filenames,
content URIs, EXIF metadata, or image-processing data to Google AdMob or any
other server.

Generated images can be saved by you to `Pictures/Photo Compressor` through
Android MediaStore, opened in another app, or shared using Android share intents.
The app keeps a local History list with output URI, display name, MIME type,
size, dimensions, operation type, and timestamp so saved and processed outputs
can remain visible after the app restarts. The History list does not store
full-resolution bitmap data.

Temporary processing files can be stored in the app cache. The operating system
or clearing app storage may remove app-cache files. Saved gallery outputs remain
under your control in your device gallery.

## Advertising And Consent

The app uses Google AdMob to show banner advertisements in reserved top,
bottom, empty-space, and History-list areas, and interstitial advertisements
when History is opened or Save is clicked. Ads are optional content and image
processing remains usable if ads fail to load, the device is offline, or consent
information cannot be loaded.

The app uses Google's User Messaging Platform SDK to request consent information,
display consent forms where required, and provide privacy choices where required.
Users are not required to consent to personalized advertising to use the core
image-processing features.

Google and its partners may process advertising-related data, including where
applicable:

- IP address, which may be used to estimate approximate location.
- Advertising ID and other device/account identifiers where permitted.
- App interactions related to ad serving.
- Diagnostic information about SDK/app performance.
- Advertising and consent-related signals.

Google's privacy information is available at:

- https://policies.google.com/privacy
- https://developers.google.com/admob/android/privacy/play-data-disclosure

## On-Device Background Removal

Background removal uses a bundled ONNX model and ONNX Runtime on the device. The
model is packaged with the app and no remote model download is performed by app
code.

## Third-Party Components

The app uses AndroidX, Jetpack Compose, Kotlin coroutines, Hilt/Dagger, Coil,
OkHttp/Okio transitively through Coil/DataStore, ONNX Runtime Android, Google
Mobile Ads SDK, and Google User Messaging Platform SDK. Additional notices are
maintained in `legal/THIRD_PARTY_NOTICES.md`.

## Data Retention And Deletion

The app does not create a user account. To delete app-local temporary data, use
Android system settings to clear the app's storage/cache. To delete saved output
images, delete them from your device gallery or file manager.

Advertising and consent data handled by Google is governed by Google's privacy
terms and the choices available through the consent/privacy options flow where
required.

## Children

[STATE WHETHER THE APP TARGETS CHILDREN OR NOT AFTER PLAY CONSOLE TARGET
AUDIENCE IS FINALIZED.] If children are included in the target audience, AdMob,
store listing, content rating, and ad-serving settings must be configured to
comply with Google Play Families requirements before publication.

## Changes

This policy may be updated to reflect app, legal, or advertising configuration
changes. The Play Store privacy-policy URL and in-app policy reference must point
to the same current policy.
