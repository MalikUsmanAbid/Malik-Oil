package com.malikoil.intelligence
/** Conservative review drafts. A message timestamp never becomes a price observation date. */
object MarketRateParser {
 fun drafts(text:String,commodities:List<String> = listOf("sarson")):String {
  val normalized=text.map{character->if(character in '۰'..'۹'||character in '٠'..'٩')Character.digit(character,10).digitToChar() else character}.joinToString("").replace(Regex("(?<=[0-9]),(?=[0-9]{3}(?:[^0-9]|$))"),"")
  return normalized.lineSequence().take(200).mapNotNull { line ->
   if(Regex("[0-9]{3,}\\s*[-–]\\s*[0-9]{3,}").containsMatchIn(line))return@mapNotNull null
   val commodity=if(MandiMessageIndex.sarsonMention(line))"sarson" else commodities.filter{it!="sarson"}.singleOrNull{Regex("(?i)(?<![a-z0-9])"+Regex.escape(it)+"(?![a-z0-9])").containsMatchIn(line)}?:return@mapNotNull null
   val cities=MandiMessageIndex.cities(line)
   if(cities.size!=1)return@mapNotNull null
   val quote=Regex("(?i)(?<![0-9,.-])([0-9]{3,8}(?:[.][0-9]{1,2})?)\\s*(?:/|per|فی)\\s*(40\\s*(?:kg|کلو)|100\\s*(?:kg|کلو)|kg|کلو)").findAll(line).toList()
   if(quote.size!=1)return@mapNotNull null
   val amount=quote[0].groupValues[1]
   val unit=quote[0].groupValues[2].lowercase().replace(Regex("\\s"),"").replace("کلو","kg")
   val observed=Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9:.]+(?:Z|[+-][0-9]{2}:[0-9]{2})").find(line)?.value?:"OBSERVATION_DATE_REQUIRED"
   "$commodity | ${cities[0]} | GRADE_REQUIRED | $amount | $unit | $observed"
  }.take(20).joinToString("\n")
 }
}
