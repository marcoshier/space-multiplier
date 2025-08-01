import classes.Mapper
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.tint
import org.openrndr.ffmpeg.loadVideoDevice
import org.openrndr.shape.Rectangle

fun main() {
    application {
        configure {
            width = 1920
            height = 1080
            display = displays[1]
        }

        program {
            val cam = loadVideoDevice()
            cam.play()

            extend(Mapper()) {
                pMap {
                    mapperElement("face", Rectangle(drawer.bounds.center, 350.0, 300.0).contour) {
                        drawer.clear(ColorRGBa.RED)
                        drawer.scale(0.85)
                      //  cam.draw(drawer)
                    }
                }
            }

            extend {



            }
        }

    }
}