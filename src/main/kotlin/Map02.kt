import classes.Mapper
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import org.openrndr.ffmpeg.loadVideoDevice
import org.openrndr.shape.Circle

fun main() {
    application {
        configure {
            width = 1280
            height = 720
            //display = displays[1]
        }

        program {
            println(VideoPlayerFFMPEG.listDeviceNames())
            val cam = loadVideoDevice()
            cam.play()

            extend(Mapper()) {
                pMap {
                    mapperElement("face", Circle(drawer.bounds.center, 500.0).contour) {
                        drawer.clear(ColorRGBa.RED)
                        drawer.scale(2.0)
                       // cam.draw(drawer)
                    }
                }
            }

            extend {



            }
        }

    }
}