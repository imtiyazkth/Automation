import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

// Reads local.properties for secrets so nothing sensitive is hardcoded or committed.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(f.inputStream())
}
fun secret(key: String): String = (localProps[key] as? String) ?: System.getenv(key) ?: ""

android {
    namespace = "com.personalai.os"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.personalai.os"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0-scaffold"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Wire real keys in local.properties (never commit that file).
        buildConfigField("String", "GEMINI_API_KEY", "\"${secret("GEMINI_API_KEY")}\"")
        buildConfigField("String", "WHATSAPP_CLOUD_API_TOKEN", "\"${secret("WHATSAPP_CLOUD_API_TOKEN")}\"")
        buildConfigField("String", "WHATSAPP_PHONE_NUMBER_ID", "\"${secret("WHATSAPP_PHONE_NUMBER_ID")}\"")
        buildConfigField("String", "TELEGRAM_BOT_TOKEN", "\"${secret("TELEGRAM_BOT_TOKEN")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    // Core / Compose
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Room (encrypted via SQLCipher wrapper — see data/AppDatabase.kt notes)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // WorkManager for scheduled workflows (e.g. daily job search)
    implementation("androidx.work:work-runtime-ktx:2.9.1")

    // Networking (Gemini / WhatsApp Cloud API / Telegram Bot API)
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // JSON parsing for agent registry definitions
    implementation("com.google.code.gson:gson:2.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Document processing (Excel export) — see tools/ExcelExportTool.kt
    implementation("org.apache.poi:poi-ooxml:5.2.5")

    // Local on-device LLM runtime: NOT bundled by default (large native binaries / model
    // weights). Integrate one of these yourself in core/ai/LocalAiProvider.kt:
    //   - llama.cpp Android JNI build (github.com/ggerganov/llama.cpp -> examples/android)
    //   - MLC-LLM Android runtime (github.com/mlc-ai/mlc-llm)
    // Until wired in, LocalAiProvider falls back to a rule-based intent classifier so the
    // rest of the pipeline (planning, permissions, agents, workflows) is fully testable.

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
