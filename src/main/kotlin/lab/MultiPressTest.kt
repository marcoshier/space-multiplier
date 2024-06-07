package lab

import org.openrndr.*
import org.openrndr.color.ColorRGBa
import org.openrndr.events.listen
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.shape.Rectangle

fun main() = application {

    configure {
        width = 800
        height = 800
    }

    oliveProgram {

        val r0 = Rectangle(0.0, 0.0, 100.0, 100.0).contour
        val r1 = Rectangle(1.0, 0.0, 100.0, 100.0).contour

        println(r0.hashCode() == r1.hashCode())

        keyboard.keyUp.listen {
            if (it.name == "z" && it.modifiers.contains(KeyModifier.CTRL)) {
                println("down")
            }
        }


        extend {



        }
    }
}