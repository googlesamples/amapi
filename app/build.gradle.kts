/* Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//classpath("com.google.android.gms:strict-version-matcher-plugin:1.2.4")

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
//    id("com.google.android.gms.strict-version-matcher-plugin")
    id("com.google.android.gms.oss-licenses-plugin")
}

android {
    namespace = "com.amapi.extensibility.demo"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.amapi.extensibility.demo"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }

        release {
            isMinifyEnabled = true
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
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
//    implementation 'io.grpc:grpc-okhttp:1.44.0' // CURRENT_GRPC_VERSION
//    implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
//    implementation 'com.google.android.material:material:1.12.0'
//    implementation 'androidx.constraintlayout:constraintlayout:2.2.1'
//    implementation 'androidx.constraintlayout:constraintlayout:2.2.1'
//    implementation 'com.google.android.gms:play-services-oss-licenses:17.1.0'

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraint.layout)
    api(libs.amapi)
    implementation(libs.play.services.oss.licenses)
    implementation(libs.material)
    implementation(libs.androidx.constraint.layout)
    implementation(libs.androidx.constraint.layout)
    implementation(libs.coroutines)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.espresso.core)
}
