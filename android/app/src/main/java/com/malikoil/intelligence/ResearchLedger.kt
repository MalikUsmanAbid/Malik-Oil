package com.malikoil.intelligence
import java.util.UUID
data class ResearchThread(val id:String=UUID.randomUUID().toString(),var title:String="",var scope:String="",val turns:MutableList<String> = mutableListOf())
class ResearchLedger(val sessions:MutableList<ResearchThread> = mutableListOf()){
 init{require(sessions.size<=8);require(sessions.map{it.id}.distinct().size==sessions.size);sessions.forEach{UUID.fromString(it.id);require(it.title.length<=100&&it.scope.length<=200&&it.turns.size<=12);it.turns.forEach{turn->require(turn.toByteArray().size<=65536)}};require(bytes()<=2097152)}
 fun create():ResearchThread{require(sessions.size<8){"Maximum 8 sessions. Export/delete an older session first."};return ResearchThread().also{sessions.add(it)}}
 fun append(id:String,turn:String){val session=sessions.first{it.id==id};require(session.turns.size<12){"Start new research after 12 answers"};require(turn.toByteArray().size<=65536){"Answer too large to save"};require(bytes()+turn.toByteArray().size<=2097152){"Session storage full; export/delete an older session"};session.turns.add(turn)}
 fun rename(id:String,title:String){require(title.trim().length in 1..100);sessions.first{it.id==id}.title=title.trim()}
 fun delete(id:String){sessions.removeAll{it.id==id}}
 fun search(query:String)=sessions.filter{it.title.contains(query,true)||it.turns.any{turn->turn.contains(query,true)}}
 private fun bytes()=sessions.sumOf{s->s.turns.sumOf{it.toByteArray().size}}
}
