# Background Removal Model Provenance

Review date: 2026-07-18

## Packaged Artifact

Name: U2-NetP ONNX

App path: `app/src/main/assets/models/u2netp.onnx`

Byte size: `4,574,861`

SHA-256: `309c8469258dda742793dce0ebea8e6dd393174f89934733ecc8b14c76f4ddd8`

Pinned download:
https://github.com/danielgatis/rembg/releases/download/v0.0.0/u2netp.onnx

Reproduction tool: `tools/background_removal/fetch_u2netp_onnx.py`

The reproduction tool verifies the exact SHA-256 before replacing the bundled
artifact. The same bytes and SHA-256 are published by the Apache-2.0-declared
`Heliosoph/u2net-onnx` model repository at commit
`7fc34deee10329bc039c10a73b98090d0c6f5c59`:
https://huggingface.co/Heliosoph/u2net-onnx/tree/7fc34deee10329bc039c10a73b98090d0c6f5c59

## Source And Distribution Chain

Original U2-Net project: https://github.com/xuebinqin/U-2-Net

Upstream source commit reviewed:
`ac7e1c817ecab7c7dff5ce6b1abba61cd213ff29`

The upstream repository is licensed under Apache License 2.0 and its official
README identifies `u2netp.pth` as the pretrained lightweight model used by the
project. A copy of that license is packaged as `legal/U2NET_LICENSE.txt`.

ONNX distributor: https://github.com/danielgatis/rembg

The pinned ONNX file is publicly distributed as a rembg GitHub release asset.
The rembg repository identifies `u2netp` as a supported model, links its source
to the upstream U2-Net project, and is distributed under the MIT License. A copy
is packaged as `legal/REMBG_LICENSE.txt`.

Artifact cross-check: the rembg release asset is byte-for-byte identical to the
U2-NetP ONNX artifact in the model repository above, which expressly declares
Apache-2.0 and lists commercial use, modification, and distribution as
permitted.

## Release Resolution

The earlier locally converted artifact with SHA-256
`2db478c3e56cc19f8076b5bc12f0725716fc82d5b9a19e554815cac1150c476b`
was replaced. The release now uses the pinned, publicly redistributed ONNX
artifact described above so its source, checksum, redistributor, and license
chain are reproducible without relying on an undocumented local conversion.

Required notices are included in the app assets. U2-Net and rembg names and
marks are used only for attribution and do not imply endorsement.

## Runtime Contract

- Input shape: `[1, 3, 320, 320]`, NCHW, float32.
- Preprocessing: RGB values scaled to `[0, 1]`, then ImageNet mean/std
  normalization.
- Primary output shape: `[1, 1, 320, 320]`, float32.
- Post-processing: min/max normalization, reverse letterbox, bilinear mask
  resizing, smooth alpha transition, and edge feathering.
- Execution: ONNX Runtime Android, fully on device.

The Android engine validates tensor types and shapes before inference. The
instrumented background-removal test loads the packaged artifact, runs offline
inference, and validates the generated mask dimensions and values.

## Runtime

Name: ONNX Runtime Android

Maven coordinate: `com.microsoft.onnxruntime:onnxruntime-android:1.27.0`

Source tag reviewed: `v1.27.0`

License: MIT License. The runtime license and third-party notices are included
in this folder.
