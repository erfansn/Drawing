package ir.erfansn.drawing

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class Element(
    val id: Int,
    val point1: Offset,
    val point2: Offset,
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
    point: Offset,
    element: Element
): Boolean {
    when (element.type) {
        ElementType.Line -> {
            val lineLength = (element.point1 - element.point2).getDistance()
            val pointToStartLineDistance = (element.point1 - point).getDistance()
            val pointToEndLineDistance = (element.point2 - point).getDistance()
            val offset = lineLength - (pointToStartLineDistance + pointToEndLineDistance)
            return abs(offset) <= 10
        }
        ElementType.Rectangle -> {
            val minX = min(element.point1.x, element.point2.x)
            val minY = min(element.point1.y, element.point2.y)
            val maxX = max(element.point1.x, element.point2.x)
            val maxY = max(element.point1.y, element.point2.y)
            return point.x in minX..maxX && point.y in minY..maxY
        }
    }
}

fun createElement(
    id: Int,
    startOffset: Offset,
    endOffset: Offset,
    type: ElementType
) = Element(
    id = id,
    point1 = startOffset,
    point2 = endOffset,
    path = when (type) {
        ElementType.Line -> {
            Path().apply {
                moveTo(startOffset.x, startOffset.y)
                lineTo(endOffset.x, endOffset.y)
            }
        }

        ElementType.Rectangle -> {
            Path().apply {
                addRect(rect = Rect(startOffset, endOffset))
            }
        }
    },
    type = type
)

fun SnapshotStateList<Element>.updateElement(
    id: Int,
    point1: Offset = this[id].point1,
    point2: Offset = this[id].point2,
    type: ElementType = this[id].type
) {
    val elementIndex = id
    val updatedElement = createElement(elementIndex, point1, point2, type)
    this[elementIndex] = updatedElement
}
