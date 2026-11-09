package com.malikoil.intelligence
import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
class MalikApplication: Application() {
 override fun onCreate() {
  super.onCreate()
  if(BuildConfig.FIREBASE_API_KEY.isNotBlank() && BuildConfig.FIREBASE_APP_ID.isNotBlank() && BuildConfig.FIREBASE_PROJECT_ID.isNotBlank() && BuildConfig.FIREBASE_SENDER_ID.isNotBlank()) {
   FirebaseApp.initializeApp(this, FirebaseOptions.Builder().setApiKey(BuildConfig.FIREBASE_API_KEY).setApplicationId(BuildConfig.FIREBASE_APP_ID).setProjectId(BuildConfig.FIREBASE_PROJECT_ID).setGcmSenderId(BuildConfig.FIREBASE_SENDER_ID).build())
  }
 }
}
