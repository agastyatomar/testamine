package com.agastyatomar.animatrix.model

import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

data class Stroke(val points: MutableList<Point> = mutableListOf(), val color: Int = Color.WHITE, val width: Float = 6f)
data class Point(val x: Float, val y: Float)
data class Frame(val index: Int, val strokes: MutableList<Stroke> = mutableListOf())
data class Layer(val id: String, var name: String, var visible: Boolean = true, var opacity: Float = 1f, val frames: MutableList<Frame> = mutableListOf())

data class AnimationProject(
    var name: String = "Untitled Animation",
    var width: Int = 1280,
    var height: Int = 720,
    var fps: Int = 12,
    var currentFrame: Int = 0,
    val layers: MutableList<Layer> = mutableListOf()
) {
    fun activeLayer(): Layer? = layers.firstOrNull()
    fun ensureFrame(index: Int): Frame {
        val layer = activeLayer() ?: error("No layer")
        return layer.frames.firstOrNull { it.index == index } ?: Frame(index).also { layer.frames.add(it); layer.frames.sortBy { it.index } }
    }
    fun toJson(): String {
        val root = JSONObject().put("formatVersion", 1).put("name", name).put("width", width).put("height", height).put("fps", fps).put("currentFrame", currentFrame)
        val layersJson = JSONArray()
        layers.forEach { layer ->
            val lj = JSONObject().put("id", layer.id).put("name", layer.name).put("visible", layer.visible).put("opacity", layer.opacity)
            val framesJson = JSONArray()
            layer.frames.forEach { frame ->
                val fj = JSONObject().put("index", frame.index)
                val strokesJson = JSONArray()
                frame.strokes.forEach { stroke ->
                    val sj = JSONObject().put("color", stroke.color).put("width", stroke.width)
                    val points = JSONArray()
                    stroke.points.forEach { p -> points.put(JSONObject().put("x", p.x).put("y", p.y)) }
                    sj.put("points", points)
                    strokesJson.put(sj)
                }
                fj.put("strokes", strokesJson)
                framesJson.put(fj)
            }
            lj.put("frames", framesJson)
            layersJson.put(lj)
        }
        root.put("layers", layersJson)
        return root.toString()
    }
    companion object {
        fun fromJson(text: String): AnimationProject {
            val root = JSONObject(text)
            val project = AnimationProject(
                name = root.optString("name", "Untitled Animation"),
                width = root.optInt("width", 1280),
                height = root.optInt("height", 720),
                fps = root.optInt("fps", 12),
                currentFrame = root.optInt("currentFrame", 0)
            )
            val layers = root.optJSONArray("layers") ?: JSONArray()
            for (i in 0 until layers.length()) {
                val lj = layers.getJSONObject(i)
                val layer = Layer(
                    lj.optString("id"),
                    lj.optString("name", "Layer " + (i + 1)),
                    lj.optBoolean("visible", true),
                    lj.optDouble("opacity", 1.0).toFloat()
                )
                val frames = lj.optJSONArray("frames") ?: JSONArray()
                for (j in 0 until frames.length()) {
                    val fj = frames.getJSONObject(j)
                    val frame = Frame(fj.optInt("index"))
                    val strokes = fj.optJSONArray("strokes") ?: JSONArray()
                    for (k in 0 until strokes.length()) {
                        val sj = strokes.getJSONObject(k)
                        val stroke = Stroke(color = sj.optInt("color", Color.WHITE), width = sj.optDouble("width", 6.0).toFloat())
                        val points = sj.optJSONArray("points") ?: JSONArray()
                        for (p in 0 until points.length()) {
                            val pj = points.getJSONObject(p)
                            stroke.points.add(Point(pj.optDouble("x").toFloat(), pj.optDouble("y").toFloat()))
                        }
                        frame.strokes.add(stroke)
                    }
                    layer.frames.add(frame)
                }
                project.layers.add(layer)
            }
            if (project.layers.isEmpty()) project.layers.add(Layer("layer-1", "Sketch"))
            return project
        }
        fun newProject(): AnimationProject =
            AnimationProject().apply {
                layers.add(Layer("layer-1", "Sketch"))
                layers[0].frames.add(Frame(0))
            }
    }
}
