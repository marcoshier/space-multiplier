import lib.UIElementImpl
import lib.UIManager
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.PURPLE
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.Arc
import org.openrndr.extra.shapes.hobbyCurve
import org.openrndr.math.Vector2
import org.openrndr.shape.*

fun main() = application {
    configure {
        width = 900
        height = 900
        //position = IntVector2(200, -1500)
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

    private var segmentsRef = contour.segments.toMutableList()
    private var points = contour.segments.map { it.start }.toMutableList()

    private fun checkProximity(pos: Vector2): Boolean {

        if (points.any {
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
        val nearestPos = contour.nearest(pos)
        val nearestSegment = nearestPos.segment

        val split = nearestSegment.split(nearestPos.segmentT)

        val segmentIdx = segmentsRef.indexOf(nearestSegment)

        segmentsRef[segmentIdx] = split[0]
        segmentsRef.add(segmentIdx + 1, split[1])

        contour = contour {
            for (segment in segmentsRef) { segment(segment) }
        }.close()

        points.add(segmentIdx + 1, split[1].start)
        actionBounds = contour
    }


    var activeSegment: Segment? = null
    var activePoint: Vector2? = null
    init {
        actionBounds = contour

        buttonDown.listen {
            it.cancelPropagation()
        }

        buttonUp.listen {
            it.cancelPropagation()
            activeSegment = null
            activePoint = null

            val isNearContour = checkProximity(it.position)

            if (isNearContour) {
                addPoint(it.position)
            }
        }

        dragged.listen {
            it.cancelPropagation()

            if (checkProximity(it.position) && activePoint == null) {
                activePoint = contour.nearest(it.position).segment.start
            }

            activePoint = it.position
            activePoint?.let { ap ->
                val idx = segmentsRef.map { it.start }.indexOf(ap)
                activeSegment = segmentsRef.getOrNull(idx)
                if (activeSegment != null) {
                    segmentsRef[idx] = Segment(it.position, segmentsRef[idx].end)
                }
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

        for (point in points) {
            drawer.stroke = null
            drawer.fill = ColorRGBa.GREEN
            drawer.circle(point, 4.0)
        }

        drawer.fill = ColorRGBa.YELLOW.opacify(0.3)
        drawer.contour(actionBounds)

        drawer.fill = ColorRGBa.PURPLE.shade(1.5)
        activePoint?.let { drawer.circle(it, 9.0) }

    }

}
