package com.malikoil.intelligence
import java.math.BigDecimal
import java.math.RoundingMode
import java.security.MessageDigest
import java.time.*
data class ReviewedRate(val id:String,val commodity:String,val market:String,val grade:String,val amount:String,val unit:String,val observedAt:Long,val source:String,val eventId:String,val approvedAt:Long){
 val stream get()=listOf(commodity,market.trim(),grade.trim(),source.trim()).joinToString("\u0000")
 val price40 get()=BigDecimal(PluginTools.convert(amount,unit).getValue("40kg"))
}
data class ReportRate(val rate:ReviewedRate,val previous:ReviewedRate?){val changePercent:BigDecimal? get()=previous?.let{rate.price40.subtract(it.price40).multiply(BigDecimal(100)).divide(it.price40,2,RoundingMode.HALF_UP)}}
data class DailyRateReport(val date:LocalDate,val generatedAt:Long,val language:String,val rates:List<ReportRate>,val history:List<ReviewedRate>,val raw:List<CapturedGroupMessage>,val filter:String){
 val summary:String get(){val changes=rates.count{it.previous!=null};val overview=rates.take(8).joinToString("\n"){row->val r=row.rate;"${r.commodity} | ${r.market} | ${r.grade}: PKR ${r.price40.toPlainString()}/40kg"+(row.changePercent?.let{" | ${if(it.signum()>0)"+" else ""}${it.toPlainString()}%"}?:"")+" | ${Instant.ofEpochSecond(r.observedAt).atZone(zone).toLocalDate()} | ${r.source}"};return when(language){
 "ur"->"روزانہ رپورٹ: $date\nمالک کے جائزہ شدہ نرخ: ${rates.size}\nپچھلے یکساں ذریعے سے قابلِ موازنہ نرخ: $changes\nغیر جائزہ شدہ گروپ پیغامات: ${raw.size}\n"+(if(rates.isEmpty())"آج کا کوئی جائزہ شدہ نرخ دستیاب نہیں۔" else "ذرائع، اکائی اور مشاہدے کی تاریخ ہر نرخ کے ساتھ درج ہے۔")+"\nپیغامات نرخ نہیں؛ نامعلوم منڈی کا نرخ نامعلوم ہے۔ گراف صرف یکساں جنس، منڈی، معیار اور ذریعے کے تاریخی مشاہدات دکھاتا ہے۔"
 "roman"->"Daily report: $date\nOwner-reviewed rates: ${rates.size}\nPichhle matching source se comparable rates: $changes\nUnreviewed group messages: ${raw.size}\n"+(if(rates.isEmpty())"Aaj ka koi owner-reviewed rate available nahi." else "Har rate ke saath source, unit aur observation date hai.")+"\nMessages rates nahi; missing mandi rate unknown hai. Graph matching commodity, mandi, grade aur source ke historical observations ka hai."
 else->"Daily report: $date\nOwner-reviewed rates: ${rates.size}\nComparable prior observations: $changes\nUnreviewed group messages: ${raw.size}\n"+(if(rates.isEmpty())"No owner-reviewed rate is available for this day." else "Source, unit and observation date are retained for each rate.")+"\nMessages are not rate counts; missing mandi rates remain unknown. The chart uses matching commodity, market, grade and source history."
 }+(if(overview.isEmpty())"" else "\n\n$overview")+(if(rates.size>8)"\nFull details: Rates sheet." else "")}
 companion object{
  val zone:ZoneId=ZoneId.of("Asia/Karachi")
  fun nextSix(now:Instant):Instant{val local=now.atZone(zone);var next=local.toLocalDate().atTime(18,0).atZone(zone);if(!next.toInstant().isAfter(now))next=next.plusDays(1);return next.toInstant()}
  fun build(date:LocalDate,now:Long,language:String,records:List<ReviewedRate>,raw:List<CapturedGroupMessage>,filter:String):DailyRateReport{
   require(!date.isAfter(Instant.ofEpochMilli(now).atZone(zone).toLocalDate()))
   val begin=date.atStartOfDay(zone).toEpochSecond();val end=date.plusDays(1).atStartOfDay(zone).toEpochSecond();val eligible=records.filter{it.observedAt<end&&it.observedAt<=now/1000&&it.approvedAt<=now&&(filter=="all"||it.commodity==filter)}
   val latest=eligible.filter{it.observedAt>=begin}.groupBy{it.stream}.values.map{it.maxWith(compareBy<ReviewedRate>{it.observedAt}.thenBy{it.approvedAt})}.sortedWith(compareBy<ReviewedRate>{MandiMessageIndex.rank(MandiMessageIndex.cities(it.market),MandiMessageIndex.priorities(MandiMessageIndex.DEFAULT_PRIORITIES))}.thenBy{it.market})
   val rates=latest.map{rate->ReportRate(rate,eligible.filter{it.stream==rate.stream&&it.observedAt<begin}.maxByOrNull{it.observedAt})}
   val history=eligible.filter{it.observedAt>=date.minusDays(29).atStartOfDay(zone).toEpochSecond()}.groupBy{it.stream to Instant.ofEpochSecond(it.observedAt).atZone(zone).toLocalDate()}.values.map{it.maxWith(compareBy<ReviewedRate>{it.observedAt}.thenBy{it.approvedAt})}
   val messages=raw.filter{Instant.ofEpochMilli(it.capturedAt).atZone(zone).toLocalDate()==date&&(filter!="sarson"||MandiMessageIndex.sarsonMention(it.text))}
   return DailyRateReport(date,now,language,rates,history,messages,filter)
  }
  fun id(value:String)=MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString(""){(it.toInt() and 255).toString(16).padStart(2,'0')}
 }
}
