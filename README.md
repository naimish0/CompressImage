# Photo Compressor

Photo Compressor is a Kotlin + Jetpack Compose Android app for selecting, compressing, resizing, converting, comparing, saving, opening, and sharing images. Core image processing runs offline.

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

Debug builds use official Google Mobile Ads test IDs. Provide production AdMob IDs through Gradle properties before publishing:

```properties
PHOTO_COMPRESSOR_ADMOB_APP_ID=ca-app-pub-...
PHOTO_COMPRESSOR_ADMOB_BANNER_ID=ca-app-pub-.../...
PHOTO_COMPRESSOR_ADMOB_INTERSTITIAL_ID=ca-app-pub-.../...
```

Before Play Store release, add a final privacy policy URL, Google User Messaging Platform or equivalent consent flow where required, app-ads.txt setup, production signing, and Play Console data-safety disclosures.

## Background Removal

The app includes an offline `BackgroundRemovalRepository` implementation using ONNX Runtime Android and a bundled U2-NetP ONNX model. The workflow runs on-device, exports a transparent PNG, and does not upload user images.

The U2-NetP model provenance, conversion process, checksums, tensor contract, and license notes are documented in `legal/MODEL_PROVENANCE.md`. This build relies on the project owner's explicit confirmation that the official pretrained U2-NetP weights may be used commercially and redistributed in the app.
