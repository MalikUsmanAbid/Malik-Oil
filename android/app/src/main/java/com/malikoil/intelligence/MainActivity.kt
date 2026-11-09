package com.malikoil.intelligence
import android.Manifest
import android.app.*
import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.graphics.Color
import android.text.InputType
import android.view.View
import android.widget.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
class MainActivity:Activity() {
 private val scope=CoroutineScope(SupervisorJob()+Dispatchers.Main)
 private lateinit var page:LinearLayout
 private lateinit var api:Api
 private lateinit var cache:SecureCache
 private var pendingExport:Pair<String,String>?=null
 private var incoming=""
 private var language="roman"
 private var privateDialog:Dialog?=null
 private var privateDestination="collector"
 private var pendingReviewActor:String?=null
 private var pendingReviewText=""
 private var pendingReviewSource="Owner reviewed/manual"
 private var pendingReviewEvent=""
 private var pendingReportExport:Pair<String,ByteArray>?=null
 private var collectorUnlockedActor:String?=null
 private var collectorUnlocked=false
 private var collectorVisible=false
 private var collectorUnlockPending:String?=null
 private var researchBusy=false
 private var activeResearchId:String?=null
 private var researchSurface=false
 private val ink get()=if(researchSurface)Color.rgb(18,60,61)else Color.rgb(24,60,43)
 private val mutedInk get()=if(researchSurface)Color.rgb(79,103,101)else Color.rgb(83,101,90)
 private val pageColor get()=if(researchSurface)Color.rgb(250,250,249)else Color.rgb(251,247,239)
 private val panelColor get()=if(researchSurface)Color.WHITE else Color.rgb(255,253,248)
 private val accent get()=if(researchSurface)Color.rgb(11,98,91)else Color.rgb(12,102,61)
 private fun tr(en:String,ur:String,roman:String)=when(language){"ur"->ur;"en"->en;else->roman}
 private val labels=mapOf(
 "Rates and offline cache" to Pair("منڈی کے نرخ اور آف لائن کیش","Mandi rates aur offline cache"),
 "Alert history" to Pair("الرٹس کی تاریخ","Alerts ki history"),
 "Review imported rates" to Pair("درآمد شدہ نرخ کا جائزہ","Imported rates ka review"),
 "Add rate for review" to Pair("جائزے کے لیے نیا نرخ","Review ke liye naya rate"),
 "Import WhatsApp text" to Pair("واٹس ایپ متن درآمد کریں","WhatsApp text import karein"),
 "Market summary" to Pair("مارکیٹ کا خلاصہ","Market ka khulasa"),
 "Business margin calculator" to Pair("کاروباری منافع کا حساب","Karobar ka margin calculator"),
 "Add commodity" to Pair("نئی جنس شامل کریں","Nayi commodity"),
 "Sign out and clear cache" to Pair("سائن آؤٹ اور کیش صاف کریں","Sign out aur cache clear"),
 "Back" to Pair("واپس","Wapas"),
 "Owner email" to Pair("مالک کا ای میل","Owner email"),"Password" to Pair("پاس ورڈ","Password"),"Sign in" to Pair("سائن اِن","Sign in"),
 "Landed cost and margin" to Pair("کل لاگت اور منافع","Landed cost aur margin"),"Quantity kg" to Pair("مقدار کلو","Miqdar kg"),"Purchase PKR/kg" to Pair("خرید روپے فی کلو","Khareed PKR/kg"),"Sale PKR/kg" to Pair("فروخت روپے فی کلو","Sale PKR/kg"),"Transport PKR" to Pair("ٹرانسپورٹ روپے","Transport PKR"),"Other costs PKR" to Pair("دیگر اخراجات روپے","Doosre kharchay PKR"),"Calculate" to Pair("حساب کریں","Hisab karein"),"Scenario result" to Pair("حساب کا نتیجہ","Hisab ka nateeja"),
 "Review before publishing" to Pair("اشاعت سے پہلے جائزہ","Publish se pehle review"),"Approve this rate" to Pair("یہ نرخ منظور کریں","Yeh rate approve karein"),"Mandi rates" to Pair("منڈی کے نرخ","Mandi rates"),"New commodity" to Pair("نئی جنس","Nayi commodity"),"Save" to Pair("محفوظ کریں","Save"),"Display name" to Pair("نام","Naam"),"WhatsApp export / shared text" to Pair("واٹس ایپ ایکسپورٹ یا شیئر شدہ متن","WhatsApp export / shared text"),"Source label" to Pair("ذریعے کا نام","Source ka naam"),"Paste rate rows" to Pair("نرخ کی سطریں پیسٹ کریں","Rate rows paste karein"),"Create review candidates" to Pair("جائزے کے لیے اندراج بنائیں","Review candidates banayein"),"New rate · review required" to Pair("نیا نرخ · جائزہ ضروری","Naya rate · review zaroori"),"Commodity ID" to Pair("جنس کا شناختی نام","Commodity ID"),"Mandi" to Pair("منڈی","Mandi"),"Grade" to Pair("معیار","Grade"),"Source" to Pair("ذریعہ","Source"),"PKR amount" to Pair("رقم روپے","Raqam PKR"),"Submit for review" to Pair("جائزے کے لیے بھیجیں","Review ke liye bhejein"))
 private fun label(s:String)=labels[s]?.let{tr(s,it.first,it.second)}?:s
 private fun entry(){if(FirebaseApp.getApps(this).isEmpty()){layout();title("Malik Oil");text(tr("Setup required: Firebase and HTTPS backend. AI key stays on server. Home: Samundri, Punjab.","سیٹ اپ درکار: فائربیس اور محفوظ بیک اینڈ۔ اے آئی کی سرور پر رہے گی۔ علاقہ: سمندری، پنجاب۔","Setup chahiye: Firebase aur HTTPS backend. AI key server par rahegi. Ilaqah: Samundri, Punjab."));text(tr("API research and phone notifications are not connected yet.","اے پی آئی تحقیق اور فون نوٹیفکیشن ابھی منسلک نہیں۔","API research aur phone notifications abhi connected nahi."));button(tr("Open research interface and offline plugins","تحقیق انٹرفیس اور آف لائن ٹولز کھولیں","Research interface aur offline plugins kholein")){researchScreen()};button(tr("WhatsApp group notifications · free","واٹس ایپ گروپ نوٹیفکیشن · مفت","WhatsApp group notifications · free")){openGroupCollector("collector")};button(tr("Daily Excel reports · 6 pm","روزانہ ایکسل رپورٹ · شام ۶ بجے","Daily Excel reports · shaam 6 baje")){openGroupCollector("reports")}}else if(FirebaseAuth.getInstance().currentUser==null)login()else dashboard()}

 override fun onCreate(savedInstanceState:Bundle?) {
  super.onCreate(savedInstanceState);window.addFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE);api=Api(this);cache=SecureCache(this)
  incoming=if(intent.action==Intent.ACTION_SEND) intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty().take(50000) else ""
  language=getSharedPreferences("preferences",MODE_PRIVATE).getString("language","roman")?:"roman";entry();runCatching{DailyReports.schedule(this)};if(intent.getBooleanExtra("open_daily_reports",false))openGroupCollector("reports")
 }
 override fun onDestroy(){pendingReportExport?.second?.fill(0);pendingReportExport=null;pendingExport=null;scope.cancel();super.onDestroy()}
 private fun dp(value:Int)=(value*resources.displayMetrics.density).toInt()
 private fun surface(color:Int=panelColor)=android.graphics.drawable.GradientDrawable().apply{setColor(color);cornerRadius=dp(12).toFloat();setStroke(dp(1),if(researchSurface)Color.rgb(156,191,181)else Color.rgb(192,196,174))}
 private fun spinner(values:List<String>,selected:Int=0):Spinner=Spinner(this).apply{
  minimumHeight=dp(48);background=surface();adapter=object:BaseAdapter(){override fun getCount()=values.size;override fun getItem(position:Int)=values[position];override fun getItemId(position:Int)=position.toLong();override fun getView(position:Int,convertView:View?,parent:android.view.ViewGroup?)=TextView(this@MainActivity).apply{text=values[position];textSize=14f;setTextColor(ink);setPadding(dp(12),dp(12),dp(12),dp(12));background=surface()};override fun getDropDownView(position:Int,convertView:View?,parent:android.view.ViewGroup?)=getView(position,convertView,parent)};setSelection(selected)
 }
 private fun layout(research:Boolean=false){
  collectorVisible=false;researchSurface=research
  val scroll=ScrollView(this);scroll.setBackgroundColor(pageColor);page=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(18),dp(20),dp(28))};scroll.addView(page);setContentView(scroll)
  if(Build.VERSION.SDK_INT>=35)scroll.setOnApplyWindowInsetsListener{view,insets->val bars=insets.getInsets(android.view.WindowInsets.Type.systemBars());view.setPadding(bars.left,bars.top,bars.right,bars.bottom);insets}
  page.layoutDirection=if(language=="ur")View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
  val head=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=android.view.Gravity.CENTER_VERTICAL;setPadding(0,0,0,dp(18))};page.addView(head)
  head.addView(TextView(this).apply{text=getString(R.string.brand_name);textSize=21f;setTextColor(accent);typeface=android.graphics.Typeface.DEFAULT_BOLD},LinearLayout.LayoutParams(0,-2,1f))
  val langs=listOf("en","ur","roman");val chooser=spinner(listOf("English","اردو","Roman Urdu"),langs.indexOf(language).coerceAtLeast(0));chooser.contentDescription="Language / زبان";head.addView(chooser,LinearLayout.LayoutParams(dp(132),-2))
  chooser.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(parent:AdapterView<*>?){};override fun onItemSelected(parent:AdapterView<*>?,view:View?,position:Int,id:Long){val next=langs[position];if(next!=language&&!researchBusy){language=next;getSharedPreferences("preferences",MODE_PRIVATE).edit().putString("language",next).apply();if(collectorVisible)showPrivateDestination()else if(researchSurface)researchScreen()else entry()}}}
 }
 private fun title(s:String){page.addView(TextView(this).apply{text=label(s);textSize=27f;setTextColor(ink);setPadding(0,dp(8),0,dp(18))})}
 private fun text(s:String){page.addView(TextView(this).apply{text=s;textSize=16f;setTextColor(mutedInk);setPadding(0,dp(8),0,dp(16))})}
 private fun button(s:String,primary:Boolean=false,action:()->Unit){page.addView(Button(this).apply{text=label(s);isAllCaps=false;minHeight=dp(52);textSize=16f;gravity=android.view.Gravity.CENTER_VERTICAL or android.view.Gravity.START;setPadding(dp(16),dp(12),dp(16),dp(12));background=android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(Color.rgb(52,77,64)),surface(if(primary)accent else panelColor),null);setTextColor(if(primary)Color.WHITE else accent);setOnClickListener{if(collectorVisible&&(!collectorUnlocked||collectorUnlockedActor!=CaptureActor.current(this@MainActivity)))openGroupCollector()else if(researchBusy&&researchSurface)Toast.makeText(this@MainActivity,tr("Research in progress","تحقیق جاری ہے","Research chal rahi hai"),Toast.LENGTH_SHORT).show()else action()}},LinearLayout.LayoutParams(if(page.orientation==LinearLayout.HORIZONTAL)-2 else -1,-2).apply{bottomMargin=dp(10);marginEnd=if(page.orientation==LinearLayout.HORIZONTAL)dp(8)else 0})}
 private fun actionStrip(block:()->Unit){val parent=page;val scroll=HorizontalScrollView(this);val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};scroll.addView(row);parent.addView(scroll);page=row;try{block()}finally{page=parent}}
 private fun field(hintText:String,value:String=""):EditText {text(label(hintText));return EditText(this).apply{hint=label(hintText);setText(value);textSize=16f;setTextColor(ink);setHintTextColor(mutedInk);setPadding(dp(14),dp(12),dp(14),dp(12));background=surface();page.addView(this,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(12)})}}
 private fun card(block:()->Unit){val parent=page;val container=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=surface();setPadding(dp(16),dp(12),dp(16),dp(12))};parent.addView(container,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(20)});page=container;try{block()}finally{page=parent}}
 private fun task(action:suspend ()->Unit){scope.launch {runCatching {action()}.onFailure {if(it is SecurityException && FirebaseApp.getApps(this@MainActivity).isNotEmpty() && FirebaseAuth.getInstance().currentUser==null)entry();Toast.makeText(this@MainActivity,it.message?:"Request failed",Toast.LENGTH_LONG).show()} }}
 private fun login(){
  layout();title("Malik Oil · Owner login");text("Private rate intelligence. Only the configured owner can access the backend.")
  val email=field("Owner email");email.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
  val pass=field("Password");pass.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
  button("Sign in"){task {FirebaseAuth.getInstance().signInWithEmailAndPassword(email.text.toString().trim(),pass.text.toString()).await();dashboard()}}
 }
 private fun dashboard(){
  layout();title(tr("Your market today","آج کی مارکیٹ","Aaj ki market"));card{text(tr("±3% movement alerts · rise + fall\nPrice history follows actual observations. No invented live rates.","±۳٪ تبدیلی کے الرٹس · دونوں سمت\nقیمت کی تاریخ اصل مشاہدات کے مطابق ہے۔ فرضی لائیو نرخ نہیں۔","±3% movement alerts · dono taraf\nHistory actual observations par hai. Live rates invent nahi hote."))}
  text(tr("Home: Samundri · Punjab | Daily observations, not exchange ticks","علاقہ: سمندری · پنجاب | روزانہ کے مشاہدات","Ilaqah: Samundri · Punjab | Rozana observations"))
  text(tr("YOUR WATCHLIST","آپ کی واچ لسٹ","AAPKI WATCHLIST"))
  val watchlist=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};page.addView(watchlist)
  task{try{val rows=JSONArray(api.request("/v1/rates"));watchlist.removeAllViews();val root=page;page=watchlist;try{if(rows.length()==0)text(tr("No approved observations yet.","ابھی کوئی منظور شدہ مشاہدہ نہیں۔","Abhi approved observations nahi."));for(i in 0 until minOf(rows.length(),3)){val rate=rows.getJSONObject(i);card{title("PKR "+rate.getLong("price_paisa_kg")/100.0+" / kg");text(rate.getString("commodity")+" · "+rate.getString("market")+" · "+rate.getString("source"));text("PKR "+rate.getLong("price_paisa_kg")/100.0*40+" / 40kg · "+tr("equivalent","کے برابر","ke barabar"));button(tr("View price chart","قیمت کا گراف دیکھیں","Rate graph dekhein")){task{val history=JSONArray(api.request("/v1/history?stream="+java.net.URLEncoder.encode(rate.getString("stream"),"UTF-8")));historyRows(rate,history,7)}}}}}finally{page=root}}catch(error:Exception){watchlist.removeAllViews();watchlist.addView(TextView(this).apply{text=tr("Sync unavailable. Open rates for the encrypted offline cache.","مطابقت دستیاب نہیں۔ محفوظ آف لائن کیش کے لیے نرخ کھولیں۔","Sync unavailable. Encrypted offline cache ke liye rates kholein.");setTextColor(mutedInk);textSize=14f});if(error is SecurityException)throw error}}
  title(tr("Research & markets","تحقیق اور مارکیٹ","Research aur markets"))
  button(tr("WhatsApp group notifications · free","واٹس ایپ گروپ نوٹیفکیشن · مفت","WhatsApp group notifications · free")){openGroupCollector("collector")};button(tr("Daily Excel reports · 6 pm","روزانہ ایکسل رپورٹ · شام ۶ بجے","Daily Excel reports · shaam 6 baje")){openGroupCollector("reports")}
  button(tr("AI research with sources","ذرائع کے ساتھ اے آئی تحقیق","Sources ke saath AI research"),true){researchScreen()}
  button(tr("Price history chart","قیمت کا گراف","Rate history graph")){history()}
  button(tr("WhatsApp connection","واٹس ایپ رابطہ","WhatsApp connection")){layout();title("WhatsApp");text(tr("Not connected. WhatsApp Business app membership does not establish official API access to this existing group. You are not admin; group eligibility is unverified. Use selected text Share or export without media, then review explicit rows.","منسلک نہیں۔ واٹس ایپ بزنس استعمال کرنا موجودہ گروپ کی اے پی آئی رسائی نہیں دیتا۔ گروپ کی اہلیت کی تصدیق باقی ہے۔ منتخب متن شیئر کریں یا بغیر میڈیا ایکسپورٹ کریں اور نرخ کا جائزہ لیں۔","Connected nahi. WhatsApp Business app se existing group ka official API access sabit nahi hota. Aap admin nahi; eligibility verify nahi hui. Selected text Share karein ya Without media export aur review karein."));back()}
  button("Rates and offline cache"){load("rates")};button("Alert history"){load("alerts")};button("Review imported rates"){review()}
  button("Add rate for review"){addRate()};button("Import WhatsApp text"){importText()};button("Market summary"){task {val j=JSONObject(api.request("/v1/summary"));layout();title("Last 24 hours");text(j.toString(2));back()}}
  title(tr("Business tools","کاروباری ٹولز","Karobar ke tools"))
  button("Business margin calculator"){
   layout();title("Landed cost and margin")
   val qty=field("Quantity kg");val buy=field("Purchase PKR/kg");val sell=field("Sale PKR/kg");val transport=field("Transport PKR","0");val other=field("Other costs PKR","0")
   button("Calculate"){task {
    val input=JSONObject().put("quantityKg",qty.text.toString().toDouble()).put("buyPkrKg",buy.text.toString().toDouble()).put("sellPkrKg",sell.text.toString().toDouble()).put("transportPkr",transport.text.toString().toDouble()).put("otherCostsPkr",other.text.toString().toDouble())
    val result=JSONObject(api.request("/v1/analytics/margin","POST",input));layout();title("Scenario result");text("Landed cost: PKR ${result.getDouble("landedCostPkr")}\nRevenue: PKR ${result.getDouble("revenuePkr")}\nGross margin: PKR ${result.getDouble("grossMarginPkr")}\nBreak-even: PKR ${result.getDouble("breakEvenPkrKg")}/kg");text(result.getString("notice"));back()
   }};back()
  }
  button("Add commodity"){layout();title("New commodity");val id=field("ID, e.g. soybean");val name=field("Display name");button("Save"){task {api.request("/v1/commodities","POST",JSONObject().put("id",id.text.toString()).put("name",name.text.toString()));dashboard()}};back()}
  button("Sign out and clear cache"){task {runCatching {api.request("/v1/devices/"+api.deviceId(),"DELETE")};FirebaseAuth.getInstance().signOut();incoming="";pendingReviewText="";pendingReviewEvent="";pendingReviewSource="Owner reviewed/manual";pendingReviewActor=null;cache.clear();getSystemService(NotificationManager::class.java).cancelAll();login()}}
  if(Build.VERSION.SDK_INT>=33) requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),1)
  task {api.register()}
  if(incoming.isNotBlank())importText()
 }
 private fun back(){button("Back"){if(FirebaseApp.getApps(this).isEmpty())entry()else dashboard()}}
 private fun load(kind:String){task {
  val uid=FirebaseAuth.getInstance().currentUser?.uid ?: return@task
  var offline=false;var savedAt=System.currentTimeMillis()
  val data=try {api.request("/v1/$kind").also {cache.save(kind,uid,it)}} catch(e:Exception) {
   // Never use cached data after server denies authorization.
   if(e is SecurityException || e.message?.startsWith("Server 401")==true||e.message?.startsWith("Server 403")==true||e.message?.startsWith("Server 503")==true)throw e
   val old=cache.read(kind,uid) ?: throw e;offline=true;savedAt=old.getLong("savedAt");old.getString("data")
  }
  NetworkSafety.requireSession(uid,FirebaseAuth.getInstance().currentUser?.uid)
  layout();title(if(kind=="rates")"Mandi rates" else "Alert history")
  text((if(offline)"OFFLINE CACHE" else "Synced")+" · "+java.util.Date(savedAt).toString())
  val rows=JSONArray(data);if(rows.length()==0)text("No approved observations yet.")
  for(i in 0 until rows.length()){
   val r=rows.getJSONObject(i)
   if(kind=="rates")text("${r.getString("commodity")} · ${r.getString("market")} · ${r.getString("grade")}\nPKR ${r.getLong("price_paisa_kg")/100.0}/kg · ${r.getString("source")}\nObserved: ${java.util.Date(r.getLong("observed_at")*1000)}${if(r.optInt("stale")==1 || System.currentTimeMillis()/1000-r.getLong("observed_at")>86400)" · STALE" else ""}\n${r.getString("source_url")}")
   else text("${r.getDouble("percent")}% · PKR ${r.getLong("old_price")/100.0} → ${r.getLong("new_price")/100.0}/kg\n${java.util.Date(r.getLong("created_at")*1000)}\n${r.getString("stream")}")
  };back()
 }}
 private fun review(){task {
  val rows=JSONArray(api.request("/v1/candidates"));layout();title("Review before publishing")
  if(rows.length()==0)text("No pending candidates.")
  for(i in 0 until rows.length()){
   val row=rows.getJSONObject(i);text(JSONObject(row.getString("payload")).toString(2))
   button("Approve this rate"){task {api.request("/v1/candidates/${row.getString("id")}/approve","POST",JSONObject());review()}}
  };back()
 }}
 private fun importText(){
  layout();title("WhatsApp export / shared text");text("Paste only rate rows you intend to import. Explicit format:\nsarson | Lahore | standard | 8000 | 40kg | 2026-10-18T10:00:00+05:00\nOrdinary chat lines are skipped. Maximum 20 rate rows per import.")
  val source=field("Source label","WhatsApp owner export");val content=field("Paste rate rows",incoming);incoming="";content.minLines=6
  button("Create review candidates"){task {val result=api.request("/v1/import/whatsapp","POST",JSONObject().put("source",source.text.toString()).put("text",content.text.toString()));Toast.makeText(this,result,Toast.LENGTH_LONG).show();review()}};back()
 }
 private fun addRate(){
  layout();title("New rate · review required")
  val commodity=field("Commodity ID","sarson");val market=field("Mandi","Samundri");val grade=field("Grade","standard");val source=field("Source","Owner manual");val sourceUrl=field("HTTPS source URL (optional)")
  val amount=field("PKR amount");amount.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
  val unit=field("Unit: kg, 40kg, 100kg","40kg");val date=field("Observed ISO timestamp with timezone",java.time.OffsetDateTime.now().toString())
  button("Submit for review"){task {
   val q=JSONObject().put("commodity",commodity.text.toString()).put("market",market.text.toString()).put("grade",grade.text.toString()).put("source",source.text.toString()).put("sourceUrl",sourceUrl.text.toString()).put("amount",amount.text.toString().toDouble()).put("unit",unit.text.toString()).put("observedAt",java.time.OffsetDateTime.parse(date.text.toString()).toEpochSecond()).put("sourceEventId",java.util.UUID.randomUUID().toString())
   api.request("/v1/candidates","POST",q);review()
  }};back()
 }
 override fun onPause(){if(collectorVisible){privateDialog?.dismiss();privateDialog=null;collectorUnlocked=false;collectorUnlockedActor=null};super.onPause()}
 override fun onResume(){super.onResume();if(collectorVisible&&!collectorUnlocked&&collectorUnlockPending==null)openGroupCollector()}
 private fun showPrivateDestination(){if(privateDestination=="reports")dailyReportsScreen()else groupCollectorScreen()}
 private fun openGroupCollector(destination:String=privateDestination){
  privateDestination=destination
  if(destination=="collector"&&Build.VERSION.SDK_INT<28){Toast.makeText(this,tr("This connector requires Android 9 or newer","اس کنیکٹر کے لیے اینڈرائیڈ ۹ یا جدید درکار ہے","Connector ke liye Android 9 ya newer chahiye"),Toast.LENGTH_LONG).show();return}
  val actor=CaptureActor.current(this)?:run{collectorUnlocked=false;collectorUnlockedActor=null;collectorVisible=false;entry();return}
  if(collectorUnlocked&&collectorUnlockedActor==actor){showPrivateDestination();return}
  collectorUnlocked=false
  val manager=getSystemService(KeyguardManager::class.java)
  if(!manager.isDeviceSecure){Toast.makeText(this,tr("Set a phone screen lock before enabling private collection","نجی کلیکشن سے پہلے فون کا سکرین لاک ضروری ہے","Private collection ke liye phone screen lock zaroori hai"),Toast.LENGTH_LONG).show();return}
  if(collectorUnlockPending!=null)return
  val unlock=manager.createConfirmDeviceCredentialIntent(tr("Malik Oil private inbox","ملک آئل نجی اِن باکس","Malik Oil private inbox"),tr("Unlock to configure or read selected group notifications","منتخب گروپ کی ترتیب یا مطالعے کے لیے اَن لاک کریں","Selected group configure/read karne ke liye unlock karein"))?:return
  collectorUnlockPending=actor;startActivityForResult(unlock,7301)
 }
 private fun groupCollectorScreen(offset:Int=0,sarsonOnly:Boolean=true,nearOnly:Boolean=false){
  val actor=CaptureActor.current(this)?:return
  if(!collectorUnlocked||collectorUnlockedActor!=actor){openGroupCollector();return}
  val store=GroupCaptureStore(this,actor)
  val data=try{store.read()}catch(error:Exception){layout(research=true);text(error.message?:"Encrypted inbox unavailable");back();return}
  layout(research=true);collectorVisible=true;title(tr("WhatsApp group notifications","واٹس ایپ گروپ نوٹیفکیشن","WhatsApp group notifications"))
  text(tr("Free local notification reader, not a WhatsApp API connection. No old chat history, replies or automatic rate approval.","مفت مقامی نوٹیفکیشن ریڈر؛ واٹس ایپ API کنکشن نہیں۔ پرانی چیٹ، جوابات یا خودکار نرخ منظوری نہیں۔","Free local notification reader; WhatsApp API connection nahi. Purani chat, replies ya auto rate approval nahi."))
  text(tr("Android notification access can expose notifications from all apps. Malik Oil filters WhatsApp Business only, explicit group conversations and the exact unique name you choose. Only the latest 200 matched messages are encrypted on this phone. No automatic upload. Muted, hidden, missing or unsupported notifications cannot be captured.","اینڈرائیڈ نوٹیفکیشن رسائی تمام ایپس کی اطلاعات تک پہنچ سکتی ہے۔ ملک آئل صرف واٹس ایپ بزنس، واضح گروپ اور آپ کا منتخب منفرد نام فلٹر کرتا ہے۔ آخری ۲۰۰ ملتی اطلاعات فون پر انکرپٹ محفوظ؛ خودکار اپ لوڈ نہیں۔ خاموش، پوشیدہ، غائب یا غیر معاون اطلاعات نہیں ملیں گی۔","Android notification access tamam apps ki notifications tak pohanch sakti hai. Malik Oil sirf WhatsApp Business, group marker aur aapka exact unique group name filter karta hai. Latest 200 matched messages phone par encrypted; auto upload nahi. Muted/hidden/missing/unsupported notification capture nahi hogi."))
  if(Build.VERSION.SDK_INT>=28){val allowed=getSystemService(NotificationManager::class.java).isNotificationListenerAccessGranted(android.content.ComponentName(this,GroupNotificationService::class.java));text(tr("Access: ","رسائی: ","Access: ")+allowed+" · "+tr("Listener ready: ","ریڈر تیار: ","Listener ready: ")+GroupNotificationService.connected+" · "+tr("Collection enabled: ","کلیکشن فعال: ","Collection enabled: ")+data.optBoolean("enabled"))}
  if(GroupNotificationService.storageError)text(tr("A notification/storage error occurred. Collection may be incomplete; existing history was preserved.","نوٹیفکیشن/محفوظ کرنے میں خرابی ہوئی؛ کلیکشن نامکمل ہو سکتی ہے، پرانا ریکارڈ محفوظ ہے۔","Notification/storage error hua; collection incomplete ho sakti hai, purana record preserve hai."))
  button(tr("Daily Excel and summary","روزانہ ایکسل اور خلاصہ","Daily Excel aur summary")){openGroupCollector("reports")}
  val group=field(tr("Exact unique group name (case sensitive)","گروپ کا عین منفرد نام","Exact unique group name (case sensitive)"),data.optString("group"));group.filters=arrayOf(android.text.InputFilter.LengthFilter(100))
  val priorityField=field(tr("Priority towns (comma separated)","ترجیحی شہر، کاما سے الگ","Priority shehr, comma se alag"),data.optString("priorityCities",MandiMessageIndex.DEFAULT_PRIORITIES));priorityField.filters=arrayOf(android.text.InputFilter.LengthFilter(400))
  text(tr("Punjab-focused feed: priority order is your chosen list, not a measured distance. Only messages received from your selected group are available; missing mandi rates remain unknown.","پنجاب فوکس فیڈ: ترجیح آپ کی فہرست کے مطابق ہے، فاصلے کی پیمائش نہیں۔ صرف منتخب گروپ کے موصول پیغامات؛ غائب منڈی نرخ نامعلوم۔","Punjab-focused feed: priority aapki list hai, measured distance nahi. Sirf selected group ke received messages; missing mandi rates unknown."))
  val discoveryConsent=CheckBox(this).apply{text=tr("Allow group titles only for 2 minutes; pause message collection","صرف ۲ منٹ گروپ نام دریافت کریں؛ پیغام کلیکشن بند رہے","2 minute sirf group titles; message collection pause");setTextColor(ink);minHeight=dp(48)};page.addView(discoveryConsent)
  button(tr("Find group names (titles only)","گروپ نام دیکھیے، صرف عنوان","Group names dhoondhein, titles only")){task{check(discoveryConsent.isChecked){"Select title-only discovery consent first"};store.discover();groupCollectorScreen(0,sarsonOnly,nearOnly)}}
  if(data.optLong("discoverUntil")>System.currentTimeMillis())text(tr("Title discovery active for up to 2 minutes. Wait for a group notification, then refresh. No message body is retained in this mode.","گروپ نام کی تلاش ۲ منٹ تک فعال۔ نوٹیفکیشن کے بعد ریفریش کریں؛ اس موڈ میں پیغام کا متن محفوظ نہیں۔","Title discovery max 2 minutes active. Group notification ke baad refresh karein; is mode mein message body save nahi hoti."))
  val foundGroups=data.optJSONArray("foundGroups")?:JSONArray();for(i in 0 until foundGroups.length()){val name=foundGroups.getString(i);button(tr("Use group: ","گروپ منتخب: ","Group chunein: ")+name){group.setText(name)}}
  val consent=CheckBox(this).apply{text=tr("I understand and allow local collection for this group only","میں سمجھتا ہوں اور صرف اس گروپ کی مقامی کلیکشن کی اجازت دیتا ہوں","Main samajhta hoon aur sirf is group ki local collection allow karta hoon");setTextColor(ink);minHeight=dp(48)};page.addView(consent)
  button(tr("Start selected-group collection","منتخب گروپ کی کلیکشن شروع کریں","Selected group collection start")){task{check(collectorUnlocked);check(consent.isChecked){tr("Select the consent box first","پہلے اجازت کا خانہ منتخب کریں","Pehle consent box select karein")};store.configure(group.text.toString(),true,priorityField.text.toString());groupCollectorScreen(0,sarsonOnly,nearOnly)}}
  button(tr("Open Android notification access","اینڈرائیڈ نوٹیفکیشن رسائی کھولیں","Android notification access kholein")){startActivity(Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))}
  button(tr("Reconnect listener","ریڈر دوبارہ جوڑیں","Listener reconnect")){android.service.notification.NotificationListenerService.requestRebind(android.content.ComponentName(this,GroupNotificationService::class.java));groupCollectorScreen(0,sarsonOnly,nearOnly)}
  button(tr("Stop collection","کلیکشن بند کریں","Collection stop")){task{store.stop();groupCollectorScreen(0,sarsonOnly,nearOnly)}}
  button(tr("Refresh inbox","اِن باکس تازہ کریں","Inbox refresh")){groupCollectorScreen(0,sarsonOnly,nearOnly)}
  button(tr("Clear captured inbox","محفوظ اِن باکس صاف کریں","Captured inbox clear")){task{store.clear();groupCollectorScreen(0,sarsonOnly,nearOnly)}}
  val rawItems=data.getJSONArray("items");val priorities=MandiMessageIndex.priorities(data.optString("priorityCities",MandiMessageIndex.DEFAULT_PRIORITIES));val indexed=(0 until rawItems.length()).map{val row=rawItems.getJSONObject(it);val cities=MandiMessageIndex.cities(row.optString("text"),priorities);Triple(row,cities,MandiMessageIndex.rank(cities,priorities))}.filter{(!sarsonOnly||MandiMessageIndex.sarsonMention(it.first.optString("text")))&&(!nearOnly||it.third<Int.MAX_VALUE)}.sortedWith(compareBy<Triple<JSONObject,List<String>,Int>>{it.third}.thenByDescending{it.first.optLong("capturedAt")});val items=JSONArray();indexed.forEach{items.put(it.first)}
  val sarsonView=CheckBox(this).apply{text=tr("Sarson testing view (keyword match, not verified price)","سرسوں ٹیسٹنگ ویو، لفظی شناخت، نرخ تصدیق نہیں","Sarson testing view, keyword match; verified rate nahi");setTextColor(ink);isChecked=sarsonOnly};page.addView(sarsonView);sarsonView.setOnCheckedChangeListener{_,checked->groupCollectorScreen(0,checked,nearOnly)}
  val nearbyView=CheckBox(this).apply{text=tr("Only priority-town mentions","صرف ترجیحی شہروں کا ذکر","Sirf priority shehron ke mentions");setTextColor(ink);isChecked=nearOnly};page.addView(nearbyView);nearbyView.setOnCheckedChangeListener{_,checked->groupCollectorScreen(0,sarsonOnly,checked)}
  text(tr("Other messages remain stored. Unknown locations need review; city mentions do not prove the market for a quoted rate.","دوسرے پیغامات محفوظ رہتے ہیں۔ نامعلوم مقام کا جائزہ ضروری؛ شہر کا ذکر نرخ کی منڈی کا ثبوت نہیں۔","Doosre messages stored rehte hain. Unknown location review karein; city mention quoted rate ki mandi ka saboot nahi."));title(tr("Captured messages · ","موصول پیغامات · ","Captured messages · ")+items.length())
  text(tr("Times below are notification/message times, not verified mandi observation dates. Duplicate group names cannot be distinguished reliably by notifications.","نیچے نوٹیفکیشن/پیغام کا وقت ہے، منڈی نرخ کی تصدیق شدہ تاریخ نہیں۔ ایک جیسے گروپ نام قابلِ اعتماد طور پر الگ نہیں ہو سکتے۔","Neeche notification/message time hai, verified mandi observation date nahi. Same group names ko reliably alag nahi kiya ja sakta."))
  for(index in items.length()-1-offset downTo maxOf(0,items.length()-offset-20)){val item=items.getJSONObject(index);card{text(item.getString("group")+" · "+java.time.Instant.ofEpochMilli(item.getLong("capturedAt")));text(item.getString("text"));button(tr("Review rows for daily report","روزانہ رپورٹ کے لیے نرخ کا جائزہ","Daily report ke liye rows review")){pendingReviewActor=actor;pendingReviewText=item.getString("text");pendingReviewSource="WhatsApp notification: "+item.getString("group");pendingReviewEvent=item.getString("id");openGroupCollector("reports")};text(tr("Mentioned cities: ","مذکورہ شہر: ","Mentioned cities: ")+MandiMessageIndex.cities(item.getString("text"),priorities).joinToString(", ").ifBlank{tr("Unknown / unrecognized","نامعلوم","Unknown / unrecognized")});button(tr("Check/edit as review rows","جائزے کی سطریں چیک/ترمیم کریں","Review rows check/edit")){incoming=item.getString("text");pluginsScreen()};button(tr("Copy message","پیغام کاپی کریں","Message copy")){getSystemService(android.content.ClipboardManager::class.java).setPrimaryClip(android.content.ClipData.newPlainText("Selected group notification",item.getString("text")))}}}
  if(offset>0)button(tr("Newer messages","نئے پیغامات","Newer messages")){groupCollectorScreen(maxOf(0,offset-20),sarsonOnly,nearOnly)}
  if(offset+20<items.length())button(tr("Older messages","پرانے پیغامات","Older messages")){groupCollectorScreen(offset+20,sarsonOnly,nearOnly)}
  button(tr("Close private inbox","نجی اِن باکس بند کریں","Private inbox band")){collectorUnlocked=false;collectorVisible=false;entry()}
 }
 private fun showPrivateSummary(title:String,summary:String){privateDialog?.dismiss();privateDialog=AlertDialog.Builder(this).setTitle(title).setMessage(summary).setPositiveButton(tr("Close","بند کریں","Band karein"),null).create();privateDialog?.show()}
 private fun dailyReportsScreen(){
  val actor=CaptureActor.current(this)?:return
  if(!collectorUnlocked||collectorUnlockedActor!=actor){openGroupCollector("reports");return}
  if(pendingReviewActor!=null&&pendingReviewActor!=actor){pendingReviewText="";pendingReviewSource="Owner reviewed/manual";pendingReviewEvent="";pendingReviewActor=null}
  val reports=DailyReports(this,actor);val config=try{reports.config()}catch(error:Exception){layout(research=true);text(error.message?:"Encrypted reports unavailable");back();return}
  layout(research=true);collectorVisible=true;privateDestination="reports";title(tr("Daily Excel reports","روزانہ ایکسل رپورٹ","Daily Excel reports"))
  text(tr("18:00 Pakistan time. Android may deliver later under battery restrictions. Reports are local/encrypted; the last 7 are retained. Exported Excel files are plaintext at your chosen destination.","پاکستان وقت شام ۶ بجے۔ اینڈرائیڈ بیٹری پابندیوں سے تاخیر ممکن۔ رپورٹ مقامی انکرپٹ؛ آخری ۷ محفوظ۔ ایکسپورٹ ایکسل منتخب جگہ پر عام فائل ہو گی۔","Pakistan time shaam 6 baje. Android battery restrictions se late ho sakta hai. Reports local/encrypted, last 7 retained. Exported Excel aapki selected jagah plain file hogi."))
  text(tr("Numeric rates require your review. Raw group messages have their own sheet; no invented rate or graph. Local approval does not publish backend rates or trigger trading/3% alerts.","عدد والے نرخ آپ کے جائزے کے بعد شامل ہوں گے۔ خام پیغامات الگ شیٹ؛ فرضی نرخ/گراف نہیں۔ مقامی منظوری بیک اینڈ اشاعت یا الرٹ نہیں۔","Numeric rates aapke review ke baad. Raw group messages separate sheet; invented rate/graph nahi. Local approval backend publish ya 3% alerts trigger nahi karti."))
  val filters=listOf("sarson","all","canola","til","taramira","alsi");text(tr("Report commodities","رپورٹ کی اجناس","Report commodities"));val filter=spinner(filters,filters.indexOf(config.optString("filter","sarson")).coerceAtLeast(0));page.addView(filter)
  val dailyConsent=CheckBox(this).apply{text=tr("Enable daily local report at 18:00 Asia/Karachi","روزانہ ۱۸:۰۰ پاکستان وقت مقامی رپورٹ فعال کریں","Daily local report 18:00 Asia/Karachi enable karein");setTextColor(ink);isChecked=config.optBoolean("enabled");minHeight=dp(48)};page.addView(dailyConsent)
  button(tr("Save daily schedule","روزانہ وقت محفوظ کریں","Daily schedule save")){task{reports.configure(dailyConsent.isChecked,language,filters[filter.selectedItemPosition]);if(dailyConsent.isChecked&&Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),7403);dailyReportsScreen()}}
  text(tr("Schedule enabled: ","وقت فعال: ","Schedule enabled: ")+config.optBoolean("enabled")+"\n"+tr("Next target: ","اگلا وقت: ","Next target: ")+DailyRateReport.nextSix(java.time.Instant.now()).atZone(DailyRateReport.zone))
  if(config.optBoolean("lastError"))text(tr("Last scheduled report failed. Existing reports are preserved; generate now to retry.","پچھلی رپورٹ میں خرابی؛ پرانی رپورٹس محفوظ۔ دوبارہ بنانے کے لیے ابھی بنائیں۔","Last scheduled report fail hui; existing reports preserved. Generate now se retry karein."))
  button(tr("Generate today's Excel and summary now","آج کی ایکسل اور خلاصہ ابھی بنائیں","Aaj ki Excel aur summary abhi banayein"),true){task{reports.configure(config.optBoolean("enabled"),language,filters[filter.selectedItemPosition]);val result=withContext(Dispatchers.IO){reports.generate()};dailyReportsScreen();showPrivateSummary(result.first.date.toString(),result.first.summary)}}
  val dates=config.optJSONArray("dates")?:JSONArray();for(index in 0 until dates.length()){val date=dates.getString(index);button(tr("Summary: ","خلاصہ: ","Summary: ")+date){task{val saved=reports.read(date);showPrivateSummary(date,saved.first)}};button(tr("Export Excel: ","ایکسل ایکسپورٹ: ","Excel export: ")+date){task{val data=reports.read(date);pendingReportExport?.second?.fill(0);pendingReportExport=actor to data.second;startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";putExtra(Intent.EXTRA_TITLE,"Malik-Oil-"+date+".xlsx")},7203)}}}
  title(tr("Review local rate rows","مقامی نرخ سطروں کا جائزہ","Local rate rows review"));text("commodity | market | grade | amount | unit | ISO observation date + timezone")
  val source=field(tr("Source label","ذریعے کا نام","Source label"),pendingReviewSource);source.filters=arrayOf(android.text.InputFilter.LengthFilter(200));val rows=field(tr("Rate rows; verify the actual observation date","نرخ سطریں؛ اصل مشاہدے کی تاریخ تصدیق کریں","Rate rows; actual observation date check karein"),pendingReviewText);rows.minLines=5;rows.filters=arrayOf(android.text.InputFilter.LengthFilter(50000))
  val reviewed=CheckBox(this).apply{text=tr("I checked commodity, mandi, grade, price, unit, source and observation date","میں نے جنس، منڈی، معیار، نرخ، اکائی، ذریعہ اور مشاہدے کی تاریخ چیک کی ہے","Main ne commodity, mandi, grade, price, unit, source aur observation date check ki hai");setTextColor(ink);minHeight=dp(48)};page.addView(reviewed)
  button(tr("Approve rows for local reports","مقامی رپورٹ کے لیے سطریں منظور کریں","Local reports ke liye rows approve")){task{check(reviewed.isChecked){"Confirm review before approving"};val count=LocalRateBook(this@MainActivity,actor).approve(rows.text.toString(),source.text.toString(),pendingReviewEvent);pendingReviewText="";pendingReviewEvent="";pendingReviewSource="Owner reviewed/manual";pendingReviewActor=null;Toast.makeText(this@MainActivity,getString(R.string.tool_row_count,count,tr("rows reviewed","سطریں جائزہ شدہ","rows reviewed")),Toast.LENGTH_LONG).show();dailyReportsScreen()}}
  button(tr("WhatsApp notification feed","واٹس ایپ نوٹیفکیشن فیڈ","WhatsApp notification feed")){openGroupCollector("collector")}
  button(tr("Close private reports","نجی رپورٹ بند کریں","Private reports band")){collectorUnlocked=false;collectorVisible=false;entry()}
 }
 private fun researchUid():String=if(FirebaseApp.getApps(this).isEmpty())"setup-preview" else FirebaseAuth.getInstance().currentUser?.uid?:error("Sign in required")
 private fun researchScreen(){
  val uid=researchUid();val store=ResearchStore(cache,uid)
  val ledger=try{store.load()}catch(error:Exception){layout(research=true);title("Research");text(error.message?:"Encrypted history unavailable");back();return}
  layout(research=true);title(tr("Research desk","تحقیق","Research desk"));text(tr("Encrypted sessions on this device. Sign out clears saved data.","اس فون پر انکرپٹ گفتگو۔ سائن آؤٹ پر محفوظ ڈیٹا صاف ہو گا۔","Is phone par encrypted sessions. Sign out par saved data clear hoga."))
  if(uid=="setup-preview")text(tr("Interface preview: offline plugins work; cloud research requires Firebase and backend setup.","انٹرفیس پیش منظر: آف لائن ٹولز دستیاب؛ ویب تحقیق کے لیے فائربیس اور بیک اینڈ سیٹ اپ درکار۔","Interface preview: offline plugins kaam karte hain; cloud research ko Firebase aur backend setup chahiye."))
  var session=ledger.sessions.find{it.id==activeResearchId}?:ledger.sessions.lastOrNull()
  fun persist(){check(researchUid()==uid);store.save(ledger)}
  fun select(item:ResearchThread?){activeResearchId=item?.id;researchScreen()}
  actionStrip{button(tr("Sessions · ","گفتگو · ","Sessions · ")+ledger.sessions.size){val input=EditText(this);input.hint=tr("Search sessions","گفتگو تلاش کریں","Sessions search");val list=ListView(this);val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(input);addView(list)};var visible=ledger.sessions.toList();fun update(){visible=ledger.search(input.text.toString());list.adapter=ArrayAdapter(this,android.R.layout.simple_list_item_1,visible.map{it.title.ifBlank{it.turns.firstOrNull()?.let{data->JSONObject(data).optString("question")}.orEmpty().ifBlank{"Research"}}})};update();val dialog=AlertDialog.Builder(this).setTitle(tr("Saved sessions","محفوظ گفتگو","Saved sessions")).setView(box).setNegativeButton(tr("Close","بند کریں","Band karein"),null).create();input.addTextChangedListener(object:android.text.TextWatcher{override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int){};override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int){update()};override fun afterTextChanged(s:android.text.Editable?){}});list.setOnItemClickListener{_,_,position,_->dialog.dismiss();select(visible[position])};dialog.show()}
  button(tr("New research","نئی تحقیق","Nayi research")){task{val item=ledger.create();persist();select(item)}}
  button(tr("Plugins","پلگ اِنز","Plugins")){pluginsScreen()}}
  session?.let{current->
   if(current.title.isNotBlank())title(current.title)
   actionStrip{button(tr("Rename session","نام بدلیں","Naam badlein")){val input=EditText(this);input.setText(current.title);input.filters=arrayOf(android.text.InputFilter.LengthFilter(100));AlertDialog.Builder(this).setTitle(tr("Session name","گفتگو کا نام","Session naam")).setView(input).setPositiveButton(tr("Save","محفوظ کریں","Save")){_,_->task{ledger.rename(current.id,input.text.toString());persist();select(current)}}.setNegativeButton(tr("Cancel","منسوخ","Cancel"),null).show()}
   button(tr("Delete session","گفتگو حذف کریں","Session delete")){task{ledger.delete(current.id);persist();select(ledger.sessions.lastOrNull())}}
   button(tr("Export conversation","گفتگو ایکسپورٹ کریں","Guftagu export")){val output=current.turns.joinToString("\n\n------------\n\n"){data->val r=JSONObject(data);val sources=r.getJSONArray("sources");r.optString("question")+"\n\n"+r.optString("text")+"\n\n"+(0 until sources.length()).joinToString("\n"){i->val source=sources.getJSONObject(i);"["+source.optInt("id",i+1)+"] "+source.optString("title")+" — "+source.optString("url")}};pendingExport=uid to output;startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="text/plain";putExtra(Intent.EXTRA_TITLE,"malik-oil-research.txt")},7202)}}
   for(data in current.turns){val result=JSONObject(data);page.addView(TextView(this).apply{text=result.optString("question");textSize=17f;setTextColor(ink);setPadding(dp(16),dp(14),dp(16),dp(14));background=surface(Color.rgb(238,240,237))},LinearLayout.LayoutParams(-1,-2).apply{marginStart=dp(32);bottomMargin=dp(14)});card{val findings=result.optJSONArray("findings");if(findings!=null&&findings.length()>0)for(index in 0 until findings.length()){val finding=findings.getJSONObject(index);val source=result.getJSONArray("sources").let{rows->(0 until rows.length()).map{rows.getJSONObject(it)}.find{it.optInt("id")==finding.optInt("sourceId")}};val summary=finding.getString("summary");val answer=android.text.SpannableString(summary+" ["+finding.getInt("sourceId")+"]");if(source!=null){val url=source.optString("url");runCatching{NetworkSafety.requireHttps(url);answer.setSpan(android.text.style.URLSpan(url),summary.length+1,answer.length,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)}};page.addView(TextView(this).apply{text=answer;textSize=19f;setTextColor(ink);setPadding(0,dp(8),0,dp(8));typeface=android.graphics.Typeface.SERIF;movementMethod=android.text.method.LinkMovementMethod.getInstance()})}else text(result.optString("text"));text(result.optString("notice"));button(tr("Copy answer and sources","جواب اور ذرائع کاپی کریں","Jawab aur sources copy")){val rows=result.getJSONArray("sources");val data=result.optString("question")+"\n\n"+result.optString("text")+"\n\n"+(0 until rows.length()).joinToString("\n"){i->val source=rows.getJSONObject(i);"["+source.optInt("id",i+1)+"] "+source.optString("url")};getSystemService(android.content.ClipboardManager::class.java).setPrimaryClip(android.content.ClipData.newPlainText("Malik Oil research",data))};val sources=result.getJSONArray("sources");button(tr("Sources · ","ذرائع · ","Sources · ")+sources.length()){val names=(0 until sources.length()).map{i->val source=sources.getJSONObject(i);"["+source.optInt("id",i+1)+"] "+source.optString("title")};AlertDialog.Builder(this).setTitle(tr("Sources and evidence","ذرائع اور اصل اقتباسات","Sources aur evidence")).setItems(names.toTypedArray()){_,index->val source=sources.getJSONObject(index);val supports=result.optJSONArray("supports")?:JSONArray();val quotes=(0 until supports.length()).map{supports.getJSONObject(it)}.filter{it.optInt("sourceId")==source.optInt("id")}.joinToString("\n"){it.optString("quote")};AlertDialog.Builder(this).setTitle(source.optString("title")).setMessage(source.optString("publisher")+"\n"+source.optString("publishedAt").ifBlank{tr("Publication date unknown","اشاعت کی تاریخ نامعلوم","Publication date maloom nahi") }+"\n"+source.optString("kind")+"\n\n"+quotes).setPositiveButton(tr("Open source","ذریعہ کھولیں","Source kholein")){_,_->runCatching{NetworkSafety.requireHttps(source.getString("url"));startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse(source.getString("url"))))}}.setNegativeButton(tr("Close","بند کریں","Band karein"),null).show()}.setNegativeButton(tr("Close","بند کریں","Band karein"),null).show()}}}
  }
  val question=field(tr("Your public market question","عوامی مارکیٹ کا سوال","Aapka public market sawal"));question.minLines=3;question.filters=arrayOf(android.text.InputFilter.LengthFilter(300))
  val previous=session?.turns?.lastOrNull()?.let{JSONObject(it)}
  val market=field(tr("Mandi","منڈی","Mandi"),previous?.optString("market")?:"Samundri");val ids=listOf("sarson","canola","til","taramira","alsi");text(tr("Commodity","جنس","Commodity"));val commodity=spinner(ids,ids.indexOf(previous?.optString("commodity")?:"sarson").coerceAtLeast(0));page.addView(commodity)
  text(tr("Research mode","تحقیق کا انداز","Research mode"));val depth=spinner(listOf(tr("Quick · 1 search","فوری · ۱ تلاش","Quick · 1 search"),tr("Deeper · up to 3 searches","گہری · ۳ تک تلاش","Deeper · max 3 searches")));page.addView(depth)
  val status=TextView(this).apply{setTextColor(mutedInk);textSize=14f};page.addView(status)
  val run=Button(this).apply{text=tr("Research the web","ویب تحقیق","Web research karein");isEnabled=uid!="setup-preview";minHeight=dp(48)};page.addView(run)
  run.setOnClickListener{if(researchBusy)return@setOnClickListener;val requestQuestion=question.text.toString().trim();val requestMarket=market.text.toString();val requestCommodity=ids[commodity.selectedItemPosition];val requestDepth=if(depth.selectedItemPosition==1)"deep" else "quick";researchBusy=true;run.isEnabled=false;status.text=tr("Searching and checking sources…","ذرائع کی تلاش اور جانچ…","Sources search aur check ho rahe hain…");task{try{
   val selectedScope=requestMarket+"|"+requestCommodity;if(session==null||session!!.scope.isNotBlank()&&session!!.scope!=selectedScope)session=ledger.create();val current=session!!;require(current.turns.size<12){"Start a new session"};current.scope=selectedScope
   val result=JSONObject(api.request("/v1/research","POST",JSONObject().put("question",requestQuestion).put("market",requestMarket).put("commodity",requestCommodity).put("depth",requestDepth).put("language",language).put("context",JSONArray(current.turns.takeLast(2).map{JSONObject(it).optString("question")}))))
   check(researchUid()==uid);result.put("question",requestQuestion);val sources=result.getJSONArray("sources");for(index in 0 until sources.length())sources.getJSONObject(index).remove("text");ledger.append(current.id,result.toString());persist();activeResearchId=current.id;researchScreen()
  }finally{researchBusy=false;run.isEnabled=true}}}
  back()
 }
 private fun pluginsScreen(){
  layout(research=true);title(tr("Plugins","پلگ اِنز","Plugins"));text(tr("Built-in tools. No downloaded code or automatic WhatsApp group access.","اندرونی ٹولز؛ خودکار واٹس ایپ گروپ رسائی نہیں۔","Built-in tools; automatic WhatsApp group access nahi."))
  button(tr("WhatsApp group notifications · free","واٹس ایپ گروپ نوٹیفکیشن · مفت","WhatsApp group notifications · free")){openGroupCollector("collector")};button(tr("Daily Excel reports · 6 pm","روزانہ ایکسل رپورٹ · شام ۶ بجے","Daily Excel reports · shaam 6 baje")){openGroupCollector("reports")}
  button(tr("Web researcher / saved-session search","ویب تحقیق اور محفوظ گفتگو","Web researcher / saved sessions")){researchScreen()}
  card{title(tr("Rate converter","نرخ کی تبدیلی","Rate converter"));val amount=field(tr("PKR amount","رقم روپے","PKR amount"),"8000");amount.inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL;val units=listOf("kg","40kg","100kg");val unit=spinner(units,1);page.addView(unit);val output=TextView(this).apply{setTextColor(ink)};page.addView(output);button(tr("Convert","تبدیل کریں","Convert")){runCatching{output.text=PluginTools.convert(amount.text.toString().trim(),units[unit.selectedItemPosition]).entries.joinToString("\n"){"PKR "+it.value+" / "+it.key}}.onFailure{output.text=it.message}}}
  card{title(tr("WhatsApp row checker","واٹس ایپ سطروں کی جانچ","WhatsApp row checker"));text("commodity | market | grade | amount | unit | ISO date + timezone");val input=field(tr("Selected rate rows only","صرف منتخب نرخ سطریں","Sirf selected rate rows"),incoming);input.minLines=4;input.filters=arrayOf(android.text.InputFilter.LengthFilter(50000));val output=TextView(this).apply{setTextColor(ink)};page.addView(output);button(tr("Check rows","سطریں چیک کریں","Rows check")){runCatching{val rows=PluginTools.checkRows(input.text.toString());output.text=getString(R.string.tool_row_count,rows.size,tr("rows ready for review; no rates approved","سطریں جائزے کے لیے تیار؛ نرخ منظور نہیں ہوئے","rows review ke liye tayyar; rates approve nahi huay"));incoming=rows.joinToString("\n")}.onFailure{output.text=it.message}};button(tr("Copy checked rows","جانچی سطریں کاپی کریں","Checked rows copy")){task{val rows=PluginTools.checkRows(input.text.toString());getSystemService(android.content.ClipboardManager::class.java).setPrimaryClip(android.content.ClipData.newPlainText("Selected rate rows",rows.joinToString("\n")))}};if(researchUid()!="setup-preview")button(tr("Open import for owner review","مالک کے جائزے کے لیے درآمد","Owner review ke liye import")){task{incoming=PluginTools.checkRows(input.text.toString()).joinToString("\n");importText()}}}
  button(tr("Back to research","تحقیق پر واپس","Research par wapas")){researchScreen()}
 }
 private fun history(){task{
  val rates=JSONArray(api.request("/v1/rates"));layout();title(tr("Price history","قیمت کی تاریخ","Rate history"));text(tr("Choose a matching commodity, mandi, grade and source.","ایک جنس، منڈی، معیار اور ذریعہ منتخب کریں۔","Ek commodity, mandi, grade aur source select karein."))
  if(rates.length()==0)text(tr("No approved observations yet.","ابھی کوئی منظور شدہ مشاہدہ نہیں۔","Abhi approved observations nahi."))
  for(i in 0 until rates.length()){val rate=rates.getJSONObject(i);card{button(rate.getString("commodity")+" · "+rate.getString("market")+" · "+rate.getString("grade")){task{val rows=JSONArray(api.request("/v1/history?stream="+java.net.URLEncoder.encode(rate.getString("stream"),"UTF-8")));historyRows(rate,rows,7)}};text("PKR "+rate.getLong("price_paisa_kg")/100.0+"/kg · "+rate.getString("source"))}};back()
 }}
 private fun historyRows(rate:JSONObject,all:JSONArray,days:Int){
  layout();title(rate.getString("commodity")+" · "+rate.getString("market"));text(rate.getString("grade")+" · "+rate.getString("source"))
  val ranges=LinearLayout(this);page.addView(ranges);for((name,range) in listOf("1D" to 1,"1W" to 7,"1M" to 30,"All" to 3650)){ranges.addView(Button(this).apply{text=if(name=="All")tr("All","سب","Sab")else name;isAllCaps=false;minHeight=dp(48);setTextColor(if(days==range)Color.rgb(11,21,18)else Color.rgb(237,244,237));backgroundTintList=android.content.res.ColorStateList.valueOf(if(days==range)Color.rgb(239,206,104)else Color.rgb(26,45,36));setOnClickListener{historyRows(rate,all,range)}},LinearLayout.LayoutParams(0,-2,1f))}
  val rows=JSONArray();val cutoff=System.currentTimeMillis()/1000-days.toLong()*86400
  for(i in 0 until all.length())if(all.getJSONObject(i).getLong("observed_at")>=cutoff)rows.put(all.getJSONObject(i))
  if(rows.length()>0){val prices=(0 until rows.length()).map{rows.getJSONObject(it).getLong("price_paisa_kg")/100.0};val delta=(prices.last()-prices.first())*100/prices.first();card{title("PKR "+prices.last()+" / kg");text(tr("Range change","مدت کی تبدیلی","Range change")+": "+"%+.2f%%".format(delta)+"\n"+tr("High / Low","بلند / کم","High / Low")+": "+prices.max()+" / "+prices.min())}}
  val inspected=TextView(this).apply{setTextColor(accent);textSize=14f}
  if(rows.length()>=4){page.addView(PriceChart(this,rows){value->inspected.text=value},LinearLayout.LayoutParams(-1,dp(270)));page.addView(inspected)}else text(tr("At least4matching observations needed for a chart.","گراف کے لیے کم از کم ۴ یکساں مشاہدات ضروری ہیں۔","Graph ke liye kam az kam4matching observations chahiye."))
  text(tr("Time axis → PKR/kg. No volume or invented candles. Latest500observations maximum.","وقت → روپے فی کلو۔ حجم یا فرضی کینڈلز نہیں۔ زیادہ سے زیادہ آخری ۵۰۰ مشاہدات۔","Time → PKR/kg. Volume ya invented candles nahi. Latest500observations maximum."))
  if(rows.length()>0)button(tr("Export observations · CSV","مشاہدات ایکسپورٹ · CSV","Observations export · CSV")){val uid=FirebaseAuth.getInstance().currentUser?.uid?:return@button;val data=mutableListOf(listOf("commodity","market","grade","source","observed_utc","price_pkr_kg","sample"));for(i in 0 until rows.length()){val row=rows.getJSONObject(i);data.add(listOf(row.getString("commodity"),row.getString("market"),row.getString("grade"),row.getString("source"),java.time.Instant.ofEpochSecond(row.getLong("observed_at")).toString(),(row.getLong("price_paisa_kg")/100.0).toString(),"unknown"))};pendingExport=uid to CsvExport.encode(data);startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="text/csv";putExtra(Intent.EXTRA_TITLE,"malik-oil-observations.csv")},7201)}
  button(tr("Exact observations","مشاہدات کی تفصیل","Exact observations")){layout();title(rate.getString("commodity")+" · "+rate.getString("market"));for(i in 0 until rows.length()){val row=rows.getJSONObject(i);text(java.util.Date(row.getLong("observed_at")*1000).toString()+" · PKR "+row.getLong("price_paisa_kg")/100.0+"/kg")};button(tr("Back to chart","گراف پر واپس","Graph par wapas")){historyRows(rate,all,days)}};back()
 }
 override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){
  super.onActivityResult(requestCode,resultCode,data)
  if(requestCode==7301){val expected=collectorUnlockPending;collectorUnlockPending=null;collectorUnlocked=resultCode==RESULT_OK&&expected!=null&&CaptureActor.current(this)==expected;collectorUnlockedActor=if(collectorUnlocked)expected else null;if(collectorUnlocked)showPrivateDestination()else{collectorVisible=false;entry()};return}
  if(requestCode==7203){val pending=pendingReportExport;pendingReportExport=null;if(resultCode!=RESULT_OK||pending==null){pending?.second?.fill(0);return};task{try{NetworkSafety.requireSession(pending.first,CaptureActor.current(this@MainActivity));val uri=data?.data?:error("No destination");require(uri.scheme=="content");withContext(Dispatchers.IO){NetworkSafety.requireSession(pending.first,CaptureActor.current(this@MainActivity));val output=contentResolver.openOutputStream(uri,"w")?:error("Cannot open destination");output.use{it.write(pending.second)}}}finally{pending.second.fill(0)}};return}
  if(requestCode!=7201&&requestCode!=7202)return
  val pending=pendingExport;pendingExport=null
  if(resultCode!=RESULT_OK||pending==null)return
  task{NetworkSafety.requireSession(pending.first,if(pending.first=="setup-preview")researchUid()else FirebaseAuth.getInstance().currentUser?.uid);val uri=data?.data?:error("No destination selected");require(uri.scheme=="content"){"Invalid document destination"};withContext(Dispatchers.IO){NetworkSafety.requireSession(pending.first,if(pending.first=="setup-preview")researchUid()else FirebaseAuth.getInstance().currentUser?.uid);val output=contentResolver.openOutputStream(uri,"w")?:error("Cannot open destination");output.use{it.write(pending.second.toByteArray(Charsets.UTF_8))}};Toast.makeText(this@MainActivity,tr("File saved","فائل محفوظ ہو گئی","File save ho gayi"),Toast.LENGTH_SHORT).show()}
 }
}
