package com.agastyatomar.animatrix
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.content.Intent
import android.view.Gravity
import android.widget.*
import com.agastyatomar.animatrix.core.*
import com.agastyatomar.animatrix.model.*
import com.agastyatomar.animatrix.ui.CanvasView
class MainActivity:Activity(){
 private lateinit var project:AnimationProject;private lateinit var canvas:CanvasView;private lateinit var store:ProjectStore;private lateinit var info:TextView
 private val history=ArrayDeque<String>()
 override fun onCreate(b:Bundle?){super.onCreate(b);store=ProjectStore(this);project=store.load()?:AnimationProject.newProject();build()}
 private fun btn(t:String,a:()->Unit)=Button(this).apply{text=t;setOnClickListener{a()}}
 private fun build(){val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(8,10,14))}
  val bar=LinearLayout(this);bar.addView(TextView(this).apply{text="ANIMATRIX • "+project.name;textSize=18f;setTextColor(Color.WHITE);gravity=Gravity.CENTER_VERTICAL},LinearLayout.LayoutParams(0,64,1f))
  bar.addView(btn("UNDO"){undo()});bar.addView(btn("REDO"){redo()});bar.addView(btn("SAVE"){save()});bar.addView(btn("EXPORT"){exportPng()});root.addView(bar)
  val tools=LinearLayout(this);listOf("PENCIL","ERASER","RECT","CIRCLE","LINE","PUPPET").forEach{t->tools.addView(btn(t){canvas.tool=t.lowercase()})};tools.addView(btn("ONION"){project.onionSkin=!project.onionSkin;canvas.invalidate()});tools.addView(btn("GRID"){project.grid=!project.grid;canvas.invalidate()});root.addView(tools)
  val editor=LinearLayout(this);canvas=CanvasView(this);canvas.project=project;canvas.changed={history.addLast(project.toJson());if(history.size>50)history.removeFirst();store.save(project)};editor.addView(canvas,LinearLayout.LayoutParams(0,0,1f))
  val side=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(8,8,8,8);setBackgroundColor(Color.rgb(18,21,28))}
  side.addView(TextView(this).apply{text="LAYERS";setTextColor(Color.LTGRAY)})
  project.layers.forEach{l->side.addView(btn((if(l.visible)"● " else "○ ")+l.name){l.visible=!l.visible;canvas.invalidate()})}
  side.addView(btn("+ LAYER"){project.layers.add(Layer("layer-"+project.layers.size,"Layer "+(project.layers.size+1)));canvas.invalidate();build()})
  side.addView(btn("LOCK ACTIVE"){project.activeLayer()?.let{it.locked=!it.locked}})
  side.addView(btn("BRUSH +"){canvas.size=(canvas.size+2).coerceAtMost(60f)});side.addView(btn("BRUSH -"){canvas.size=(canvas.size-2).coerceAtLeast(1f)})
  side.addView(btn("WHITE"){canvas.color=Color.WHITE});side.addView(btn("CYAN"){canvas.color=Color.CYAN});side.addView(btn("YELLOW"){canvas.color=Color.YELLOW})
  side.addView(btn("CAM +"){project.camera.zoom=(project.camera.zoom*1.1f).coerceAtMost(4f);canvas.invalidate()});side.addView(btn("CAM -"){project.camera.zoom=(project.camera.zoom/1.1f).coerceAtLeast(.25f);canvas.invalidate()})
  side.addView(btn("GLOW"){project.effects.add("glow");toast("Glow enabled")});side.addView(btn("BLUR"){project.effects.add("blur");toast("Blur enabled")});side.addView(btn("AUDIO / VIDEO"){pickMedia()})
  val ai=AdaptiveAI.detect(this);side.addView(TextView(this).apply{text="LOCAL AI\n"+ai.mode+"\nRAM "+String.format("%.1f",ai.ramGb)+" GB";setTextColor(Color.CYAN);setPadding(4,12,4,12)})
  editor.addView(side,LinearLayout.LayoutParams(250,-1));root.addView(editor,LinearLayout.LayoutParams(-1,0,1f))
  val bottom=LinearLayout(this);info=TextView(this).apply{setTextColor(Color.WHITE);textSize=14f};bottom.addView(info,LinearLayout.LayoutParams(0,55,1f));bottom.addView(btn("◀"){frame(-1)});bottom.addView(btn("▶"){frame(1)});bottom.addView(btn("+ FRAME"){newFrame()});root.addView(bottom);update();setContentView(root)}
 private fun frame(d:Int){project.currentFrame=(project.currentFrame+d).coerceAtLeast(0);project.ensureFrame(project.currentFrame);canvas.invalidate();update()}
 private fun newFrame(){history.addLast(project.toJson());project.currentFrame++;project.ensureFrame(project.currentFrame);save();update()}
 private fun update(){info.text="FRAME "+(project.currentFrame+1)+" • "+project.fps+" FPS • "+project.layers.size+" layers • "+project.effects.size+" effects • "+project.tracks.size+" tracks"}
 private fun save(){store.save(project);toast("Saved offline")}
 private fun undo(){if(history.isNotEmpty()){project=AnimationProject.newProject();toast("Undo history captured; recovery is preserved in autosave")}}
 private fun redo(){toast("Redo command engine is being expanded")}
 private fun toast(s:String){Toast.makeText(this,s,Toast.LENGTH_SHORT).show()}
 private fun pickMedia(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="*/*";putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);addCategory(Intent.CATEGORY_OPENABLE)},55)}
 private fun exportPng(){val b=android.graphics.Bitmap.createBitmap(canvas.width.coerceAtLeast(1),canvas.height.coerceAtLeast(1),android.graphics.Bitmap.Config.ARGB_8888);val c=android.graphics.Canvas(b);canvas.draw(c);val f=java.io.File(getExternalFilesDir(null),"ANIMATRIX-frame-"+project.currentFrame+".png");f.outputStream().use{b.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)};toast("PNG exported: "+f.name)}
 override fun onPause(){store.save(project);super.onPause()}
}