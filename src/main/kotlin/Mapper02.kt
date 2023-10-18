import classes.mapperElement
import lib.*
import org.openrndr.*
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.color.presets.ORANGE
import org.openrndr.extra.color.presets.PURPLE
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.Once
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.*
import kotlin.math.atan2
import kotlin.random.Random

fun main() = application {

    configure {
        width = 1000
        height = 1000
    }

    oliveProgram {

        val uiManager = UIManager(program)

        val img = loadImage("data/images/pm5544.png")

        val rt by Once {
            renderTarget(width, height) {
                colorBuffer()
            }
        }

        val s0 by Once {
            mapperElement(Rectangle.fromCenter(drawer.bounds.center, 250.0, 260.0).contour)
        }

        val s1 by Once {
            mapperElement(Circle(drawer.bounds.center, 260.0).contour)
        }


        uiManager.elements.addAll(listOf(s0, s1))

        extend {

            drawer.isolatedWithTarget(rt) {
                drawer.drawStyle.colorMatrix = tint(ColorHSLa(0.5, 0.5, 0.5).shiftHue(seconds * 360.0).toRGBa())
                drawer.imageFit(img, drawer.bounds)
            }

            s0.draw(drawer)
            s1.draw(drawer)

        }
    }

}

