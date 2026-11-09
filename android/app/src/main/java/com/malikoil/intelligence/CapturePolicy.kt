package com.malikoil.intelligence
import java.text.Normalizer
import java.security.MessageDigest
data class CapturedGroupMessage(val id:String,val group:String,val text:String,val messageAt:Long,val capturedAt:Long)
object CapturePolicy{
 fun group(value:String)=Normalizer.normalize(value.trim(),Normalizer.Form.NFC)
 fun accept(packageName:String,target:String,title:String,isGroup:Boolean,isSummary:Boolean,text:String,messageAt:Long,startedAt:Long,now:Long):CapturedGroupMessage?{
  val expected=group(target);val observed=group(title)
  if(packageName!="com.whatsapp.w4b"||!isGroup||isSummary||expected.isBlank()||expected.length>100||expected!=observed||text.isBlank()||text.length>5000)return null
  // Notification timestamp is provenance, never the rate observation date.
  if(messageAt<startedAt||messageAt>now+300000||messageAt<=0)return null
  val bytes=(packageName+"\u0000"+observed+"\u0000"+messageAt+"\u0000"+text).toByteArray()
  val id=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){(it.toInt() and 255).toString(16).padStart(2,'0')}
  return CapturedGroupMessage(id,observed,text,messageAt,now)
 }
 fun discoverTitle(packageName:String,title:String,isGroup:Boolean,isSummary:Boolean,until:Long,now:Long):String?{
  val value=group(title);if(packageName!="com.whatsapp.w4b"||!isGroup||isSummary||until<=now||until-now>120000||value.isBlank()||value.length>100)return null;return value
 }
 fun add(existing:List<CapturedGroupMessage>,item:CapturedGroupMessage):List<CapturedGroupMessage>{if(existing.any{it.id==item.id})return existing;return (existing+item).sortedBy{it.capturedAt}.takeLast(200)}
}
