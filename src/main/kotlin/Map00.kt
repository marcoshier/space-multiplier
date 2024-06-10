import classes.Mapper
import classes.MapperMode
import classes.OBSControl
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.openrndr.*
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.ffmpeg.PlayMode
import org.openrndr.ffmpeg.VideoPlayerFFMPEG
import org.openrndr.ffmpeg.loadVideo
import org.openrndr.ffmpeg.loadVideoDevice
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.IntRectangle
import org.openrndr.shape.Rectangle
import java.io.File

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
 *      - CTRL-Z to undo
 *
 */


fun main() = application {

    configure {
        position= IntVector2(1920, 0)
        width = 1920
        height = 1080
        hideWindowDecorations = true
        windowAlwaysOnTop=true
       // hideCursor=true
    }

    program {


        val obs = OBSControl()
        obs.startVirtualCamera()
        obs.stopSource()
        obs.playSource("MAIN")

        GlobalScope.launch {
            embeddedServer(Netty, port = 9999) {
                install(WebSockets)
                routing {
                    staticFiles("/static", File("static")) {

                    }

                    webSocket("/ws") {
                        while (true) {
                            val t = listOf(obs.getNormalizedCursor(), obs.getSourceCursor() / 1000.0, obs.getSourceDuration() / 1000.0)

                            send(Frame.Text(t.joinToString(" ")))
                            window.requestDraw()
                            delay(10)
                        }
                    }
                }
            }.start(wait = true)
        }

        val background = loadVideo("data/videos/4.mp4", PlayMode.VIDEO)
        background.play()

        val deviceList = VideoPlayerFFMPEG.listDeviceNames()
        val main = loadVideoDevice(deviceList.first { it.startsWith("OBS") || it.startsWith("0") }, PlayMode.VIDEO)

        val r =  Rectangle.fromCenter(drawer.bounds.center, 1920.0 / 1.5, 1080 / 1.5)

        main.play()
        println(main.width)

        extend(Mapper()) {
            settings.mode = MapperMode.ADJUST

            pMap {
                mapperElement("background", drawer.bounds.contour) {
                    background.draw(drawer)
                }
                mapperElement("main", r.contour) {
                    drawer.scale(1.0 / 1.5)
                    main.draw(drawer)
                }
            }
        }
        extend {

        }
    }

}

