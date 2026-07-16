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

The app includes a `BackgroundRemovalRepository` abstraction, domain use case, UI workflow, progress state, error state, and background replacement/export UI. This build does not bundle a production-quality segmentation model or online provider API key, so removal reports an unavailable state instead of generating fake transparent images.

To enable removal later, add an offline model or online provider behind `BackgroundRemovalRepository` without changing UI/domain layers.
