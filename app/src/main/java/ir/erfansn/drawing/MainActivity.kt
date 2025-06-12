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
    var drawing by remember { mutableStateOf(false) }
    val elements = remember { mutableStateListOf<Element>() }
    var selectedElementType by remember { mutableStateOf<ElementType>(ElementType.Line) }
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
                                    drawing = true

                                    val element = createElement(position, position, selectedElementType)
                                    elements += element
                                }

                                PointerEventType.Release -> {
                                    drawing = false
                                }

                                PointerEventType.Move -> {
                                    if (!drawing) return@awaitPointerEventScope

                                    val element = elements.last()
                                    val updatedElement = createElement(element.startOffset, position, selectedElementType)
                                    elements[elements.lastIndex] = updatedElement
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
                selected = selectedElementType == ElementType.Line,
                onClick = { selectedElementType = ElementType.Line },
                text = "Line"
            )
            SelectableRow(
                selected = selectedElementType == ElementType.Rectangle,
                onClick = { selectedElementType = ElementType.Rectangle },
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

private fun createElement(
    startOffset: Offset,
    endOffset: Offset,
    type: ElementType
) = Element(
    startOffset = startOffset,
    endOffset = endOffset,
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
    }
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DrawingTheme {
        DrawingApp()
    }
}