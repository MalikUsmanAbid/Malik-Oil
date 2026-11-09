package com.malikoil.intelligence
import org.junit.Test
import org.junit.Assert.*
import java.io.ByteArrayInputStream
class NetworkSafetyTest {
 @Test fun authorizationDenialCannotBecomeOfflineFallback(){
  NetworkSafety.checkAuthorization(200);NetworkSafety.checkAuthorization(503)
  for(status in listOf(401,403)){
   try{NetworkSafety.checkAuthorization(status);fail("Authorization denial accepted")}catch(expected:AccessDeniedException){assertEquals(status,expected.status)}
  }
 }

 @Test fun pendingResponseIsRejectedAfterLogoutOrUserChange(){
  NetworkSafety.requireSession("owner","owner")
  for(current in listOf(null,"another-user")){
   try{NetworkSafety.requireSession("owner",current);fail("Old session accepted")}catch(expected:SecurityException){}
  }
 }
 @Test fun responseLimitCountsBytesAndNeverReturnsOversizedPayload(){
  val bytes="اردو".toByteArray(Charsets.UTF_8)
  assertEquals("اردو",NetworkSafety.readLimited(ByteArrayInputStream(bytes),bytes.size))
  try{NetworkSafety.readLimited(ByteArrayInputStream(bytes),bytes.size-1);fail("Oversized body accepted")}catch(expected:SecurityException){}
 }
 @Test fun linksRejectCleartextAndEmbeddedCredentials(){
  NetworkSafety.requireHttps("https://www.dawn.com/news/123")
  for(url in listOf("http://example.com","https://user:secret@example.com/","javascript:alert(1)","https:///missing-host")){
   try{NetworkSafety.requireHttps(url);fail("Unsafe URL accepted")}catch(expected:IllegalArgumentException){}
  }
 }
}
