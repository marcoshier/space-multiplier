import lib.UIElementImpl
import lib.UIManager
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.hobbyCurve
import org.openrndr.math.Vector2
import org.openrndr.shape.*

fun main() = application {
    configure {
        width = 1400
        height = 1400
    }
    program {

        val mapper = Mapper(program)

        mapper.elements = listOf(
            MapperElement(
                hobbyCurve((0..3).map { Random.point(drawer.bounds.offsetEdges(-200.0)) }, true)
            )
        )

        extend {
            mapper.draw()
        }
    }
}

class Mapper(val program: Program) {

    val uiManager = UIManager(program.window, program.mouse)

    var elements = listOf<MapperElement>()
        set(value) {
            field = value
            uiManager.elements.clear()
            uiManager.elements.addAll(value)
        }

    fun draw() {
            elements.forEach {
                program.drawer.isolated {
                    it.draw(this)
                }
            }
        }

}

class MapperElement(var contour: ShapeContour): UIElementImpl() {

    private val proximityThreshold = 9.0

    var contourPoints = contour.segments.map { it.start }

    private fun checkProximity(pos: Vector2): Boolean {

        if (contourPoints.any {
                pos.distanceTo(it) < proximityThreshold
            }) return false

        val shapeProximity = compound {
            difference {
                shape(contour.offset(proximityThreshold).shape)
                shape(contour.offset(-proximityThreshold).shape)
            }
        }.compound

        return pos in shapeProximity
    }

    fun addPoint(pos: Vector2) {
        val nearest = contour.nearest(pos)
        val nearestSegment = nearest.segment
        val nearestSegmentT = nearest.segmentT

        val split = nearestSegment.split(nearestSegmentT)
        val segments = contour.segments.toMutableList()

        val segmentIndex = segments.indexOf(nearestSegment)
        segments[segmentIndex] = split[0]
        segments.add(segmentIndex + 1, split[1])

        val newContour = contour {
            for (segment in segments) {
                segment(segment)
            }
        }.close()

        contour = newContour
        contourPoints = newContour.segments.map { it.start }
        actionBounds = newContour
    }

    init {
        actionBounds = contour

        buttonDown.listen {
            it.cancelPropagation()
        }

        buttonUp.listen {
            it.cancelPropagation()

            val isNearContour = checkProximity(it.position)

            if (isNearContour) {
                addPoint(it.position)
            }
        }

        dragged.listen {
            it.cancelPropagation()

            val notInList = checkProximity(it.position)

            if (notInList) {
                addPoint(it.position)
            } else {
                val nearest = contourPoints.minBy { p -> p.distanceTo(it.position) }
            }
        }

    }

    fun draw(drawer: Drawer) {
        drawer.fill = ColorRGBa.WHITE
        drawer.stroke = ColorRGBa.BLUE
        drawer.contour(contour)

        drawer.strokeWeight = 0.2
        drawer.stroke = ColorRGBa.RED
        drawer.fill = null
        drawer.contour(contour.offset(-9.0))
        drawer.contour(contour.offset(9.0))

        val shapeProximity = compound {
            difference {
                shape(contour.offset(9.0).shape)
                shape(contour.offset(-9.0).shape)
            }
        }.compound
        drawer.stroke = null
        drawer.fill = ColorRGBa.PINK.opacify(0.4)
        drawer.shape(shapeProximity)

        for (segment in contour.segments) {
            drawer.fill = ColorRGBa.GREEN
            drawer.circle(segment.end, 4.0)
        }

        drawer.fill = ColorRGBa.YELLOW.opacify(0.3)
        drawer.contour(actionBounds)
    }

}