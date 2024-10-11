package data

import processing.core.PVector

class Body(
    var pos: PVector,
    var vel: PVector = PVector(0f, 0f),
    var mass: Float = 1f,
)