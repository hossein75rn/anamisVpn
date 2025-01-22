plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.v2ray.anamin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.v2ray.anamin"
        minSdk = 21
        targetSdk = 34
        versionCode = 566
        versionName = "2.1"
        multiDexEnabled = true
        splits.abi {
            reset()
            include(
                "arm64-v8a",
                "armeabi-v7a",
                "x86_64",
                "x86"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildTypes {
        release {
            isMinifyEnabled = false

        }
        debug {
            isMinifyEnabled = false

        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    splits {
        abi {
            isEnable = true
            isUniversalApk = false
        }
    }

    applicationVariants.all {
        val variant = this
        val versionCodes =
            mapOf("armeabi-v7a" to 1, "arm64-v8a" to 2, "x86" to 3, "x86_64" to 4)

        variant.outputs
            .map { it as com.android.build.gradle.internal.api.ApkVariantOutputImpl }
            .forEach { output ->
                val abi = if (output.getFilter("ABI") != null)
                    output.getFilter("ABI")
                else
                    "all"

                output.outputFileName = "AnamisVPN_${variant.versionName}_${abi}.apk"
                if(versionCodes.containsKey(abi))
                {
                    output.versionCodeOverride = (1000000 * versionCodes[abi]!!).plus(variant.versionCode)
                }
                else
                {
                    return@forEach
                }
            }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar","*.jar"))))
    testImplementation("junit:junit:4.13.2")

    // Androidx
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Androidx ktx
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    //kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

    implementation("com.tencent:mmkv-static:1.3.4")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.reactivex:rxjava:1.3.8")
    implementation("io.reactivex:rxandroid:1.2.1")
    implementation("com.tbruyelle.rxpermissions:rxpermissions:0.9.4@aar")
    implementation("me.drakeet.support:toastcompat:1.1.0")
    implementation("com.blacksquircle.ui:editorkit:2.9.0")
    implementation("com.blacksquircle.ui:language-base:2.9.0")
    implementation("com.blacksquircle.ui:language-json:2.9.0")
    implementation("io.github.g00fy2.quickie:quickie-bundled:1.9.0")
    implementation("com.google.zxing:core:3.5.3")

    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.work:work-multiprocess:2.10.0")

    // circle image view
    implementation("de.hdodenhof:circleimageview:3.1.0")
    //retrofit and Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.github.tbruyelle:rxpermissions:0.12")
    implementation("com.github.FlodCoding:LoadingButton:1.1.0-alpha01")
    implementation("com.airbnb.android:lottie:5.2.0")
    // loading
    implementation("com.kaopiz:kprogresshud:1.0.2")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.github.iwgang:countdownview:2.1.6")
    implementation ("com.mikhaellopez:circularprogressbar:3.1.0")
}