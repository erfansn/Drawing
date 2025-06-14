package ir.erfansn.drawing

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import kotlin.collections.plusAssign

class DrawingHistoryState {
    private val history = mutableStateListOf<List<Element>>(emptyList())
    private var current by mutableIntStateOf(0)

    val currentElements: List<Element>
        get() = history[current]

    fun saveState(elements: List<Element>, overwrite: Boolean = false) {
        if (overwrite) {
            history[current] = elements
        } else {
            history.subList(current + 1, history.size).clear()
            history += elements
            current++
        }
    }

    fun undo() {
        if (current > 0) {
            current--
        }
    }

    fun redo() {
        if (current < history.lastIndex) {
            current++
        }
    }
}
