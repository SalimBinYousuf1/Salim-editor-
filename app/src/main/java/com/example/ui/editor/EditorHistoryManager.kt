package com.example.ui.editor

import androidx.compose.ui.text.input.TextFieldValue

class EditorHistoryManager(
    private val maxHistory: Int = 80
) {
    private val undoStack = mutableListOf<TextFieldValue>()
    private val redoStack = mutableListOf<TextFieldValue>()
    private var lastRecordedTime: Long = 0L

    val canUndo: Boolean
        get() = undoStack.isNotEmpty()

    val canRedo: Boolean
        get() = redoStack.isNotEmpty()

    fun record(currentValue: TextFieldValue, isAtomic: Boolean = false) {
        val now = System.currentTimeMillis()
        val lastValue = undoStack.lastOrNull()

        if (lastValue != null && lastValue.text == currentValue.text) {
            // Only cursor or selection moved; don't push another text state
            return
        }

        val isRapidTyping = !isAtomic && (now - lastRecordedTime < 600) &&
                lastValue != null &&
                Math.abs(lastValue.text.length - currentValue.text.length) == 1 &&
                !currentValue.text.endsWith("\n") &&
                !currentValue.text.endsWith(" ")

        if (isRapidTyping && undoStack.isNotEmpty()) {
            // Replace top of stack for smooth character-by-character typing grouping
            // but keep the start of the typing run intact
        } else {
            if (lastValue != null) {
                // Ensure the previous state is saved before this new divergence
                if (undoStack.size >= maxHistory) {
                    undoStack.removeAt(0)
                }
            }
            if (lastValue == null || lastValue.text != currentValue.text) {
                undoStack.add(currentValue)
            }
        }

        lastRecordedTime = now
        redoStack.clear()
    }

    fun pushInitial(initialValue: TextFieldValue) {
        undoStack.clear()
        redoStack.clear()
        undoStack.add(initialValue)
    }

    fun undo(currentValue: TextFieldValue): TextFieldValue? {
        if (!canUndo) return null
        // Put current onto redo stack
        redoStack.add(currentValue)
        val previous = undoStack.removeAt(undoStack.lastIndex)
        // If previous is identical to current, try one further back
        if (previous.text == currentValue.text && undoStack.isNotEmpty()) {
            return undoStack.removeAt(undoStack.lastIndex)
        }
        return previous
    }

    fun redo(currentValue: TextFieldValue): TextFieldValue? {
        if (!canRedo) return null
        undoStack.add(currentValue)
        return redoStack.removeAt(redoStack.lastIndex)
    }

    fun clear() {
        undoStack.clear()
        redoStack.clear()
    }
}
