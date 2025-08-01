import org.openrndr.Fullscreen
import org.openrndr.application
import org.openrndr.color.ColorRGBa

fun main() {
    application {
        configure {
            width = 1080
            height = 1920
            display = displays[1]
            fullscreen = Fullscreen.SET_DISPLAY_MODE
        }

        program {


            extend {
                drawer.clear(ColorRGBa.WHITE)

            }
        }
    }
}