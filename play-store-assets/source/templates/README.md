# Editable asset sources

`layout-spec.json` records the fixed canvases, palette, and composition rules.
`../../tools/generate_assets.py` is the editable layout source: each frame is
ordinary HTML/CSS assembled from the approved copy deck and real capture paths.
The generator writes the resolved HTML proofs to `rendered-html/`, so a designer
can inspect the exact DOM and CSS used for every exported frame.

Do not replace a capture path with a recreated interface. Marketing text is
rendered by Chromium using the vendored Noto fonts; application UI and all image
results always come from `../captures/` and `../sample-images/real-app-results/`.

The hi-IN, gu-IN, and ru-RU copy remains review-required until a qualified
reviewer updates the copy-deck status and review record. The visible red review
footer is controlled separately: `--omit-review-footer` can suppress the banner
without changing the underlying review disposition. The exact rendered state
is recorded in `qa/render-policy.json`.
