package com.malikoil.intelligence
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
class Api(private val context:Context) {
 private fun clearDeniedSession(uid:String){
  if(FirebaseAuth.getInstance().currentUser?.uid==uid){FirebaseAuth.getInstance().signOut();SecureCache(context).clear();context.getSystemService(android.app.NotificationManager::class.java).cancelAll()}
 }

 suspend fun request(path:String,method:String="GET",body:JSONObject?=null):String {
  val user=FirebaseAuth.getInstance().currentUser ?: error("Sign in required")
  val token=try {user.getIdToken(false).await().token ?: error("Session expired")}catch(error:FirebaseAuthInvalidUserException){clearDeniedSession(user.uid);throw AccessDeniedException(403)}
  val result=try { withContext(Dispatchers.IO) {
   NetworkSafety.requireSession(user.uid,FirebaseAuth.getInstance().currentUser?.uid)
   val base=BuildConfig.API_BASE_URL.trimEnd('/')
   NetworkSafety.requireHttps(base)
   val c=URL(base+path).openConnection() as HttpURLConnection
   try {
    c.connectTimeout=15000;c.readTimeout=60000;c.instanceFollowRedirects=false;c.requestMethod=method
    c.setRequestProperty("Authorization","Bearer $token")
    if(body!=null) { c.doOutput=true;c.setRequestProperty("Content-Type","application/json");c.outputStream.use { it.write(body.toString().toByteArray()) } }
    val status=c.responseCode
    NetworkSafety.checkAuthorization(status)
    if(status !in 200..299) error("Server $status: "+(c.errorStream?.use { NetworkSafety.readLimited(it,65536).take(300) } ?: "Request refused"))
    c.inputStream.use { NetworkSafety.readLimited(it,2097152) }
   } finally { c.disconnect() }
  }} catch(error:AccessDeniedException){
   clearDeniedSession(user.uid);throw error
  }
  NetworkSafety.requireSession(user.uid,FirebaseAuth.getInstance().currentUser?.uid)
  return result
 }
 fun deviceId():String {
  val prefs=context.getSharedPreferences("device",Context.MODE_PRIVATE)
  return prefs.getString("id",null) ?: java.util.UUID.randomUUID().toString().also { prefs.edit().putString("id",it).apply() }
 }
 suspend fun register() {
  val token=FirebaseMessaging.getInstance().token.await()
  request("/v1/devices","POST",JSONObject().put("id",deviceId()).put("token",token))
 }
}
