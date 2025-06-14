package ir.erfansn.drawing

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
) = elements.firstNotNullOfOrNull { element ->
    positionWithinElement(offset, element)?.let { position -> element to position }
}

fun positionWithinElement(
    point: Offset,
    element: Element
): String? {
    val (x, y) = point
    val (x1, y1) = element.point1
    val (x2, y2) = element.point2
    when (element.type) {
        ElementType.Line -> {
            val lineLength = (element.point1 - element.point2).getDistance()
            val pointToStartLineDistance = (element.point1 - point).getDistance()
            val pointToEndLineDistance = (element.point2 - point).getDistance()
            val offset = lineLength - (pointToStartLineDistance + pointToEndLineDistance)

            val start = nearPoint(point, element.point1, "start")
            val end = nearPoint(point, element.point2, "end")
            val inside = if (abs(offset) <= 10) "inside" else null
            return start ?: end ?: inside
        }
        ElementType.Rectangle -> {
            val topLeft = nearPoint(point, Offset(x1, y1), "tl")
            val topRight = nearPoint(point, Offset(x2, y1), "tr")
            val bottomLeft = nearPoint(point, Offset(x1, y2), "bl")
            val bottomRight = nearPoint(point, Offset(x2, y2), "br")
            val inside = if (x in x1..x2 && y in y1..y2) "inside" else null
            return topLeft ?: topRight ?: bottomRight ?: bottomLeft ?: inside
        }
    }
}

fun nearPoint(
    userPoint: Offset,
    targetPoint: Offset,
    name: String
): String? {
    return if (abs(userPoint.x - targetPoint.x) <= 20 && abs(userPoint.y - targetPoint.y) <= 20) name else null
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

fun DrawingHistoryState.updateElement(
    id: Int,
    point1: Offset = currentElements[id].point1,
    point2: Offset = currentElements[id].point2,
    type: ElementType = currentElements[id].type
) {
    val updatedCurrentElements = currentElements.toMutableList().apply {
         this[id] = createElement(id, point1, point2, type)
    }
    saveState(updatedCurrentElements, overwrite = true)
}

fun resizeCoordinates(
    eventPosition: Offset,
    position: String,
    point1: Offset,
    point2: Offset
): Pair<Offset, Offset> {
    return when (position) {
        "tl", "start" -> {
            eventPosition to point2
        }
        "tr" -> {
            point1.copy(y = eventPosition.y) to point2.copy(x = eventPosition.x)
        }
        "bl" -> {
            point1.copy(x = eventPosition.x) to point2.copy(y = eventPosition.y)
        }
        "br", "end" -> {
            point1 to eventPosition
        }
        else -> error("Should not reach this")
    }
}

// For lines, the coordinates are adjusted so that the starting point is always to the left or above the ending point.
// For rectangles, the coordinates are adjusted to ensure the top-left corner is always the starting point.
fun adjustElementCoordinates(element: Element): Pair<Offset, Offset> {
    val (x1, y1) = element.point1
    val (x2, y2) = element.point2
    when (element.type) {
        ElementType.Line -> {
            if (x1 < x2 || (x1 == x2 && y1 < y2)) {
                return Offset(x1, y1) to Offset(x2, y2)
            } else {
                return Offset(x2, y2) to Offset(x1, y1)
            }
        }

        ElementType.Rectangle -> {
            val minX = min(x1, x2)
            val minY = min(y1, y2)
            val maxX = max(x1, x2)
            val maxY = max(y1, y2)
            return Offset(minX, minY) to Offset(maxX, maxY)
        }
    }
}
