package classes

import RabbitControlServer
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import lib.UIManager
import mu.KotlinLogging
import org.openrndr.*
import org.openrndr.draw.Drawer
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.OptionParameter
import org.openrndr.extra.viewbox.ViewBox
import org.openrndr.extra.viewbox.viewBox
import org.openrndr.math.Vector2
import org.openrndr.shape.Segment2D
import org.openrndr.shape.ShapeContour
import java.io.File

class MapperSettings {
    @OptionParameter("mode", 0)
    var mode = MapperMode.ADJUST
}


private val logger = KotlinLogging.logger { }

class Mapper(val control: RabbitControlServer? = null): Extension {
    override var enabled: Boolean = true

    val settings  = MapperSettings()

    lateinit var uiManager: UIManager
    var elements = linkedMapOf<String, MapperElement>()

    val defaultPath = "mapper-parameters"


    var builder: Mapper.() -> Unit = {}

    // Segment class has lazy length property, which is not supported by Gson decoding
    data class SegmentRef(val start: Vector2, val control: List<Vector2>, val end:Vector2)

    private fun refFromSegment(from: Segment2D): SegmentRef {
        return SegmentRef(from.start, from.control, from.end)
    }

    private fun refToSegment(from: SegmentRef): Segment2D {
        return Segment2D(from.start, from.control, from.end)
    }

    fun fromObject(segmentLists: Map<String, List<List<SegmentRef>>>) {
        for ((i, l) in segmentLists) {
            elements[i]?.let {
                it.mask = MapperContour(ShapeContour.fromSegments(l[0].map { v -> refToSegment(v) }, true))
                it.textureQuad = MapperContour(ShapeContour.fromSegments(l[1].map { v -> refToSegment(v) }, true))
            }
        }

        initialStates.clear()
        for (m in elements.values) {
            initialStates.add(m.name to (0 to m.mask.contour))
            initialStates.add(m.name to (1 to m.textureQuad.contour))
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


    val initialStates = mutableListOf<Pair<String, Pair<Int, ShapeContour>>>()
    val history = mutableListOf<Pair<String, Pair<Int, ShapeContour>>>()

    private fun undo() {
        if (history.isNotEmpty()) {
            val last = history.getOrNull(history.lastIndex - 1)
            last?.let { (name, ic) ->
                println("$name - $ic")
                val (index, contour) = ic
                if (index == 0) {
                    println("changing mask")
                    elements[name]?.mask?.contour = contour
                } else {
                    elements[name]?.textureQuad?.contour = contour
                }
                elements[name]?.calculateBounds()

                history.removeLast()
            }
        }

    }

    fun addListener(e: MapperElement) {
        if (e.contourChangedEvent.listeners.isEmpty()) {
            e.contourChangedEvent.listeners.add {
                if (initialStates.map { it.first }.contains(e.name)) {
                    println("finding initial state of name ${e.name} and idx $it")
                    val els = initialStates.filter { it.first == e.name }
                    val el  = els.firstOrNull { e -> e.second.first == it }
                    if (el != null) {
                        println("found, adding to history")
                        history.add(el)
                        initialStates.remove(el)
                    } else {
                        println("not found $it - ${el?.second?.first}")
                    }
                }
                val c = if (it == 0) e.mask else e.textureQuad
                history.add(e.name to (it to c.contour))
            }
        }
    }


    lateinit var p: Program

    fun mapperElement(id: String, contour: ShapeContour, feather: Double = 0.0, f: ViewBox.() -> Unit) {
        val viewbox = p.viewBox(contour.bounds).apply { extend { f() } }

        println("spawning mapper element")
        val m = elements.getOrPut(id) { MapperElement(id, contour, feather, settings.mode).apply { vb = viewbox } }

        initialStates.add(m.name to (0 to m.mask.contour))
        initialStates.add(m.name to (1 to m.textureQuad.contour))

        uiManager.elements.add(m)
        addListener(m)
    }

    fun pMap(function: Mapper.() -> Unit) {
        builder = function
    }



    private var ctrlPressed = false

    override fun setup(program: Program) {
        if (!File(defaultPath).exists()) {
            File(defaultPath).mkdir()
        }

        p = program

        uiManager = UIManager(program)
        uiManager.elements.clear()

        builder()

        val mapperState = File(defaultPath, "${program.name}-latest.json")
        if (mapperState.exists()) {
            fromFile(mapperState)
        } else {
            toFile(mapperState)
        }

        elements.onEach { it.value.mapperMode = settings.mode }

        initialStates.forEach {
            println(it)
        }

        program.mouse.buttonUp.listen {
            if (mapperState.exists()) {
                toFile(File(defaultPath, "${program.name}-latest.json"))
            }
        }

        program.run {
            keyboard.keyUp.listen {
                if (it.name == "z" && it.modifiers.contains(KeyModifier.SHIFT)) {
                    undo()
                }
                if (it.name == "p") {
                    if (settings.mode ==MapperMode.ADJUST ) {
                        settings.mode = MapperMode.PRODUCTION
                        for (e in elements) {
                            e.value.mapperMode = MapperMode.PRODUCTION
                        }
                    } else {
                        settings.mode = MapperMode.ADJUST
                        for (e in elements) {
                            e.value.mapperMode = MapperMode.ADJUST
                        }
                    }
                }
            }
        }

        if (control != null) {
        }
    }

    override fun beforeDraw(drawer: Drawer, program: Program) {
        elements.values.forEach {
            val el = if (uiManager.activeElement == null) elements.values.first() else uiManager.activeElement
            it.draw(drawer, isActive = it == el)
        }
    }

}
