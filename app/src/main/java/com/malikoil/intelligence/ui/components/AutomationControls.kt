package com.malikoil.intelligence.ui.components
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.malikoil.intelligence.*
import com.malikoil.intelligence.ui.MarketViewModel
import com.malikoil.intelligence.tts.MarketTtsManager
import kotlinx.coroutines.launch
@Composable
fun AutomationControls(viewModel:MarketViewModel,summary:String?,language:String){
 val context=LocalContext.current
 val scope=rememberCoroutineScope()
 val settings=remember{AutomationStore.read(context)}
 var group by remember{mutableStateOf(settings.optString("group"))}
 var capture by remember{mutableStateOf(settings.optBoolean("capture"))}
 var daily by remember{mutableStateOf(settings.optBoolean("daily"))}
 var id by remember{mutableStateOf("")}
 var status by remember{mutableStateOf("")}
 var busy by remember{mutableStateOf(false)}
 val speaker=remember{MarketTtsManager(context)}
 DisposableEffect(speaker){
  val lifecycle=(context as? androidx.lifecycle.LifecycleOwner)?.lifecycle
  val observer=androidx.lifecycle.LifecycleEventObserver{_,event->if(event==androidx.lifecycle.Lifecycle.Event.ON_PAUSE)speaker.stop()}
  lifecycle?.addObserver(observer)
  onDispose{lifecycle?.removeObserver(observer);speaker.stop();speaker.shutdown()}
 }
 fun label(en:String,ur:String,roman:String)=when(language){"ur"->ur;"roman"->roman;else->en}
 Card(modifier=Modifier.fillMaxWidth()){
  Column(modifier=Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
   Text(label("Daily Punjab market collection","روزانہ پنجاب مارکیٹ معلومات","Daily Punjab market collection"),style=MaterialTheme.typography.titleMedium)
   Text(label("Sarson first. Nearby priority includes Samundri, Faisalabad and Gojra. Public news can lack current mandi quotes.","پہلے سرسوں۔ سمندری، فیصل آباد اور گوجرہ ترجیح ہیں۔ تازہ منڈی نرخ خبر میں دستیاب نہ بھی ہوں۔","Pehle sarson. Samundri, Faisalabad aur Gojra priority. News mein current mandi quote na bhi ho sakta hai."))
   OutlinedTextField(value=id,onValueChange={id=it.take(40)},label={Text(label("Add commodity ID, e.g. wheat","نئی جنس کا شناختی نام، مثلاً wheat","Commodity ID, misal wheat"))},modifier=Modifier.fillMaxWidth())
   Button(onClick={runCatching{require(id.matches(Regex("[a-z][a-z0-9_-]{1,39}")));require(viewModel.repository.commodities.value.size<20);viewModel.repository.addCustomCommodity(id,id,id);viewModel.repository.persistOwnerState();id="";status="Commodity saved"}.onFailure{status=it.message?:"Invalid commodity"}}){Text(label("Add commodity","جنس شامل کریں","Commodity add"))}
   Row {Checkbox(checked=daily,onCheckedChange={daily=it});Text(label("Daily research and report at 18:00 Pakistan time","روزانہ شام ۶ بجے تحقیق اور رپورٹ","Daily research aur report shaam 6 baje"))}
   Text(label("Android battery and network availability can delay the job.","اینڈرائیڈ بیٹری اور نیٹ ورک سے تاخیر ممکن ہے۔","Android battery/network ki wajah se late ho sakta hai."))
   OutlinedTextField(value=group,onValueChange={group=it.take(100)},label={Text(label("Exact WhatsApp Business group title","واٹس ایپ بزنس گروپ کا عین نام","Exact WhatsApp Business group name"))},modifier=Modifier.fillMaxWidth())
   Row {Checkbox(checked=capture,onCheckedChange={capture=it});Text(label("Allow incoming rate notifications from this selected group","منتخب گروپ کے نرخ نوٹیفکیشن وصول کرنے کی اجازت","Is selected group ke incoming rate notifications allow"))}
   Text(label("Received group notifications are encrypted locally. Captured rates enter owner review before price history.","گروپ نوٹیفکیشن مقامی انکرپٹ محفوظ ہوتے ہیں۔ نرخ تاریخ سے پہلے مالک جائزہ لیتا ہے۔","Group notifications local encrypted save hote hain. Price history se pehle owner review hota hai."))
   Button(onClick={runCatching{AutomationStore.configure(context,group,capture,daily);status="Settings saved"}.onFailure{status=it.message?:"Settings unavailable"}}){Text(label("Save permissions and schedule","اجازت اور وقت محفوظ کریں","Permissions aur schedule save"))}
   OutlinedButton(onClick={context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))}){Text(label("Grant Android notification access","اینڈرائیڈ نوٹیفکیشن رسائی دیں","Android notification access dein"))}
   Button(enabled=!busy,onClick={busy=true;scope.launch{try{viewModel.repository.dailyResearch();viewModel.repository.generateTodayReport();viewModel.repository.persistOwnerState();status="Research and report saved"}catch(e:Exception){status=e.message?:"Public research unavailable"}finally{busy=false}}}){Text(if(busy)"Searching public sources…" else label("Research and report now","ابھی تحقیق اور رپورٹ","Abhi research aur report"))}
   if(summary!=null){
    OutlinedButton(onClick={speaker.speak(summary,language)}){Text(label("Listen to daily summary","روزانہ خلاصہ سنیں","Daily summary sunein"))}
    OutlinedButton(onClick={speaker.stop()}){Text(label("Stop voice","آواز بند کریں","Voice band"))}
   }
   if(status.isNotBlank())Text(status)
  }
 }
}
