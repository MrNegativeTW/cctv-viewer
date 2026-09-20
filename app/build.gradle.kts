import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// 本機設定只供建置使用；公開安裝檔可用 -PincludeLocalCameras=false 排除。
val includeLocalCameras = providers.gradleProperty("includeLocalCameras")
    .map { it.toBooleanStrict() }
    .orElse(true)
val cameraProperties = Properties().apply {
    if (includeLocalCameras.get()) {
        val contents = providers.fileContents(
            rootProject.layout.projectDirectory.file("cameras.local.properties")
        ).asText.orElse("").get()
        contents.reader().use { load(it) }
    }
}

fun javaStringLiteral(value: String): String = buildString {
    append('"')
    value.forEach { character ->
        when (character) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> if (character.code < 0x20) {
                append("\\u%04x".format(character.code))
            } else {
                append(character)
            }
        }
    }
    append('"')
}

val cameraNames = (1..8).map { index ->
    cameraProperties.getProperty("camera.$index.name")?.takeIf { it.isNotBlank() }
        ?: "攝影機 $index"
}
val cameraUrls = (1..8).map { index -> cameraProperties.getProperty("camera.$index.url", "") }

android {
    namespace = "com.txwstudio.app.cctvviewer"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.txwstudio.app.cctvviewer"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        buildConfigField(
            "String[]", "CAMERA_NAMES",
            cameraNames.joinToString(prefix = "new String[] {", postfix = "}", transform = ::javaStringLiteral),
        )
        buildConfigField(
            "String[]", "CAMERA_URLS",
            cameraUrls.joinToString(prefix = "new String[] {", postfix = "}", transform = ::javaStringLiteral),
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
