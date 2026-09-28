package com.agastyatomar.animatrix.core

import android.content.Context
import com.agastyatomar.animatrix.model.AnimationProject
import java.io.File

class ProjectStore(private val context: Context) {
    private val file: File get() = File(context.filesDir, "autosave.animatrix")
    fun save(project: AnimationProject) {
        val tmp = File(context.filesDir, "autosave.animatrix.tmp")
        tmp.writeText(project.toJson())
        if (!tmp.renameTo(file)) { file.writeText(project.toJson()); tmp.delete() }
    }
    fun load(): AnimationProject? =
        if (file.exists()) runCatching { AnimationProject.fromJson(file.readText()) }.getOrNull() else null
}
