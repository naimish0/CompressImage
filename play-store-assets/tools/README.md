# Reproduce and validate

Run from the repository root on a machine with Python 3 and Chromium/Google
Chrome. The current package was rendered with the locally installed Google
Chrome; set `CHROME_BIN` if the executable is elsewhere.

```sh
python3 -m venv /tmp/compressimage-assets-venv
source /tmp/compressimage-assets-venv/bin/activate
python -m pip install -r play-store-assets/tools/requirements.txt
python play-store-assets/tools/generate_assets.py
python play-store-assets/tools/validate_assets.py
```

The default localized renders retain a red `TRANSLATION REVIEW REQUIRED • DO
NOT PUBLISH` footer. `--omit-review-footer` removes only that visible banner and
does not change copy-deck review status, release disposition, or the review
documents. `--translations-reviewed` removes the footer and changes the render
policy to approved only when every row for each non-English locale has the
exact `TRANSLATION_APPROVED` status and a non-pending reviewer/date record;
otherwise generation fails. The selected state is recorded in
`qa/render-policy.json` and enforced by the validator.

Generation fails if a real capture, app result, font, copy deck, icon input, or
large-screen state is missing. `generate_assets.py` writes resolved HTML to
`source/templates/rendered-html/`, converts Chrome screenshots to embedded-sRGB
RGB PNGs, exports the conditional 32-bit RGBA icon, and produces contact sheets.
`validate_assets.py` recomputes measurements, checks glyph coverage, dimensions,
format, mode, alpha, ICC profiles, icon size, screenshot ratios, hashes, and
within-carousel near-duplicates.

Do not use the script to replace capture paths with a fabricated interface.
Recapture the real debug app when UI or results change, then regenerate.
