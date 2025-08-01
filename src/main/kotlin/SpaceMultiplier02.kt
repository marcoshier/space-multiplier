import classes.Mapper
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorFormat
import org.openrndr.draw.ColorType
import org.openrndr.draw.colorBuffer
import org.openrndr.extra.fx.Post
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.extra.jumpfill.ShapeSDF
import org.openrndr.extra.shapes.primitives.regularStar
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import org.openrndr.ffmpeg.loadVideoDevice
import org.openrndr.math.map
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.Shape
import kotlin.math.sin

fun main() {
    application {
        configure {
            width = 1080
            height = 1920
            //display = displays[1]
        }

        program {

            val video = loadVideoDevice(
                VideoPlayerFFMPEG.listDeviceNames().first { it.contains("HD") },
                width = 1280,
                height = 720
            )
            video.play()

            extend(Mapper()) {
                pMap {
                    mapperElement("main0", Circle(drawer.bounds.center, 300.0).contour) {
                        video.draw(drawer)
                    }
                }
            }

            extend {

            }
        }
    }
}