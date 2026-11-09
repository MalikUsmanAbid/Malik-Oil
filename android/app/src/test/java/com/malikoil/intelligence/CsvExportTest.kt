package com.malikoil.intelligence
import org.junit.Test
import org.junit.Assert.*
class CsvExportTest {
 @Test fun escapesQuotesCommasAndProtectsFormulaCells(){
  val csv=CsvExport.encode(listOf(listOf("source","market"),listOf("=HYPERLINK(\"x\")","Samundri, Punjab")))
  assertTrue(csv.startsWith("\uFEFF"));assertTrue(csv.contains("\"'=HYPERLINK(\"\"x\"\")\""));assertTrue(csv.contains("\"Samundri, Punjab\""))
 }
 @Test fun blocksLeadingWhitespaceBeforeFormula(){assertTrue(CsvExport.encode(listOf(listOf("  =1+1"))).contains("\"'  =1+1\""))}
 @Test fun preservesUrduAndNumericValues(){assertEquals("\uFEFF\"سرسوں\",\"200.5\",\"false\"",CsvExport.encode(listOf(listOf("سرسوں","200.5","false"))))}
}
