# Play Store asset QA report

Generated: **2026-07-20 (Asia/Kolkata)**

## Automated result

**PASS** — 52 final asset files checked; expected 52.

- Exact dimensions and PNG format: PASS
- Color modes and unexpected alpha: PASS
- Embedded sRGB ICC profile: PASS
- Icon 512×512, 32-bit RGBA, opaque alpha, under 1024 KB: PASS
- Screenshot aspect ratio (long side ≤ 2× short side): PASS
- Within-carousel duplicate/near-duplicate scan: PASS
- Font glyph coverage: PASS — Noto font union covers all copy-deck letters and digits; Chromium performs complex-script shaping.
- Numeric evidence recomputation: PASS — 2.68 MB → 236 kB; 91.2% saved; 11.38:1.
- Alt-text coverage and ≤140-character limit: PASS — 55 locale/asset entries checked.
- Translation render-policy gate: PASS — review warning is omitted by explicit render policy; metadata remains review-required.

The per-file dimensions, modes, byte sizes, hashes and release gates are in
`asset-manifest.csv`. Exact measurement inputs and hashes are in
`measurements.csv`.

## Release gates that automated validation cannot clear

- The common icon is technically valid but conditional because the requested
  `ic_splitframe_logo.png` is absent. Confirm the installed identity source.
- English phone and feature assets are production candidates after visual and
  policy review. This script does not claim that graphics guarantee installs.
- hi-IN, gu-IN and ru-RU exports remain **DO NOT PUBLISH** in the manifest and
  review documents until qualified native-language review, whether or not the
  visible image footer is enabled. Gujarati maps to Play locale `gu`.
- Tablet and Chromebook files pass local image QA but remain conditional on the
  exact release AAB's Play Console Device Catalog eligibility.
- Blue/red solid-color export trials with a damaged wedge are excluded. The
  final color frame uses correct, genuine in-app preview captures only.
- Visual inspection, claim review, ad/personal-data review and thumbnail-speed
  review are documented below after human inspection; they are not inferred
  from file metadata.

## Automated failures

- None.

## Localized footer-removal visual QA

**PASS** — the visible `TRANSLATION REVIEW REQUIRED • DO NOT PUBLISH`
footer was removed from all 27 localized final graphics (Hindi, Gujarati and
Russian), and the six corresponding contact-sheet/thumbnail review images were
regenerated.

- Hindi: 1 feature graphic, 8 phone screenshots, contact sheet and thumbnail
  strip inspected at full resolution and thumbnail scale — PASS.
- Gujarati: 1 feature graphic, 8 phone screenshots, contact sheet and thumbnail
  strip inspected at full resolution and thumbnail scale — PASS.
- Russian: 1 feature graphic, 8 phone screenshots, contact sheet and thumbnail
  strip inspected at full resolution and thumbnail scale — PASS.
- No warning text or red-footer residue, clipping, overflow, tofu boxes or
  broken localized glyphs was found.

This is a presentation-only change. Translation approval status remains
recorded in the manifest, copy deck and locale review documents so an image can
be clean without being mislabeled as translation-approved.
