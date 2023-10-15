import lib.UIElementImpl
import lib.UIManager
import lib.points
import org.openrndr.application
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.ORANGE
import org.openrndr.extra.color.presets.PURPLE
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.shapes.alphaShape
import org.openrndr.math.Vector2
import org.openrndr.shape.*
import kotlin.random.Random

fun main() = application {

    configure {
        width = 800
        height = 800
    }

    oliveProgram {

        val uiManager = UIManager(program)
        val c = object: UIElementImpl() {

            var contour = Circle(drawer.bounds.center, 200.0).contour.sub(0.0, 0.25)
                set(value) {
                    field = value
                    actionBounds = contour.bounds.offsetEdges(25.0).contour
                }

            var activePointIdx = -1
            init {
                actionBounds = contour.bounds.offsetEdges(25.0).contour

                buttonDown.listen {
                    it.cancelPropagation()
                }

                buttonUp.listen {
                    it.cancelPropagation()
                    activePointIdx = -1
                }

                dragged.listen {
                    it.cancelPropagation()

                    if(activePointIdx == -1) {
                        val points = contour.points()
                        val idx = points.indexOf(points.minBy { p -> p.distanceTo(it.position) })
                        activePointIdx = idx
                    } else {
                        val segmentRef = contour.segments[0]
                        val extrema = listOf(segmentRef.start, segmentRef.end)
                        println(extrema.size)
                        val activePoint = extrema.getOrNull(activePointIdx)

                        activePoint?.let { ap ->
                            contour = if (ap == segmentRef.start) {
                                Segment(it.position, segmentRef.control[0], segmentRef.control[1], segmentRef.end).contour
                            } else {
                                Segment(segmentRef.start, segmentRef.control[0], segmentRef.control[1], it.position).contour
                            }
                        }

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


            }

        }

        uiManager.elements.add(c)

        extend {

            c.draw()
        }
    }

}