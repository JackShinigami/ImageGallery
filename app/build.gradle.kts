plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.imagegallery"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.imagegallery"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        resourceConfigurations += "en"
        resourceConfigurations += "vi"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation ("com.journeyapps:zxing-android-embedded:4.1.0")
    implementation ("androidx.appcompat:appcompat:1.0.2")
    implementation ("org.mindrot:jbcrypt:0.4")
    implementation ("com.google.mlkit:image-labeling:17.0.7")



    implementation("com.vanniktech:android-image-cropper:4.5.0")
    implementation(platform("com.google.firebase:firebase-bom:32.4.1"))
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-messaging")

    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.0")

}
