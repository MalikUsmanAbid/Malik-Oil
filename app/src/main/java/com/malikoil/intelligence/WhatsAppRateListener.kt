package com.malikoil.intelligence
import android.app.Notification
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.malikoil.intelligence.data.MarketRepository
import kotlinx.coroutines.*
class WhatsAppRateListener:NotificationListenerService(){
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.IO)
 override fun onDestroy(){scope.cancel();super.onDestroy()}
 override fun onNotificationPosted(sbn:StatusBarNotification){
  if(Build.VERSION.SDK_INT<28||sbn.packageName!="com.whatsapp.w4b"||sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY!=0)return
  scope.launch {
   runCatching {
    val config=AutomationStore.read(this@WhatsAppRateListener)
    if(!config.optBoolean("capture"))return@runCatching
    val style=Notification.Builder.recoverBuilder(this@WhatsAppRateListener,sbn.notification).style as? Notification.MessagingStyle?:return@runCatching
    val group=style.conversationTitle?.toString()?.trim()?:return@runCatching
    if(!style.isGroupConversation||group!=config.optString("group"))return@runCatching
    val repo=MarketRepository.shared
    repo.attach(this@WhatsAppRateListener)
    for(message in style.messages.takeLast(200)){
     val text=message.text?.toString()?.take(5000)?:continue
     if(message.timestamp<config.optLong("startedAt")||message.timestamp>System.currentTimeMillis()+300000)continue
     if(!MandiMessageIndex.sarsonMention(text)&&repo.commodities.value.none{text.contains(it.id,true)})continue
     repo.receiveRateNotification(group,text,message.timestamp)
    }
   }
  }
 }
}
