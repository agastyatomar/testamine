package com.agastyatomar.animatrix.core
import android.app.ActivityManager
import android.content.Context
data class AICapability(val level:Int,val ramGb:Float,val mode:String)
object AdaptiveAI{fun detect(c:Context):AICapability{val m=c.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager;val i=ActivityManager.MemoryInfo();m.getMemoryInfo(i);val r=i.totalMem/1073741824f;val l=when{r>=10->4;r>=7->3;r>=4->2;r>=2->1;else->0};return AICapability(l,r,when(l){0->"AI optional";1->"Tiny local AI";2->"Small quantized AI";3->"Medium local AI";else->"Full adaptive local AI"})}}