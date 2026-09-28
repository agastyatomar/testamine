package com.agastyatomar.animatrix.ui

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import com.agastyatomar.animatrix.model.AnimationProject
import com.agastyatomar.animatrix.model.Point
import com.agastyatomar.animatrix.model.Stroke

class CanvasView(context: Context) : View(context) {
    var project: AnimationProject? = null
    var brushColor = Color.WHITE
    var brushWidth = 6f
    var onStrokeFinished: (() -> Unit)? = null
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private var activeStroke: Stroke? = null

    init { setBackgroundColor(Color.rgb(18, 21, 28)); isFocusable = true }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val p = project ?: return
        val layer = p.activeLayer() ?: return
        if (!layer.visible) return
        val frame = layer.frames.firstOrNull { it.index == p.currentFrame } ?: return
        paint.alpha = (layer.opacity * 255).toInt().coerceIn(0, 255)
        frame.strokes.forEach { stroke ->
            paint.color = stroke.color
            paint.strokeWidth = stroke.width
            drawStroke(canvas, stroke)
        }
        activeStroke?.let {
            paint.color = it.color
            paint.strokeWidth = it.width
            drawStroke(canvas, it)
        }
    }

    private fun drawStroke(canvas: Canvas, stroke: Stroke) {
        if (stroke.points.size == 1) {
            val pt = stroke.points[0]
            canvas.drawCircle(pt.x, pt.y, stroke.width / 2f, paint)
            return
        }
        val path = Path()
        stroke.points.firstOrNull()?.let { path.moveTo(it.x, it.y) }
        for (i in 1 until stroke.points.size) {
            val a = stroke.points[i - 1]
            val b = stroke.points[i]
            path.quadTo(a.x, a.y, (a.x + b.x) / 2f, (a.y + b.y) / 2f)
        }
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val p = project ?: return true
        val layer = p.activeLayer() ?: return true
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                p.ensureFrame(p.currentFrame)
                activeStroke = Stroke(mutableListOf(Point(event.x, event.y)), brushColor, brushWidth)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                activeStroke?.points?.add(Point(event.x, event.y))
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activeStroke?.let { layer.frames.first { it.index == p.currentFrame }.strokes.add(it) }
                activeStroke = null
                onStrokeFinished?.invoke()
                invalidate()
                return true
            }
        }
        return true
    }
}
