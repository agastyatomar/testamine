package com.agastyatomar.animatrix

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import com.agastyatomar.animatrix.core.History
import com.agastyatomar.animatrix.core.ProjectStore
import com.agastyatomar.animatrix.model.AnimationProject
import com.agastyatomar.animatrix.ui.CanvasView

class MainActivity : Activity() {
    private lateinit var project: AnimationProject
    private lateinit var canvas: CanvasView
    private lateinit var frameLabel: TextView
    private lateinit var store: ProjectStore
    private val history = History()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = ProjectStore(this)
        project = store.load() ?: AnimationProject.newProject()
        buildUi()
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(9, 11, 16))
        }
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(12, 8, 12, 8)
        }
        fun button(label: String, action: () -> Unit): Button =
            Button(this).apply { text = label; setOnClickListener { action() }; minWidth = 0 }

        val title = TextView(this).apply {
            text = "ANIMATRIX  •  " + project.name
            textSize = 18f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 0, 18, 0)
        }
        top.addView(title, LinearLayout.LayoutParams(0, 60, 1f))
        top.addView(button("UNDO") { undo() }, LinearLayout.LayoutParams(95, 60))
        top.addView(button("REDO") { redo() }, LinearLayout.LayoutParams(95, 60))
        top.addView(button("SAVE") { save() }, LinearLayout.LayoutParams(95, 60))
        top.addView(button("NEW FRAME") { newFrame() }, LinearLayout.LayoutParams(125, 60))
        root.addView(top)

        val editor = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        canvas = CanvasView(this).apply {
            project = this@MainActivity.project
            brushColor = Color.WHITE
            brushWidth = 7f
            onStrokeFinished = {
                history.push(this@MainActivity.project.toJson())
                store.save(this@MainActivity.project)
            }
        }
        editor.addView(canvas, LinearLayout.LayoutParams(0, 0, 1f))

        val side = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 10, 10, 10)
            setBackgroundColor(Color.rgb(14, 17, 23))
        }
        side.addView(TextView(this).apply {
            text = "LAYERS"
            textSize = 14f
            setTextColor(Color.LTGRAY)
            setPadding(4, 4, 4, 12)
        })
        project.layers.forEachIndexed { index, layer ->
            side.addView(button(if (index == 0) "● " + layer.name else "○ " + layer.name) {
                Toast.makeText(this, "Layer: " + layer.name, Toast.LENGTH_SHORT).show()
            })
        }
        side.addView(Space(this), LinearLayout.LayoutParams(1, 0, 1f))
        side.addView(button("PENCIL  •  7px") {})
        side.addView(button("BLACK") { canvas.brushColor = Color.BLACK })
        side.addView(button("WHITE") { canvas.brushColor = Color.WHITE })
        editor.addView(side, LinearLayout.LayoutParams(250, -1))
        root.addView(editor, LinearLayout.LayoutParams(-1, 0, 1f))

        val timeline = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(10, 8, 10, 8)
            setBackgroundColor(Color.rgb(13, 15, 20))
        }
        frameLabel = TextView(this).apply { setTextColor(Color.WHITE); textSize = 14f }
        timeline.addView(frameLabel)
        val row = LinearLayout(this)
        for (i in 0 until 24) {
            val b = TextView(this).apply {
                text = if (project.activeLayer()?.frames?.any { it.index == i } == true) "●" else "□"
                gravity = Gravity.CENTER
                textSize = 18f
                setTextColor(Color.WHITE)
                setOnClickListener { selectFrame(i) }
            }
            row.addView(b, LinearLayout.LayoutParams(42, 44))
        }
        timeline.addView(row)
        root.addView(timeline, LinearLayout.LayoutParams(-1, 115))
        updateFrameLabel()
        setContentView(root)
    }

    private fun selectFrame(index: Int) {
        history.push(project.toJson())
        project.currentFrame = index
        project.ensureFrame(index)
        canvas.invalidate()
        updateFrameLabel()
    }

    private fun newFrame() {
        history.push(project.toJson())
        project.currentFrame++
        project.ensureFrame(project.currentFrame)
        canvas.invalidate()
        updateFrameLabel()
        store.save(project)
    }

    private fun updateFrameLabel() {
        frameLabel.text = "TIMELINE • Frame " + (project.currentFrame + 1) + " • " + project.fps + " FPS • " + project.layers.size + " layer(s)"
    }

    private fun save() {
        store.save(project)
        Toast.makeText(this, "Project saved offline", Toast.LENGTH_SHORT).show()
    }

    private fun undo() {
        history.undo(project.toJson())?.let {
            project = AnimationProject.fromJson(it)
            canvas.project = project
            canvas.invalidate()
            updateFrameLabel()
        }
    }

    private fun redo() {
        history.redo(project.toJson())?.let {
            project = AnimationProject.fromJson(it)
            canvas.project = project
            canvas.invalidate()
            updateFrameLabel()
        }
    }

    override fun onPause() {
        store.save(project)
        super.onPause()
    }
}
