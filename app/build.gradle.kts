import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("dagger.hilt.android.plugin")
}

// Load version properties from version.properties.
val versionPropsFile = file("version.properties")
val versionProps = Properties().apply {
    load(FileInputStream(versionPropsFile))
}

// Define your base app name.
val appName = "GpxAnalyzer"

android {
    namespace = "com.itservices.gpxanalyzer"
    compileSdk = 34

    flavorDimensions += "environment"

    defaultConfig {
        applicationId = "com.itservices.gpxanalyzer"
        minSdk = 24
        targetSdk = 34

        multiDexEnabled = true

        versionCode = versionProps["VERSION_CODE"].toString().toInt()
        versionName = versionProps["VERSION_NAME"].toString()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Room schema export location
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
            }
        }

        // Load secure properties
/*        val secureProperties = Properties()
        val securePropertiesFile = rootProject.file("app/secure.properties")
        if (securePropertiesFile.exists()) {
            secureProperties.load(FileInputStream(securePropertiesFile))
        }

        // Make API keys available in BuildConfig
        buildConfigField("String", "API_KEY", "\"${secureProperties.getProperty("API_KEY", "")}\"")*/

    }

    // Define product flavors.
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
        }
        create("staging") {
            dimension = "environment"
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
        }
        create("prod") {
            dimension = "environment"
        }
    }

    // Define build types.
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ""
            versionNameSuffix = "-debug"
            isDebuggable = true
        }
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
        }
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.md",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

    // Lint configuration.
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(files("libs/proj4j-1.1.0.jar"))

    implementation(libs.osmdroid.android)

    // RxJava dependencies.
    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    // Networking - Retrofit, OkHttp, Gson
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.adapter.rxjava2) // Use RxJava2 adapter to match project
    implementation(platform(libs.okhttp.bom)) // Use BOM for consistent OkHttp versions
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor) // Optional: For logging network requests

    implementation(libs.joda.time)
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.android.compiler)
    implementation(libs.javax.inject)

    implementation(libs.multidex)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.protolite.well.known.types)

    implementation(libs.math3)

    // Room dependencies
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.rxjava2)
    annotationProcessor(libs.androidx.room.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Required for OkHttp
    implementation("org.bouncycastle:bctls-jdk15on:1.68")
    implementation("org.conscrypt:conscrypt-android:2.5.2")
    implementation("org.openjsse:openjsse:1.1.10")
    
    // Required for Joda-Time
    implementation("org.joda:joda-convert:2.2.3")
}