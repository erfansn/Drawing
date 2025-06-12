package ir.erfansn.drawing

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

data class Element(
    val startOffset: Offset,
    val endOffset: Offset,
    val path: Path
)
