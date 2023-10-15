import org.openrndr.application
import org.openrndr.draw.colorBuffer
import org.openrndr.extra.olive.oliveProgram
import lib.RS2Sensor

fun main() = application {

    configure {
        width = 1000
        height = 1000
    }

    oliveProgram {


        val sensor = RS2Sensor.openFirstOrDummy()
        val cb = colorBuffer(320, 240)

        sensor.depthFrameReceived.listen {
            it.copyTo(cb)
        }

        extend {

            sensor.waitForFrames()
            drawer.image(cb)
        }
    }
}