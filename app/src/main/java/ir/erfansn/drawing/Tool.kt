package ir.erfansn.drawing

sealed interface Tool {
    data object LineDrawing : Tool
    data object RectangleDrawing : Tool
    data object Selecting : Tool
}
