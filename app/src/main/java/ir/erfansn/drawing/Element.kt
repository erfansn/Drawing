package ir.erfansn.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

data class Element(
    val startOffset: Offset,
    val endOffset: Offset,
    val path: Path,
    val type: ElementType,
)

fun getElementAtPosition(
    offset: Offset,
    elements: List<Element>
) = elements.find {  element ->
    isWithinElement(offset, element)
}

fun isWithinElement(
    offset: Offset,
    element: Element
): Boolean {
    when (element.type) {
        ElementType.Line -> {
            val lineLength = hypot(element.endOffset.x - element.startOffset.x, element.endOffset.y - element.startOffset.y)
            val pointToEndLineDistance = hypot(element.endOffset.x - offset.x, element.endOffset.y - offset.y)
            val pointToStartLineDistance = hypot(element.startOffset.x - offset.x, element.startOffset.y - offset.x)
            val offset = lineLength - (pointToStartLineDistance + pointToEndLineDistance)
            return abs(offset) <= 1
        }
        ElementType.Rectangle -> {
            val minX = min(element.startOffset.x, element.endOffset.x)
            val minY = min(element.startOffset.y, element.endOffset.y)
            val maxX = max(element.startOffset.x, element.endOffset.x)
            val maxY = max(element.startOffset.y, element.endOffset.y)
            return offset.x in minX..maxX && offset.y in minY..maxY
        }
    }
}

fun createElement(
    startOffset: Offset,
    endOffset: Offset,
    type: Tool
) = when (type) {
    Tool.LineDrawing -> {
        Element(
            startOffset = startOffset,
            endOffset = endOffset,
            path = Path().apply {
                moveTo(startOffset.x, startOffset.y)
                lineTo(endOffset.x, endOffset.y)
            },
            type = ElementType.Line
        )
    }
    Tool.RectangleDrawing -> {
        Element(
            startOffset = startOffset,
            endOffset = endOffset,
            path = Path().apply {
                addRect(rect = Rect(startOffset, endOffset))
            },
            type = ElementType.Rectangle
        )
    }
    else -> error("Unknown drawing tool")
}
