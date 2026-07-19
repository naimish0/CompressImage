# Third-Party Notices

Review date: 2026-07-17

This build includes Google advertising/consent SDKs, ONNX Runtime Android, and
a U2-NetP ONNX model for offline background removal.

## Advertising And Consent

### Google Mobile Ads SDK

Source: https://developers.google.com/admob/android

Maven coordinate reviewed: `com.google.android.gms:play-services-ads:25.4.0`

Usage: Google AdMob banner, native, interstitial, and app-open advertising.
Development builds use Google's sample ad units; release builds use the
publisher's production units.

Data disclosure reference: https://developers.google.com/admob/android/privacy/play-data-disclosure

Important note: Google states that the SDK may automatically collect and share
advertising-related data such as IP address, user product interactions,
diagnostics, and device/account identifiers for advertising, analytics, and
fraud-prevention purposes. This app must reflect that in Play Console Data
Safety and the public privacy policy.

### Google User Messaging Platform SDK

Source: https://developers.google.com/admob/android/privacy

Maven coordinate reviewed: `com.google.android.ump:user-messaging-platform:4.0.0`

Usage: Requests consent information, displays required consent forms, and
provides privacy-options access where required before requesting ads.

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
