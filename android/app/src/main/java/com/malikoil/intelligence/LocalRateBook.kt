package com.malikoil.intelligence
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.OffsetDateTime
import java.math.BigDecimal
class LocalRateBook(private val context:Context,private val actor:String){
 private val cache=SecureCache(context);private val name="ratebook_"+DailyRateReport.id(actor).take(32)
 companion object{private val lock=Any()}
 private fun load():MutableList<ReviewedRate>{check(CaptureActor.current(context)==actor);val saved=cache.read(name,actor);if(saved==null){check(!cache.exists(name)){"Encrypted rate book unavailable; existing data preserved"};return mutableListOf()};val rows=JSONArray(saved.getString("data"));require(rows.length()<=2000);return (0 until rows.length()).map{r->val o=rows.getJSONObject(r);ReviewedRate(o.getString("id"),o.getString("commodity"),o.getString("market"),o.getString("grade"),o.getString("amount"),o.getString("unit"),o.getLong("observedAt"),o.getString("source"),o.optString("eventId"),o.getLong("approvedAt"))}.toMutableList()}
 fun read():List<ReviewedRate> = synchronized(lock){load()}
 fun approve(text:String,source:String,eventId:String):Int=synchronized(lock){require(source.trim().length in 1..200){"Source label is required"};require(eventId.length<=256);val rows=load();val checked=PluginTools.checkRows(text);var added=0
  for(line in checked){val f=line.split('|').map{it.trim()};val normalized=BigDecimal(PluginTools.convert(f[3],f[4]).getValue("kg"));require(normalized>BigDecimal.ZERO){"Price below one paisa/kg is unsupported"};val at=OffsetDateTime.parse(f[5]).toEpochSecond();val amount=BigDecimal(f[3]).setScale(2).toPlainString();val id=DailyRateReport.id(listOf(f[0],f[1],f[2],amount,f[4],at.toString(),source.trim(),eventId).joinToString("\u0000"));if(rows.none{it.id==id}){rows.add(ReviewedRate(id,f[0],f[1],f[2],amount,f[4],at,source.trim(),eventId,System.currentTimeMillis()));added++}}
  require(rows.size<=2000){"Local rate book full (2,000 rows); export before adding more"};val output=JSONArray();rows.forEach{r->output.put(JSONObject().put("id",r.id).put("commodity",r.commodity).put("market",r.market).put("grade",r.grade).put("amount",r.amount).put("unit",r.unit).put("observedAt",r.observedAt).put("source",r.source).put("eventId",r.eventId).put("approvedAt",r.approvedAt))};check(CaptureActor.current(context)==actor);cache.save(name,actor,output.toString());added
 }
}
