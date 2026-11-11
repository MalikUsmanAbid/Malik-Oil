package com.malikoil.intelligence
import android.content.Context
import org.json.JSONObject
object AutomationStore {
 private val lock=Any()
 fun read(context:Context):JSONObject=synchronized(lock){
  val cache=SecureCache(context)
  val saved=cache.read("automation_v1","device-owner")
  check(saved!=null||!cache.exists("automation_v1")){"Encrypted owner settings unavailable"}
  saved?.getString("data")?.let{JSONObject(it)}?:JSONObject().put("group","").put("capture",false).put("daily",false)
 }
 fun configure(context:Context,group:String,capture:Boolean,daily:Boolean)=synchronized(lock){
  require(group.length<=100&&(!capture||group.isNotBlank()))
  val data=read(context).put("group",group.trim()).put("capture",capture).put("daily",daily).put("startedAt",System.currentTimeMillis())
  SecureCache(context).save("automation_v1","device-owner",data.toString())
  DailyMarketJob.schedule(context)
 }
}
