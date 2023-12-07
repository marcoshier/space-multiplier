package classes

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import lib.UIManager
import mu.KotlinLogging
import org.openrndr.Extension
import org.openrndr.Program
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment
import org.openrndr.shape.ShapeContour
import java.io.File
import kotlin.math.log

private val logger = KotlinLogging.logger { }

class Mapper(val mode: MapperMode = MapperMode.ADJUST, val builder: Mapper.() -> Unit): Extension {
    override var enabled: Boolean = true

    lateinit var uiManager: UIManager
    var elements = linkedMapOf<String, MapperElement>()

    val defaultPath = "mapper-parameters"

    // Segment class has lazy length property, which is not supported by Gson decoding
    data class SegmentRef(val start: Vector2, val control: Array<Vector2>, val end:Vector2)

    private fun refFromSegment(from: Segment): SegmentRef {
        return SegmentRef(from.start, from.control, from.end)
    }

    private fun refToSegment(from: SegmentRef): Segment {
        return Segment(from.start, from.control, from.end)
    }

    fun fromObject(segmentLists: Map<String, List<List<SegmentRef>>>) {
        for ((i, l) in segmentLists) {
            elements[i]?.let {
                it.mask = MapperContour(ShapeContour.fromSegments(l[0].map { v -> refToSegment(v) }, true))
                it.textureQuad = MapperContour(ShapeContour.fromSegments(l[1].map { v -> refToSegment(v) }, true))
            }
        }
    }

    fun fromFile(file:File) {
        logger.info { "reading from file ${file.name}" }
        val json = file.readText()
        val typeToken = object : TypeToken<Map<String, List<List<SegmentRef>>>>() {}
        val labeledValues: Map<String, List<List<SegmentRef>>> = try {
            Gson().fromJson(json, typeToken.type)
        } catch (e: JsonSyntaxException) {
            println("could not parse json: $json")
            throw e
        }

        fromObject(labeledValues)
    }
    fun toObject(): Map<String, List<List<SegmentRef>>> {
        return elements.mapValues {
            listOf(
                it.value.mask.cSegments.map { s -> refFromSegment(s) },
                it.value.textureQuad.cSegments.map { s -> refFromSegment(s) })
        }
    }

    fun toFile(file: File) {
        file.writeText(Gson().toJson(toObject()))
        logger.info { "saved to file ${file.name}" }
    }

    fun mapperElement(id: String, contour: ShapeContour, feather: Double = 0.0, f: () -> ColorBuffer) {
        val cb = f()

        val m = elements.getOrPut(id) { MapperElement(contour, feather).apply { texture = cb } }
        uiManager.elements.add(m)
    }

    override fun setup(program: Program) {
        uiManager = UIManager(program)
        uiManager.elements.clear()

        builder()

        val mapperState = File(defaultPath, "${program.name}-latest.json")
        if (mapperState.exists()) {
            fromFile(mapperState)
        } else {
            toFile(mapperState)
        }


        program.mouse.buttonUp.listen {
            if (mapperState.exists()) {
                toFile(File(defaultPath, "${program.name}-latest.json"))
            }
        }
    }

    override fun beforeDraw(drawer: Drawer, program: Program) {
        elements.forEach { it.value.draw(drawer) }
    }

}
