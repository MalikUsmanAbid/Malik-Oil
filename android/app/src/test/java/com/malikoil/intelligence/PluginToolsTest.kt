package com.malikoil.intelligence
import org.junit.Assert.*
import org.junit.Test
class PluginToolsTest{
 @Test fun conversionsKeepOriginalPrecision(){val values=PluginTools.convert("8001.01","40kg");assertEquals("200.03",values["kg"]);assertEquals("8001.01",values["40kg"]);assertEquals("20002.53",values["100kg"]);for(bad in listOf("0","-1","1e3","1.123","100000000.01"))assertThrows(IllegalArgumentException::class.java){PluginTools.convert(bad,"kg")}}
 @Test fun rowCheckerRejectsAmbiguousOrFutureData(){val row="sarson | Samundri | standard | 8000 | 40kg | 2026-01-01T10:00:00+05:00";assertEquals(1,PluginTools.checkRows(row).size);assertThrows(Exception::class.java){PluginTools.checkRows("ordinary chat")};assertThrows(Exception::class.java){PluginTools.checkRows(row.replace("2026-01-01","2026-02-30"))};assertThrows(Exception::class.java){PluginTools.checkRows(row.replace("2026-01-01","2099-01-01"))}}
}
