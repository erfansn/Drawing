package ir.erfansn.drawing

sealed class Action {
    data object None : Action()
    data object Drawing : Action()
    data object Moving : Action()
}