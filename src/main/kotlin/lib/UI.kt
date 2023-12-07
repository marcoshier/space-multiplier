package lib

import mu.KotlinLogging
import org.openrndr.*
import org.openrndr.events.Event
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Shape
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.contains

// UI Manager with shape as action bounds, button pressed listener and devoid of touch functionality

interface UIElement: MouseEvents {
    val zOrder: Int
    val actionBounds: List<ShapeContour>
    val visible: Boolean
    var shiftPressed: Boolean
    var tabPressed: Boolean
    val clicked: Event<MouseEvent>
    val doubleClicked: Event<MouseEvent>
}

open class UIElementImpl : UIElement {

    override var zOrder = 0
    override var actionBounds = listOf(Rectangle(0.0, 0.0, 100.0, 100.0).contour)
    override var visible = true
    override var shiftPressed = false
    override var tabPressed = false

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

class UIManager(val program: Program) {
    var activeElement: UIElement? = null
    var dragElement: UIElement? = null
    var postDragElement: UIElement? = null

    var lastMousePosition = Vector2(0.0, 0.0)

    var lastAction = System.currentTimeMillis()
    var lastClicked = 0L

    fun requestDraw() {
        lastAction = System.currentTimeMillis()
        program.window.requestDraw()
    }

    val clicked = Event<Unit>()
    val doubleClicked = Event<Unit>()


    init {
        program.mouse.buttonDown.listen { event ->
            lastMousePosition = event.position
            if (!event.propagationCancelled) {
                for (e in elements.filter { it.visible && it.actionBounds.any { ab -> ab.contains(event.position) } }.sortedBy { it.zOrder }) {
                    e.buttonDown.trigger(event)
                    if (event.propagationCancelled) {
                        requestDraw()
                        activeElement = e
                        dragElement = null
                    }
                }
            }
        }

        program.mouse.dragged.listen { event ->
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

        program.mouse.buttonUp.listen { event ->
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

        program.mouse.scrolled.listen {
            if (!it.propagationCancelled) {
                activeElement?.scrolled?.trigger(it)
                if (activeElement != null) {
                    requestDraw()
                }
            }
        }


        program.keyboard.keyDown.listen {
            if(it.key == KEY_LEFT_SHIFT || it.key == KEY_RIGHT_SHIFT) {
                elements.onEach { e -> e.shiftPressed = true }
            }
            if(it.key == KEY_TAB) {
                elements.onEach { e -> e.tabPressed = true }
            }
        }

        program.keyboard.keyUp.listen {
            if(it.key == KEY_LEFT_SHIFT || it.key == KEY_RIGHT_SHIFT) {
                elements.onEach { e -> e.shiftPressed = false }
            }
            if(it.key == KEY_TAB) {
                elements.onEach { e -> e.tabPressed = false }
            }
        }
    }

    val elements = mutableListOf<UIElement>()

}

