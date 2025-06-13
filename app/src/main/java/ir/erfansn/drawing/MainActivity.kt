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
                DrawingApp()
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

                                PointerEventType.Release -> {
                                    selectedElement = null
                                    action = Action.None
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