# Third-Party Notices

Review date: 2026-07-17

This build includes Google advertising/consent SDKs, ONNX Runtime Android, and
a U2-NetP ONNX model for offline background removal.

## Advertising And Consent

### Google Mobile Ads SDK

Source: https://developers.google.com/admob/android

Maven coordinate reviewed: `com.google.android.gms:play-services-ads:25.4.0`

Usage: Google AdMob top, bottom, inline, and empty-space banner ads plus
History-open and Save-click interstitial ads. The app must use test ad units
during development and production ad units only after Play Console and AdMob
setup is complete.

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

## Reviewed But Not Bundled

### U2-Net / U2-NetP

Source: https://github.com/xuebinqin/U-2-Net

Repository source-code license: Apache License 2.0

Usage: Background-removal model converted to ONNX for on-device inference.

Modification: Converted from official PyTorch weights to ONNX opset 17 with
constant folding. No quantization applied.

Important note: the exact pretrained weight redistribution terms were not
independently found in the official repository files. This build relies on the
project owner's explicit confirmation that the official U2-NetP weights may be
used commercially and redistributed in the app.

### ONNX Runtime Android

Source: https://github.com/microsoft/onnxruntime

Maven coordinate reviewed: `com.microsoft.onnxruntime:onnxruntime-android:1.27.0`

License: MIT License

Usage: On-device ONNX inference runtime for the bundled background-removal
model.

License text: `legal/ONNX_RUNTIME_LICENSE.txt`

Third-party notices: `legal/ONNX_RUNTIME_THIRD_PARTY_NOTICES.txt`
