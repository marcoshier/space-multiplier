import classes.Mapper
import classes.MapperMode
import lib.*
import org.openrndr.*
import org.openrndr.color.ColorHSLa
import org.openrndr.draw.*
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.extra.olive.Once
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.shapes.regularPolygon
import org.openrndr.math.Vector2
import org.openrndr.shape.*

fun main() = application {

    configure {
        width = 1000
        height = 1000
    }

    oliveProgram {

        val rt by Once {
            renderTarget(width, height) {
                colorBuffer()
            }
        }

        val rt2 by Once {
            renderTarget(width, height) {
                colorBuffer()
            }
        }

        val img = loadImage("data/images/pm5544.png")
        val img2 = loadImage("data/images/cheeta.jpg")

        val uiManager = UIManager(this)
        val pMapper = Mapper(uiManager, MapperMode.ADJUST) {
            mapperElement(Rectangle.fromCenter(drawer.bounds.center, 250.0, 260.0).contour) { rt.colorBuffer(0) }
            mapperElement(Circle(drawer.bounds.center, 260.0).contour) { rt.colorBuffer(0) }
            mapperElement(regularPolygon(5, Vector2(100.0, 100.0), 200.0)) { rt2.colorBuffer(0) }
        }

        extend(pMapper)

        extend {
            drawer.isolatedWithTarget(rt) {
                drawer.drawStyle.colorMatrix = tint(ColorHSLa(0.5, 0.5, 0.5).shiftHue(seconds * 360.0).toRGBa())
                drawer.imageFit(img, drawer.bounds)
            }

            drawer.isolatedWithTarget(rt2) {
                drawer.drawStyle.colorMatrix = tint(ColorHSLa(0.5, 0.5, 0.5).shiftHue(seconds * 180.0).toRGBa())
                drawer.imageFit(img2, drawer.bounds)
            }
        }
    }

}

