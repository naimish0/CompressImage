# Google Play Store asset package

Application: **Photo Compressor & BG Remover**  
Actual application ID: `com.rameshta.photocompressor`  
Package working date: **2026-07-20 (Asia/Kolkata)**

## Current final package

The superseding multilingual export is under `final/` and was generated on
2026-07-23. It contains one shared package icon plus localized feature
graphics, per-language all-feature overview sheets, and eight authentic app
screenshots per device profile for all 14 configured locales across phone,
7-inch tablet, 10-inch tablet and Chromebook. The prior four-locale candidate
notes below are retained as historical provenance only.

Use `qa/final-asset-manifest.csv` and `qa/final-qa-report.md` as the authority
for this export. The complete visual-review sheets are in
`qa/final-contact-sheets/`.

This directory contains the evidence, source inputs, real-app captures,
editable generation inputs, experiment plan and final-export locations for the
Google Play listing package. Nothing in this directory has been committed,
published or uploaded to Play Console by this workflow.

## Readiness at a glance

The English creative is the only candidate for production approval. Hindi,
Gujarati and Russian graphics are explicitly **translation-review required and
must not be published** until a qualified reviewer approves the copy in its
rendered context.

Do not upload any file solely because it exists. The final authority is
`qa/asset-manifest.csv` together with `qa/qa-report.md`; those QA files must show
that the exact export passed before upload.

| Area | Current disposition |
| --- | --- |
| Feature and claim evidence | Audited in `research/feature-evidence.md`; runtime evidence exists for the primary phone workflows. |
| Competitor and Play-policy research | Recorded in `research/competitor-analysis.md`, dated 2026-07-20. Install bands are market context, not proof that competitor graphics caused installs. |
| Source imagery | Three newly generated, unbranded samples registered in `source/licenses.md`; real app derivatives are retained beside them. |
| Phone capture | Genuine 1080×2400 debug-app captures retained under `source/captures/phone/`. |
| Large-screen capture | Genuine 1920×1080 app renders retained by profile; upload remains conditional on exact release-AAB availability in Play Console Device Catalog. |
| Icon | **Blocked from source-verified final status:** the required `ic_splitframe_logo.png` is missing. See `source/icon/README.md`. |
| `en-US` final exports | **PASS:** production candidates passed automated, full-resolution, phone-scale and thumbnail visual QA. |
| `hi-IN`, `gu-IN`, `ru-RU` exports | The visible review warning has been removed from every image. Copy and typography still require qualified human review before upload; Play's Gujarati target is `gu`, not `gu-IN`. |
| Experiments | Prepared but not published. No uplift is promised. See `experiments/experiment-plan.md`. |

## Evidence-backed product scope

The repository and debug run support compression toward a chosen KB/MB target,
measured before/after results, percentage and pixel resizing, JPG/PNG/WEBP
conversion, batch compression, on-device background removal, solid-color
replacement, History, Save, Open and Share. No account flow exists, and code
and output inspection support the narrower “no watermark added by the app”
claim.

Approved privacy wording is **“Image Processing Stays on Device”** or “Images
aren't uploaded for processing.” The app uses Google advertising and consent
services, so the package deliberately does not claim that the whole app is
offline, network-free or data-collection-free.

The retained compression run used for primary numeric proof reports:

- source: `source/sample-images/portrait-curly-hair-original.png` — 2,683,003
  bytes, displayed by the app as **2.68 MB**;
- app result: `source/sample-images/real-app-results/compression/portrait-compressed.jpg`
  — 235,719 bytes, displayed as **236 kB**;
- app statistics: **91.2% saved** and **11.38:1** compression ratio; and
- UI evidence: `source/captures/phone/07-result-comparison-raw.png` and
  `09-result-stats-actions-raw.png`.

These figures describe this specific retained photo/result pair, not a
universal promise. The app also reported that its requested 205 kB target was
not safely achievable while preserving the configured quality floor; no asset
may imply that an exact target is guaranteed.

## Supported form factors

This is a repository/manifest/build assessment. Exact Play distribution is not
available locally.

| Form factor | Asset decision |
| --- | --- |
| Android phone | Supported; use the phone carousel after QA. |
| 7-inch tablet | Manifest/build-compatible, not tablet-optimized; use only after Device Catalog confirmation. |
| 10-inch tablet | Manifest/build-compatible, not tablet-optimized; use only after Device Catalog confirmation. |
| Chromebook | Manifest/build-compatible, not Chromebook-optimized; use only after Device Catalog confirmation. |
| Wear OS, Android TV, Android Automotive, Android XR | Unsupported by the current project; no asset slot should be populated from this package. |

The large-screen files are separate real app renders at the documented display
constraints, not scaled phone artwork. Capture methodology and limitations are
in `source/captures/README.md`.

## Directory guide

- `research/` — current Play requirements, competitor benchmark and the
  feature/claim evidence matrix.
- `source/icon/` — installed-brand references and the missing-source blocker.
- `source/templates/` — editable layout/copy inputs used by the deterministic
  generator.
- `source/captures/` — preserved raw real-UI screenshots by device profile.
- `source/sample-images/` — registered source samples and real-app outputs.
- `source/licenses.md` — image provenance and font-license receipts.
- `common/icon/` — 512 px common Play icon candidate; conditional until the
  named brand source is confirmed.
- `en-US/` — default English feature graphic and ordered device screenshots.
- `hi-IN/`, `gu-IN/`, `ru-RU/` — clean localized previews with review status
  retained in documentation rather than overlaid on the images.
- `experiments/` — controlled first-frame variants and the experiment plan.
- `qa/` — copy deck, final manifest, QA report and contact sheets.
- `tools/` — deterministic generation and validation commands.

## Exact Play Console upload mapping

Use this mapping only after the referenced file appears as a passing final
export in the QA manifest.

| Play Console target | Repository source | Order / condition |
| --- | --- | --- |
| Main store listing → App icon | `common/icon/app-icon-512.png` | Shared 512×512 icon. **Do not upload until `ic_splitframe_logo.png` is supplied or the installed 1024 px source is explicitly confirmed.** |
| Main store listing → Default language (`en-US`) → Feature graphic | `en-US/feature-graphic/` | Upload the single passing 1024×500 no-alpha export. |
| Main store listing → Default language (`en-US`) → Phone screenshots | `en-US/phone/` | Upload the eight passing 1080×1920 exports in numeric order 01–08. |
| Main store listing → Default language (`en-US`) → 7-inch tablet screenshots | `en-US/tablet-7/` | Upload at least four passing 1920×1080 renders only if Device Catalog confirms release distribution to 7-inch tablets. |
| Main store listing → Default language (`en-US`) → 10-inch tablet screenshots | `en-US/tablet-10/` | Upload at least four passing 1920×1080 renders only if Device Catalog confirms release distribution to 10-inch tablets. |
| Main store listing → Default language (`en-US`) → Chromebook screenshots | `en-US/chromebook/` | Upload the four passing 1920×1080 renders only if Play Console accepts the release. Prefer order **03, 04, 01, 02** because the genuine desktop-density batch screens are visually sparse. |
| Main store listing → Hindi (`hi-IN`) | `hi-IN/` | **Do not upload before human translation and rendered-layout approval.** Then map feature graphic and device folders to their matching Hindi slots. |
| Main store listing → Gujarati (`gu`) | `gu-IN/` | Internal directory is `gu-IN`; Play target is **`gu`**. **Do not upload before human translation and rendered-layout approval.** |
| Main store listing → Russian (`ru-RU`) | `ru-RU/` | **Do not upload before human translation and rendered-layout approval.** |
| Store Listing Experiment → Phone screenshot 1, Cell A | passing savings-first export under `experiments/` | Use with the existing English listing as control; keep screenshots 02–08 and all other listing fields constant. |
| Store Listing Experiment → Phone screenshot 1, Cell B | passing quality-first export under `experiments/` | Same measured image, result and layout as Cell A; change only headline/value emphasis. |
| Background-removal custom listing candidate | ordered assets documented in `experiments/experiment-plan.md` | For a verified background-removal-intent audience only; lead with the unchanged real removal frame and keep remaining creative constant. |

Do not populate Wear OS, TV banner/screenshots, Automotive or XR sections. Do
not substitute a phone export for any large-screen slot.

## Claims deliberately rewritten or excluded

- “Remove Backgrounds in One Tap” became **“Remove Backgrounds Easily”** to
  avoid implying instant or perfect results.
- “Replace Any Background Color” became **“Replace with Any Solid Color”**
  because the app provides palette/custom RGB colors, not arbitrary photo
  scenes.
- “Your Photos Stay on Your Device” became **“Image Processing Stays on
  Device”** to keep the claim specific to processing rather than ads, consent,
  Open or Share.
- “Lossless,” “no quality loss,” “exact target guaranteed,” “fully offline,”
  “collects no data,” “no ads,” platform presets and invented measurement
  examples are excluded.
- The real result screen has no comparison slider or built-in zoom. A source
  and output detail crop may demonstrate quality, but cannot be presented as an
  in-app zoom feature.

## Open blockers and required confirmations

1. **Brand source:** obtain `ic_splitframe_logo.png` or owner confirmation that
   `ic_image_compressor_logo.png` is the authoritative equivalent. Until then,
   the icon is conditional.
2. **Play distribution:** verify the exact release AAB's phone, 7-inch tablet,
   10-inch tablet and Chromebook availability in Device Catalog and check for
   publisher exclusions. Local manifest compatibility is not console proof.
3. **Translations:** obtain qualified Hindi, Gujarati and Russian review of
   every headline, supporting line and alt text in the final layout. Existing
   app-resource translations do not approve new marketing copy. Current real
   app crops remain English; reviewers must approve that mixed-language design
   or request new real locale-specific captures. No localized large-screen set
   is supplied until that decision is made.
4. **Publisher rights review:** provenance, exact hashes, pinned Noto sources
   and OFL text are complete in `source/licenses.md`; the publisher should still
   perform its normal final legal/brand review before upload.
5. **Excluded diagnostic outputs:** early blue and red solid-color output trials
   retained under `source/sample-images/real-app-results/background/` showed a
   visible damaged wedge during capture review. They are diagnostic evidence,
   not approved marketing results. Use only genuine app previews/exports that
   pass final visual QA, and do not retouch the failed files. They are not used
   by any of the 52 passing final assets.

## Reproduction and release discipline

Run the checked-in generation and validation tools from the repository root
using the pinned dependencies documented under `tools/`. The generator should
fail on missing captures, fonts, translations or measurements. Review contact
sheets after every regeneration and compare the QA manifest hashes with the
actual files selected in Play Console.

This package intentionally does not alter the production UI, navigation,
functionality, ads, package name or release behavior. It also does not commit,
push, publish, upload or interact with production ads.
