package ir.erfansn.drawing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
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
                DrawingApp()
            }
        }
    }
}

@Composable
fun DrawingApp(modifier: Modifier = Modifier) {
    var action by remember { mutableStateOf<Action>(Action.None) }
    val elements = remember { mutableStateListOf<Element>() }
    var tool by remember { mutableStateOf<Tool>(Tool.LineDrawing) }
    var selectedElement  by remember { mutableStateOf<Element?>(null) }
    Box {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            val position = event.changes.first().position

                            when (event.type) {
                                PointerEventType.Press -> {
                                    if (tool == Tool.Selecting) {
                                        getElementAtPosition(position, elements)?.let {
                                            selectedElement = it
                                            action = Action.Moving
                                        }
                                    } else {
                                        val element = createElement(position, position, tool)
                                        elements += element

                                        action = Action.Drawing
                                    }
                                }

                                PointerEventType.Release -> {
                                    selectedElement = null
                                    action = Action.None
                                }

                                PointerEventType.Move -> {
                                    if (action == Action.Drawing) {
                                        val element = elements.last()
                                        val updatedElement =
                                            createElement(element.startOffset, position, tool)
                                        elements[elements.lastIndex] = updatedElement
                                    }
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
                selected = tool == Tool.LineDrawing,
                onClick = { tool = Tool.LineDrawing },
                text = "Line"
            )
            SelectableRow(
                selected = tool == Tool.RectangleDrawing,
                onClick = { tool = Tool.RectangleDrawing },
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