# Source and license register

Record date: **2026-07-20 (Asia/Kolkata)**  
Scope: sample imagery and fonts used to produce the Play Store asset package.

This register records provenance; it is not legal advice. The publisher should
perform its normal final rights and trademark review before publishing.

## Original sample photography

The three source images below were newly generated for this repository with
OpenAI's image-generation service during the asset-production session. They
were not downloaded from a stock library or copied from another Play listing.
The generation briefs required fictional, unbranded subjects and excluded
celebrities, public figures, logos, readable text, trademarked packaging and
recognizable copyrighted characters.

The files are **generated outputs, not Creative Commons or public-domain
images**. No separate third-party stock-photo license applies. Their permitted
use remains subject to the publisher's agreement with the generation service
and applicable law; the statements below are provenance records rather than a
guarantee that no visually similar work can exist.

| Source file | Recorded generation brief | Technical record | SHA-256 |
| --- | --- | --- | --- |
| `sample-images/portrait-curly-hair-original.png` | Original neutral editorial photograph of a fictional adult woman with curly hair, a cobalt-blue blouse and an orange scarf in a natural garden; detailed hair and fabric edges; no text, logos, products or identifiable person. | PNG, 1122×1402, 8-bit RGB | `811dcacb040b08d684ab438876b3fa5cbbaa34fd269a17e2e9f7eed139088380` |
| `sample-images/wildflower-still-life-original.png` | Original detailed still-life photograph featuring wildflowers and fine natural texture; neutral, unbranded scene; no text, logos or trademarked packaging. | PNG, 1448×1086, 8-bit RGB | `332667533f62ec70a036abbddde9170be16cae4926ca2832cecf5a95c30a56d8` |
| `sample-images/blue-rowboat-original.png` | Original landscape photograph of an unbranded blue rowboat on a lake; no people, text, logos, landmarks or trademarked products. | PNG, 1448×1086, 8-bit RGB | `793d2db2677b32538b038d285c62817820ecb058170c8b8defcc2995fa27ae09` |

The briefs above are the generation records retained with this package. They
describe the requested content and restrictions; they are not represented as
embedded PNG metadata or as verbatim service logs.

### Real-app derivatives

Files below `sample-images/real-app-results/` were produced from the registered
source images by the real debug application. They are app-processing evidence,
not independently sourced artwork:

- `compression/*.jpg` — JPEG results created by the app's compression flow.
- `background/portrait-transparent.png` — transparent cutout created by the
  app's bundled on-device background-removal workflow.
- `background/portrait-white.png`, `portrait-blue.png`,
  `portrait-green.png`, and `portrait-red.png` — solid-color export trials from
  the app. Inclusion here records origin only; the QA report decides whether an
  individual trial is technically fit for marketing use.

Raw UI captures under `source/captures/` contain these same registered samples
and their genuine app results. They do not introduce an additional photo
license.

## Fonts

The application does not bundle a branded typeface. The deterministic asset
pipeline is intended to vendor script-appropriate Noto families under
`source/fonts/`, using the **SIL Open Font License 1.1 (OFL-1.1)**. Noto's
official repositories identify these families as OFL-licensed:

| Intended text | Family | Official project |
| --- | --- | --- |
| English and Russian | Noto Sans (Latin, Greek, Cyrillic) | <https://github.com/notofonts/latin-greek-cyrillic> |
| Hindi | Noto Sans Devanagari | <https://github.com/notofonts/devanagari> |
| Gujarati | Noto Sans Gujarati | <https://github.com/notofonts/gujarati> |

OFL-1.1 permits use of the font software to render graphics and permits
redistribution subject to the license terms. Any vendored binaries must remain
accompanied by their unmodified copyright and OFL text.

### Vendored-font receipt

The binaries below were downloaded on 2026-07-20 from the official Google Fonts
repository's `main` branch. Their hashes pin the exact local inputs; before a
future regeneration, preserve these files or update this receipt intentionally.

| Repository path | Upstream URL and revision | SHA-256 | License-copy path | Status |
| --- | --- | --- | --- | --- |
| `source/fonts/NotoSans-Variable.ttf` | `https://raw.githubusercontent.com/google/fonts/main/ofl/notosans/NotoSans%5Bwdth,wght%5D.ttf` | `bfb7bb691513f12e734dc346c03a03f784912432d7e3fa8e56efcf906fe86b3d` | `source/fonts/OFL.txt` | Vendored |
| `source/fonts/NotoSansDevanagari-Variable.ttf` | `https://raw.githubusercontent.com/google/fonts/main/ofl/notosansdevanagari/NotoSansDevanagari%5Bwdth,wght%5D.ttf` | `9ce7b04f60e363d8870e5997744cf85cf69d38a4d7d129d364d92a3b14b461d7` | `source/fonts/OFL.txt` | Vendored |
| `source/fonts/NotoSansGujarati-Variable.ttf` | `https://raw.githubusercontent.com/google/fonts/main/ofl/notosansgujarati/NotoSansGujarati%5Bwdth,wght%5D.ttf` | `9901d8552f1dd5d2c50dbd4caa6f6e174e74e8264f06594ab259ae6e7b1ac428` | `source/fonts/OFL.txt` | Vendored |

`source/fonts/OFL.txt` has SHA-256
`cee9892f9f0cc8fe882c9e9537ee6a89621d86ee7ceaf70b02e2b2b1c25c061a`.
The generator uses local Chromium for Devanagari/Gujarati shaping; the validator
checks that the vendored font union contains every copy-deck letter and digit.

Before any localized graphic is marked production-ready, also verify that the
renderer uses a shaping engine suitable for Devanagari and Gujarati, that all
glyphs exist, and that a qualified human reviewer approves the rendered copy.

## Excluded sources

- Competitor listing graphics were used only for visual research. None is
  copied into this package.
- Existing legacy screenshots under `play-store/` are not used as measurement
  or rights evidence for the new package.
- No celebrity, third-party brand, social-network logo, copyrighted character,
  or user photo is approved as a marketing source.
