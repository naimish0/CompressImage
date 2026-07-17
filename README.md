# Photo Compressor

Photo Compressor is a Kotlin + Jetpack Compose Android app for selecting, compressing, resizing, converting, comparing, saving, opening, and sharing images. Image processing happens entirely on the device. An internet connection may be used to load advertisements and advertising consent information.

## Run

```bash
./gradlew :app:assembleDebug
```

Open the project in Android Studio and run the `app` configuration on a device or emulator. The app uses Android's system photo picker and does not request broad storage access.

## Tests

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

## Production Configuration

Debug builds use Google's official sample AdMob app ID and test ad-unit IDs.
Release builds only enable ads when all production identifiers are supplied
through Gradle properties:

```properties
PHOTO_COMPRESSOR_ADMOB_APP_ID=ca-app-pub-...
PHOTO_COMPRESSOR_ADMOB_TOP_BANNER_ID=ca-app-pub-.../...
PHOTO_COMPRESSOR_ADMOB_BOTTOM_BANNER_ID=ca-app-pub-.../...
PHOTO_COMPRESSOR_ADMOB_INLINE_ID=ca-app-pub-.../...
PHOTO_COMPRESSOR_ADMOB_HISTORY_INTERSTITIAL_ID=ca-app-pub-.../...
PHOTO_COMPRESSOR_ADMOB_SAVE_INTERSTITIAL_ID=ca-app-pub-.../...
```

If release identifiers are missing, ads are disabled at runtime and the release
build does not fall back to Google's sample ad-unit IDs.

Short Gradle property aliases are also supported for CI:
`ADMOB_APP_ID`, `TOP_BANNER_AD_UNIT_ID`, `BOTTOM_BANNER_AD_UNIT_ID`,
`INLINE_AD_UNIT_ID`, `HISTORY_INTERSTITIAL_AD_UNIT_ID`, and
`SAVE_INTERSTITIAL_AD_UNIT_ID`.

Before Play Store release, add a final privacy policy URL, production signing,
verified background-removal model licence records, AdMob GDPR/US privacy
messages, app-ads.txt, and Play Console data-safety disclosures.

## Background Removal

The app includes an offline `BackgroundRemovalRepository` implementation using ONNX Runtime Android and a bundled U2-NetP ONNX model. The workflow runs on-device, exports a transparent PNG, and does not upload user images.

The U2-NetP model provenance, conversion process, checksums, tensor contract, and license notes are documented in `legal/MODEL_PROVENANCE.md`. This build relies on the project owner's explicit confirmation that the official pretrained U2-NetP weights may be used commercially and redistributed in the app.
