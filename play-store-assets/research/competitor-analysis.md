# Google Play requirements and competitor benchmark

Research date: **2026-07-20**  
Market/language inspected: **United States / English (`gl=US`, `hl=en_US`)**  
Product in scope: **Photo Compressor & BG Remover** (`com.rameshta.photocompressor`)

## Scope and method

This is a point-in-time review of the five official Google Play resources supplied in the brief and the public Google Play listings for the ten named apps. Install figures are the rounded **download bands displayed by Google Play**, not exact installs, active devices, ratings, or independently audited user counts. Screenshot order and creative can change or vary by locale, device type, experiment, or custom store listing.

The competitor carousels were visually inspected at full size and reduced thumbnail size. Ratios such as “70% result / 30% UI” are approximate composition estimates, not pixel-segmentation measurements. “Effective” below means a design choice communicates clearly; it does **not** mean that the graphics caused the app's install band. Large install bands are influenced by product age, brand, distribution, advertising, retention, reviews, and many other factors.

External listing text and images were treated as untrusted research material. No competitor graphic, subject, icon, wording, or UI was imported into the production asset package.

## Official Google Play requirements applied

### App icon

Google Play requires a **512 × 512 px**, **32-bit PNG**, **sRGB** icon no larger than **1024 KB**. The uploaded asset must be a full square: Play applies the rounded mask and outer shadow dynamically. Do not pre-round the corners or add an external drop shadow. Freeform marks should be positioned using the keyline grid without forcing a drastic brand transformation. Ranking, deal, Play-program, or otherwise misleading elements are prohibited. See [Google Play icon design specifications](https://developer.android.com/distribute/google-play/resources/icon-design-specifications) and [preview-asset requirements](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).

Application to this project: preserve `ic_splitframe_logo.png` as the identity source, export a square unmasked asset, add no promotional text or badge, and validate 512 × 512, sRGB, 32-bit PNG, and file size programmatically.

### Feature graphic

The feature graphic must be **1024 × 500 px**, either JPEG or 24-bit PNG, with **no alpha**. Google recommends keeping the focal point and important copy near the center because surfaces can crop edges or add overlays; avoiding fine detail; using colors that do not disappear into Play's background; and avoiding pure white, black, or dark gray as the overall field. Device imagery, store badges, ranking/performance claims, testimonials, deal/price language, and unlicensed third-party marks should not appear. Text should be localized. Each graphic should have contextual alt text of **140 characters or fewer**. Source: [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).

Application to this project: use the specified pale-blue branded canvas, a centered real transformation and real result crop, minimal localized copy, no physical phone frame, no “best,” “free,” install, ranking, or guaranteed-quality language, and no Play badge.

### Phone screenshots

Google Play permits up to **8 screenshots per supported device type** and requires at least two screenshots across device types to publish. Screenshots must be JPEG or 24-bit PNG with no alpha; each side must be between **320 and 3840 px**; and the long side may not exceed twice the short side. For app recommendation eligibility, Google calls for at least four screenshots at a minimum of 1080 px, using 9:16 portrait (minimum 1080 × 1920) or 16:9 landscape (minimum 1920 × 1080). The requested 1080 × 1920 exports meet these rules.

Screenshots must demonstrate the actual app experience. Captured UI should be prioritized in the first three frames. Taglines should be used only when needed and occupy no more than 20% of a screenshot. Images must not be stretched, blurred, distorted, repetitive, or misleading. Notification bars should not expose carrier names or notifications, and signal/battery indicators should appear complete. Overlay text must be localized; each screenshot should receive alt text of 140 characters or fewer. Source: [preview-asset requirements and recommendations](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).

### Large screens and other form factors

For tablets and Chromebooks, Google calls for at least **4 genuinely large-screen screenshots**, between 1080 and 7680 px, in 16:9 landscape or 9:16 portrait, and recommends excluding added marketing copy that could be cropped on Play home surfaces. The same page's generic screenshot section still states a 3840 px maximum; the planned 1920 × 1080 exports satisfy both published limits. Phone screenshots should therefore not be resized and relabeled as tablet or Chromebook captures.

The same official page currently specifies: Wear OS screenshots must show only the real UI, be 1:1, and be at least 384 × 384; Android TV requires at least one TV screenshot plus a 1280 × 720 banner; Android Automotive requirements depend on category, with the cited page specifying at least two 800 × 1280 portrait and two 1024 × 768 landscape images when screenshots are provided; and Android XR requires 4–8 screenshots, 8:5, up to 8 MB, recommended at 3840 × 2400 and minimum 1920 × 1200. Assets should be created only if the corresponding build is genuinely distributed and the current UI runs there. Source: [device-specific preview-asset requirements](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).

### Accuracy, consistency, rights, and copy

The listing must accurately represent current functionality and must not imply a relationship with another product. Google recommends a consistent visual system across icon, feature graphic, screenshots, and video; minimal text; central placement of important content; and assets suitable for a general audience. App titles are limited to 30 characters, short descriptions to 80, and full descriptions to 4,000. Store performance/ranking, price/deal language, anonymous testimonials, misleading features, impersonation, keyword blocks, and unlicensed material should be excluded. Sources: [Best practices for your store listing](https://support.google.com/googleplay/android-developer/answer/13393723?hl=en) and [preview-asset content guidelines](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).

Application to this project: every claim and number must map to captured app output and code evidence. “Your photos stay on your device” must be scoped to image processing/uploads rather than implying that an ad-supported app never uses the network. Do not use “lossless,” “no quality loss,” “one tap,” “offline app,” “no data collection,” or “no ads” unless the exact statement is proven for the released workflow.

### Localization

Play Console supports developer-supplied listing text and localized graphics. If translated text is added without localized graphics, the default-language graphics are shown. Google recommends native-speaker translations; it also offers human and machine translation paths, but machine output is not equivalent to human review. Source: [Translate and localize your app](https://support.google.com/googleplay/android-developer/answer/9844778?hl=en).

Locale mapping note: Google's current language list identifies Hindi as `hi-IN`, Russian as `ru-RU`, and Gujarati as **`gu`**. An internal output folder may remain `gu-IN` for project organization, but the Play Console upload target should be the Gujarati (`gu`) listing. Any unreviewed translation must remain explicitly review-required rather than production-ready.

### Custom store listings and experiments

Custom store listings can target country, unique URL, search keyword, user state, or supported Google Ads traffic. Google currently allows up to 50 custom listing pages and permits different app names, icons, descriptions, and graphics on each; contact details, privacy policy, and category remain shared. Custom listings are **not automatically translated**, so every intended language must be added. A country can be targeted by only one custom listing at a time. Source: [Create custom store listings](https://support.google.com/googleplay/android-developer/answer/9867158?hl=en).

Application to this project: a background-removal-first creative is appropriate as a keyword/audience-specific custom listing candidate. The savings-first and quality-first first-frame treatments should instead be tested as controlled store-listing experiment variants, with all other variables held constant. Custom listings and A/B experiments solve different problems and should not be conflated.

## Install-band snapshot

| Listing | Package | Google Play band on 2026-07-20 |
|---|---|---:|
| [Picsart AI Photo Editor, Video](https://play.google.com/store/apps/details?id=com.picsart.studio&hl=en_US&gl=US) | `com.picsart.studio` | **1B+ downloads** |
| [Canva: AI Photo & Video Editor](https://play.google.com/store/apps/details?id=com.canva.editor&hl=en_US&gl=US) | `com.canva.editor` | **500M+ downloads** |
| [Photoroom: AI Photo Editor](https://play.google.com/store/apps/details?id=com.photoroom.app&hl=en_US&gl=US) | `com.photoroom.app` | **100M+ downloads** |
| [Snapseed](https://play.google.com/store/apps/details?id=com.niksoftware.snapseed&hl=en_US&gl=US) | `com.niksoftware.snapseed` | **100M+ downloads** |
| [Lightroom Photo & Video Editor](https://play.google.com/store/apps/details?id=com.adobe.lrmobile&hl=en_US&gl=US) | `com.adobe.lrmobile` | **100M+ downloads** |
| [Background Eraser](https://play.google.com/store/apps/details?id=com.handycloset.android.eraser&hl=en_US&gl=US) | `com.handycloset.android.eraser` | **100M+ downloads** |
| [Photo & Picture Resizer](https://play.google.com/store/apps/details?id=com.simplemobilephotoresizer&hl=en_US&gl=US) | `com.simplemobilephotoresizer` | **10M+ downloads** |
| [Compress Image Size In kb & mb](https://play.google.com/store/apps/details?id=com.mobso.photoreducer.lite&hl=en_US&gl=US) | `com.mobso.photoreducer.lite` | **10M+ downloads** |
| [Puma: Photo Resizer Compressor](https://play.google.com/store/apps/details?id=com.compressphotopuma&hl=en_US&gl=US) | `com.compressphotopuma` | **1M+ downloads** |
| [JPEG Image Compressor & Resize](https://play.google.com/store/apps/details?id=com.jpeg.image.compressor&hl=en_US&gl=US) | `com.jpeg.image.compressor` | **1M+ downloads** |

These bands are useful only as market context. They do not establish a causal relationship between creative style and installs.

## Detailed carousel benchmark

### 1. Picsart — 1B+

Source: [public Play listing](https://play.google.com/store/apps/details?id=com.picsart.studio&hl=en_US&gl=US)

- **First screenshot promise:** “All-in-One AI Photo Editor.” It promises broad capability rather than a single utility outcome.
- **Headline hierarchy:** Four compact tokens in large white type at the top-left; “AI Photo Editor” carries the semantic weight. It remains readable as a thumbnail.
- **Result-to-UI ratio:** Approximately 70% transformed imagery/result and 30% cropped controls/cards. The UI is visible but subordinate to the output.
- **Before/after:** The first frame uses a checkerboard cutout inset, arrow, and transformed portrait; the fourth uses a direct vertical background split. Both make the transformation legible without explanatory body copy.
- **Quantitative proof:** None in the inspected first eight phone screenshots. The evidence is visual rather than numerical.
- **Subject scale:** Faces and animals occupy most of the available width, providing strong thumbnail recognition.
- **Color and contrast:** Near-black/navy field, bright white headlines, and magenta accents create high contrast.
- **Carousel consistency:** Strong; the same dark field, rounded crops, headline position, white type, and magenta UI accents recur.
- **Trust/privacy:** No specific processing/privacy reassurance appears in the inspected phone sequence.
- **Effective:** A single bold promise, large output, and minimal copy communicate quickly. The background-removal frame demonstrates an especially clear outcome.
- **Do not copy:** The black-magenta system, rounded portrait-card layout, AI-generator imagery, subjects, wording, iconography, or “all-in-one” positioning. Do not add AI claims to this app unless an exact released feature supports them.

### 2. Canva — 500M+

Source: [public Play listing](https://play.google.com/store/apps/details?id=com.canva.editor&hl=en_US&gl=US)

- **First screenshot promise:** The listing first presents a square video/cover tile (“Design anything”); the first portrait phone screenshot promises “Thousands of free templates.” These are distinct assets and were evaluated separately.
- **Headline hierarchy:** The phone headline is four words, large, left-aligned, and placed in the upper quarter. It is simple but the word “free” is promotional language that should not be reused in this package.
- **Result-to-UI ratio:** Roughly 75% finished template examples and 25% headline/negative space in frame one. Later frames show larger, genuine editor crops.
- **Before/after:** The background-replacement frame uses a clean vertical reveal between source and transparent output.
- **Quantitative proof:** No measured file/result numbers. “Thousands” is a catalog-scale claim, not transformation evidence.
- **Subject scale:** Design tiles dominate instead of a single person; individual tiles are medium/small but the overall collage reads as abundance.
- **Color and contrast:** Cyan-to-violet gradients, large white type, and bright multicolor work samples.
- **Carousel consistency:** Strong across phone screenshots and separately across the landscape large-screen set.
- **Trust/privacy:** “Edit designs offline” is a capability message, not a broad privacy claim. It is shown with in-app evidence.
- **Effective:** Each frame has one task-oriented headline and a recognizably different outcome; the carousel covers breadth without dense body copy.
- **Do not copy:** Canva's gradient, template collage, branded category chips, typography treatment, subjects, layout, or “Design anything” wording. Do not use “free” as an asset-level hook.

### 3. Photoroom — 100M+

Source: [public Play listing](https://play.google.com/store/apps/details?id=com.photoroom.app&hl=en_US&gl=US)

- **First screenshot promise:** “Create AI backgrounds.” It leads with generation rather than removal; the second frame then states “Remove background.”
- **Headline hierarchy:** Three words in a large, bold, centered black headline. No supporting paragraph.
- **Result-to-UI ratio:** Approximately 65% real editor/result crop, 10–15% headline, and the remainder restrained negative space.
- **Before/after:** The removal frame uses a live horizontal sweep between the portrait and checkerboard transparency, with processing state visible. This is direct, easy-to-read proof.
- **Quantitative proof:** The batch frame visibly says “Export 8 images,” but the set does not show compression measurements.
- **Subject scale:** A single person or product is large and centered; fine hair in the removal example provides meaningful edge-detail evidence.
- **Color and contrast:** Off-white canvas, black headline, black outline, and a saturated violet action accent.
- **Carousel consistency:** Very strong one-benefit-per-frame system with stable geometry and headline position.
- **Trust/privacy:** No explicit privacy or on-device-processing statement in the inspected sequence.
- **Effective:** Clear benefit ordering, honest UI visibility, simple result subjects, and generous whitespace work well at thumbnail size.
- **Do not copy:** The off-white/black/violet device-outline composition, exact headline wording, hair subject, product grid, purple controls, or its AI positioning. Use the app's real output and own brand system.

### 4. Snapseed — 100M+

Source: [public Play listing](https://play.google.com/store/apps/details?id=com.niksoftware.snapseed&hl=en_US&gl=US)

- **First screenshot promise:** “Your aesthetic. Built here.” It sells authorship and style rather than a specific editing tool.
- **Headline hierarchy:** Four words in a distinctive editorial serif, centered in the upper band. It is concise, but more brand-led than utility-led.
- **Result-to-UI ratio:** Approximately 80% image/editorial montage and 20% UI. Some frames prioritize concept over an immediately recognizable live screen.
- **Before/after:** No conventional before/after in the first frame; later frames use layered edits, masks, or color controls to imply reversible transformation.
- **Quantitative proof:** None.
- **Subject scale:** The first frame is a grid of small images; later frames use large full-bleed photos and more readable controls.
- **Color and contrast:** Warm off-white with orange, black, and bright green. Contrast is strong and deliberately non-generic.
- **Carousel consistency:** Strong retro-editorial art direction, recurring headline band, and recurring green accents.
- **Trust/privacy:** The third frame states “Always yours, no strings” and visibly repeats no ads, purchases, subscriptions, and watermarks. Those are app-specific claims, not a reusable pattern.
- **Effective:** The set is memorable and differentiated; the trust frame makes a complex commercial promise quickly.
- **Do not copy:** Its serif editorial identity, orange/green palette, sticker collage, repeated-text motif, subjects, or “no strings” framing. This app contains advertising, so Snapseed's trust claims would be misleading here.

### 5. Adobe Lightroom — 100M+

Source: [public Play listing](https://play.google.com/store/apps/details?id=com.adobe.lrmobile&hl=en_US&gl=US)

- **First screenshot promise:** A landscape video/cover tile says “Easily edit photos and get pro-quality results.” The first portrait screenshot says “Add a classic film look with one tap.” The portrait sequence is the relevant phone-carousel benchmark.
- **Headline hierarchy:** Eight words, bold black and centered across two lines. It is longer than the strongest utility headlines but remains legible because of high contrast and generous space.
- **Result-to-UI ratio:** About 65–70% photo outcome, 20–25% real editing controls, and 10–15% headline.
- **Before/after:** Subsequent screenshots use a vertical divider or stacked before/after images for color, object removal, blur, and subject enhancement.
- **Quantitative proof:** None; proof is the visual transformation and visible control state.
- **Subject scale:** Portraits and single scenes are large; subjects remain recognizable in thumbnails.
- **Color and contrast:** White field, black headline, saturated photography, dark editor controls, and small blue action accents.
- **Carousel consistency:** Strong headline, split-result, and editor-control grammar across phone and large-screen images.
- **Trust/privacy:** No dedicated trust/privacy frame in the inspected sequence.
- **Effective:** Large visual outcomes plus clearly readable app controls make the functionality tangible. Several frames show one controlled change rather than a generic finished photo.
- **Do not copy:** Adobe's subjects, split compositions, blue/black control styling, headline wording, “pro” positioning, or “one tap” claim. This project's flow must determine whether “one tap” is accurate.

### 6. Background Eraser — 100M+

Source: [public Play listing](https://play.google.com/store/apps/details?id=com.handycloset.android.eraser&hl=en_US&gl=US)

- **First screenshot promise:** A small three-step strip shows source → transparent cutout → composite, labeled “Background Eraser” and “Use As STAMP.” The outcome is literal but the hierarchy is weak.
- **Headline hierarchy:** Small labels embedded in the workflow rather than one bold headline; difficult at thumbnail size.
- **Result-to-UI ratio:** The first frame is primarily an explanatory composite with almost no recognizable app UI. The second is nearly 100% real eraser UI.
- **Before/after:** Explicit three-stage workflow with checkerboard transparency and arrows.
- **Quantitative proof:** None.
- **Subject scale:** The person is small in the first frame and medium in the editor capture; edge detail is hard to judge at thumbnail size.
- **Color and contrast:** White/beige field, cyan system UI, gray checkerboard, and a bright pink arrow. Overall contrast is modest.
- **Carousel consistency:** Only two public images were present in the inspected sequence, with different aspect ratios and limited shared art direction.
- **Trust/privacy:** No explicit trust or privacy message.
- **Effective:** Checkerboard transparency is immediately recognizable and the workflow is unambiguous at full size.
- **Do not copy:** The model, landmark image, arrow treatment, “stamp” terminology, dated cyan UI styling, or screenshot composition. Rights to competitor sample imagery are unknown.

### 7. Photo & Picture Resizer — 10M+

Source: [public Play listing](https://play.google.com/store/apps/details?id=com.simplemobilephotoresizer&hl=en_US&gl=US)

- **First screenshot promise:** A real home screen names the app and presents “Select Photos,” “Take a Photo,” and “Resized Photos.” It proves the product exists but does not isolate one persuasive benefit.
- **Headline hierarchy:** No marketing headline; the app bar/name and action list form the hierarchy.
- **Result-to-UI ratio:** Essentially 100% real UI throughout. Finished photo results occupy less space than controls and navigation.
- **Before/after:** One frame stacks resized and original images; a batch frame labels before/after columns.
- **Quantitative proof:** Strongest of the narrow utility set: visible examples include 3.8 MB source, 116 kB result and 98% savings, plus batch totals and 90% savings. These are competitor numbers and must never be reused.
- **Subject scale:** The sample group image is medium; batch thumbnails are small but supported by readable summary numbers.
- **Color and contrast:** Bright blue/white utility UI with multicolor action icons. Clear but visually dated.
- **Carousel consistency:** Mostly consistent real screens, although later device/resolution captures and duplicate flows reduce polish.
- **Trust/privacy:** No dedicated privacy frame. The real UI and “original” comparison provide functional reassurance rather than data-handling reassurance.
- **Effective:** Authentic controls, custom dimensions, batch output, and measured savings give concrete evidence.
- **Do not copy:** The blue app shell, exact sample photo, numbers, navigation, grid, social-platform references, or stacked comparison treatment. Only measured output from this project's real app may be shown.

### 8. Compress Image Size In kb & mb — 10M+

Source: [public Play listing](https://play.google.com/store/apps/details?id=com.mobso.photoreducer.lite&hl=en_US&gl=US)

- **First screenshot promise:** An almost empty “Compress Image” screen with a “Choose Photo” button. The function is apparent, but no result is demonstrated.
- **Headline hierarchy:** No marketing headline; small native UI text carries the entire message.
- **Result-to-UI ratio:** 100% UI, with large unused white space and a very small image/control cluster.
- **Before/after:** The sequence progresses from selection to target size, progress, and saved result, but it does not provide a useful side-by-side quality comparison.
- **Quantitative proof:** Result screens display actual-looking source and compressed file-size values. They are too small to communicate well at thumbnail scale and are not independently verifiable.
- **Subject scale:** A flower thumbnail occupies a small portion of the canvas; quality cannot be assessed reliably.
- **Color and contrast:** White canvas, dark blue bar, and pink accents. Body copy is low-impact because of scale.
- **Carousel consistency:** Consistent UI but several near-duplicate frames, making the sequence feel procedural rather than benefit-led.
- **Trust/privacy:** No privacy or on-device-processing message; an ad-related indicator appears in the chrome, which is unsuitable for a central marketing composition.
- **Effective:** The target-size entry flow is direct and the final file-size value is concrete.
- **Do not copy:** Sparse layout, tiny result, repeated states, flower image, size values, blue/pink styling, or ad-bearing chrome. Do not include production ads or placeholders in this project's assets.

### 9. Puma: Photo Resizer Compressor — 1M+

Source: [public Play listing](https://play.google.com/store/apps/details?id=com.compressphotopuma&hl=en_US&gl=US)

- **First screenshot promise:** “Compress MB to KB without quality loss.” The seven-word promise is direct and benefit-led, but “without quality loss” is an absolute claim that should not be adopted.
- **Headline hierarchy:** Large white type with the claimed benefit highlighted in yellow; brand mark is secondary.
- **Result-to-UI ratio:** Roughly 45% app crop and 55% headline/brand/negative space in frame one. Later frames devote most of the canvas to controls.
- **Before/after:** No convincing visible before/after quality proof in the inspected first eight phone frames, despite the listing describing a comparison tool.
- **Quantitative proof:** “MB to KB” and “under 1 MB” are broad target claims. Screens show exact target controls, not an evidenced source-to-result measurement.
- **Subject scale:** Gallery thumbnails and edit subjects are relatively small; control flows are the main visual subject.
- **Color and contrast:** Charcoal background, yellow headlines/actions, and white body type create excellent contrast.
- **Carousel consistency:** Strong recurring dark field, yellow accent, device crop, and top headline.
- **Trust/privacy:** The public description makes a privacy claim, but no dedicated proof frame appears in the inspected phone sequence.
- **Effective:** Clear one-task headlines and a consistent control-by-control story make the carousel easy to scan.
- **Do not copy:** Charcoal/yellow palette, cat mascot, exact device crops, headline wording, social presets, or “without quality loss.” Compression is inherently input- and setting-dependent.

### 10. JPEG Image Compressor & Resize — 1M+

Source: [public Play listing](https://play.google.com/store/apps/details?id=com.jpeg.image.compressor&hl=en_US&gl=US)

- **First screenshot promise:** “Image Compressor & Editor” over a four-action menu. It communicates breadth but not a measured benefit.
- **Headline hierarchy:** Four large words with “Compressor” in green; feature buttons form a strong secondary hierarchy.
- **Result-to-UI ratio:** About 65–70% app-like feature/result panel and 30–35% headline/decorative framing.
- **Before/after:** The second frame uses repeated portraits, an arrow, and 14 MB → 1 MB labels; the third uses circular size graphics and result rows. Neither proves fine-detail retention.
- **Quantitative proof:** Very prominent (including 14 MB → 1 MB and 650 MB → 22 MB), but the public creative does not expose reproducible evidence. The numbers must be treated as promotional claims, not facts for this project.
- **Subject scale:** A large portrait dominates the compression frame; batch and history subjects are smaller.
- **Color and contrast:** Dark navy/charcoal, bright green, white, and occasional pink/blue labels. Contrast is strong but visually busy.
- **Carousel consistency:** Strong dark/green system, repeated corner decoration, typography, and UI card treatments.
- **Trust/privacy:** No dedicated privacy or on-device-processing frame.
- **Effective:** Big numbers and format labels are easy to parse quickly; the carousel covers compression, batch, resize, conversion, and history.
- **Do not copy:** The subjects, numeric claims, multicolor format chips, dark-green system, corner decoration, exact menu, or unsupported PDF/HEIC capabilities. This project's screenshots must use its own verified JPG/PNG/WEBP flows and recorded measurements.

## Cross-competitor conclusions for the original asset system

1. **Lead with a verified result, not a menu.** Narrow utilities become more persuasive when the first frame shows an actual source size, result size, savings percentage, and a large quality detail. Empty start screens and home menus communicate existence but not value.
2. **Keep UI visible while letting the outcome dominate.** The strongest modern sets generally place one large visual result above or around a recognizable real UI crop. The planned 65–75% transformation/result area and no more than roughly 20% copy are consistent with both Google guidance and the clearest current carousels.
3. **Use one promise per frame.** Photoroom, Lightroom, Puma, and Picsart demonstrate the thumbnail advantage of a short headline plus one transformation. The eight requested feature frames should retain their fixed role and not add secondary feature lists.
4. **Make numerical evidence reproducible.** Several compression competitors use aggressive or tiny figures without visible provenance. This listing can differentiate through real captured input/output values and a retained evidence record. Never reuse illustrative values from another listing.
5. **Fine texture is stronger quality proof than a generic portrait.** Hair, fabric, foliage, or other high-frequency detail lets the viewer evaluate compression and background edges. It must come from a rights-cleared source and remain unsharpened.
6. **Checkerboard transparency is a widely understood convention.** It can communicate a real transparent result quickly, but the subject, framing, color system, and UI crop must be original.
7. **Privacy language is a potential differentiator only when narrowly accurate.** A dedicated trust frame can state on-device image processing and no photo uploads if verified. It must not imply that AdMob or the entire application is offline or network-free.
8. **Brand consistency matters more than copying category conventions.** Use the specified blue/pale-blue Material 3-inspired system and `ic_splitframe_logo.png`; avoid competitor dark-magenta, cyan-violet, violet-outline, retro orange-green, blue utility, yellow-charcoal, and green-charcoal identities.
9. **Large-screen assets should be UI-led.** Google's large-screen guidance warns that added copy may crop on some promotional surfaces. Use genuine tablet/Chromebook captures with minimal decoration rather than repurposing the text-heavy phone carousel.
10. **Experiment one major variable at a time.** Compare measured-storage-savings versus sharp-detail emphasis with identical subjects, layout, colors, remaining screenshots, listing copy, and traffic conditions. Treat an inconclusive result as insufficient evidence, not proof that both variants are equally effective.

## Source index

Official requirements reviewed:

- [Add preview assets to showcase your app](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en)
- [Google Play icon design specifications](https://developer.android.com/distribute/google-play/resources/icon-design-specifications)
- [Best practices for your store listing](https://support.google.com/googleplay/android-developer/answer/13393723?hl=en)
- [Translate and localize your app](https://support.google.com/googleplay/android-developer/answer/9844778?hl=en)
- [Create custom store listings to target specific user segments](https://support.google.com/googleplay/android-developer/answer/9867158?hl=en)

Competitor sources are the ten public Google Play listing links in the install-band table and each detailed section above. All claims about this project must ultimately be governed by the separate feature-evidence matrix and captured real-app results, not by category convention.
