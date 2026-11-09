package com.malikoil.intelligence
import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
object CaptureActor{
 fun current(context:Context):String?=if(FirebaseApp.getApps(context).isEmpty())"local-device-owner" else FirebaseAuth.getInstance().currentUser?.uid
}
class GroupCaptureStore(private val context:Context,private val actor:String){
 private val cache=SecureCache(context)
 private val name="wa_"+MessageDigest.getInstance("SHA-256").digest(actor.toByteArray()).joinToString(""){(it.toInt() and 255).toString(16).padStart(2,'0')}
 companion object{private val lock=Any()}
 private fun load():JSONObject{val saved=cache.read(name,actor);if(saved==null){check(!cache.exists(name)){"Encrypted WhatsApp inbox unavailable; existing file preserved"};return JSONObject().put("enabled",false).put("group","").put("startedAt",0).put("items",JSONArray())};return JSONObject(saved.getString("data")).also{require(it.getJSONArray("items").length()<=200)}}
 fun read():JSONObject=synchronized(lock){check(CaptureActor.current(context)==actor){"Owner session changed"};load()}
 fun configure(group:String,enabled:Boolean,priorityCities:String=MandiMessageIndex.DEFAULT_PRIORITIES)=synchronized(lock){check(CaptureActor.current(context)==actor){"Owner session changed"};val data=load();val value=CapturePolicy.group(group);require(!enabled||value.length in 1..100){"Enter an exact, unique group name"};MandiMessageIndex.priorities(priorityCities);data.put("priorityCities",priorityCities).put("discoverUntil",0).put("group",value).put("enabled",enabled).put("startedAt",System.currentTimeMillis());cache.save(name,actor,data.toString())}
 fun stop()=synchronized(lock){check(CaptureActor.current(context)==actor){"Owner session changed"};val data=load();data.put("enabled",false).put("discoverUntil",0);cache.save(name,actor,data.toString())}
 fun clear()=synchronized(lock){check(CaptureActor.current(context)==actor){"Owner session changed"};val data=load();data.put("items",JSONArray()).put("foundGroups",JSONArray());cache.save(name,actor,data.toString())}
 fun discover()=synchronized(lock){check(CaptureActor.current(context)==actor);val data=load();data.put("enabled",false).put("discoverUntil",System.currentTimeMillis()+120000).put("foundGroups",JSONArray());cache.save(name,actor,data.toString())}
 fun noteTitle(title:String)=synchronized(lock){if(CaptureActor.current(context)!=actor)return@synchronized;val data=load();val valid=CapturePolicy.discoverTitle("com.whatsapp.w4b",title,true,false,data.optLong("discoverUntil"),System.currentTimeMillis())?:return@synchronized;val old=data.optJSONArray("foundGroups")?:JSONArray();val names=(0 until old.length()).map{old.getString(it)};if(valid in names||names.size>=20)return@synchronized;data.put("foundGroups",JSONArray(names+valid));cache.save(name,actor,data.toString())}
 fun append(messages:List<CapturedGroupMessage>)=synchronized(lock){if(CaptureActor.current(context)!=actor)return@synchronized;val data=load();if(!data.optBoolean("enabled"))return@synchronized;val old=data.getJSONArray("items");var rows=(0 until old.length()).map{val r=old.getJSONObject(it);CapturedGroupMessage(r.getString("id"),r.getString("group"),r.getString("text"),r.getLong("messageAt"),r.getLong("capturedAt"))};var changed=false
  for(item in messages){if(data.optString("group")!=item.group||item.messageAt<data.optLong("startedAt"))continue;val next=CapturePolicy.add(rows,item);if(next!==rows){rows=next;changed=true}}
  if(!changed)return@synchronized
  val items=JSONArray();rows.forEach{r->items.put(JSONObject().put("id",r.id).put("group",r.group).put("text",r.text).put("messageAt",r.messageAt).put("capturedAt",r.capturedAt))};data.put("items",items);cache.save(name,actor,data.toString())
 }
}
