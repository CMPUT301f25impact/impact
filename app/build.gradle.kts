import com.android.build.gradle.AppExtension
import org.gradle.api.tasks.javadoc.Javadoc

plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    kotlin("android")
}

android {
    namespace = "com.example.impact"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.impact"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("com.google.zxing:core:3.5.3")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.13.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
}

afterEvaluate {
    val androidExt = extensions.getByType<AppExtension>()
    tasks.register<Javadoc>("androidJavadocs") {
        val debugVariant = androidExt.applicationVariants.first { it.name == "debug" }
        val javaCompileProvider = debugVariant.javaCompileProvider.get()

        setSource(androidExt.sourceSets.getByName("main").java.getSourceFiles())
        classpath = files(
            javaCompileProvider.classpath,
            androidExt.bootClasspath
        )
        setDestinationDir(rootProject.file("javadocs"))
        options.encoding = "UTF-8"
        isFailOnError = false
    }
}
