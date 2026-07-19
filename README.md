# Photo Compressor

Photo Compressor is a Kotlin + Jetpack Compose Android app for selecting, compressing, resizing, converting, comparing, saving, opening, and sharing images. Image processing happens entirely on the device. An internet connection may be used to load advertisements and advertising consent information.

## Run

```bash
./gradlew :app:assembleDebug
```

Open the project in Android Studio and run the `app` configuration on a device or emulator. The app uses Android's system photo picker for input and does not request read access to the user's complete photo library. On Android 9 (API 28) and earlier, saving a generated image to the shared Pictures collection requests the legacy `WRITE_EXTERNAL_STORAGE` permission. Android 10 and later use `MediaStore` without that broad storage permission.

## Tests

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:lintDebug
```

## Advertising Configuration

Ads are enabled in both build types. Debug builds use Google's official sample identifiers and test mode; release builds use the configured production identifiers and disable test mode. Google User Messaging Platform consent state controls when the Google Mobile Ads SDK can request ads.

The current implementation includes:

- Adaptive banner ads at eligible top, bottom, home-empty-space, and result-empty-space placements.
- Inline native ads on eligible screens.
- Interstitial ads at eligible History, Save, Share, and Open opportunities. History uses the history interstitial configuration; Save, Share, and Open share the save interstitial configuration.
- App-open ads on eligible returns from the background, subject to the app's eligibility and suppression checks.

| Build | AdMob App ID | Banner ID (top, bottom, inline) | Native ID | Interstitial ID (History, Save, Share, Open) | App-open ID |
| --- | --- | --- | --- | --- | --- |
| Debug/test | `ca-app-pub-3940256099942544~3347511713` | `ca-app-pub-3940256099942544/9214589741` | `ca-app-pub-3940256099942544/2247696110` | `ca-app-pub-3940256099942544/1033173712` | `ca-app-pub-3940256099942544/9257395921` |
| Release | `ca-app-pub-7742442202074564~2488993156` | `ca-app-pub-7742442202074564/5574321499` | `ca-app-pub-7742442202074564/8690760808` | `ca-app-pub-7742442202074564/1251933104` | `ca-app-pub-7742442202074564/6620644008` |

The canonical declarations are centralized in `app/build.gradle.kts` and exposed through generated `BuildConfig` fields. The AdMob App ID is also supplied to the SDK manifest metadata.

Before publishing, complete the publisher-owned and Play Console tasks in `play-store/store-listing.md`, host the final `PRIVACY_POLICY.md` as an active, publicly accessible, non-geofenced, non-editable HTTPS webpage (not a PDF), configure and verify AdMob privacy messages and app-ads.txt, and sign a fresh release App Bundle with the production upload key.

## Background Removal

The app includes an offline `BackgroundRemovalRepository` implementation using ONNX Runtime Android and a bundled U2-NetP ONNX model. The workflow runs on-device, supports transparent PNG or WEBP output and colored PNG, WEBP, or JPG output, and does not upload user images.

The U2-NetP model's pinned public artifact, checksum, redistribution chain,
tensor contract, and included license notices are documented in
`legal/MODEL_PROVENANCE.md`.
