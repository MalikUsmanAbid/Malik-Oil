package com.malikoil.intelligence
object CsvExport {
 fun encode(rows:List<List<String>>):String="\uFEFF"+rows.joinToString("\r\n"){row->row.joinToString(","){value->
  val safe=if(value.trimStart().firstOrNull() in listOf('=','+','-','@')||value.firstOrNull() in listOf('\t','\r','\n'))"'"+value else value
  "\""+safe.replace("\"","\"\"")+"\""
 }}
}
