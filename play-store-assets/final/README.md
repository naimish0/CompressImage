# Final Google Play assets

This directory contains the final localized asset package for the single
application ID `com.rameshta.photocompressor`.

- Locales: `en`, `de`, `fr`, `ja`, `hi`, `ru`, `es`, `pt-PT`, `pt-BR`, `it`,
  `id`, `ar`, `ko`, `ur`
- Device profiles: phone, 7-inch tablet, 10-inch tablet, Chromebook
- Per locale: 1 feature graphic, 1 all-feature overview sheet and 8 authentic
  UI screenshots for each device profile
- Shared asset: 1 package icon under `common/icon/`
- Total: 477 PNG files

The `all-features-3840x2160.png` image is stored directly in each locale folder
beside that language's screenshot device folders. It is a clearly separated
overview collage made from the locale's feature graphic and eight authentic
10-inch tablet captures, not evidence of a single app screen.

All screenshots use Play's maximum 3,840-pixel long edge: 2160×3840 for phone
and 3840×2160 for the landscape profiles and overview sheets. Exports are
lossless sRGB PNGs. The feature graphic and icon retain Play's required
1024×500 and 512×512 dimensions.

The screenshots are captures of production UI Composables rendered by a
debug-only harness with three registered safe sample images. Before/after
figures use retained, genuine app outputs. The capture harness suppresses ads
at the controller and throws if an ad request is attempted; it is excluded from
release builds.

Validation authority:

- `../qa/final-qa-report.md`
- `../qa/final-asset-manifest.csv`
- `../qa/final-evidence-manifest.csv`
- `../qa/final-contact-sheets/`

Reproduce the package with:

```shell
python3 play-store-assets/tools/capture_final_assets.py --serial emulator-5556
python3 play-store-assets/tools/generate_final_listing.py
python3 play-store-assets/tools/optimize_final_assets.py
python3 play-store-assets/tools/validate_final_assets.py
```
