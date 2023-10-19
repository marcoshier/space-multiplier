import classes.Mapper
import lib.UIManager
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.fx.Post
import org.openrndr.extra.fx.patterns.Checkers
import org.openrndr.extra.viewbox.viewBox

fun main() = application {

    configure {
        width = 1920
        height = 1000
    }

    program {
       val bg = viewBox(drawer.bounds) {
            extend(Post()) {
                val c = Checkers().apply {
                    background = ColorRGBa.RED
                    foreground = ColorRGBa.RED.shade(0.666)
                    size = 1.0
                }
                post { input, output ->
                    c.apply(input, output)
                }
            }
        }

        val uiManager = UIManager(program)
        val mapper = Mapper() {
            mapperElement("one", drawer.bounds.offsetEdges(-10.0).contour) { bg.result }
        }

        extend(mapper)
        extend {
            bg.update()
        }
    }

}