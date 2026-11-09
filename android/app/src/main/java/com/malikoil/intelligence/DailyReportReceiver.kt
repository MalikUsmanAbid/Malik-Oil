package com.malikoil.intelligence
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.*
class DailyReportReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent){val pending=goAsync();CoroutineScope(SupervisorJob()+Dispatchers.IO).launch{try{val actor=CaptureActor.current(context)?:return@launch;val reports=DailyReports(context,actor);if(intent.action==null&&reports.config().optBoolean("enabled")){try{reports.generate(intent.getStringExtra("report_date")?.let{java.time.LocalDate.parse(it)}?:java.time.Instant.now().atZone(DailyRateReport.zone).toLocalDate());reports.notifyReady()}catch(_:Exception){reports.fail()}};runCatching{DailyReports.schedule(context)}}catch(_:Exception){/* Fail closed when owner state or encrypted storage is unavailable. */}finally{pending.finish()}}}
}
