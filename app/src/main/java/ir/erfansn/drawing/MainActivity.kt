package ir.erfansn.drawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import ir.erfansn.drawing.ui.theme.DrawingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrawingTheme {
                DrawingApp(
                    modifier = Modifier.fillMaxSize()
                        .safeDrawingPadding()
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
    var drawingHistory = remember { DrawingHistoryState() }
    var tool by remember { mutableStateOf<Tool>(Tool.Selecting) }
    var selectedElement  by remember { mutableStateOf<SelectedElement?>(null) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var zoomOffset by remember { mutableStateOf(Offset.Zero) }
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    var offset = Offset.Zero
                    fun touchCoordinate(point: Offset): Offset {
                        return ((point - panOffset * zoom) + zoomOffset) / zoom
                    }

                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val eventPoint = touchCoordinate(event.changes.first().position)

                            when (event.type) {
                                PointerEventType.Press -> {
                                    when (val tool = tool) {
                                        is Tool.Drawing -> {
                                            val id = drawingHistory.currentElements.size
                                            drawingHistory.saveState(drawingHistory.currentElements + createElement(id, eventPoint, eventPoint, tool.type))
                                            selectedElement = SelectedElement(id, "")

                                            action = Action.Drawing
                                        }
                                        Tool.Selecting -> {
                                            getElementAtPosition(eventPoint, drawingHistory.currentElements)?.let { (element, position) ->
                                                offset = eventPoint - element.point1
                                                drawingHistory.saveState(drawingHistory.currentElements)
                                                selectedElement = SelectedElement(element.id, position)

                                                if (position == "inside") {
                                                    action = Action.Moving
                                                } else {
                                                    action = Action.Resizing
                                                }
                                            } ?: run {
                                                offset = eventPoint
                                                action = Action.Panning
                                            }
                                        }
                                    }
                                }

                                PointerEventType.Move -> {
                                    when (action) {
                                        Action.Drawing -> {
                                            drawingHistory.updateElement(drawingHistory.currentElements.lastIndex, point2 = eventPoint)
                                        }
                                        Action.Moving -> {
                                            selectedElement?.let { (id, _) ->
                                                val newPosition = eventPoint - offset
                                                val element = drawingHistory.currentElements[id]
                                                drawingHistory.updateElement(
                                                    id = id,
                                                    point1 = newPosition,
                                                    point2 = newPosition + (element.point2 - element.point1)
                                                )
                                            }
                                        }
                                        Action.Resizing -> {
                                            selectedElement?.let { (id, position) ->
                                                val element = drawingHistory.currentElements[id]
                                                val (point1, point2) = resizeCoordinates(eventPoint, position, element.point1, element.point2)
                                                drawingHistory.updateElement(
                                                    id = id,
                                                    point1 = point1,
                                                    point2 = point2
                                                )
                                            }
                                        }
                                        Action.Panning -> {
                                            val deltaPoint = eventPoint - offset
                                            panOffset += deltaPoint
                                        }
                                        Action.None -> {}
                                    }
                                }

                                PointerEventType.Release -> {
                                    if (action == Action.Drawing || action == Action.Resizing) {
                                        val element = drawingHistory.currentElements[selectedElement!!.id]
                                        val (point1, point2) = adjustElementCoordinates(element)
                                        drawingHistory.updateElement(element.id, point1, point2)
                                    }
                                    selectedElement = null
                                    action = Action.None
                                }
                            }
                        }
                    }
                }
        ) {
            val (scaledWidth, scaledHeight) = size * zoom
            zoomOffset = (Offset(scaledWidth, scaledHeight) - Offset(size.width, size.height)) / 2f

            translate(panOffset.x * zoom - zoomOffset.x, panOffset.y * zoom - zoomOffset.y) {
                scale(zoom, pivot = Offset.Zero) {
                    for (element in drawingHistory.currentElements) {
                        drawPath(
                            path = element.path,
                            color = Color.Black,
                            style = Stroke()
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .selectableGroup()
                .padding(8.dp)
        ) {
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
        Row(
            Modifier.align(Alignment.BottomStart).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column {
                Button(onClick = drawingHistory::undo) {
                    Text("Undo")
                }
                Button(onClick = drawingHistory::redo) {
                    Text("Redo")
                }
            }
            Column {
                Button(onClick = {
                    if (zoom < 3.0f) {
                        zoom += 0.1f
                    }
                }) {
                    Text("Zoom In")
                }
                Button(onClick = {
                    if (zoom > 0.1f) {
                        zoom -= 0.1f
                    }
                }) {
                    Text("Zoom Out")
                }
            }
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