package com.malikoil.intelligence
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent
import android.view.KeyEvent
import android.util.TypedValue
import org.json.JSONArray
class PriceChart @JvmOverloads constructor(context:Context,rows:JSONArray=JSONArray(),private val inspect:(String)->Unit={}):View(context){
 private val pen=Paint(Paint.ANTI_ALIAS_FLAG);private val linePath=Path();private val areaPath=Path()
 private val density=resources.displayMetrics.density;private val prices=FloatArray(rows.length()){rows.getJSONObject(it).getLong("price_paisa_kg")/100f};private val times=LongArray(rows.length()){rows.getJSONObject(it).getLong("observed_at")}
 private val pointX=FloatArray(prices.size);private val pointY=FloatArray(prices.size);private var selected=-1;private var plotLeft=0f;private var plotRight=0f;private var plotTop=0f;private var plotBottom=0f;private var shade:Shader?=null
 private val low=prices.minOrNull()?:0f;private val high=prices.maxOrNull()?:0f;private val pad=maxOf((high-low)*.16f,1f);private val min=low-pad;private val max=high+pad
 private val axis=Array(5){"%.1f".format(max-(max-min)*it/4)}
 private val dateFormat=java.text.SimpleDateFormat("dd MMM HH:mm",java.util.Locale.ENGLISH)
 private val firstLabel=if(times.isEmpty())""else dateFormat.format(java.util.Date(times.first()*1000));private val lastLabel=if(times.isEmpty())""else dateFormat.format(java.util.Date(times.last()*1000))
 private val axisSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12f,resources.displayMetrics);private val dateSize=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10f,resources.displayMetrics)
 init{contentDescription="Price history in PKR/kg. Use left/right arrows or touch to inspect; exact observations are available below.";isFocusable=true;isClickable=true;layoutDirection=LAYOUT_DIRECTION_LTR;setBackgroundColor(Color.rgb(18,34,27))}
 private fun show(index:Int){selected=index.coerceIn(0,prices.size-1);val value=java.util.Date(times[selected]*1000).toString()+" · PKR "+prices[selected]+"/kg";contentDescription=value;inspect(value);invalidate()}
 override fun performClick():Boolean{super.performClick();return true}
 override fun onTouchEvent(event:MotionEvent):Boolean{
  if(prices.size<4)return false
  if(event.action==MotionEvent.ACTION_DOWN||event.action==MotionEvent.ACTION_MOVE||event.action==MotionEvent.ACTION_UP){var nearest=0;for(i in pointX.indices)if(kotlin.math.abs(pointX[i]-event.x)<kotlin.math.abs(pointX[nearest]-event.x))nearest=i;show(nearest);if(event.action==MotionEvent.ACTION_UP)performClick();return true};return super.onTouchEvent(event)
 }
 override fun onKeyDown(keyCode:Int,event:KeyEvent?):Boolean{if(prices.size>=4&&(keyCode==KeyEvent.KEYCODE_DPAD_LEFT||keyCode==KeyEvent.KEYCODE_DPAD_RIGHT)){show((if(selected<0)0 else selected)+(if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT)1 else -1));return true};return super.onKeyDown(keyCode,event)}
 override fun onSizeChanged(w:Int,h:Int,oldw:Int,oldh:Int){
  super.onSizeChanged(w,h,oldw,oldh);if(prices.size<4)return;pen.textSize=axisSize;plotLeft=axis.maxOf{pen.measureText(it)}+12*density;plotRight=w-12*density;plotTop=24*density;plotBottom=h-40*density;val span=maxOf(1L,times.last()-times.first())
  linePath.reset();for(i in prices.indices){pointX[i]=plotLeft+(times[i]-times.first()).toFloat()/span*(plotRight-plotLeft);pointY[i]=plotTop+(max-prices[i])/(max-min)*(plotBottom-plotTop);if(i==0)linePath.moveTo(pointX[i],pointY[i])else linePath.lineTo(pointX[i],pointY[i])}
  areaPath.set(linePath);areaPath.lineTo(pointX.last(),plotBottom);areaPath.lineTo(pointX.first(),plotBottom);areaPath.close();shade=LinearGradient(0f,plotTop,0f,plotBottom,Color.argb(48,231,193,132),Color.TRANSPARENT,Shader.TileMode.CLAMP)
 }
 override fun onDraw(canvas:Canvas){
  super.onDraw(canvas);if(prices.size<4)return;pen.textSize=axisSize;pen.style=Paint.Style.FILL;pen.shader=null
  for(i in axis.indices){val y=plotTop+(plotBottom-plotTop)*i/4;pen.color=Color.rgb(43,68,54);pen.strokeWidth=1f;canvas.drawLine(plotLeft,y,plotRight,y,pen);pen.color=Color.rgb(169,188,175);canvas.drawText(axis[i],2*density,y+4*density,pen)}
  pen.shader=shade;canvas.drawPath(areaPath,pen);pen.shader=null;pen.color=Color.rgb(231,193,132);pen.style=Paint.Style.STROKE;pen.strokeWidth=2*density;canvas.drawPath(linePath,pen);pen.style=Paint.Style.FILL
  if(selected>=0){pen.color=Color.rgb(169,188,175);pen.strokeWidth=density;canvas.drawLine(pointX[selected],plotTop,pointX[selected],plotBottom,pen);pen.color=Color.rgb(231,193,132);canvas.drawCircle(pointX[selected],pointY[selected],4*density,pen)}
  pen.color=Color.rgb(169,188,175);pen.textSize=dateSize;canvas.drawText(firstLabel,plotLeft,height-12*density,pen);canvas.drawText(lastLabel,plotRight-pen.measureText(lastLabel),height-12*density,pen)
 }
}
