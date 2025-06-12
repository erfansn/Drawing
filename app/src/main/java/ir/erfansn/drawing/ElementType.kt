package ir.erfansn.drawing

sealed interface ElementType {
    data object Line : ElementType
    data object Rectangle : ElementType
}
