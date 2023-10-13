import lib.UIElementImpl
import lib.UIManager
import lib.points
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.ORANGE
import org.openrndr.extra.color.presets.PURPLE
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import kotlin.math.atan2
import kotlin.random.Random

fun main() = application {

    configure {
        width = 800
        height = 800
    }

    oliveProgram {



        val uiManager = UIManager(program.window, mouse)
        val c = object: UIElementImpl() {

            var contour = Circle(drawer.bounds.center, 200.0).contour.sub(0.0, 0.25)
                set(value) {
                    field = value

                    contourPoints = value.points()
                    controlPoints = value.segments.map { it.control.toList() }.flatten()
                    actionablePoints = contourPoints + controlPoints

                    actionBounds = ShapeContour.fromPoints(actionablePoints, true).offset(proximityThreshold * 2)
                }

            var contourPoints = contour.points()
            var controlPoints = contour.segments.map { it.control.toList() }.flatten()
            var actionablePoints = (contourPoints + controlPoints).sortedClockwise()
                set(value) {
                    field = value.sortedClockwise()
                }


            val proximityThreshold = 9.0

            fun isInRange(pos: Vector2, mousePos: Vector2): Boolean {
                return pos.distanceTo(mousePos) < proximityThreshold
            }

            fun List<Vector2>.sortedClockwise(): List<Vector2> {
                return sortedBy { atan2(it.y - bounds.center.y, it.x - bounds.center.x) }
            }


            var activePointIdx = -1
            var activeControlPointIdx = -1

            private fun movePoint(mousePosition: Vector2) {
                val segmentRef = contour.segments[0]
                val activePoint = contourPoints.getOrNull(activePointIdx)

                if (activePoint != null) {
                    contour = if (activePoint == segmentRef.start) {
                        Segment(mousePosition, segmentRef.control[0], segmentRef.control[1], segmentRef.end).contour
                    } else {
                        Segment(segmentRef.start, segmentRef.control[0], segmentRef.control[1], mousePosition).contour
                    }
                }
            }


            private fun moveControlPoint(mousePosition: Vector2) {
                val segmentRef = contour.segments[0]
                val activePoint = controlPoints.getOrNull(activeControlPointIdx)

                if (activePoint != null) {
                    contour = if (activePoint == segmentRef.control[0]) {
                        Segment(segmentRef.start, mousePosition, segmentRef.control[1], segmentRef.end).contour
                    } else {
                        Segment(segmentRef.start, segmentRef.control[0], mousePosition,  segmentRef.end).contour
                    }
                }
            }

            init {

                actionBounds = ShapeContour.fromPoints(actionablePoints, true).offset(proximityThreshold * 2)

                buttonDown.listen {
                    it.cancelPropagation()
                    val activePoint = actionablePoints.firstOrNull { ap -> isInRange(ap, it.position) }

                    if (activePoint in contourPoints) {
                        val idx = contourPoints.indexOf(contourPoints.minBy { p -> p.distanceTo(it.position) })
                        activePointIdx = idx
                    } else if (activePoint in controlPoints) {
                        val idx = controlPoints.indexOf(controlPoints.minBy { p -> p.distanceTo(it.position) })
                        activeControlPointIdx = idx
                    }
                }

                buttonUp.listen {
                    it.cancelPropagation()
                    activePointIdx = -1
                    activeControlPointIdx = -1
                }

                dragged.listen {
                    it.cancelPropagation()

                    if (activePointIdx != -1) {
                        movePoint(it.position)
                    }

                    if (activeControlPointIdx != -1) {
                        moveControlPoint(it.position)
                    }
                }
            }

            fun draw() {
                drawer.fill = null
                drawer.stroke = ColorRGBa.WHITE
                drawer.contour(contour)

                drawer.stroke = ColorRGBa.PINK.shade(0.4)
                drawer.contour(actionBounds)

                for ((i, segment) in contour.segments.withIndex()) {
                    drawer.strokeWeight = 1.0
                    drawer.stroke = ColorRGBa.GREEN.toHSLa().shiftHue(Double.uniform(0.0, 360.0, Random(i))).toRGBa()
                    drawer.segment(segment)

                    drawer.fill = ColorRGBa.WHITE.opacify(0.4)
                    drawer.stroke = null
                    drawer.circles(segment.control.toList(), 4.0)

                    drawer.stroke = ColorRGBa.WHITE.opacify(0.4)
                    drawer.strokeWeight = 0.5
                    segment.control.getOrNull(0)?.let { drawer.lineSegment(segment.start, it) }
                    segment.control.getOrNull(1)?.let { drawer.lineSegment(it, segment.end) }

                }

                drawer.fill = ColorRGBa.PURPLE.mix(ColorRGBa.PINK, 0.5)
                drawer.circles(contour.points(), 6.0)

                if (activePointIdx != -1) {
                    drawer.fill = ColorRGBa.ORANGE
                    drawer.circle(contour.points()[activePointIdx], 9.0)
                }

                for (actionablePoint in actionablePoints) {
                    drawer.fill = null
                    drawer.stroke = ColorRGBa.WHITE
                    drawer.circle(actionablePoint, 9.0 + proximityThreshold)
                }


            }

        }

        uiManager.elements.add(c)

        extend {
            c.draw()
        }
    }

}
