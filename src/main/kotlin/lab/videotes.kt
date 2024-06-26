package lab

import org.openrndr.application
import org.openrndr.ffmpeg.loadVideo

fun main() = application {

    configure {
        width = 1920
        height = 1080
    }
    program {

        val vid = loadVideo("data/videos/4_1.mp4")
        vid.play()

        extend {
            vid.draw(drawer)
        }
    }
}