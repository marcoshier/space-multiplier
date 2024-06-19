package classes

import io.obswebsocket.community.client.OBSRemoteController
import org.openrndr.events.Event
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.TextParameter

@Description("OBS Settings")
class OBScontrolSettings {

    val connectRequest = Event<Unit>("connect-request")

    @TextParameter("host")
    var host = "localhost"

    @TextParameter("port")
    var port = "4455"

    @ActionParameter("connect")
    fun clicked() {
        connectRequest.trigger(Unit)
    }
}

class OBSControl {

    val settings = OBScontrolSettings()

    val timeout = 5000L

    var controller: OBSRemoteController? = null

    init {
        buildController()

        settings.connectRequest.listen {
            controller?.stop()
            controller?.disconnect()
            controller = null

            buildController()
        }
    }

    fun buildController() {
        controller = OBSRemoteController.builder()
            .host(settings.host)
            .port(settings.port.toInt())
            .connectionTimeout(10)
            .build()

        controller!!.connect()
    }

    var currentScene = ""
    var currentSource = ""

    fun setScene(sceneName: String) {
        currentScene = sceneName
        controller?.setCurrentProgramScene(sceneName, timeout)
    }

    fun playSource(sourceName: String) {
        controller?.triggerMediaInputAction(sourceName, "OBS_WEBSOCKET_MEDIA_INPUT_ACTION_PLAY", timeout)
        currentSource = sourceName
    }

    fun restartSource(source: String) {
        controller?.triggerMediaInputAction(source, "OBS_WEBSOCKET_MEDIA_INPUT_ACTION_RESTART", timeout)
        currentSource = ""
    }


    fun stopSource(source: String? = null) {
        controller?.triggerMediaInputAction(source ?: currentSource, "OBS_WEBSOCKET_MEDIA_INPUT_ACTION_STOP", timeout)
        currentSource = ""
    }

    fun pauseSource() {
        controller?.triggerMediaInputAction(currentSource, "OBS_WEBSOCKET_MEDIA_INPUT_ACTION_PAUSE", timeout)
    }

    fun getSourceCursor(): Double {
        return controller?.getMediaInputStatus(currentSource, timeout)!!.mediaCursor.toDouble()
    }

    fun getSourceDuration(): Double {
        return controller?.getMediaInputStatus(currentSource, timeout)!!.mediaDuration.toDouble()
    }

    fun getNormalizedCursor(): Double {
        return (getSourceCursor() / getSourceDuration()).coerceIn(0.0, 1.0)
    }

    fun startVirtualCamera() {
        controller?.startVirtualCam(timeout)
    }


}