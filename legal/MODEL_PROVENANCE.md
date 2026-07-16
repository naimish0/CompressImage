# Background Removal Model Provenance Review

Review date: 2026-07-16

This app bundles a converted U2-NetP ONNX model for offline background removal.

## Candidate Evaluated

Name: U2-NetP

Official source: https://github.com/xuebinqin/U-2-Net

Source commit checked: `ac7e1c817ecab7c7dff5ce6b1abba61cd213ff29`

Original model filename referenced by the official README: `u2netp.pth`

Official README model source: Google Drive file
`1rbSTGKAE-MTxBYHd-51l2hMOQPT_7EPy`, with an alternate Baidu Pan link.

Source-code license: Apache License 2.0, based on the repository `LICENSE`
file.

Original model SHA-256:
`e7567cde013fb64813973ce6e1ecc25a80c05c3ca7adbc5a54f3c3d90991b854`

Converted ONNX filename: `app/src/main/assets/models/u2netp.onnx`

Converted ONNX SHA-256:
`2db478c3e56cc19f8076b5bc12f0725716fc82d5b9a19e554815cac1150c476b`

Conversion script: `tools/background_removal/convert_u2netp_to_onnx.py`

Conversion environment:

- Python 3.9.6
- torch 2.7.1
- onnx 1.18.0
- onnxruntime 1.19.2 for conversion validation
- gdown 5.2.0
- numpy 2.0.2
- pillow 11.2.1

ONNX opset: 17

Optimization or quantization: no quantization applied; PyTorch ONNX export used
constant folding.

Tensor contract:

- Input name: `input`
- Input shape: `[1, 3, 320, 320]`
- Input type: `tensor(float)`
- Layout: NCHW
- Color order: RGB
- Normalization: channel value divided by 255, then ImageNet mean/std
  normalization.
- Output name used: `d0`
- Output shape: `[1, 1, 320, 320]`
- Output type: `tensor(float)`
- Output post-processing: min/max normalization, reverse letterbox, bilinear mask
  resizing, smooth alpha transition, and lightweight edge feathering.

## License Review Result

Proceeding with documented risk.

The repository source code is under Apache License 2.0, but the official
pretrained U2-NetP weights are not committed to the repository or published as a
GitHub release asset. The README links to external file-hosting locations for
the weight file, and the reviewed repository files do not explicitly state that
the `u2netp.pth` pretrained weights are licensed under Apache License 2.0 or
that they may be modified, converted to ONNX, redistributed inside an Android
App Bundle, and distributed through Google Play in a commercial app.

The exact pretrained weight redistribution terms were not independently found in
the official repository files. The project owner explicitly instructed that the
official U2-NetP weights are free for commercial use and requested
implementation. This build therefore records that reliance here instead of
claiming independent legal verification of the weight file.

## Required Resolution Before Implementation

Before publishing, obtain or retain one of the following from the original rights
holder or an official project artifact:

- A clear license statement covering the exact `u2netp.pth` pretrained weight
  file.
- Permission for commercial use, modification, ONNX conversion and
  optimization, redistribution inside an Android App Bundle, and Google Play
  distribution.
- The original weight file checksum and a reproducible official or project-owned
  download path.

Quality comparison before and after conversion: ONNX Runtime validation was run
with a zero tensor to verify shape/type compatibility. A visual quality
comparison against the original PyTorch model is still required before Play
Store release.

## Runtime Candidate

Name: ONNX Runtime Android

Maven coordinate reviewed: `com.microsoft.onnxruntime:onnxruntime-android:1.27.0`

Source tag checked: `v1.27.0`

Tag SHA checked: `8f0278c77bf44b0cc83c098c6c722b92a36ac4b5`

License: MIT License, based on the ONNX Runtime repository `LICENSE` and Maven
POM metadata.

Status: Added to the app as `com.microsoft.onnxruntime:onnxruntime-android:1.27.0`.

The MIT license text and ONNX Runtime third-party notices are included in this
folder.
