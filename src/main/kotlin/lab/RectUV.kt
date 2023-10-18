package lab

import lib.uv
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noise.Random
import org.openrndr.extra.shapes.hobbyCurve

fun main() = application {
    configure {
        width = 900
        height = 900
        //position = IntVector2(200, -1500)
    }
    program {

        extend {
            val rect = drawer.bounds.offsetEdges(-200.0)
            drawer.rectangle(rect)

            drawer.fill = ColorRGBa.RED
            drawer.stroke = null
            val pos = rect.uv(mouse.position)
            drawer.circle(rect.position(pos), 12.0)
        }
    }
}