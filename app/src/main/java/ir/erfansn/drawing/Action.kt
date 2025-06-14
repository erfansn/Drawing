package ir.erfansn.drawing

sealed class Action {
    data object None : Action()
    data object Drawing : Action()
    data object Moving : Action()
    data object Resizing : Action()
    data object Panning : Action()
}