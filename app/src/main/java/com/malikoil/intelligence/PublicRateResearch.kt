package com.malikoil.intelligence
import com.malikoil.intelligence.model.ResearchSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.xml.parsers.DocumentBuilderFactory
object PublicRateResearch {
 suspend fun fetch(commodity:String,market:String,question:String=""):List<ResearchSource> = withContext(Dispatchers.IO){
  require(commodity.matches(Regex("[a-z][a-z0-9_-]{1,39}")))
  require(!Regex("tvly-|AIza|AQ[.]|password|@",RegexOption.IGNORE_CASE).containsMatchIn(question)){"Use a public market question"}
  val subject=if(commodity=="sarson")"mustard seed sarson" else commodity.replace('_',' ')
  val cleanQuestion=question.take(300).replace(Regex("[^\\p{L}\\p{N} ]")," ")
  val query=URLEncoder.encode("$subject Punjab Pakistan $market Faisalabad Gojra mandi rate $cleanQuestion when:30d","UTF-8")
  val connection=URL("https://news.google.com/rss/search?q=$query&hl=en-PK&gl=PK&ceid=PK:en").openConnection() as HttpURLConnection
  try {
   connection.connectTimeout=10000;connection.readTimeout=15000;connection.instanceFollowRedirects=false
   require(connection.responseCode==200){"Public search unavailable"}
   val bytes=connection.inputStream.use{input->
    val output=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192)
    while(true){val count=input.read(buffer);if(count<0)break;require(output.size()+count<=262144);output.write(buffer,0,count)}
    output.toByteArray()
   }
   require(bytes.size<=262144){"Search response too large"}
   parse(String(bytes,Charsets.UTF_8))
  }finally{connection.disconnect()}
 }
 fun parse(xml:String):List<ResearchSource>{
  require(!Regex("<!DOCTYPE|<!ENTITY",RegexOption.IGNORE_CASE).containsMatchIn(xml))
  val factory=DocumentBuilderFactory.newInstance();factory.isExpandEntityReferences=false
  val document=factory.newDocumentBuilder().parse(xml.byteInputStream())
  val items=document.getElementsByTagName("item")
  val result=mutableListOf<ResearchSource>()
  for(index in 0 until minOf(items.length,100)){
   val item=items.item(index) as? org.w3c.dom.Element?:continue
   fun text(name:String)=item.getElementsByTagName(name).item(0)?.textContent.orEmpty().take(2048)
   val title=text("title");val url=text("link");val published=text("pubDate")
   val date=runCatching{ZonedDateTime.parse(published,DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()}.getOrNull()?:continue
   val age=java.time.Duration.between(date,java.time.Instant.now()).seconds
   if(title.isBlank()||!url.startsWith("https://news.google.com/")||age !in -900..2592000)continue
   if(result.any{it.url==url})continue
   result.add(ResearchSource(result.size+1,title.take(300),text("source").ifBlank{"Google News RSS"},date.toString(),url,"headline-only"))
   if(result.size>=8)break
  }
  return result
 }
}
