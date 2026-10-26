plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.android.notepad"
    compileSdk { version = release(37) }
    defaultConfig {
        applicationId = "com.example.test.notepad"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.example.android.notepad.NotePadTestRunner"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
