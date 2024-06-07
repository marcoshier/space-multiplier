import classes.Mapper
import classes.MapperMode
import io.obswebsocket.community.client.OBSRemoteController
import kotlinx.coroutines.delay
import org.openrndr.*
import org.openrndr.color.ColorHSLa
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.extra.olive.OliveScriptHost
import org.openrndr.extra.olive.Once
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.shapes.hobbyCurve
import org.openrndr.extra.shapes.regularPolygon
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.Rectangle


fun main() = application {

    configure {
        width = 1280
        height = 720
    }

    class OBSControl {

        val timeout = 5000L

        val controller = OBSRemoteController.builder()
            .host("localhost")
            .port(4455)
            .connectionTimeout(10)
            .build()

        init {
            controller.connect()
        }

        fun setScene(sceneName: String) {
            controller.setCurrentProgramScene(sceneName, timeout)
        }

        fun playSource(sourceName: String) {
            controller.triggerMediaInputAction(sourceName, "OBS_WEBSOCKET_MEDIA_INPUT_ACTION_PLAY", timeout)
        }

        fun stopSource(sourceName: String) {
            controller.triggerMediaInputAction(sourceName, "OBS_WEBSOCKET_MEDIA_INPUT_ACTION_STOP", timeout)
        }

        fun pauseSource(sourceName: String) {
            controller.triggerMediaInputAction(sourceName, "OBS_WEBSOCKET_MEDIA_INPUT_ACTION_PAUSE", timeout)
        }

        fun startVirtualCamera() {
            controller.startVirtualCam(timeout)
        }


    }

    program {

        val obs = OBSControl()
        obs.setScene("Film")
        obs.playSource("MAIN")
        obs.pauseSource("MAIN")

        obs.startVirtualCamera()


        extend {

        }
    }

}

