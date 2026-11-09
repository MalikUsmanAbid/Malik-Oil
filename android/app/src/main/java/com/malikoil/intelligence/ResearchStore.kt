package com.malikoil.intelligence
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
class ResearchStore(private val cache:SecureCache,private val uid:String){
 private val name="research_"+MessageDigest.getInstance("SHA-256").digest(uid.toByteArray()).joinToString(""){"%02x".format(it)}
 fun load():ResearchLedger{
  val saved=cache.read(name,uid)
  if(saved==null){check(!cache.exists(name)){"Encrypted history unavailable; existing file preserved"};return ResearchLedger()}
  val rows=JSONArray(saved.getString("data"));val sessions=mutableListOf<ResearchThread>()
  require(rows.length()<=8)
  for(i in 0 until rows.length()){val row=rows.getJSONObject(i);val turns=row.getJSONArray("turns");require(turns.length()<=12);sessions.add(ResearchThread(row.getString("id"),row.optString("title"),row.optString("scope"),(0 until turns.length()).map{turns.getString(it).also{data->JSONObject(data)}}.toMutableList()))}
  return ResearchLedger(sessions)
 }
 fun save(ledger:ResearchLedger){val rows=JSONArray();ledger.sessions.forEach{s->rows.put(JSONObject().put("id",s.id).put("title",s.title).put("scope",s.scope).put("turns",JSONArray(s.turns)))};val data=rows.toString();require(data.toByteArray().size<=3145728){"Session storage full"};cache.save(name,uid,data)}
}
