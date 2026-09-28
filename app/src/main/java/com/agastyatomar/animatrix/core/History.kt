package com.agastyatomar.animatrix.core

class History(private val limit: Int = 80) {
    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()
    fun push(snapshot: String) { undoStack.addLast(snapshot); while (undoStack.size > limit) undoStack.removeFirst(); redoStack.clear() }
    fun undo(current: String): String? { if (undoStack.isEmpty()) return null; redoStack.addLast(current); return undoStack.removeLast() }
    fun redo(current: String): String? { if (redoStack.isEmpty()) return null; undoStack.addLast(current); return redoStack.removeLast() }
    fun clear() { undoStack.clear(); redoStack.clear() }
}
