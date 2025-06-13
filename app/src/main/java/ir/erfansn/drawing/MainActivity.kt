package ir.erfansn.drawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import ir.erfansn.drawing.ui.theme.DrawingTheme
import kotlin.math.max
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingTheme {
                DrawingApp(
                    modifier = Modifier.safeDrawingPadding()
                )
            }
        }
    }
}

@Composable
fun DrawingApp(modifier: Modifier = Modifier) {
    var action by remember { mutableStateOf<Action>(Action.None) }
    val elements = remember { mutableStateListOf<Element>() }
    var tool by remember { mutableStateOf<Tool>(Tool.Selecting) }
    var selectedElement  by remember { mutableStateOf<Pair<Element, Offset>?>(null) }
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .safeDrawingPadding()
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val position = event.changes.first().position

                            when (event.type) {
                                PointerEventType.Press -> {
                                    when (val tool = tool) {
                                        is Tool.Drawing -> {
                                            val id = elements.size
                                            elements += createElement(id, position, position, tool.type)

                                            action = Action.Drawing
                                        }
                                        Tool.Selecting -> {
                                            getElementAtPosition(position, elements)?.let {
                                                selectedElement = it to (position - it.point1)

                                                action = Action.Moving
                                            }
                                        }
                                    }
                                }

                                PointerEventType.Move -> {
                                    when (action) {
                                        Action.Drawing -> {
                                            elements.updateElement(elements.lastIndex, point2 = position)
                                        }
                                        Action.Moving -> {
                                            selectedElement?.let { (element, offset) ->
                                                val newPosition = position - offset
                                                elements.updateElement(
                                                    id = element.id,
                                                    point1 = newPosition,
                                                    point2 = newPosition + (element.point2 - element.point1)
                                                )
                                            }
                                        }
                                        Action.None -> {}
                                    }
                                }

                                PointerEventType.Release -> {
                                    if (action == Action.Drawing) {
                                        val element = elements.last()
                                        val (point1, point2) = adjustElementCoordinates(element)
                                        elements.updateElement(element.id, point1, point2, element.type)
                                    }
                                    selectedElement = null
                                    action = Action.None
                                }
                            }
                        }
                    }
                }
        ) {
            for (element in elements) {
                drawPath(
                    path = element.path,
                    color = Color.Black,
                    style = Stroke()
                )
            }
        }

        Row(modifier = Modifier.selectableGroup()) {
            SelectableRow(
                selected = tool == Tool.Selecting,
                onClick = { tool = Tool.Selecting },
                text = "Selection"
            )
            SelectableRow(
                selected = tool == Tool.Drawing(ElementType.Line),
                onClick = { tool = Tool.Drawing(ElementType.Line) },
                text = "Line"
            )
            SelectableRow(
                selected = tool == Tool.Drawing(ElementType.Rectangle),
                onClick = { tool = Tool.Drawing(ElementType.Rectangle) },
                text = "Rectangle"
            )
        }
    }
}

// For lines, the coordinates are adjusted so that the starting point is always to the left or above the ending point.
// For rectangles, the coordinates are adjusted to ensure the top-left corner is always the starting point.
private fun adjustElementCoordinates(element: Element): Pair<Offset, Offset> {
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

@Composable
private fun SelectableRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(text)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DrawingTheme {
        DrawingApp()
    }
}