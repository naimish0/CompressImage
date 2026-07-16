# Third-Party Notices

Review date: 2026-07-16

This build includes ONNX Runtime Android and a U2-NetP ONNX model for offline
background removal.

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
