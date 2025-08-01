import classes.Mapper
import org.openrndr.application
import org.openrndr.draw.loadImage
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.extra.jumpfill.ShapeSDF
import org.openrndr.extra.shapes.primitives.Tear
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle

fun main() {
    application {
        configure {
            width = 1080
            height = 1920
            display = displays[1]
        }

        program {

            val img = loadImage("data/images/cheeta.jpg")

            extend(Mapper()) {
                pMap {
                    mapperElement("ola", Circle(drawer.bounds.center, 300.0).contour) {
                        drawer.imageFit(img, drawer.bounds)
                    }
                }
            }

            extend {

            }
        }
    }
}