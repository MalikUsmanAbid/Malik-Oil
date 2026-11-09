package com.malikoil.intelligence
import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.*
class PushService:FirebaseMessagingService() {
 override fun onNewToken(token:String) {
  if(FirebaseAuth.getInstance().currentUser!=null) CoroutineScope(SupervisorJob()+Dispatchers.IO).launch { runCatching { Api(this@PushService).register() } }
 }
 override fun onMessageReceived(message:RemoteMessage) {
  if(FirebaseAuth.getInstance().currentUser==null)return
  val id=message.data["alert_id"]?.takeIf { it.matches(Regex("[a-f0-9]{64}")) } ?: return
  if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return
  val prefs=getSharedPreferences("push",MODE_PRIVATE)
  if(prefs.contains(id))return
  val nm=getSystemService(NotificationManager::class.java)
  nm.createNotificationChannel(NotificationChannel("rates","Rate alerts",NotificationManager.IMPORTANCE_HIGH))
  val intent=Intent(this,MainActivity::class.java).putExtra("alert_id",id)
  val pending=PendingIntent.getActivity(this,id.hashCode(),intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
  nm.notify(id,1,Notification.Builder(this,"rates").setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Malik Oil: 3% rate movement").setContentText("Open to verify the rate, source and observation time.").setContentIntent(pending).setAutoCancel(true).build())
  // Same alert tag replaces an existing notification after a process crash.
  prefs.edit().putLong(id,System.currentTimeMillis()).apply()
  val cutoff=System.currentTimeMillis()-30L*86400000L
  prefs.all.filterValues { (it as? Long ?: 0)<cutoff }.keys.forEach { prefs.edit().remove(it).apply() }
 }
}
