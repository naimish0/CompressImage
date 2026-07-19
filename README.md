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
Release builds use the production AdMob identifiers centralized in
`app/build.gradle.kts`: the AdMob App ID is used only for the Google Mobile Ads
SDK manifest metadata, one banner ad-unit ID is used for every banner placement,
and one interstitial ad-unit ID is used for every interstitial placement.

Before Play Store release, add the publisher's final privacy-policy URL and
production signing, then complete AdMob privacy messages, app-ads.txt, and Play
Console declarations using the prepared release documentation.

## Background Removal

The app includes an offline `BackgroundRemovalRepository` implementation using ONNX Runtime Android and a bundled U2-NetP ONNX model. The workflow runs on-device, exports a transparent PNG, and does not upload user images.

The U2-NetP model's pinned public artifact, checksum, redistribution chain,
tensor contract, and included license notices are documented in
`legal/MODEL_PROVENANCE.md`.
