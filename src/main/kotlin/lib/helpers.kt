package lib

import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.bounds
import kotlin.math.atan2

fun ShapeContour.points(): List<Vector2> {
    return segments.map { it.start } + segments.last().end
}

fun List<Vector2>.sortedClockwise(): List<Vector2> {
    return sortedBy { atan2(it.y - bounds.center.y, it.x - bounds.center.x) }
}

fun Rectangle.uv(pos: Vector2): Vector2 {
    return pos.map(corner, corner + dimensions, Vector2.ZERO, Vector2.ONE, true)
}