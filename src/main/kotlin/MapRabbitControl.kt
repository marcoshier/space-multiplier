import classes.Mapper
import classes.MapperMode
import classes.OBSControl
import org.openrndr.*
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle

fun main() = application {

    configure {
        width = 1280
        height = 720
    }

    program {

        val rc = RabbitControlServer()
        val obs = OBSControl()

        rc.add(obs.settings)

        val img = loadImage("data/images/pm5544.png")

        extend(Mapper(control = rc)) {
            settings.mode = MapperMode.ADJUST

            pMap {
                mapperElement("big", Rectangle.fromCenter(Vector2(100.0, 100.0), 500.0).contour, feather = 0.1) {
                    drawer.clear(ColorRGBa.RED)
                    drawer.rotate(seconds)
                    drawer.drawStyle.colorMatrix = tint(ColorHSLa(0.5, 0.5, 0.5).shiftHue(seconds * 360.0).toRGBa())
                    drawer.imageFit(img, drawer.bounds)
                }
                mapperElement("small", Circle(250.0, 250.0, 100.0).contour, feather = 0.1) {
                    drawer.clear(ColorRGBa.RED)
                    drawer.rotate(seconds)
                    drawer.drawStyle.colorMatrix = tint(ColorHSLa(0.5, 0.5, 0.5).shiftHue(seconds * 360.0).toRGBa())
                    drawer.imageFit(img, drawer.bounds)
                }

            }
        }
        extend {

        }
    }

}

