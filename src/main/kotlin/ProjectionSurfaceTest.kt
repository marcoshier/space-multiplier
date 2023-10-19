import classes.Mapper
import lib.UIManager
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.createEquivalent
import org.openrndr.drawImage
import org.openrndr.extra.fx.Post
import org.openrndr.extra.fx.patterns.Checkers
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.viewbox.viewBox
import org.openrndr.math.IntVector2

fun main() = application {

    configure {
        width = 1920
        height = 1000
        //hideWindowDecorations = true
        windowResizable = true
        position = IntVector2(0, 0)
    }

    program {


        extend {
            val cb0 = colorBuffer(width, height)
            val cb1 = cb0.createEquivalent()

            val c = Checkers().apply {
                background = ColorRGBa.RED
                foreground = ColorRGBa.RED.shade(0.666)
                size = 1.0
            }

            c.apply(cb0, cb1)
            drawer.image(cb1)

            drawer.text("width $width", 30.0, 30.0)
            drawer.text("height $height", 30.0, 60.0)
            drawer.text("position ${window.position}",30.0, 90.0)

        }
    }

}