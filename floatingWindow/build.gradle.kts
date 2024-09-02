plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.dokka") version "1.9.20" // add this line when you using kotlin project else comment it
    id("io.github.jeadyx.sonatype-uploader") version "2.8"
}
group = "io.github.jeadyx.compose"
version = "1.0"
val tokenUsername:String by project
val tokenPassword:String by project
sonatypeUploader {
    tokenName = tokenUsername
    tokenPasswd = tokenPassword
    pom = Action<MavenPom>{
        name = "floatingwindow"
        description = "A floating window with jetpack compose."
        url = "https://github.com/jeadyx/FloatingWindow"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "jeady"
                name = "jeady"
                email = "jeadyx@outlook.com"
            }
        }
        scm {
            connection = "scm:git:git://github.com/jeadyx/FloatingWindow"
            developerConnection = "scm:git:ssh://github.com/jeadyx/FloatingWindow"
            url = "https://github.com/jeadyx/FloatingWindow"
        }
    }
}

android {
    namespace = "io.github.jeadyx.compose.floatingwindow"
    compileSdk = 34

    defaultConfig {
        minSdk = 31

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}