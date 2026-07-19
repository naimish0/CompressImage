# Third-Party Notices

Review date: 2026-07-19

This focused notice records key privacy and model components in the build:
Google advertising/consent SDKs, ONNX Runtime Android, and a U2-NetP ONNX model
for offline background removal. It is not an exhaustive dependency inventory.

## Advertising And Consent

### Google Mobile Ads SDK

Source: https://developers.google.com/admob/android

Maven coordinate reviewed: `com.google.android.gms:play-services-ads:25.4.0`

Usage: Google AdMob banner, native, interstitial, and app-open advertising.
Development builds use Google's sample ad units; release builds use the
publisher's production units.

Data disclosure reference: https://developers.google.com/admob/android/privacy/play-data-disclosure

Google's data disclosure for this SDK version states that the SDK automatically
collects and shares:

- IP address, which may be used to estimate approximate location.
- User product interactions such as app launches, taps, and video views.
- Diagnostic information such as app launch time, hang rate, and energy usage.
- Device and account identifiers, including Android Advertising ID, App Set ID,
  and, when applicable, identifiers related to accounts signed in on the device.

Google lists advertising or marketing, analytics, and fraud prevention,
security, and compliance as purposes for these data. Google also states that
data transmitted by the SDK is encrypted in transit using TLS. The categories
must be reflected in Play Console Data Safety and the public and in-app privacy
policies. App code does not provide selected images, generated images,
filenames, content URIs, EXIF metadata, or image-processing data to this SDK.

### Google User Messaging Platform SDK

Source: https://developers.google.com/admob/android/privacy

Maven coordinate reviewed: `com.google.android.ump:user-messaging-platform:4.0.0`

Usage: Requests consent information from Google, displays a consent form when
Google reports that one is required, and provides privacy-options access when
required. Google handles the form, participating advertising partners, and the
resulting consent or privacy-choice signals. Core image processing does not
require consent to personalized advertising.

## Background Removal

### U2-Net / U2-NetP

Source: https://github.com/xuebinqin/U-2-Net

Upstream project license: Apache License 2.0

Usage: Bundled background-removal model for on-device inference.

Packaged artifact source:
https://github.com/danielgatis/rembg/releases/download/v0.0.0/u2netp.onnx

Packaged SHA-256:
`309c8469258dda742793dce0ebea8e6dd393174f89934733ecc8b14c76f4ddd8`

The public rembg release artifact is byte-identical to the artifact published
by the Apache-2.0-declared `Heliosoph/u2net-onnx` model repository. See
`legal/MODEL_PROVENANCE.md` for the pinned source and full evidence chain.

License texts: `legal/U2NET_LICENSE.txt`, `legal/REMBG_LICENSE.txt`

### ONNX Runtime Android

Source: https://github.com/microsoft/onnxruntime

Maven coordinate reviewed: `com.microsoft.onnxruntime:onnxruntime-android:1.27.0`

License: MIT License

Usage: On-device ONNX inference runtime for the bundled background-removal
model.

License text: `legal/ONNX_RUNTIME_LICENSE.txt`

Third-party notices: `legal/ONNX_RUNTIME_THIRD_PARTY_NOTICES.txt`
