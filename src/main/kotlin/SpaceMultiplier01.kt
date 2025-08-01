import classes.Mapper
import org.openrndr.application
import org.openrndr.draw.persistent
import org.openrndr.extra.fx.Post
import org.openrndr.extra.fx.distort.StackRepeat
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.extra.olive.Once
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.ffmpeg.ScreenRecorder
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import org.openrndr.ffmpeg.loadVideoDevice
import org.openrndr.math.map
import kotlin.math.absoluteValue
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.sin

fun main() {
    application {
        configure {
            width = 1920
            height = 1080
            hideWindowDecorations = true
        }

        oliveProgram {


            val cam by Once {
                persistent {
                    loadVideoDevice(VideoPlayerFFMPEG.listDeviceNames().first { it.contains("HD") })
                }.apply { play() }
            }

            val sr = StackRepeat2()
            extend(Post()) {

                post { input, output ->
                    sr.apply(input,  output)
                }
            }
            extend(Mapper()) {
                pMap {
                    mapperElement("frame", drawer.bounds.offsetEdges(-200.0).contour) {
                        cam.draw(drawer, true)

                        cam.colorBuffer?.let {
                            drawer.imageFit(it, drawer.bounds)
                        }
                    }
                }
            }
            extend {

                sr.repeats = ((sin(seconds) * 0.5 + 0.5) * 40.0).toInt() + 50
                sr.zoom = sin(seconds * 0.5).absoluteValue


            }
        }
    }
}