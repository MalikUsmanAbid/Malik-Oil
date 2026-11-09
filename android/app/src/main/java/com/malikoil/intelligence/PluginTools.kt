package com.malikoil.intelligence
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.OffsetDateTime
object PluginTools{
 fun convert(amount:String,unit:String):Map<String,String>{
  require(amount.matches(Regex("(?:0|[1-9]\\d{0,8})(?:\\.\\d{1,2})?"))){"Enter a positive amount with at most 2 decimal places"}
  val value=BigDecimal(amount);require(value>BigDecimal.ZERO&&value<=BigDecimal("100000000"));val base=when(unit){"kg"->1;"40kg"->40;"100kg"->100;else->error("Choose kg, 40kg or 100kg")}
  return linkedMapOf("kg" to 1,"40kg" to 40,"100kg" to 100).mapValues{(_,kg)->value.multiply(BigDecimal(kg)).divide(BigDecimal(base),2,RoundingMode.HALF_UP).toPlainString()}
 }
 fun checkRows(text:String,now:Long=System.currentTimeMillis()/1000):List<String>{
  require(text.length<=50000);val rows=text.lines().map{it.trim()}.filter{it.isNotBlank()};require(rows.size in 1..20){"Paste 1 to 20 explicit rate rows"}
  return rows.map{row->val f=row.split('|').map{it.trim()};require(f.size==6){"commodity | market | grade | amount | unit | ISO date with timezone"};require(f[0] in listOf("sarson","canola","til","taramira","alsi")&&f[1].length in 1..80&&f[2].length in 1..80);convert(f[3],f[4]);require(OffsetDateTime.parse(f[5]).toEpochSecond()<=now+300){"Future observation rejected"};f.joinToString(" | ")}
 }
}
