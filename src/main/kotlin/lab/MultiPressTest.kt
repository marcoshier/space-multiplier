package lab

import org.openrndr.KEY_LEFT_SHIFT
import org.openrndr.KEY_RIGHT_SHIFT
import org.openrndr.application
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.launch

fun main() = application {

    configure {
        width = 800
        height = 800
    }

    oliveProgram {

        var shiftPressed = false

        launch {
            keyboard.keyRepeat.listen {
                if(it.key == KEY_LEFT_SHIFT || it.key == KEY_RIGHT_SHIFT) {
                    shiftPressed = true
                }
            }
            keyboard.keyUp.listen {
                if(it.key == KEY_LEFT_SHIFT || it.key == KEY_RIGHT_SHIFT) {
                    shiftPressed = false
                }
            }
        }

        var currentChar = " "

        keyboard.keyUp.listen {
            currentChar = it.name
        }

        extend {

            println("${ if (shiftPressed) "SHIFT " else "" } + $currentChar")

        }
    }
}