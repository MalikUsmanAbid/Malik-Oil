package com.malikoil.intelligence
import android.app.job.*
import android.content.*
import com.malikoil.intelligence.data.MarketRepository
import kotlinx.coroutines.*
import java.time.*
class DailyMarketJob:JobService(){
 private var work:Job?=null
 override fun onStartJob(params:JobParameters):Boolean {
  work=CoroutineScope(SupervisorJob()+Dispatchers.IO).launch {
   try {
    if(!AutomationStore.read(this@DailyMarketJob).optBoolean("daily"))return@launch
    val repo=MarketRepository.shared
    repo.attach(this@DailyMarketJob)
    repo.dailyResearch()
    repo.generateTodayReport()
    repo.persistOwnerState()
   }catch(_:Exception){/* Existing encrypted history is preserved. */}
   finally {runCatching{schedule(this@DailyMarketJob)};jobFinished(params,false)}
  }
  return true
 }
 override fun onStopJob(params:JobParameters):Boolean{work?.cancel();return false}
 companion object {
  fun schedule(context:Context){
   val scheduler=context.getSystemService(JobScheduler::class.java)
   if(!AutomationStore.read(context).optBoolean("daily")){scheduler.cancel(7410);return}
   val now=Instant.now();val zone=ZoneId.of("Asia/Karachi")
   var next=now.atZone(zone).toLocalDate().atTime(18,0).atZone(zone).toInstant()
   if(!next.isAfter(now))next=next.plusSeconds(86400)
   scheduler.schedule(JobInfo.Builder(7410,ComponentName(context,DailyMarketJob::class.java)).setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setMinimumLatency(Duration.between(now,next).toMillis()).setPersisted(true).build())
  }
 }
}
