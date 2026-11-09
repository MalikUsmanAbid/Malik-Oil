package com.malikoil.intelligence
import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.*
class GroupNotificationService:NotificationListenerService(){
 private val captureScope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
 companion object{@Volatile var connected=false;@Volatile var storageError=false}
 override fun onListenerConnected(){connected=true}
 override fun onListenerDisconnected(){connected=false}
 override fun onDestroy(){connected=false;captureScope.cancel();super.onDestroy()}
 override fun onNotificationPosted(sbn:StatusBarNotification){
  if(!connected||Build.VERSION.SDK_INT<28||sbn.packageName!="com.whatsapp.w4b")return
  if(sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY!=0)return
  val actor=CaptureActor.current(this)?:return
  captureScope.launch {try{
   val store=GroupCaptureStore(this@GroupNotificationService,actor);val config=store.read();if(!config.optBoolean("enabled")&&config.optLong("discoverUntil")<=System.currentTimeMillis())return@launch
   val style=(Notification.Builder.recoverBuilder(this@GroupNotificationService,sbn.notification).style as? Notification.MessagingStyle)?:return@launch
   if(!style.isGroupConversation)return@launch
   val title=style.conversationTitle?.toString()?:return@launch
   if(config.optLong("discoverUntil")>System.currentTimeMillis()){store.noteTitle(title);return@launch}
   if(!config.optBoolean("enabled")||CapturePolicy.group(title)!=config.optString("group"))return@launch
   val now=System.currentTimeMillis()
   val collected=mutableListOf<CapturedGroupMessage>();for(message in style.messages.takeLast(200)){val item=CapturePolicy.accept(sbn.packageName,config.optString("group"),title,true,false,message.text?.toString()?:continue,message.timestamp,config.optLong("startedAt"),now)?:continue;collected.add(item)};store.append(collected)
   storageError=false
  }catch(_:Exception){storageError=true}} // No message content or contact names in logs.
 }
}
