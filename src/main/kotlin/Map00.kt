import classes.Mapper
import classes.MapperMode
import org.openrndr.*
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.extra.olive.OliveScriptHost
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.shapes.hobbyCurve
import org.openrndr.extra.shapes.regularPolygon
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle

/**
 *  * Projection Mapper v0.1 mini-guide
 *
 *
 *  - Start with an initial contour, then edit it within the UI.
 *    The state persists between startups (unless you delete the relevant json in mapper-parameters)
 *
 *  - GIVE UNIQUE IDs to each mapper element (this is fundamental to preserve the contour-color buffer
 *    link, and for you to remember what is what after you have changed the contours a lot)
 *
 *  - openrndr/orx v0.4.4.alpha4+ required
 *
 *  - Make backups of the parameter files you deem important. The extension is still a bit unstable
 *    so stuff may get overwritten
 *
 *  - Controls:
 *      - Hold TAB to edit mask contour, release to edit texture quad
 *      - Left-click on segment: Add point (only for mask)
 *      - Right-click on point: Remove point (only for mask)
 *      - Drag inside shape: Move shape
 *      - Drag segment: Move segment
 *      - Hold shift while moving control point to move the opposite one in a specular fashion
 *
 */


fun main() = application {

    configure {
        width = 1280
        height = 720
    }

    oliveProgram(scriptHost = OliveScriptHost.JSR223) {

        val img = loadImage("data/images/pm5544.png")

        extend(Mapper()) {
            mode = MapperMode.ADJUST

            pMap {
                mapperElement("cheeta2", Rectangle.fromCenter(Vector2(100.0, 100.0), 500.0).contour, feather = 0.1) {
                    drawer.clear(ColorRGBa.RED)
                    drawer.rotate(seconds)
                    drawer.drawStyle.colorMatrix = tint(ColorHSLa(0.5, 0.5, 0.5).shiftHue(seconds * 360.0).toRGBa())
                    drawer.imageFit(img, drawer.bounds)
                }
                mapperElement("cheeta", Circle(250.0, 250.0, 100.0).contour, feather = 0.1) {
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

