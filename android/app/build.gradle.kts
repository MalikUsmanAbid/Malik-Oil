import java.util.Properties

plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }
val localFirebase = Properties().apply {
 val configFile = rootProject.file("firebase.local.properties")
 if (configFile.isFile) configFile.inputStream().use { load(it) }
}
fun config(name: String): String {
 val value = providers.gradleProperty(name).orNull ?: localFirebase.getProperty(name, "")
 return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\""
}
android {
 namespace = "com.malikoil.intelligence"
 compileSdk = 35
 defaultConfig {
  applicationId = "com.malikoil.intelligence"; minSdk = 26; targetSdk = 35
  versionCode = 1; versionName = "0.1.0"
  buildConfigField("String", "API_BASE_URL", config("MALIK_API_URL"))
  buildConfigField("String", "FIREBASE_API_KEY", config("MALIK_FIREBASE_API_KEY"))
  buildConfigField("String", "FIREBASE_APP_ID", config("MALIK_FIREBASE_APP_ID"))
  buildConfigField("String", "FIREBASE_PROJECT_ID", config("MALIK_FIREBASE_PROJECT_ID"))
  buildConfigField("String", "FIREBASE_SENDER_ID", config("MALIK_FIREBASE_SENDER_ID"))
 }
 buildFeatures { buildConfig = true }
 compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
 kotlinOptions { jvmTarget = "17" }
 buildTypes { release { isMinifyEnabled = false } }
}
dependencies {
 testImplementation("junit:junit:4.13.2")
 implementation(platform("com.google.firebase:firebase-bom:33.10.0"))
 implementation("com.google.firebase:firebase-auth")
 implementation("com.google.firebase:firebase-messaging")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.1")
 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.1")
}
