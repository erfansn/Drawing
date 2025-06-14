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

data class SelectedElement(
    val id: Int,
    val position: String
)

@Composable
fun DrawingApp(modifier: Modifier = Modifier) {
    var action by remember { mutableStateOf<Action>(Action.None) }
    val elements = remember { mutableStateListOf<Element>() }
    var tool by remember { mutableStateOf<Tool>(Tool.Selecting) }
    var selectedElement  by remember { mutableStateOf<SelectedElement?>(null) }
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .safeDrawingPadding()
                .fillMaxSize()
                .pointerInput(Unit) {
                    var offset = Offset.Zero

                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val eventPoint = event.changes.first().position

                            when (event.type) {
                                PointerEventType.Press -> {
                                    when (val tool = tool) {
                                        is Tool.Drawing -> {
                                            val id = elements.size
                                            elements += createElement(id, eventPoint, eventPoint, tool.type)
                                            selectedElement = SelectedElement(id, "inside")

                                            action = Action.Drawing
                                        }
                                        Tool.Selecting -> {
                                            getElementAtPosition(eventPoint, elements)?.let { (element, position) ->
                                                offset = eventPoint - element.point1
                                                selectedElement = SelectedElement(element.id, position )

                                                if (position == "inside") {
                                                    action = Action.Moving
                                                } else {
                                                    action = Action.Resizing
                                                }
                                            }
                                        }
                                    }
                                }

                                PointerEventType.Move -> {
                                    when (action) {
                                        Action.Drawing -> {
                                            elements.updateElement(elements.lastIndex, point2 = eventPoint)
                                        }
                                        Action.Moving -> {
                                            selectedElement?.let { (id, _) ->
                                                val newPosition = eventPoint - offset
                                                val element = elements[id]
                                                elements.updateElement(
                                                    id = id,
                                                    point1 = newPosition,
                                                    point2 = newPosition + (element.point2 - element.point1)
                                                )
                                            }
                                        }
                                        Action.Resizing -> {
                                            selectedElement?.let { (id, position) ->
                                                val element = elements[id]
                                                val (point1, point2) = resizeCoordinates(eventPoint, position, element.point1, element.point2)
                                                elements.updateElement(
                                                    id = id,
                                                    point1 = point1,
                                                    point2 = point2
                                                )
                                            }
                                        }
                                        Action.None -> {}
                                    }
                                }

                                PointerEventType.Release -> {
                                    if (action == Action.Drawing || action == Action.Resizing) {
                                        val element = elements[selectedElement!!.id]
                                        val (point1, point2) = adjustElementCoordinates(element)
                                        elements.updateElement(element.id, point1, point2)
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