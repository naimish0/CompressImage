# Play Store Screenshot Captions

The default English candidate set contains eight files in
`play-store/upload-phone-screenshots/`. Do not upload the full set until the
three rows marked **Recapture** are regenerated from the current UI. The
source-capture column records the corresponding file in
`play-store/screenshots/`.

| Order | Upload file | Source capture | Status | Caption |
| --- | --- | --- | --- | --- |
| 1 | `01-home-select-images.png` | `01-home-select-images.png` | **Recapture** — contains the previous Home intro copy. | Compress, resize, convert, and remove backgrounds from one place. |
| 2 | `02-home-batch-selected.png` | `02-home-batch-selected.png` | **Recapture** — contains the previous Home privacy/formats copy. | Process one photo or a batch of up to 50 while image processing stays on your device. |
| 3 | `03-custom-size-ui.png` | `10-custom-size-ui.png` | Ready | Set a custom target size in KB or MB before compressing. |
| 4 | `04-batch-compression-progress.png` | `04-batch-compression-progress.png` | Ready | Track total progress and each image in the batch queue. |
| 5 | `05-compare-result.png` | `05-compare-result.png` | Ready | Compare original and processed images before saving the result. |
| 6 | `06-result-actions.png` | `08-result-actions.png` | Ready | Review output details, then save, share, open, or process another image. |
| 7 | `07-background-export.png` | `09-background-export.png` | Ready | Export a transparent background as PNG or WEBP, or add a color and export as PNG, WEBP, or JPG. |
| 8 | `08-history.png` | `07-history.png` | **Recapture** — predates the top banner and inline native History layout. | Open, share, or remove recent compressed, converted, and background-removed images. |

## Supplemental And Localized Captures

These source captures are valid 1560 x 3120, 24-bit RGB PNGs but are not yet
copied into an upload set and are currently untracked in version control:

| Source capture | Intended listing | Caption |
| --- | --- | --- |
| `11-language-screen.png` | Default English; replace one default candidate if language support should be highlighted. | Search and choose from 26 app language or locale options, or follow the system default. |
| `12-home-hindi.png` | Hindi localized listing only. | हिन्दी में फ़ोटो कम्प्रेस करें, आकार और फ़ॉर्मैट बदलें और बैकग्राउंड हटाएँ। |

Screenshots containing text should not be mixed across listing languages. Build
language-specific upload sets and review the rendered copy before publishing.
Add retained supplemental captures to version control before treating them as
release evidence or listing sources.
