package com.malikoil.intelligence
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONObject
class SecureCache(private val context: Context) {
 private fun key(): SecretKey {
  val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
  (store.getKey("malik.cache", null) as? SecretKey)?.let { return it }
  return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
   init(KeyGenParameterSpec.Builder("malik.cache",KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
  }.generateKey()
 }
 private fun file(name:String):java.io.File {require(name.matches(Regex("[a-zA-Z0-9_-]{1,100}")));return java.io.File(context.filesDir,"cache_$name.bin")}
 fun exists(name:String)=file(name).exists()
 fun save(name:String,uid:String,data:String) {
  val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key())
  val payload=JSONObject().put("uid",uid).put("savedAt",System.currentTimeMillis()).put("data",data).toString().toByteArray()
  val bytes=c.iv+c.doFinal(payload)
  val temp=java.io.File(context.filesDir,"cache_$name.tmp");temp.writeBytes(bytes)
  check(temp.renameTo(file(name))) { "Cache write failed" }
 }
 fun read(name:String,uid:String):JSONObject? = runCatching {
  require(file(name).length() in 29L..8388608L) { "Invalid cache size" };val bytes=file(name).readBytes();val c=Cipher.getInstance("AES/GCM/NoPadding")
  c.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,bytes.copyOfRange(0,12)))
  JSONObject(String(c.doFinal(bytes.copyOfRange(12,bytes.size)))).takeIf { it.getString("uid")==uid }
 }.getOrNull()
 fun remove(name:String){val target=file(name);check(!target.exists()||target.delete()){ "Encrypted report cleanup failed" }}
 fun clear() { context.filesDir.listFiles()?.filter { it.name.startsWith("cache_") }?.forEach { it.delete() } }
}
