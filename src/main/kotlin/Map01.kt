import classes.Mapper
import classes.MapperMode
import org.openrndr.*
import org.openrndr.color.ColorHSLa
import org.openrndr.draw.*
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.extra.olive.OliveScriptHost
import org.openrndr.extra.olive.Once
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.shapes.regularPolygon
import org.openrndr.math.Vector2
import org.openrndr.shape.*

fun main() = application {

    configure {
        width = 1280
        height = 720
    }

    oliveProgram(scriptHost = OliveScriptHost.JSR223) {

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

        extend(Mapper()) {
            mode = MapperMode.ADJUST

            pMap {
                mapperElement("one", Circle(Vector2(360.0, 360.0), 180.0).contour) { rt2.colorBuffer(0) }
                mapperElement("two", regularPolygon(7, Vector2(840.0, 360.0), 180.0), feather = 0.1) { rt.colorBuffer(0) }
            }
        }
        extend {

            drawer.isolatedWithTarget(rt) {
                drawer.rotate(seconds)
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

