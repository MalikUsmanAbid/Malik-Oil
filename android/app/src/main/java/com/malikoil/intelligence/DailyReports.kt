package com.malikoil.intelligence
import android.content.Context
import android.content.Intent
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Notification
import android.os.Build
import org.json.JSONObject
import org.json.JSONArray
import java.time.Instant
import java.time.LocalDate
class DailyReports(private val context:Context,private val actor:String){
 private val cache=SecureCache(context);private val suffix=DailyRateReport.id(actor).take(32);private val configName="reports_"+suffix
 companion object{private val lock=Any()
  fun schedule(context:Context){val actor=CaptureActor.current(context)?:return;val reports=DailyReports(context,actor);val config=reports.config();val now=Instant.now();val local=now.atZone(DailyRateReport.zone);val dates=config.optJSONArray("dates")?:JSONArray();val hasToday=(0 until dates.length()).any{dates.optString(it)==local.toLocalDate().toString()};val target=if(local.hour>=18&&!hasToday)Instant.ofEpochMilli(maxOf(now.toEpochMilli()+5000,config.optLong("lastFailedAt",0)+900000))else DailyRateReport.nextSix(now);val intent=Intent(context,DailyReportReceiver::class.java).putExtra("report_date",target.atZone(DailyRateReport.zone).toLocalDate().toString());val alarm=context.getSystemService(AlarmManager::class.java);val pending=PendingIntent.getBroadcast(context,7401,intent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);if(!config.optBoolean("enabled")){alarm.cancel(pending);return};alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,target.toEpochMilli(),pending)}
 }
 fun config():JSONObject=synchronized(lock){check(CaptureActor.current(context)==actor);val saved=cache.read(configName,actor);if(saved==null){check(!cache.exists(configName)){"Encrypted report settings unavailable; existing data preserved"};return@synchronized JSONObject().put("enabled",false).put("language","roman").put("filter","sarson").put("dates",JSONArray())};JSONObject(saved.getString("data"))}
 fun configure(enabled:Boolean,language:String,filter:String)=synchronized(lock){require(language in listOf("en","ur","roman"));require(filter in listOf("all","sarson","canola","til","taramira","alsi"));val config=config();config.put("enabled",enabled).put("language",language).put("filter",filter);cache.save(configName,actor,config.toString());schedule(context)}
 fun generate(date:LocalDate=Instant.now().atZone(DailyRateReport.zone).toLocalDate()):Pair<DailyRateReport,ByteArray> = synchronized(lock){
  val config=config();val data=GroupCaptureStore(context,actor).read();val raw=data.getJSONArray("items");val messages=(0 until raw.length()).map{val r=raw.getJSONObject(it);CapturedGroupMessage(r.getString("id"),r.getString("group"),r.getString("text"),r.getLong("messageAt"),r.getLong("capturedAt"))}
  val report=DailyRateReport.build(date,System.currentTimeMillis(),config.optString("language","roman"),LocalRateBook(context,actor).read(),messages,config.optString("filter","sarson"));val bytes=DailyWorkbook.write(report);require(bytes.size<=3145728){"Report exceeds local size limit"};check(CaptureActor.current(context)==actor)
  cache.save("report_"+suffix+"_"+date,actor,JSONObject().put("summary",report.summary).put("xlsx",android.util.Base64.encodeToString(bytes,android.util.Base64.NO_WRAP)).toString())
  val dates=config.optJSONArray("dates")?:JSONArray();val retained=((0 until dates.length()).map{dates.getString(it)}+date.toString()).distinct().sortedDescending();config.put("dates",JSONArray(retained.take(7))).put("lastGenerated",System.currentTimeMillis()).put("lastError",false);cache.save(configName,actor,config.toString());retained.drop(7).forEach{cache.remove("report_"+suffix+"_"+it)};report to bytes
 }
 fun read(date:String):Pair<String,ByteArray>{LocalDate.parse(date);check(CaptureActor.current(context)==actor);val value=cache.read("report_"+suffix+"_"+date,actor)?:error("Saved report unavailable");val data=JSONObject(value.getString("data"));return data.getString("summary") to android.util.Base64.decode(data.getString("xlsx"),android.util.Base64.NO_WRAP)}
 fun fail(){synchronized(lock){runCatching{val config=config();config.put("lastError",true).put("lastFailedAt",System.currentTimeMillis());cache.save(configName,actor,config.toString())}}}
 fun notifyReady(){if(Build.VERSION.SDK_INT>=33&&context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)return;val manager=context.getSystemService(NotificationManager::class.java);manager.createNotificationChannel(NotificationChannel("daily_reports","Daily Malik Oil reports",NotificationManager.IMPORTANCE_DEFAULT));val open=PendingIntent.getActivity(context,7402,Intent(context,MainActivity::class.java).putExtra("open_daily_reports",true),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);manager.notify(7402,Notification.Builder(context,"daily_reports").setSmallIcon(android.R.drawable.ic_menu_report_image).setContentTitle("Malik Oil daily report ready").setContentText("Open the private report screen to view or export.").setContentIntent(open).setAutoCancel(true).setVisibility(Notification.VISIBILITY_PRIVATE).build())}
}
