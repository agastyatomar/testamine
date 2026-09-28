package com.agastyatomar.animatrix.model
import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
data class Point(val x:Float,val y:Float)
data class Stroke(val points:MutableList<Point> = mutableListOf(),var color:Int=Color.WHITE,var width:Float=6f,var tool:String="pencil")
data class VectorObject(var type:String,var x:Float,var y:Float,var w:Float,var h:Float,var color:Int=Color.WHITE)
data class PuppetNode(var id:String,var x:Float,var y:Float,var parent:String?=null,var radius:Float=12f)
data class Frame(val index:Int,val strokes:MutableList<Stroke> = mutableListOf(),val vectors:MutableList<VectorObject> = mutableListOf(),val puppet:MutableList<PuppetNode> = mutableListOf())
data class Layer(val id:String,var name:String,var kind:String="drawing",var visible:Boolean=true,var locked:Boolean=false,var opacity:Float=1f,val frames:MutableList<Frame> = mutableListOf())
data class Track(var name:String,var type:String,var source:String="")
data class Camera(var x:Float=0f,var y:Float=0f,var zoom:Float=1f,var rotation:Float=0f)
data class AnimationProject(var name:String="Untitled Animation",var width:Int=1280,var height:Int=720,var fps:Int=12,var currentFrame:Int=0,var onionSkin:Boolean=true,var grid:Boolean=false,var camera:Camera=Camera(),val layers:MutableList<Layer> = mutableListOf(),val tracks:MutableList<Track> = mutableListOf(),val effects:MutableSet<String> = mutableSetOf()){
 fun activeLayer():Layer?=layers.firstOrNull{it.visible}
 fun ensureFrame(i:Int):Frame{val l=activeLayer()?:error("No layer");return l.frames.firstOrNull{it.index==i}?:Frame(i).also{l.frames.add(it);l.frames.sortBy{f->f.index}}}
 fun toJson():String{val r=JSONObject().put("formatVersion",2).put("name",name).put("width",width).put("height",height).put("fps",fps).put("currentFrame",currentFrame).put("onionSkin",onionSkin).put("grid",grid).put("camera",JSONObject().put("x",camera.x).put("y",camera.y).put("zoom",camera.zoom).put("rotation",camera.rotation));val ls=JSONArray();layers.forEach{l->val lj=JSONObject().put("id",l.id).put("name",l.name).put("kind",l.kind).put("visible",l.visible).put("locked",l.locked).put("opacity",l.opacity);val fs=JSONArray();l.frames.forEach{f->val fj=JSONObject().put("index",f.index);val ss=JSONArray();f.strokes.forEach{s->val sj=JSONObject().put("color",s.color).put("width",s.width).put("tool",s.tool);val ps=JSONArray();s.points.forEach{p->ps.put(JSONObject().put("x",p.x).put("y",p.y))};sj.put("points",ps);ss.put(sj)};val vs=JSONArray();f.vectors.forEach{v->vs.put(JSONObject().put("type",v.type).put("x",v.x).put("y",v.y).put("w",v.w).put("h",v.h).put("color",v.color))};val ns=JSONArray();f.puppet.forEach{n->ns.put(JSONObject().put("id",n.id).put("x",n.x).put("y",n.y).put("parent",n.parent).put("radius",n.radius))};fj.put("strokes",ss).put("vectors",vs).put("puppet",ns);fs.put(fj)};lj.put("frames",fs);ls.put(lj)};r.put("layers",ls);val ts=JSONArray();tracks.forEach{t->ts.put(JSONObject().put("name",t.name).put("type",t.type).put("source",t.source))};r.put("tracks",ts);val es=JSONArray();effects.forEach{es.put(it)};return r.put("effects",es).toString()}
 companion object{fun newProject()=AnimationProject().apply{layers.add(Layer("layer-1","Sketch"));layers[0].frames.add(Frame(0));tracks.add(Track("Animation","animation"));tracks.add(Track("Camera","camera"))}}
}
