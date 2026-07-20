# Play Store creative experiment plan

Status: **prepared, not published**  
Initial locale: **English (`en-US`)**  
Prepared: **2026-07-20**

These tests are designed to measure listing performance, not to promise an
uplift. No creative should enter a Play experiment until its real debug-app
capture, source/output files, byte measurements, rights record, and QA results
have passed the gates in `../research/feature-evidence.md` and
`../qa/qa-report.md`.

## Shared controls and prerequisites

- Use one rights-cleared source photo and the exact same app-created output in
  both cells of the compression test.
- Resolve `{ORIGINAL_SIZE}`, `{PROCESSED_SIZE}`, `{PERCENT_SAVED}`, and
  `{COMPRESSION_RATIO}` from the retained measurement evidence. Do not type
  illustrative values into a final asset.
- Keep the same real UI crop, unaltered 100% detail crops, crop position,
  headline position, type size, colors, spacing, and status bar in both cells.
- Keep the app icon, feature graphic, screenshots 2–8, app title, short and full
  descriptions, countries, and acquisition campaigns constant.
- Do not launch a production-code change, price/promotion change, or another
  overlapping listing test to the same audience during measurement.
- Run the initial test in one locale so translation differences are not an
  uncontrolled variable. Localized tests require independently approved copy.
- Select the audience split and stopping rule before launch. Use Play Console's
  reported uncertainty/confidence, include complete weekly traffic cycles, and
  do not stop because of a short-lived early lead.
- Record experiment ID, exact assets, start/end timestamps, traffic allocation,
  countries, acquisition mix, app version, and result in the eventual log.

## Experiment 1 — savings proof versus detail proof

This is one controlled two-cell Store Listing Experiment. **The only changed
major variable is the headline/value emphasis in phone screenshot 01.** Both
cells retain the same measured number, the same detail crop, and the same real
result UI; neither cell receives additional badges or altered photography.

| Field | Cell A — measured savings | Cell B — visual quality |
| --- | --- | --- |
| Hypothesis | A measured, sample-specific savings headline may communicate the storage benefit more quickly to compression-intent visitors. | A detail-led headline may better reassure visitors who are concerned that compression will visibly degrade a photo. |
| Asset changed | Phone screenshot 01 only. | Phone screenshot 01 only. |
| Headline | **This Photo File Is `{PERCENT_SAVED}%` Smaller** | **Smaller Photos. Still Sharp.** |
| Evidence shown in both | The same actual original size, processed size, percentage saved, compression ratio, real result UI, and unaltered source/result detail crops. | The same actual original size, processed size, percentage saved, compression ratio, real result UI, and unaltered source/result detail crops. |
| Intended audience | General `en-US` listing visitors with compression, file-size, or storage intent. | The same audience and traffic allocation. |
| Primary conversion metric | Store-listing visitor-to-installer conversion rate for eligible experiment traffic. | The same metric. |

The percentage headline is deliberately scoped to **“This Photo File.”** It
must not imply that every input receives the same reduction, and it avoids a
bare “Save N%” construction that could resemble price/deal language. “Still
Sharp” is acceptable only if the paired 100% crops visibly support it without
sharpening, blurring, or other post-processing.

### Interpretation

- Adopt a cell only when Play Console reports sufficiently conclusive evidence
  under the predefined stopping rule and the result is operationally meaningful.
- Check guardrails before adoption: no claim complaint, unsupported-number
  finding, rights issue, rendering defect, or unusual traffic-mix change.
- An inconclusive result means the collected data does not support choosing a
  winner. It does **not** prove equal performance. Keep the current control,
  archive the result, and either collect more eligible traffic or test a new,
  single-variable hypothesis later.
- Do not generalize an `en-US` result to another locale without a separately
  reviewed localized test.

## Candidate 2 — background-removal-first custom listing

This candidate is intended for a background-removal keyword audience or
another explicitly configured custom-listing segment. It should not run
concurrently against the same traffic as Experiment 1.

| Field | Plan |
| --- | --- |
| Hypothesis | Visitors arriving with explicit background-removal intent may understand the listing faster when the genuine cutout result leads the carousel. |
| Asset changed | Change only the first benefit in the phone carousel: place the existing, verified **“Remove Backgrounds Easily”** frame first and move the unchanged compression frame to position 02. Keep frames 03–08 unchanged. |
| Intended audience | An `en-US` custom listing targeted to a verified background-removal search keyword, unique URL, or matching campaign audience. Do not infer intent from all general traffic. |
| Primary conversion metric | Visitor-to-installer conversion rate for the eligible custom-listing segment. If Play Console cannot randomize an experiment on that custom listing, treat the result as directional audience reporting, not a causal A/B result. |
| What remains constant | App icon, feature graphic, title, descriptions, individual screenshot designs and copy, source subject, capture evidence, colors, and campaign creative. Only the lead-benefit order changes. |

“Remove Backgrounds Easily” is mandatory. Do not replace it with “in one tap,”
“instant,” or a perfect-edge claim. The frame must use the app-created cutout,
not an edited or synthesized mask.

### Interpretation

- Compare only like-for-like eligible traffic and annotate campaign, country,
  seasonality, and audience changes.
- If the result is inconclusive, keep the general compression-first listing and
  do not claim that background removal performs equally well or poorly.
- If the result is positive only for the targeted segment, retain it as a
  custom-listing insight; do not automatically reorder the default listing.

## Change log template

| Experiment ID | Locale | Cell | Asset SHA-256 | Start | End | Traffic/audience | Result | Decision |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| _Complete in Play Console workflow_ | `en-US` | A/B | _pending final exports_ | _pending_ | _pending_ | _pending_ | _pending_ | _pending_ |
