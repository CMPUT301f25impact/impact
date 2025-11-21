import com.android.build.gradle.AppExtension
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.external.javadoc.StandardJavadocDocletOptions
import java.io.File

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

// 1) First create the task globally (empty)
val generateAndroidJavadocs = tasks.register("generateAndroidJavadocs", Javadoc::class) {
    description = "Generates Javadocs including Android classes"
    group = "documentation"

    // Output directory (relative to repo root)
    destinationDir = File(project.rootDir, "javadocs")

    isFailOnError = false

    (options as StandardJavadocDocletOptions).apply {
        encoding = "UTF-8"
        charSet = "UTF-8"
        author(true)
        use(true)
        splitIndex(true)
        links("https://developer.android.com/reference")
    }

    // exclude useless generated stuff
    exclude("**/R.java", "**/R.class", "**/R\\$*.class", "**/BuildConfig.java")
}

// 2) Now configure the task with variant info
android.applicationVariants.all(object : Action<com.android.build.gradle.api.ApplicationVariant> {
    override fun execute(variant: com.android.build.gradle.api.ApplicationVariant) {
        tasks.named<Javadoc>("generateAndroidJavadocs").configure {

            // Add Java source dirs
            val mainSources = android.sourceSets.getByName("main").java.srcDirs
            source(mainSources)

            // Add Android boot classpath + variant compile classpath
            val bootCp = android.bootClasspath.map { file(it) }
            val variantCp = variant.javaCompileProvider.get().classpath

            classpath = files(bootCp, variantCp)
        }
    }
})
