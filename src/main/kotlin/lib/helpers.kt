package lib

import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.bounds
import kotlin.math.atan2

fun ShapeContour.points(): List<Vector2> {
    return segments.map { it.start } + segments.last().end
}

fun List<Vector2>.sortedClockwise(): List<Vector2> {
    return sortedBy { atan2(it.y - bounds.center.y, it.x - bounds.center.x) }
}
