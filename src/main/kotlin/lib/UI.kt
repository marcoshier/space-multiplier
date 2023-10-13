package lib

import mu.KotlinLogging
import org.openrndr.*
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.events.Event
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Shape
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contains
import kotlin.random.Random

interface UIElement: MouseEvents {
    val zOrder: Int
    val actionBounds: ShapeContour
    val visible: Boolean
    val clicked: Event<MouseEvent>
    val doubleClicked: Event<MouseEvent>
}

open class UIElementImpl : UIElement {

    override var zOrder = 0
    override var actionBounds = Rectangle(0.0, 0.0, 100.0, 100.0).contour
    override var visible = true

    override val buttonDown: Event<MouseEvent> = Event("ui-element-button-down")
    override val buttonUp: Event<MouseEvent> = Event("ui-element-button-up")
    override val clicked: Event<MouseEvent> = Event("ui-element-clicked")
    override val doubleClicked: Event<MouseEvent> = Event("ui-element-double-clicked")

    override val dragged: Event<MouseEvent> = Event("ui-element-dragged")


    override val entered: Event<MouseEvent> = Event("ui-element-entered")
    override val exited: Event<MouseEvent> = Event("ui-element-exited")
    override val moved: Event<MouseEvent> = Event("ui-element-moved")

    override val position: Vector2
        get() = error("don't use 'position'")

    override val scrolled: Event<MouseEvent> = Event("ui-element-scrolled")
}

private val logger = KotlinLogging.logger {  }

class UIManager(val window: Window, mouseEvents: MouseEvents) {
    var activeElement: UIElement? = null
    var dragElement: UIElement? = null
    var postDragElement: UIElement? = null

    var lastMousePosition = Vector2(0.0, 0.0)

    var lastAction = System.currentTimeMillis()
    var lastClicked = 0L

    fun requestDraw() {
        lastAction = System.currentTimeMillis()
        window.requestDraw()
    }

    val clicked = Event<Unit>()
    val doubleClicked = Event<Unit>()


    init {
        mouseEvents.buttonDown.listen { event ->
            lastMousePosition = event.position
            if (!event.propagationCancelled) {
                for (e in elements.filter { it.visible && it.actionBounds.contains(event.position) }.sortedBy { it.zOrder }) {
                    e.buttonDown.trigger(event)
                    if (event.propagationCancelled) {
                        requestDraw()
                        activeElement = e
                        dragElement = null
                    }
                }
            }
        }

        mouseEvents.dragged.listen { event ->
            if (!event.propagationCancelled) {


                if (System.currentTimeMillis() - lastClicked >= 300) {
                   // logger.info { "setting dragElement to $activeElement" }
                    dragElement = activeElement
                }

                if (dragElement != null) {
                    requestDraw()
                    lastMousePosition = event.position
                }

                dragElement?.dragged?.trigger(event)
            }
        }

        mouseEvents.buttonUp.listen { event ->
            if (!event.propagationCancelled) {
                lastMousePosition = event.position

                activeElement?.buttonUp?.trigger(event)
                if (activeElement != null && dragElement == null) {
                    if (System.currentTimeMillis() - lastClicked < 300) {
                        logger.info {
                            "double click dispatched to ${activeElement}, ${System.currentTimeMillis() - lastClicked}ms"
                        }
                        activeElement?.doubleClicked?.trigger(event)
                        doubleClicked.trigger(Unit)
                        lastClicked = 0L
                    } else {
                        logger.info {
                            "single click dispatched to ${activeElement}"
                        }
                        activeElement?.clicked?.trigger(event)
                        clicked.trigger(Unit)
                        lastClicked = System.currentTimeMillis()
                    }
                }

                if (dragElement != null) {
                    postDragElement = dragElement
                    dragElement = null
                }
            }
        }


        mouseEvents.scrolled.listen {
            if (!it.propagationCancelled) {
                activeElement?.scrolled?.trigger(it)
                if (activeElement != null) {
                    requestDraw()
                }
            }
        }
    }

    val elements = mutableListOf<UIElement>()

}

