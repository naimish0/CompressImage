plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

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
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val testAdMobAppId = "ca-app-pub-3940256099942544~3347511713"
    val testBannerAdUnitId = "ca-app-pub-3940256099942544/9214589741"
    val testInterstitialAdUnitId = "ca-app-pub-3940256099942544/1033173712"
    val disabledReleaseAdMobAppId = "ca-app-pub-0000000000000000~0000000000"
    fun releaseAdProperty(projectPropertyName: String, shortPropertyName: String): String {
        return providers.gradleProperty(projectPropertyName)
            .orElse(providers.gradleProperty(shortPropertyName))
            .orElse("")
            .get()
            .trim()
    }

    val releaseAdMobAppId = releaseAdProperty("PHOTO_COMPRESSOR_ADMOB_APP_ID", "ADMOB_APP_ID")
    val releaseTopBannerAdUnitId = releaseAdProperty("PHOTO_COMPRESSOR_ADMOB_TOP_BANNER_ID", "TOP_BANNER_AD_UNIT_ID")
    val releaseBottomBannerAdUnitId = releaseAdProperty("PHOTO_COMPRESSOR_ADMOB_BOTTOM_BANNER_ID", "BOTTOM_BANNER_AD_UNIT_ID")
    val releaseInlineAdUnitId = releaseAdProperty("PHOTO_COMPRESSOR_ADMOB_INLINE_ID", "INLINE_AD_UNIT_ID")
    val releaseHistoryInterstitialAdUnitId = releaseAdProperty("PHOTO_COMPRESSOR_ADMOB_HISTORY_INTERSTITIAL_ID", "HISTORY_INTERSTITIAL_AD_UNIT_ID")
    val releaseSaveInterstitialAdUnitId = releaseAdProperty("PHOTO_COMPRESSOR_ADMOB_SAVE_INTERSTITIAL_ID", "SAVE_INTERSTITIAL_AD_UNIT_ID")
    val releaseAdIds = listOf(
        releaseAdMobAppId,
        releaseTopBannerAdUnitId,
        releaseBottomBannerAdUnitId,
        releaseInlineAdUnitId,
        releaseHistoryInterstitialAdUnitId,
        releaseSaveInterstitialAdUnitId,
    )
    if (releaseAdIds.any { it.contains("ca-app-pub-3940256099942544") }) {
        throw GradleException("Release AdMob properties must not use Google's sample ad IDs.")
    }
    val releaseAdsConfigured = releaseAdIds.all { it.isNotBlank() }

    buildTypes {
        debug {
            manifestPlaceholders["ADMOB_APP_ID"] = testAdMobAppId
            buildConfigField("Boolean", "ADS_ENABLED", "true")
            buildConfigField("Boolean", "ADS_TEST_MODE", "true")
            buildConfigField("String", "ADMOB_APP_ID", "\"$testAdMobAppId\"")
            buildConfigField("String", "TOP_BANNER_AD_UNIT_ID", "\"$testBannerAdUnitId\"")
            buildConfigField("String", "BOTTOM_BANNER_AD_UNIT_ID", "\"$testBannerAdUnitId\"")
            buildConfigField("String", "INLINE_AD_UNIT_ID", "\"$testBannerAdUnitId\"")
            buildConfigField("String", "HISTORY_INTERSTITIAL_AD_UNIT_ID", "\"$testInterstitialAdUnitId\"")
            buildConfigField("String", "SAVE_INTERSTITIAL_AD_UNIT_ID", "\"$testInterstitialAdUnitId\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            manifestPlaceholders["ADMOB_APP_ID"] = if (releaseAdsConfigured) {
                releaseAdMobAppId
            } else {
                disabledReleaseAdMobAppId
            }
            buildConfigField("Boolean", "ADS_ENABLED", releaseAdsConfigured.toString())
            buildConfigField("Boolean", "ADS_TEST_MODE", "false")
            buildConfigField("String", "ADMOB_APP_ID", "\"${if (releaseAdsConfigured) releaseAdMobAppId else ""}\"")
            buildConfigField("String", "TOP_BANNER_AD_UNIT_ID", "\"${if (releaseAdsConfigured) releaseTopBannerAdUnitId else ""}\"")
            buildConfigField("String", "BOTTOM_BANNER_AD_UNIT_ID", "\"${if (releaseAdsConfigured) releaseBottomBannerAdUnitId else ""}\"")
            buildConfigField("String", "INLINE_AD_UNIT_ID", "\"${if (releaseAdsConfigured) releaseInlineAdUnitId else ""}\"")
            buildConfigField("String", "HISTORY_INTERSTITIAL_AD_UNIT_ID", "\"${if (releaseAdsConfigured) releaseHistoryInterstitialAdUnitId else ""}\"")
            buildConfigField("String", "SAVE_INTERSTITIAL_AD_UNIT_ID", "\"${if (releaseAdsConfigured) releaseSaveInterstitialAdUnitId else ""}\"")
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
    androidResources {
        noCompress += "onnx"
    }
    sourceSets {
        getByName("main") {
            assets.directories.add("../legal")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
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
