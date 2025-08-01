package classes

import StackRepeat2
import lib.*
import offset.offset
import org.openrndr.MouseButton
import org.openrndr.MouseEventType
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.events.Event
import org.openrndr.extra.color.presets.FUCHSIA
import org.openrndr.extra.jumpfill.ShapeSDF
import org.openrndr.extra.shadestyles.imageFit
import org.openrndr.extra.shapes.adjust.adjustContour
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.*
import org.openrndr.extra.viewbox.ViewBox
import org.openrndr.math.map
import org.openrndr.math.mix
import org.openrndr.math.smoothstep
import org.openrndr.math.transforms.buildTransform
import org.openrndr.shape.Segment2D
import kotlin.math.sin

class MapperContour(initialContour: ShapeContour) {
    var contour = initialContour
        set(value) {
            field = if (!value.closed) value.close() else value

            cSegments = value.segments.toMutableList()
            cPoints = value.points()
            cControls = value.segments.map { it.control.toList() }.flatten()
            actionablePoints = cPoints + cControls
        }

    var cSegments = contour.segments.toMutableList()
    var cPoints = contour.points()
    var cControls = contour.segments.map { it.control.toList() }.flatten()
    var actionablePoints = (cPoints + cControls)

}


class MapperElement(
    val name: String,
    initialMask: ShapeContour,
    feather: Double = 0.0,
    var mapperMode: MapperMode = MapperMode.ADJUST
): UIElementImpl() {

    val contourChangedEvent = Event<Int>("contour-changed")

    var mask = MapperContour(initialMask)
        set(value) {
            field = value
            calculateBounds()
        }
    var textureQuad = MapperContour(initialMask.bounds.contour)
        set(value) {
            field = value.apply {
                // remove control points to keep a quad-like configuration
                val pts = contour.points().take(4).sortedClockwise()
                contour = ShapeContour.fromPoints(pts, true)
            }
            calculateBounds()
        }

    var texture: ColorBuffer? = null
    var vb: ViewBox? = null

    var personPosition = mask.contour.bounds.center
    var smoothPersonPosition = personPosition

    var personZ = 0.5
    var smoothPersonZ = personZ

    var rectangles = listOf<Rectangle>()
        set(value) {
            field = value

            if (field.isEmpty()) {
                personPosition = mask.contour.bounds.center
            } else {
                val bounds = field.bounds

                personPosition = bounds.center

                val scale = bounds.area
                val z = scale.map(0.0, 1080.0 * 1920.0, 0.0, 1.0, true)

                personZ = (z * 10.0).coerceIn(0.0, 1.0)
            }

        }

    var hide = false

    var feather = feather
        set(value) {
            field = value.coerceIn(0.0, 1.0)
        }

    private val proximityThreshold = 5.0

    private fun movePoint(mouseP: Vector2) {
        activeContour.run {
            val activePoint = cPoints.getOrNull(activePointIdx)

            if (activePoint != null) {
                val i = cPoints.indexOf(activePoint)

                contour = adjustContour(contour) {
                    selectVertex(i)
                    vertex.moveBy(mouseP - activePoint)
                }
            }
        }
    }

    private fun moveControlPoint(mouseP: Vector2) {
        activeContour.run {

            val activePoint = cControls.getOrNull(activeControlPointIdx)

            val segment = cSegments.firstOrNull { it.control.any { c -> c == activePoint } }

            if (activePoint != null && segment != null) {
                val segmentIdx = cSegments.indexOf(segment)

                val sbIdx = (segmentIdx - 1).mod(cSegments.size)
                val saIdx = (segmentIdx + 1).mod(cSegments.size)

                if (activePoint == segment.control[0]) {
                    cSegments[segmentIdx] = Segment2D(segment.start, mouseP, segment.control[1], segment.end)
                    if (shiftPressed) cSegments[sbIdx] = Segment2D(cSegments[sbIdx].start, cSegments[sbIdx].control[0], segment.control[0].rotate(180.0, segment.start), cSegments[sbIdx].end)
                } else {
                    cSegments[segmentIdx] = Segment2D(segment.start, segment.control[0], mouseP,  segment.end)
                    if (shiftPressed) cSegments[saIdx] = Segment2D(cSegments[saIdx].start, segment.control[1].rotate(180.0, segment.end), cSegments[saIdx].control[1], cSegments[saIdx].end)
                }

                contour = contour {
                    for (s in cSegments) {
                        segment(s)
                    }
                }.close()
            }
        }
    }

    private fun moveSegment(mouseP: Vector2) {
        activeContour.run {
            val activeSegment = cSegments.getOrNull(activeSegmentIdx)

            if (activeSegment != null) {
                val sbIdx = (activeSegmentIdx - 1).mod(cSegments.size)
                val saIdx = (activeSegmentIdx + 1).mod(cSegments.size)

                val d = mouseP - activeSegment.position(activeSegmentT)

                cSegments[activeSegmentIdx] = cSegments[activeSegmentIdx].transform(transform { translate(d) })
                cSegments[sbIdx] = Segment2D(cSegments[sbIdx].start, cSegments[sbIdx].control, cSegments[activeSegmentIdx].start)
                cSegments[saIdx] = Segment2D(cSegments[activeSegmentIdx].end, cSegments[saIdx].control, cSegments[saIdx].end)

                contour = contour {
                    for (s in cSegments) {
                        segment(s)
                    }
                }.close()
            }

        }

    }

    private fun moveShape(mouseP: Vector2) {
        var movableContours = listOf<MapperContour>()
        movableContours = if (tabPressed) listOf(mask)
        else if (shiftPressed) listOf(textureQuad)
        else listOf(mask, textureQuad)

        for (c in movableContours) {
            if (mouseP in c.contour.offset(proximityThreshold * 2)) {
                val b = c.contour.bounds
                c.contour = c.contour.transform(transform {
                    translate(mouseP - b.position(activeAnchor))
                })
            }
        }
    }

    private fun addPoint(mouseP: Vector2) {
        if (activeContour == mask) {
            activeContour.run {
                val nearest = contour.nearest(mouseP)

                if (mouseP.distanceTo(nearest.position) < proximityThreshold) {
                    val segmentRef = nearest.segment
                    val idx = cSegments.indexOf(segmentRef)

                    contour = adjustContour(contour) {
                        selectEdge(idx)
                        edge.splitAt(nearest.segmentT)
                        edge.toCubic()
                        edge.next?.toCubic()
                    }
                }
            }
        }

    }

    private fun removePoint(mouseP: Vector2) {
        if (activeContour == mask) {
            activeContour.run {
                val pointInRange = cPoints.firstOrNull { it.distanceTo(mouseP) < proximityThreshold }

                if (pointInRange != null) {
                    val i = cPoints.indexOf(pointInRange)

                    contour = adjustContour(contour) {
                        selectVertex(i)
                        vertex.remove()
                    }
                }
            }
        }
    }


    private var activeContour = mask

    private var activePointIdx = -1
    private var activeControlPointIdx = -1
    private var activeSegmentIdx = -1
    private var activeSegmentT = 0.5
    private var activeAnchor = Vector2.ZERO
    private var lastMouseEvent = MouseEventType.BUTTON_UP


    fun calculateBounds() {
        actionBounds = listOf(
            mask.contour,//.offset(proximityThreshold * 2),
            textureQuad.contour.offset(proximityThreshold * 2)
        )
    }

    init {
        calculateBounds()
        visible = mapperMode != MapperMode.PRODUCTION

        buttonDown.listen {
            it.cancelPropagation()
            lastMouseEvent = it.type

            activeContour = if (tabPressed) mask else textureQuad

            activeContour.run {
                val b = contour.bounds
                val activePoint = actionablePoints.firstOrNull { ap -> isInRange(ap, it.position) }

                if (activePoint != null) {
                    if (activePoint in cPoints) {
                        val idx = cPoints.indexOf(cPoints.minBy { p -> p.distanceTo(it.position) })
                        activePointIdx = idx
                    } else if (activePoint in cControls) {
                        val idx = cControls.indexOf(cControls.minBy { p -> p.distanceTo(it.position) })
                        activeControlPointIdx = idx
                    }
                } else {
                    val nearest = contour.nearest(it.position)
                    if (it.position.distanceTo(nearest.position) < proximityThreshold) {
                        activeSegmentIdx = cSegments.indexOf(nearest.segment)
                        activeSegmentT = nearest.segmentT
                    } else {
                        activeAnchor = b.uv(it.position)
                    }
                }

            }


            calculateBounds()
        }

        buttonUp.listen {
            it.cancelPropagation()

            activeContour.run {
                if (lastMouseEvent == MouseEventType.BUTTON_DOWN) {
                    if (it.button == MouseButton.LEFT) {
                        addPoint(it.position)
                    } else if (it.button == MouseButton.RIGHT) {
                        if (cSegments.size > 3) {
                            removePoint(it.position)
                        }
                    }
                }

            }

            contourChangedEvent.trigger(if (activeContour == mask) 0 else 1)

            activeContour = mask
            activePointIdx = -1
            activeControlPointIdx = -1
            activeSegmentIdx = -1
            activeSegmentT = 0.5
            lastMouseEvent = it.type

            calculateBounds()

        }

        dragged.listen {
            it.cancelPropagation()

            if (lastMouseEvent != MouseEventType.BUTTON_UP) {
                lastMouseEvent = it.type

                if (activePointIdx != -1) {
                    movePoint(it.position)
                } else if (activeControlPointIdx != -1) {
                    moveControlPoint(it.position)
                } else if (activeSegmentIdx != -1){
                    moveSegment(it.position)
                } else {
                    moveShape(it.position)
                }

            }

            calculateBounds()

        }
    }

    private val initialT = System.currentTimeMillis()

    private val ss = shadeStyle {
        fragmentPreamble = cross2d + inverseBilinear

        fragmentTransform = """
                      vec2 uv = invBilinear(va_position, p_pos[0], p_pos[1], p_pos[2], p_pos[3]);
                      
                      if( uv.x > -0.5 ) x_fill.xyz = texture(p_img, uv).xyz; 

                      x_fill = texture(p_img, uv.xy);
                      x_fill.a = p_o;
                      x_fill.rgb = (x_fill.rgb - 0.5) * (1.0 + 0.1 * p_i) + 0.5;
                     // x_fill.a *= (smoothstep(0.0, p_feather, uv.x)) * (1.0 - smoothstep(1.0 - p_feather, 1.0, uv.x));
                     // x_fill.a *= (smoothstep(0.0, p_feather, uv.y)) * (1.0 - smoothstep(1.0 - p_feather, 1.0, uv.y));
                    """.trimIndent()
        parameter("o", 1.0)
        parameter("i", 0)
    }

    val t = System.currentTimeMillis()

    fun draw(drawer: Drawer, isActive: Boolean = true, mode: MapperMode) {

            val t0 = (System.currentTimeMillis() - t) / 1000.0

            smoothPersonPosition = personPosition.mix(smoothPersonPosition, 0.95)
            smoothPersonZ = mix(personZ, smoothPersonZ, 0.85)

            mapperMode = mode

            val opacity = if (isActive || mapperMode == MapperMode.PRODUCTION) 1.0 else 0.5

            vb?.let {
                it.update()
                texture = it.result
            }

            val nRepeats = 20



            texture?.let {
                for (i in 0 until nRepeats) {
                    drawer.isolated {
                        ss.parameter("img", it)
                        ss.parameter("pos", textureQuad.cPoints.take(4).reversed().toTypedArray())
                        ss.parameter("feather", 0.0)

                        var o: Double

                        if (mapperMode == MapperMode.ADJUST) {
                            o = 0.1 * opacity
                            drawer.stroke = null
                            drawer.fill = ColorRGBa.WHITE.opacify(opacity)

                            ss.parameter("o", o)

                            drawer.shadeStyle = ss
                            drawer.shape(textureQuad.contour.shape)
                        }


                        drawer.shadeStyle = null
                        o = 1.0 * opacity

                        ss.parameter("o", o)
                        ss.parameter("i", i)
                        drawer.stroke = ColorRGBa.TRANSPARENT
                        drawer.shadeStyle = ss
                        drawer.fill = ColorRGBa.WHITE
                        val ogshape = textureQuad.contour.shape.intersection(mask.contour.shape)

                        val i0t = ((1.0 - (i / (nRepeats.toDouble()))) * smoothPersonZ + (1.0 - smoothPersonZ)).smoothstep(0.5, 1.0)


                        translate(ogshape.bounds.center.x + (smoothPersonPosition.x - 320.0) * 0.5 * (1.0 - i0t), ogshape.bounds.center.y)
                        scale(i0t * 1.0)
                        translate(-ogshape.bounds.center)

                        drawer.shape(ogshape)
                    }
                }


            }

            if (mapperMode == MapperMode.ADJUST) {

                val c = if (tabPressed) mask else textureQuad

                drawer.fill = ColorRGBa.PINK.opacify(opacity)
                drawer.circles(c.cPoints, 6.0)

                if (activePointIdx != -1) {
                    drawer.fill = ColorRGBa.FUCHSIA.opacify(opacity)
                    c.cPoints.getOrNull(activePointIdx)?.let { drawer.circle(it, 9.0) }
                }

                for (segment in c.cSegments) {
                    drawer.fill = ColorRGBa.WHITE.opacify(0.4 * opacity)
                    drawer.stroke = null
                    drawer.circles(segment.control.toList(), 4.0)

                    drawer.stroke = ColorRGBa.WHITE.opacify(0.4 * opacity)
                    drawer.strokeWeight = 0.5
                    segment.control.getOrNull(0)?.let { drawer.lineSegment(segment.start, it) }
                    segment.control.getOrNull(1)?.let { drawer.lineSegment(it, segment.end) }

                }

                drawer.fill = null
                drawer.strokeWeight = 2.5
                drawer.stroke = ColorRGBa.PINK.opacify(opacity)
                drawer.contour(mask.contour)

                drawer.stroke = ColorRGBa.GREEN.opacify(opacity)
                drawer.contour(textureQuad.contour)

                if (isActive) {
                    drawer.strokeWeight = 1.0
                    drawer.stroke = ColorRGBa.WHITE
                    drawer.fill = null
                    drawer.shadeStyle = shadeStyle {
                        fragmentTransform = """
                     float m = sin(c_contourPosition * 0.85 - p_t * 5.0) * 0.5 + 0.5;
                     x_stroke.rgba *= mix(vec4(1.0), vec4(0.0), m);
                """.trimIndent()
                        parameter("t", (System.currentTimeMillis() - initialT) / 1000.0)
                    }
                    drawer.contours(actionBounds)
                    drawer.shadeStyle = null
                }

            }



            drawer.fill = ColorRGBa.WHITE
            drawer.fontMap = loadFont("data/fonts/default.otf", 30.0)
            drawer.text(smoothPersonPosition.x.toString(), drawer.bounds.center)

    }


    private fun isInRange(pos: Vector2, mousePos: Vector2): Boolean {
        return pos.distanceTo(mousePos) < proximityThreshold
    }


}


