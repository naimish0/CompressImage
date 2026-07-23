plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

// This is the single source of truth for packaged locales, Android's generated
// locale config, and the in-app language selector. Keep the order aligned with
// the product language list.
val supportedLocaleResources = listOf(
    "en" to "en",
    "de" to "de",
    "fr" to "fr",
    "ja" to "ja",
    "hi" to "hi",
    "ru" to "ru",
    "es" to "es",
    "pt-PT" to "pt-rPT",
    "pt-BR" to "pt-rBR",
    "it" to "it",
    // Android resources retain the legacy `in` qualifier for Indonesian.
    // LocaleConfig and the public language tag remain the modern `id`.
    "id" to "in",
    "ar" to "ar",
    "ko" to "ko",
    "ur" to "ur",
)
val supportedProductionLanguageTags = supportedLocaleResources
    .joinToString(separator = ",") { (languageTag, _) -> languageTag }
val productionResourceQualifiers = supportedLocaleResources
    .map { (_, resourceQualifier) -> resourceQualifier }

val appVersionCode = 2
val appVersionName = "1.0"
val releaseBundleFileName =
    "Photo-Compressor-BG-Remover-v$appVersionName-code$appVersionCode.aab"

android {
    namespace = "com.rameshta.photocompressor"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.rameshta.photocompressor"
        minSdk = 24
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "SUPPORTED_LOCALE_TAGS",
            "\"$supportedProductionLanguageTags\"",
        )
    }

    val testAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
    val testBannerAdUnitId = "ca-app-pub-3940256099942544/9214589741"
    val testInterstitialAdUnitId = "ca-app-pub-3940256099942544/1033173712"
    val testAppOpenAdUnitId = "ca-app-pub-3940256099942544/9257395921"
    val testNativeAdUnitId = "ca-app-pub-3940256099942544/2247696110"
    val releaseAdMobAppId = "ca-app-pub-7742442202074564~2488993156"
    val releaseBannerAdUnitId = "ca-app-pub-7742442202074564/5574321499"
    val releaseInterstitialAdUnitId = "ca-app-pub-7742442202074564/1251933104"
    val releaseAppOpenAdUnitId = "ca-app-pub-7742442202074564/6620644008"
    val releaseNativeAdUnitId = "ca-app-pub-7742442202074564/8690760808"
    val releaseTopBannerAdUnitId = releaseBannerAdUnitId
    val releaseBottomBannerAdUnitId = releaseBannerAdUnitId
    val releaseInlineAdUnitId = releaseBannerAdUnitId
    val releaseHistoryInterstitialAdUnitId = releaseInterstitialAdUnitId
    val releaseSaveInterstitialAdUnitId = releaseInterstitialAdUnitId
    val releaseAdIds = listOf(
        releaseAdMobAppId,
        releaseTopBannerAdUnitId,
        releaseBottomBannerAdUnitId,
        releaseInlineAdUnitId,
        releaseHistoryInterstitialAdUnitId,
        releaseSaveInterstitialAdUnitId,
        releaseAppOpenAdUnitId,
        releaseNativeAdUnitId,
    )
    if (releaseAdIds.any { it.contains("ca-app-pub-3940256099942544") }) {
        throw GradleException("Release AdMob properties must not use Google's sample ad IDs.")
    }

    buildTypes {
        debug {
            isPseudoLocalesEnabled = true
            manifestPlaceholders["ADMOB_APP_ID"] = testAdMobAppId
            buildConfigField("Boolean", "ADS_ENABLED", "true")
            buildConfigField("Boolean", "ADS_TEST_MODE", "true")
            buildConfigField("String", "ADMOB_APP_ID", "\"$testAdMobAppId\"")
            buildConfigField("String", "TOP_BANNER_AD_UNIT_ID", "\"$testBannerAdUnitId\"")
            buildConfigField("String", "BOTTOM_BANNER_AD_UNIT_ID", "\"$testBannerAdUnitId\"")
            buildConfigField("String", "INLINE_AD_UNIT_ID", "\"$testBannerAdUnitId\"")
            buildConfigField("String", "HISTORY_INTERSTITIAL_AD_UNIT_ID", "\"$testInterstitialAdUnitId\"")
            buildConfigField("String", "SAVE_INTERSTITIAL_AD_UNIT_ID", "\"$testInterstitialAdUnitId\"")
            buildConfigField("String", "ADMOB_APP_OPEN_AD_UNIT_ID", "\"$testAppOpenAdUnitId\"")
            buildConfigField("String", "NATIVE_AD_UNIT_ID", "\"$testNativeAdUnitId\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["ADMOB_APP_ID"] = releaseAdMobAppId
            buildConfigField("Boolean", "ADS_ENABLED", "true")
            buildConfigField("Boolean", "ADS_TEST_MODE", "false")
            buildConfigField("String", "ADMOB_APP_ID", "\"$releaseAdMobAppId\"")
            buildConfigField("String", "TOP_BANNER_AD_UNIT_ID", "\"$releaseTopBannerAdUnitId\"")
            buildConfigField("String", "BOTTOM_BANNER_AD_UNIT_ID", "\"$releaseBottomBannerAdUnitId\"")
            buildConfigField("String", "INLINE_AD_UNIT_ID", "\"$releaseInlineAdUnitId\"")
            buildConfigField("String", "HISTORY_INTERSTITIAL_AD_UNIT_ID", "\"$releaseHistoryInterstitialAdUnitId\"")
            buildConfigField("String", "SAVE_INTERSTITIAL_AD_UNIT_ID", "\"$releaseSaveInterstitialAdUnitId\"")
            buildConfigField("String", "ADMOB_APP_OPEN_AD_UNIT_ID", "\"$releaseAppOpenAdUnitId\"")
            buildConfigField("String", "NATIVE_AD_UNIT_ID", "\"$releaseNativeAdUnitId\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    installation {
        // Avoid local release deploy failures caused by stale secondary-dex profile
        // state on test devices. Packaged release baseline profiles are unchanged.
        enableBaselineProfile = false
    }
    androidResources {
        generateLocaleConfig = true
        localeFilters += productionResourceQualifiers
        noCompress += "onnx"
    }
    bundle {
        // The app has an in-app language picker and must be able to switch to
        // every shipped locale immediately, including while offline. Keep all
        // language resources in the base APK delivered by Google Play.
        language {
            enableSplit = false
        }
    }
    lint {
        // Source catalogs outside localeFilters are intentionally not shipped.
        // tools/check_localizations.py enforces complete parity for every
        // packaged production locale, including placeholders and plurals.
        disable += "MissingTranslation"
    }
    sourceSets {
        getByName("main") {
            assets.directories.add("../legal")
        }
        getByName("debug") {
            // Rights-cleared samples and genuine app outputs used only by the
            // deterministic Play Store screenshot harness.
            assets.directories.add("../play-store-assets/source/sample-images")
        }
    }
}

val exportNamedReleaseBundle = tasks.register("exportNamedReleaseBundle") {
    group = "build"
    description = "Creates an upload-ready AAB named with the app and version."

    val releaseBundleDirectory = layout.buildDirectory.dir("outputs/bundle/release")
    val generatedBundle = releaseBundleDirectory.map { it.file("app-release.aab") }
    val namedBundle = releaseBundleDirectory.map { it.file(releaseBundleFileName) }
    inputs.file(generatedBundle)
    outputs.file(namedBundle)

    doLast {
        generatedBundle.get().asFile.copyTo(
            target = namedBundle.get().asFile,
            overwrite = true,
        )
    }
}

tasks.configureEach {
    if (name == "bundleRelease") {
        finalizedBy(exportNamedReleaseBundle)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.process)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.exifinterface)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.coil.compose)
    implementation(libs.google.hilt.android)
    ksp(libs.google.hilt.compiler)
    implementation(libs.onnxruntime.android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.google.play.services.ads)
    implementation(libs.google.user.messaging.platform)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
