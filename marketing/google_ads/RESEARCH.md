# Google Ads creative research

Accessed: **2026-08-26 (Asia/Kolkata)**

## Scope correction

The repository is not a puzzle game. It is the Android utility **Photo Compressor & BG Remover** (`com.rameshta.photocompressor`). There are no boards, levels, moves, lives, timers, hints, boosters, coins, streaks, game rewards, difficulty bands, animations presented as game mechanics, or gameplay audio. Creating puzzle situations or describing these app workflows as gameplay would be misleading.

This package therefore applies the requested ad formats and pacing to truthful, real-app workflows. Puzzle-game benchmarks are retained below only as transferable creative research; they are not sources for product claims or artwork.

## Repository-verified product profile

| Area | Verified finding | Repository source |
| --- | --- | --- |
| Product | Photo compression, resizing, conversion, comparison, background removal/replacement, History, Save, Open, and Share | `README.md`; `play-store/store-listing.md` |
| Control method | Tap controls, chips, buttons, scrolling, and Android's system photo picker | `app/src/main/java/.../ui/`; `README.md` |
| Differentiator | Compression toward a chosen KB/MB target plus bundled on-device U2-NetP background removal in one utility | `README.md`; `play-store/store-listing.md` |
| Modes | Single or batch compression; quality modes; preset/custom resize; JPG/PNG/WEBP output; transparent or solid-color background output | `play-store/store-listing.md` |
| Batch capacity | Up to 50 selected images | `play-store/store-listing.md` |
| Progress | Processing progress, completed-batch summary, and local History exist; game progression does not | `BatchSummaryScreen.kt`; `HistoryScreen.kt` |
| Network truth | Core image processing runs on-device and selected images are not uploaded for processing. The app still uses internet access for Google ads and consent, so the whole app must not be advertised as fully offline | `README.md`; `play-store-assets/README.md` |
| Orientation | Production does not lock orientation; the debug capture activity explicitly allows unspecified orientation. Phone portrait and 1920×1080 landscape captures rendered successfully | `app/src/main/AndroidManifest.xml`; `app/src/debug/AndroidManifest.xml` |
| Visual identity | Material 3; teal `#006D77`; pale `#F7FAF9`; warm yellow secondary container; system sans typography; rounded cards and controls | `ui/theme/Color.kt`; `ui/theme/Type.kt` |
| Sound | No repository audio assets or app sound playback were found | repository-wide source/resource inspection |
| Audience | Ages 13+; everyday photo cleanup, smaller sharing files, product photos, documents, and social posts | `play-store/store-listing.md` |
| Deterministic capture | Debug-only `PlayStoreCaptureActivity` provides seeded, rights-cleared samples and genuine app-result fixtures while excluding ads from the capture surface | `app/src/debug/.../PlayStoreCaptureActivity.kt`; `play-store-assets/source/` |

The installed build confirmed the Home, configuration, batch summary, comparison, background, and History states used in this package. No production behavior or ad configuration was changed.

## Current Google Ads requirements

Live verification succeeded against the four requested official pages.

- [App campaign asset requirements](https://support.google.com/google-ads/answer/17091671?hl=en): 1–5 headlines of up to 30 characters and 1–5 descriptions of up to 90 characters; supported image targets include horizontal 1200×628, square 1200×1200, and vertical 1200×1500 with a 5 MB limit. Videos can be 16:9, 9:16, or 1:1, must be 10–60 seconds, and must be hosted on YouTube before campaign use.
- [Performance Max asset requirements](https://support.google.com/google-ads/answer/17091269?hl=en): headlines are up to 30 characters, with at least one up to 15; long headlines and descriptions are up to 90. The documented ranges allow 3–15 headlines, 1–5 long headlines, and 2–5 descriptions. Google recommends broad image coverage and custom video in the three principal orientations.
- [App creative best practices](https://support.google.com/google-ads/answer/6167158?hl=en): each text asset must work independently; focus each line on one selling point; avoid hype, clickbait, emojis, and redundant wording. Show the app experience immediately, create early visual change, design for muted viewing, keep overlays minimal, and iterate using asset reporting.
- [Gameplay video guidance](https://support.google.com/google-ads/answer/13268252?hl=en): Google's “gameplay video” category is actual in-game footage and applies to games. Because this repository is a utility, these exports are accurately labeled **real app workflow videos**, not gameplay videos.

Package decisions: 15 short headlines, five long headlines, five descriptions; 12 images in all three requested formats; and two 16–21 second concepts in 9:16, 1:1, and 16:9. Images are text-free real UI. Videos contain no synthetic scenes, fake buttons, or unlicensed music.

## Market and creative benchmark

### Direct product-category competitors

| Product | Current presentation observed | Transferable lesson; never copy |
| --- | --- | --- |
| [Photo Compressor and Resizer](https://play.google.com/store/apps/details?id=com.photocompress.photoeditor) | Leads with a clear compression/resizing task and compact utility screens | Name the task immediately and keep one action per frame |
| [Compress Image – MB to KB](https://play.google.com/store/apps/details?id=com.psoffritti.compress.image) | 1M+ Play install band; emphasizes a specific file-size target, live preview, manual controls, and batch handling | Show the selected target and a real preview, but do not promise an exact result or “no quality loss” |
| [Compress Photo Size – MB to KB](https://play.google.com/store/apps/details?id=com.mobso.photoreducer) | 1M+ Play install band; uses simple task labels, KB/MB proof, and batch utility framing | Numerical evidence is legible faster than feature lists |

These competitors largely use blue/white utility palettes, screenshot-heavy cards, and strong “MB to KB” wording. This app can look distinct through its quieter teal/yellow Material 3 identity, a human subject, and truthful before/after proof.

### High-install puzzle benchmarks requested by the brief

| Product | Current Play presentation | Transferable creative principle only |
| --- | --- | --- |
| [Candy Crush Saga](https://play.google.com/store/apps/details?id=com.king.candycrushsaga) | 1B+ Play install band; colorful board fills most of the frame, a move produces an immediate chain reaction, and trailers start in action | Lead on the consequence of one understandable action, not on a logo |
| [Royal Match](https://play.google.com/store/apps/details?id=com.dreamgames.royalmatch) | 100M+ Play install band; large pieces, strong character/color hierarchy, close-call framing, and quick outcome beats | Keep the subject/control large enough to parse in one second. Do not imitate rescue scenarios or any play not present in this app |

Public reviews visible on Royal Match's current Play presentation also complain about ads/store scenes that do not match ordinary play. That is a useful warning: the installed product, not an attention-grabbing fantasy scenario, is the source of truth.

### Newer distinctive puzzle and adjacent creative tools

- [Color Block Jam](https://play.google.com/store/apps/details?id=com.GybeGames.ColorBlockJam) has a 10M+ Play install band and a trailer-led presentation built around large color blocks moving to matching exits. The one-second lesson is a simple visual rule, central action, and immediate disappearance/reveal.
- [Picsart](https://play.google.com/store/apps/details?id=com.picsart.studio) and [Canva](https://play.google.com/store/apps/details?id=com.canva.editor) are highly installed adjacent creative apps. Their store presentations lead with finished outcomes and recognizable use cases rather than dense settings screens.
- [Photoroom](https://play.google.com/store/apps/details?id=com.photoroom.app) uses a distinctive subject-first, before-to-after style. Its [official workflow video](https://www.youtube.com/watch?v=inyzCXmjNA8), [official promo](https://www.youtube.com/watch?v=EssunFiuFCk), [background-removal help](https://help.photoroom.com/en/articles/13713793-remove-the-background-of-a-photo-with-background-remover-android), and [ad-assets landing page](https://www.photoroom.com/solutions/ad-assets) consistently foreground a clean result. The safe transferable idea is the visible original-to-transparent transformation, not its templates, claims, branding, or layouts.
- [Google Ads Transparency Center](https://adstransparency.google.com/?platform=SEARCH&region=US) and [TikTok Creative Center Top Ads](https://ads.tiktok.com/business/creativecenter/inspiration/topads/pc/en?rid=ugi8ugniuhg) were reviewed as public, dynamic ad-example indexes. Because their result sets and asset URLs change, this research does not treat a transient library result as durable evidence about a named competitor.

## What reads in one second

- A subject isolated against the transparent checkerboard.
- “3 images selected” followed by a 100% completed batch.
- The highlighted 205 kB target alongside the original/output dimensions.
- “Result ready” with original and processed views.
- A History row showing a real output name, dimensions, and file size.

Settings become unreadable when the full portrait screen is forced into a horizontal ad. Horizontal variants therefore crop around the relevant app region; they do not stretch or rearrange the UI.

## Tension, cause and outcome

- The truthful high-attention decision is a quality/size tradeoff: target size, quality mode, resize percentage, and output format. It replaces the brief's game-like “one move left” trope.
- A selected 205 kB target must not be presented as a guarantee. The retained real result is 236 kB and repository notes explicitly warn that exact targets may not be safely achievable at the configured quality floor.
- Cause and effect are strongest in the background-removal transition and the completed batch → comparison transition.
- Completion is the real 100% batch summary and History, not coins, rewards, mastery ranks, or game progression.

## Framing, HUD, captions, and hooks

- First frame: useful app UI, not a title card. Concept A starts with the chosen portrait and compression controls; Concept B starts with three selected images.
- First five seconds: at least two real changes—configuration scroll, action, and resulting screen.
- HUD/UI: preserve the page title, selected value, output proof, or completion state; crop away empty phone margins and unrelated controls.
- Captions: none were added. The real UI already labels the action and stays valid when muted.
- Branding: the app Home screen is used only for the final 1.2 seconds, within safe areas.

## Misleading patterns excluded

- Fake puzzle boards, levels, moves, timers, rewards, daily challenges, or game difficulty.
- “Lossless,” “no quality loss,” “exact target guaranteed,” “perfect,” or “instant” removal.
- “Fully offline,” “no ads,” “free,” “unlimited,” or data-collection-free claims.
- Invented touch indicators, comparison sliders, AI-generated sample photos, fake phone frames, install buttons, or product UI copied from competitors.
- Ads, consent UI, system notifications, personal data, debug labels, and test banners.

## Game-specific recommendation translated to this repository

Use the same clarity discipline as strong puzzle creatives—one rule, one action, one reaction—but apply it to real utility decisions. Lead with the transparent-background reveal or selected-size control, keep numerical proof readable, and finish on the genuine app Home screen. The strongest first test is the square background-removal video paired with “Remove photo backgrounds” and the on-device description.
