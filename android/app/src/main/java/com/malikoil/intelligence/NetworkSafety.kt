package com.malikoil.intelligence
import java.io.InputStream
import java.io.ByteArrayOutputStream
import java.net.URI
class AccessDeniedException(val status:Int):SecurityException("Server $status: access denied; sign in again")
object NetworkSafety {
 fun checkAuthorization(status:Int){if(status==401||status==403)throw AccessDeniedException(status)}
 fun requireSession(expected:String,actual:String?){if(expected!=actual)throw SecurityException("Session changed; sign in again")}
 fun readLimited(input:InputStream,limit:Int):String {
  val result=ByteArrayOutputStream();val buffer=ByteArray(8192);var count=0
  while(true){val n=input.read(buffer);if(n<0)break;count+=n;if(count>limit)throw SecurityException("Server response exceeds safe size");result.write(buffer,0,n)}
  return result.toString("UTF-8")
 }
 fun requireHttps(value:String){val uri=URI(value);require(uri.scheme=="https" && !uri.host.isNullOrBlank() && uri.rawUserInfo==null && uri.rawFragment==null){"A valid HTTPS URL without credentials is required"}}
}
