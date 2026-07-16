#!/usr/bin/env python3
"""Convert the official U2-NetP PyTorch weights to ONNX.

This script intentionally keeps downloaded source and weights outside the app
module. Only the final verified ONNX model should be copied into
app/src/main/assets/models/ after license/provenance review is complete.
"""

from __future__ import annotations

import argparse
import hashlib
import importlib.util
import json
import subprocess
import sys
import urllib.request
from pathlib import Path

import numpy as np
import onnx
import onnxruntime as ort
import torch


U2NET_COMMIT = "ac7e1c817ecab7c7dff5ce6b1abba61cd213ff29"
U2NET_MODEL_SOURCE = (
    "https://raw.githubusercontent.com/xuebinqin/U-2-Net/"
    f"{U2NET_COMMIT}/model/u2net.py"
)
U2NETP_DRIVE_ID = "1rbSTGKAE-MTxBYHd-51l2hMOQPT_7EPy"


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def download_text(url: str, destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    with urllib.request.urlopen(url, timeout=60) as response:
        destination.write_bytes(response.read())


def download_weights(destination: Path) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    command = [
        sys.executable,
        "-m",
        "gdown",
        "--fuzzy",
        f"https://drive.google.com/file/d/{U2NETP_DRIVE_ID}/view",
        "-O",
        str(destination),
    ]
    subprocess.run(command, check=True)


def load_u2netp(model_source: Path, weights: Path) -> torch.nn.Module:
    spec = importlib.util.spec_from_file_location("u2net_official", model_source)
    if spec is None or spec.loader is None:
        raise RuntimeError(f"Could not load official model source: {model_source}")
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)

    model = module.U2NETP(3, 1)
    try:
        state = torch.load(weights, map_location="cpu", weights_only=True)
    except TypeError:
        state = torch.load(weights, map_location="cpu")
    model.load_state_dict(state)
    model.eval()
    return model


def export_onnx(model: torch.nn.Module, destination: Path, input_size: int, opset: int) -> None:
    destination.parent.mkdir(parents=True, exist_ok=True)
    dummy = torch.randn(1, 3, input_size, input_size, dtype=torch.float32)
    torch.onnx.export(
        model,
        dummy,
        destination,
        export_params=True,
        opset_version=opset,
        do_constant_folding=True,
        input_names=["input"],
        output_names=["d0", "d1", "d2", "d3", "d4", "d5", "d6"],
        dynamic_axes=None,
    )


def validate_onnx(path: Path) -> dict:
    model = onnx.load(path)
    onnx.checker.check_model(model)
    session = ort.InferenceSession(str(path), providers=["CPUExecutionProvider"])
    input_meta = session.get_inputs()[0]
    output_meta = session.get_outputs()[0]
    input_shape = [int(value) for value in input_meta.shape]
    sample = np.zeros(input_shape, dtype=np.float32)
    outputs = session.run(None, {input_meta.name: sample})
    first_output = outputs[0]
    return {
        "input_name": input_meta.name,
        "input_shape": input_shape,
        "input_type": input_meta.type,
        "output_name": output_meta.name,
        "output_shape": list(first_output.shape),
        "output_type": output_meta.type,
        "output_min": float(first_output.min()),
        "output_max": float(first_output.max()),
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--work-dir", default=".background-removal-work")
    parser.add_argument("--output", default="app/src/main/assets/models/u2netp.onnx")
    parser.add_argument("--input-size", type=int, default=320)
    parser.add_argument("--opset", type=int, default=17)
    args = parser.parse_args()

    work_dir = Path(args.work_dir)
    model_source = work_dir / "u2net.py"
    weights = work_dir / "u2netp.pth"
    output = Path(args.output)

    if not model_source.exists():
        download_text(U2NET_MODEL_SOURCE, model_source)
    if not weights.exists():
        download_weights(weights)

    model = load_u2netp(model_source, weights)
    export_onnx(model, output, args.input_size, args.opset)
    contract = validate_onnx(output)

    provenance = {
        "source_commit": U2NET_COMMIT,
        "source_file": str(model_source),
        "source_file_sha256": sha256(model_source),
        "weights_file": str(weights),
        "weights_sha256": sha256(weights),
        "onnx_file": str(output),
        "onnx_sha256": sha256(output),
        "opset": args.opset,
        "input_size": args.input_size,
        "contract": contract,
        "torch_version": torch.__version__,
        "onnx_version": onnx.__version__,
        "onnxruntime_version": ort.__version__,
    }
    print(json.dumps(provenance, indent=2, sort_keys=True))


if __name__ == "__main__":
    main()
