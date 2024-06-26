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
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import lab.lightLeaks
import org.openrndr.*
import org.openrndr.events.Event
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.shapes.primitives.toRounded
import org.openrndr.extra.viewbox.viewBox
import org.openrndr.ffmpeg.*
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle
import java.io.File
import kotlin.concurrent.thread


fun main() = application {

    configure {
        //position= IntVector2(1920, 0)
        width = 1920
        height = 1080
        hideWindowDecorations = true
        windowAlwaysOnTop=true
        fullscreen = Fullscreen.SET_DISPLAY_MODE
        hideCursor=false
    }

    program {

        val obs = OBSControl()
        obs.stopSource("MAIN")

        val thread = thread(isDaemon = true) {
            embeddedServer(Netty, port = 9999) {
                install(WebSockets)
                routing {
                    staticFiles("/static", File("static")) {

                    }

                    staticResources("/resources", "assets")
                    webSocket("/ws") {
                        while (true) {
                            val t = listOf(obs.getNormalizedCursor(), (obs.getSourceCursor() / 1000.0).toInt(), obs.getSourceDuration() / 1000.0)

                            send(Frame.Text(t.joinToString(" ")))
                            window.requestDraw()
                            delay(10)
                        }
                    }
                }
            }.start(wait = true)
        }

        val status = object  {
            val videoEnded = Event<Unit>()
            var restarted = false
            var t = 0.0
                set(value) {
                    field = value
                    if (!restarted && value > (obs.getSourceDuration() / 1000.0) - 1.0) {
                        videoEnded.trigger(Unit)
                    }
                    if (value < 1.0) {
                        restarted = false
                    }
                }
        }

        var background: VideoPlayerFFMPEG? = null//clock = obs::getSourceCursor
        background = loadVideoWithClock("data/videos/4.mp4", { status.t }, PlayMode.VIDEO,  )
        obs.restartSource("MAIN")
        background.play()

        val deviceList = VideoPlayerFFMPEG.listDeviceNames()
        println(deviceList)

        val main = loadVideoDevice("0", PlayMode.VIDEO)

        val r =  Rectangle.fromCenter(drawer.bounds.center, 1920.0 / 1.5, 1080 / 1.5)

        main.play()

        var obsPaused = false
        keyboard.keyUp.listen {
            if (it.key == KEY_ESCAPE) {
                thread.interrupt()
                application.exit()
            }
            if (it.key == KEY_SPACEBAR) {
                obsPaused = !obsPaused
                if (obsPaused) {
                    obs.playSource("MAIN")
                } else {
                    obs.pauseSource("MAIN")
                }
            }
        }

        program.ended.listen {
            obs.stopSource()
        }


        var sceneSettings = object {
            @BooleanParameter("I LOVE MY LAND")
            var scenePlaying = true
                set(value) {
                    if (field && !value) {
                        obs.pauseSource("MAIN")
                    } else if (!field && value){
                        obs.restartSource("MAIN")
                        background.restart()
                    }

                    field = value
                }
        }


        keyboard.character.listen {
            if (it.character == 's') {
                sceneSettings.scenePlaying = false
            }
            if (it.character == 'o') {
                sceneSettings.scenePlaying = true
            }
        }

        status.videoEnded.listen {
            println("restarting")
            status.restarted = true
            runBlocking {
                sceneSettings.scenePlaying = false
                delay(5000)
                sceneSettings.scenePlaying = true
            }
        }


        println(obs.getSourceDuration())


        val m = extend(Mapper()) {
            settings.mode = MapperMode.PRODUCTION
            pMap {
                mapperElement("background", drawer.bounds.contour) {


                    status.t = (obs.getSourceCursor() / 1000.0).mod(obs.getSourceDuration() / 1000.0)

                    if (sceneSettings.scenePlaying) {
                        background.draw(drawer)
                    }
                }
                mapperElement("main", r.contour) {
                    if (sceneSettings.scenePlaying) {

                        drawer.scale(1.0 / 1.5)
                        main.draw(drawer)
                    }
                }

                mapperElementVB("lights", r.contour)
            }
        }
        extend {

            m.playing = sceneSettings.scenePlaying
           // println(obs.getSourceCursor())
        }
    }

}

fun Program.loadVideoWithClock(fileOrUrl: String, clock: () -> Double, mode: PlayMode = PlayMode.BOTH, configuration: VideoPlayerConfiguration = VideoPlayerConfiguration()): VideoPlayerFFMPEG {
    return VideoPlayerFFMPEG.fromFile(fileOrUrl, clock = clock, mode = mode, configuration = configuration)
}