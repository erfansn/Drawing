package ir.erfansn.drawing

sealed interface Tool {
    data class Drawing(val type: ElementType) : Tool
    data object Selecting : Tool
}
