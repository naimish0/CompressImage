# Real-app capture plan

## Scope and creative angles

This repository contains no puzzle levels or seeds. The deterministic IDs below identify real debug-capture fixtures and UI states, not game levels. “Difficulty” describes how much app information a new viewer must parse; it is not a gameplay claim.

1. **Understand the objective instantly:** selected photos and the “Preview and configure” path.
2. **Show the signature capability:** a real subject changes from a photographic background to the transparent checkerboard.
3. **Present a dense but readable task:** target size, quality, resize, and format controls in one genuine configuration flow.
4. **Create a meaningful decision:** balance the 205 kB target, Balanced quality, 75% resize, and JPG output without claiming an exact target.
5. **Show cause and effect:** configuration → processing → result, or original → processed comparison.
6. **Show completion and retained work:** 100% batch summary and local History. This is workflow completion, not game mastery or progression.

## Deterministic states

| State ID | Fixture / screen | Difficulty | Required setup | Intended action sequence | Best capture moment | Creative angle | Why it attracts a new user | Cropping risks |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| CI-01 | Debug fixture `home`; portrait, rowboat, and flowers selected | Easy / immediate | Launch `PlayStoreCaptureActivity` with `screen=home`, English, seeded rights-cleared images | View three selected images → tap **Preview and configure** | First frame with title, “3 images selected,” subject thumbnails, and CTA | Objective instantly | Batch purpose and supported output formats are understandable without narration | In horizontal crops retain the title/selection card; lower thumbnails may be omitted |
| CI-02 | Debug fixture `editor`; 205 kB, Balanced, 75%, JPG | Medium / balanced | Launch `screen=editor` with the same three seeded originals | Inspect photo/output information → scroll through target, quality, resize, and format → choose next action | 205 kB selected in yellow, or the original/output dimensions with the subject | Meaningful size decision; dense readable task | Demonstrates control instead of a vague “compress” promise | A portrait-to-landscape crop cannot show the full subject and every control; choose the relevant region and never rearrange UI |
| CI-03 | Debug fixture `batch`; three genuine app results | Easy outcome | Launch `screen=batch`; fixture reports all three completed | Complete processing → view 100% summary → tap **Compare results** | “Done: 3 · Failed: 0,” three 100% rows, and comparison CTA | Completion and satisfaction | Gives immediate, measurable completion proof | Keep the top summary and at least two rows; omit any eligible ad area |
| CI-04 | Debug fixture `result`; genuine original and compressed output | Medium / proof | Launch `screen=result` from the retained original/result pair | View original → scroll to processed output and statistics/actions | “Result ready,” original/processed content, or a clear subject close-up | Cause and effect; before/after proof | Reduces uncertainty by showing the real output before save/share/open | Horizontal crop may show one comparison region at a time; do not imply a slider or zoom feature |
| CI-05 | Debug fixture `background`; genuine U2-NetP transparent result | Signature | Launch `screen=background` using the retained on-device result | From editor tap **Remove background** → wait for app result → view transparent preview and color choices | Subject centered against the checkerboard with **Transparent** selected | Signature capability | The transformation is visually clear in under a second and looks distinct from blue/white compressor competitors | Preserve enough checkerboard to prove transparency; do not crop hair/shoulders or imply perfect edges at unsupported zoom |
| CI-06 | Debug fixture `history`; three deterministic completed outputs | Medium / retained work | Launch `screen=history` with seeded compression/background records | View output names, sizes, dimensions, Open/Share/Remove actions | First two complete rows and History title | Completion and retained work | Shows useful results remain accessible after processing | A tight crop can make Remove look like an ad CTA; retain page title and row context |

## Captured source inventory

Untouched screenshots are in `images/raw/`: Home, editor, batch, comparison result, background result, and History in both portrait and landscape where useful, plus the portrait Settings state. The two untouched portrait recordings in `videos/raw/` reproduce:

- **Concept A:** editor/configuration → Remove background → genuine transparent result.
- **Concept B:** Home selection → configuration → Compress → completed batch → Compare results.

The real app has no sound effects or music, so no audio was fabricated. Source images come from the repository's rights-cleared capture fixtures; the transparent and compressed outputs were produced by the actual app pipeline and retained in the repository before this package.

## Capture hygiene

- Device: isolated Pixel 7a Android Emulator, API 37, 1080×1920 portrait and 1920×1080 landscape.
- Notifications, touch-debug indicators, layout bounds, FPS overlays, navigation/status bars, consent screens, ads, and personal accounts were absent from the capture surface.
- The debug capture activity uses real production composables and debug-only deterministic data. Production ad IDs and the release ads architecture were not changed.
- A temporary debug-only tap-routing hook used to record the real transitions was removed immediately after capture; final repository diff contains no capture-only production or debug-code changes.
