# Localization and brand audit

Audit date: **2026-07-20**  
Scope: repository instructions, production locale resources, Play-listing locale mapping, translation-review status, brand/icon assets and references, app color/type systems, and reproducible font licensing.

## Decision summary

- No `AGENTS.md` exists in the repository or any ancestor from the repository root through `/`; there are no additional repository instructions from that mechanism.
- The build packages **26 production app language tags including English**. All 25 non-English catalogs pass the repository's structural localization checker, but neither the repository nor Play Console evidence establishes native-speaker review of those catalogs or of any proposed marketing headline.
- The requested minimum localized asset languages are implemented in the app: Hindi (`hi`), Gujarati (`gu`), and Russian (`ru`). Their exact Google Play listing codes are **`hi-IN`, `gu`, and `ru-RU`**. The brief's `gu-IN` directory name may be retained as an internal artifact label, but the Play Console upload target is Gujarati **`gu`**, not `gu-IN`.
- Hindi, Gujarati, and Russian text-bearing store graphics must therefore be marked **translation review required** until a qualified human reviewer approves the final copy in context. The repository's existing translated UI terms are useful terminology references, not approval of new advertising copy.
- The mandatory brand source **`ic_splitframe_logo.png` is absent**: there is no such file and no textual reference anywhere in the repository (excluding generated/build and VCS data). A compliant final Play icon cannot honestly be attributed to that required source until it is supplied or its authoritative location is identified.
- The current installed visual identity uses a blue/cyan/purple layered-photo and compression-arrow mark. Its available 1024 px raster source is `app/src/main/res/drawable-nodpi/ic_image_compressor_logo.png`; it is not named or documented as `ic_splitframe_logo.png`.
- The app bundles no font files. Compose uses `FontFamily.Default`, so a deterministic asset pipeline must vendor pinned, licensed font binaries rather than depend on workstation or Android platform fallbacks.

## Repository and locale evidence

The production tag list is discovered from complete `values-*/strings.xml` directories and written to `BuildConfig.SUPPORTED_LOCALE_TAGS` by `app/build.gradle.kts:10-119`. `app/src/main/res/resources.properties:1` declares unqualified resources as English. Packaging generates a locale config and filters resources from the same discovery result (`app/build.gradle.kts:199-202`). The in-app selector reads that generated list (`LanguageScreen.kt:45-88`).

Current production app tags, in selector order:

```text
en, hi, gu, mr, bn, pa, ta, te, kn, ml, as, or, ur, ru, es, fr, de,
pt, pt-BR, it, id, ar, ja, ko, zh-Hans, zh-Hant
```

Additional implementation facts:

- `values-in` is a byte-matched legacy Android alias for canonical Indonesian `values-id`; it does not create a 27th user-facing option.
- Debug enables the pseudo-locales `en-XA` and `ar-XB`; neither is a production listing language.
- The manifest sets `android:supportsRtl="true"` (`AndroidManifest.xml:19`), and tests verify RTL layout direction for Arabic and Urdu.
- The existing checker result on this tree is: `Localization validation passed for 25 locales, 232 translated strings, and 1 plural resource(s).`
- The checker verifies catalog completeness, XML, placeholders, plural quantities, protected technical terms, obvious untranslated long English strings, and the Indonesian alias. It does **not** perform linguistic, cultural, typography, overflow, or marketing review.
- `PLAY_STORE_RELEASE_AUDIT.md:190-192` explicitly says the locale packs have not received human linguistic review and localized listings should be published only after native-speaker review.
- No Fastlane/Play metadata locale tree or other local record identifies languages actually enabled in Play Console. The repository proves packaged app locales only. Play Console must be checked before deciding which localized listing slots really exist.

## App locale to Google Play listing mapping

The Play codes below were checked on 2026-07-20 against Google's current [Translate and localize your app](https://support.google.com/googleplay/android-developer/answer/9844778?hl=en) language list. A mapping means the Play language slot exists; it does not mean that slot is configured for this app or that its text is reviewed.

| App resources | App tag | Play listing target | Mapping/readiness note |
| --- | --- | --- | --- |
| Default `values` | `en` | `en-US` | Requested default asset locale and a valid Play slot; actual Play default-language setting remains unverified. |
| `values-hi` | `hi` | `hi-IN` | Exact Play target; supported in app; human marketing review required. |
| `values-gu` | `gu` | `gu` | Exact Play target; Android requests such as `gu-IN` resolve to this base pack; human marketing review required. |
| `values-mr` | `mr` | `mr-IN` | Exact Play target; review required. |
| `values-bn` | `bn` | `bn-BD` | Exact Play target; review required. |
| `values-pa` | `pa` | `pa` | Exact Play target; review required. |
| `values-ta` | `ta` | `ta-IN` | Exact Play target; review required. |
| `values-te` | `te` | `te-IN` | Exact Play target; review required. |
| `values-kn` | `kn` | `kn-IN` | Exact Play target; review required. |
| `values-ml` | `ml` | `ml-IN` | Exact Play target; review required. |
| `values-as` | `as` | None in current Play list | App localization exists, but no self-managed Play listing language slot is listed by Google. Do not create an upload mapping. |
| `values-or` | `or` | None in current Play list | App localization exists, but no self-managed Play listing language slot is listed by Google. Do not create an upload mapping. |
| `values-ur` | `ur` | `ur` | Exact Play target and RTL; review plus RTL composition QA required. |
| `values-ru` | `ru` | `ru-RU` | Exact Play target; supported in app; human marketing review required. |
| `values-es` | `es` | `es-ES`, `es-419`, or `es-US` | The generic app pack serves multiple regions, while Play separates them. Select and review each intended regional variant; do not silently reuse one asset set for all three. |
| `values-fr` | `fr` | `fr-FR` or `fr-CA` | Generic app pack; intended Play region and terminology require review. |
| `values-de` | `de` | `de-DE` | Direct practical mapping; review required. |
| `values-pt` | `pt` | `pt-PT` | The app tests resolve `pt-PT` here and keep Brazil separate; review required. |
| `values-pt-rBR` | `pt-BR` | `pt-BR` | Exact Play target; review required. |
| `values-it` | `it` | `it-IT` | Direct practical mapping; review required. |
| `values-id` plus `values-in` alias | `id` | `id` | Exact Play target; `in` is an Android compatibility alias, not a second listing. |
| `values-ar` | `ar` | `ar` | Exact Play target and RTL; review plus RTL composition QA required. |
| `values-ja` | `ja` | `ja-JP` | Direct practical mapping; review required. |
| `values-ko` | `ko` | `ko-KR` | Direct practical mapping; review required. |
| `values-b+zh+Hans` | `zh-Hans` | `zh-CN` | Simplified Chinese practical mapping; review required. |
| `values-b+zh+Hant` | `zh-Hant` | `zh-TW` and/or `zh-HK` | The app deliberately maps both regions to one script pack, but Play separates them. Regional review is required before targeting either slot. |

For this asset task, the honest initial status is:

- `en-US`: may be the working/default copy source, subject to normal claim and visual QA.
- `hi-IN`, internal folder `gu-IN` / Play target `gu`, and `ru-RU`: may be generated only as **review-required** variants unless the publisher supplies reviewer approval.
- Every other language: do not infer an enabled listing from the presence of app strings. Confirm the Play Console slot and obtain human copy review first.

## Marketing terminology already present

None of the proposed feature-graphic or screenshot headlines exists verbatim in any resource catalog. In particular, there is no reviewed translation of “Smaller Photos. Still Sharp.”, “Remove Backgrounds in One Tap”, “Replace Any Background Color”, “Compress More Photos at Once”, “Resize by Percent or Pixels”, “Convert JPG, PNG & WEBP”, “See the Difference”, “Your Photos Stay on Your Device”, or the feature-graphic headline/secondary line.

The following exact in-app terms can anchor a human marketing translation. They must not be treated as pre-approved headlines.

| Resource concept | Hindi (`values-hi`) | Gujarati (`values-gu`) | Russian (`values-ru`) |
| --- | --- | --- | --- |
| `home_intro` | फ़ोटो कम्प्रेस करें, आकार बदलें, फ़ॉर्मैट बदलें और बैकग्राउंड हटाएँ। | ફોટા કમ્પ્રેસ કરો, કદ બદલો, ફૉર્મેટ બદલો અને બૅકગ્રાઉન્ડ દૂર કરો. | Сжимайте изображения, меняйте их размер и формат, удаляйте фон. |
| `home_formats_privacy` | एक फ़ोटो या पूरे बैच को प्रोसेस करें। लक्षित आकार सेट करें और JPG, PNG या WEBP में एक्सपोर्ट करें—सब कुछ आपके डिवाइस पर। | એક ફોટો અથવા આખી બૅચ પ્રોસેસ કરો. લક્ષિત કદ સેટ કરો અને JPG, PNG અથવા WEBP તરીકે એક્સપોર્ટ કરો—બધું તમારા ડિવાઇસ પર જ. | Обрабатывайте одно или сразу несколько изображений. Задайте нужный размер и экспортируйте в JPG, PNG или WEBP — всё на вашем устройстве. |
| `remove_background` | पृष्ठभूमि हटाएँ | બૅકગ્રાઉન્ડ દૂર કરો | Удалить фон |
| `background_offline_note` | यह इस डिवाइस पर चलता है और पारदर्शी PNG निर्यात करता है। छवियाँ अपलोड नहीं की जातीं। | આ ડિવાઇસ પર ચાલે છે અને પારદર્શક PNG નિકાસ કરે છે. છબીઓ અપલોડ થતી નથી. | Работает на этом устройстве и экспортирует прозрачный PNG. Изображения не загружаются. |
| `replacement_color` / `custom_color` | बदलने का रंग / कस्टम रंग | બદલવાનો રંગ / કસ્ટમ રંગ | Цвет замены / Пользовательский цвет |
| `compressing` / `batch_summary` | संपीड़ित हो रहा है / बैच सारांश | કમ્પ્રેસ થઈ રહ્યું છે / બૅચ સારાંશ | Сжатие / Сводка пакета |
| `resize`, width, height | आकार बदलें / चौड़ाई / ऊँचाई | કદ બદલો / પહોળાઈ / ઊંચાઈ | Изменить размер / Ширина / Высота |
| `compare_result` | परिणाम की तुलना करें | પરિણામની સરખામણી કરો | Сравнить результат |
| result metrics | मूल आकार; प्रोसेस किया गया आकार; बचत प्रतिशत; संपीड़न अनुपात | મૂળ કદ; પ્રોસેસ કરેલું કદ; બચતની ટકાવારી; કમ્પ્રેશન ગુણોત્તર | Исходный размер; Обработанный размер; Сэкономлено; Коэффициент сжатия |
| `save_and_share` | सहेजें और साझा करें | સાચવો અને શેર કરો | Сохранить и поделиться |

Review cautions:

- The exact “one tap” headline has no localized resource support and must also match the verified workflow before translation. Prefer the approved “easily” wording if the flow requires more than one primary action.
- “On device” / “images are not uploaded” is supported by localized UI text. Do not broaden it to “the app is offline” because advertising and consent services use the network.
- “No account required” is stated only in English `translatable="false"` privacy-policy resources. It needs a new human-reviewed translation for localized graphics.
- No `watermark` string or marketing translation exists anywhere in the repository. Even if implementation review proves the claim, copy still needs human translation.
- A concrete Russian QA defect reinforces the review requirement: `quality_best_description` begins with lowercase `уделяет` in a standalone description (`values-ru/strings.xml:73`). Structural tests do not catch this class of issue.
- Brand and technical identifiers (`app_name`, `Photo Compressor`, `JPG`, `PNG`, `WEBP`, byte units) are intentionally protected/non-translatable in the source catalog.

## Brand and icon audit

### Required source status

`ic_splitframe_logo.png` is not present under the repository root and has zero source references. This is an exact-source blocker, not a small-resolution blocker: a faithful vector reconstruction cannot be authenticated against a missing source.

Available identity assets:

| Asset | Technical state | Actual reference |
| --- | --- | --- |
| `app/src/main/res/drawable-nodpi/ic_image_compressor_logo.png` | 1024×1024, 8-bit RGB PNG, no alpha, no embedded ICC profile, about 822 KB | Android 12+ splash icon only: `values-v31/themes.xml:5` |
| `mipmap-*/ic_launcher_foreground.png` | 108, 162, 216, 324, and 432 px RGBA raster layers | Adaptive launcher XML foreground (`mipmap-anydpi-v26/ic_launcher.xml:4` and round equivalent) |
| `mipmap-*/ic_launcher_monochrome.png` | Matching adaptive-layer sizes, RGBA | Adaptive themed-icon layer (`ic_launcher.xml:5` and round equivalent) |
| `mipmap-*/ic_launcher.png` and `_round.png` | 48–192 px legacy RGBA icons | Manifest launcher and round launcher through `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round` (`AndroidManifest.xml:16-18`) |
| `play-store/assets/app-icon-512.png` | Existing 512×512 RGBA candidate, no embedded ICC profile | Publishing documentation only; it visually matches the installed layered-photo/compression mark but is not documented as the mandated source |

The visible mark is a rounded blue-to-purple pair of overlapping image cards, a cyan edge highlight, and a pale tile containing inward compression arrows. Launcher and splash backgrounds use pale lavender-blue `#E7ECFD` (`values/colors.xml:3-4`). The mark contains no text.

The exact application label is non-translatable **Photo Compressor & BG Remover** (`values/strings.xml:4`) and is used by the manifest. The actual Gradle namespace/application ID is lowercase `com.rameshta.photocompressor` (`app/build.gradle.kts:100,108`), despite the uppercase `R` spelling in the task brief. Do not encode either package spelling into graphics.

### App visual system

- The real light UI is Material 3-inspired but teal-led: primary `#006D77`, primary container `#BFEFF1`, secondary `#6E5B00`, secondary container `#FFE68A`, background `#F7FAF9`, surface `#FBFDFB`, dark text `#171D1D`, and surface-variant text `#3F4948` (`ui/theme/Color.kt:5-22`).
- Cards use 10/16/22 dp radii, 1/3 dp elevations, and 4–48 dp spacing tokens (`DesignTokens.kt:7-27`).
- Dynamic color is disabled by default; real captures therefore use the declared theme rather than wallpaper colors (`Theme.kt:64-78`).
- The asset brief's blue `#2563EB` can frame the carousel and aligns with the blue/purple icon, but real UI crops must preserve their teal app colors. Do not recolor UI captures to force palette uniformity.

### Icon handoff requirement

Before declaring `play-store-assets/common/icon/app-icon-512.png` final, obtain the authoritative `ic_splitframe_logo.png` or written confirmation that `ic_image_compressor_logo.png` is that source under a different name. Then record the source hash and derivation. The current 1024 px raster has enough pixels for a 512 px export, but it has no embedded color profile; the deterministic export must explicitly convert/tag sRGB and validate the final file independently.

## Typography and licensing

The application does not define a branded font. Every Material typography style uses `FontFamily.Default` (`ui/theme/Type.kt:9-79`), and the native-ad view uses Android's default typeface. No `.ttf`, `.otf`, `.ttc`, `.woff`, font license, or font attribution file is present in the repository.

Workstation system fonts are not acceptable reproducibility inputs: they vary by OS version, are not stored with the project, and their redistribution terms are not documented here. This host does not provide a project-controlled Noto Sans Latin/Cyrillic, Devanagari, or Gujarati binary.

Recommended deterministic minimum for the required four asset locales:

| Asset text | Font family | Official source/license |
| --- | --- | --- |
| English and Russian | Noto Sans (Latin/Greek/Cyrillic) | [notofonts/latin-greek-cyrillic](https://github.com/notofonts/latin-greek-cyrillic), SIL Open Font License 1.1 |
| Hindi | Noto Sans Devanagari | [notofonts/devanagari](https://github.com/notofonts/devanagari), SIL Open Font License 1.1 |
| Gujarati | Noto Sans Gujarati | [notofonts/gujarati](https://github.com/notofonts/gujarati), SIL Open Font License 1.1 |

The official Noto repositories state that the font software is licensed under the SIL OFL 1.1. That license permits using the fonts to render graphics and permits bundling/redistribution subject to its terms; preserve the copyright and OFL text alongside any vendored binaries. This is license-source verification, not legal advice.

For the generation pipeline:

1. Pin an upstream release or commit, download only the required Regular/SemiBold/Bold or variable font files, and store their URLs, versions, SHA-256 hashes, and unmodified OFL text.
2. Render Indic scripts with a shaping stack that supports HarfBuzz/RAQM; cmap presence alone does not prove correct conjuncts and combining marks.
3. Programmatically verify every final character has a glyph, then visually inspect headline shaping, matras, punctuation, line breaks, and weight at full and thumbnail sizes.
4. If more listing languages are approved, add script-appropriate Noto families rather than relying on silent system fallback; CJK, Arabic/Urdu, Bengali/Assamese, Gurmukhi, and the remaining Indic scripts need their own reviewed font choices.

## Required follow-up

1. Supply or locate `ic_splitframe_logo.png`, or explicitly confirm the existing 1024 px `ic_image_compressor_logo.png` is the same authoritative brand source.
2. Confirm the default and enabled translation languages inside Play Console; local source cannot verify console state.
3. Treat all Hindi, Gujarati, and Russian text-bearing outputs as review-required until a named human reviewer approves the final headline and supporting copy in the rendered layout.
4. Use Play upload targets `hi-IN`, `gu`, and `ru-RU`; document the internal `gu-IN` folder-to-Play-`gu` mapping in the final upload guide.
5. Vendor pinned Noto binaries plus their OFL texts before the asset generator claims deterministic font inputs.
6. Preserve the real teal Material UI inside captures while using the requested blue store-art system only around those captures.

No production app source or resource was changed by this audit.
