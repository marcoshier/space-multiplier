
import classes.OBSControl
import org.openrndr.*

fun main() = application {

    configure {
        width = 1280
        height = 720
    }

    program {

        val obs = OBSControl()
        obs.setScene("Film")
        obs.playSource("MAIN")
        obs.pauseSource()

        obs.startVirtualCamera()


        extend {

        }
    }

}

