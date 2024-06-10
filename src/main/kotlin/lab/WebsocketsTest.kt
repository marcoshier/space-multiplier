package lab

import classes.OBSControl
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import org.openrndr.application
import org.openrndr.launch

@OptIn(DelicateCoroutinesApi::class)
fun main() = application {

    program {

        val obs = OBSControl()
        obs.playSource("MAIN")

        GlobalScope.launch {
            embeddedServer(Netty, port = 9999) {
                install(WebSockets)
                routing {
                    webSocket("/ws") {
                        while (true) {
                            val t = listOf(obs.getNormalizedCursor(), obs.getSourceCursor() / 1000.0, obs.getSourceDuration() / 1000.0)

                            send(Frame.Text(t.joinToString(" ")))
                            delay(10)
                        }
                    }
                }
            }.start(wait = true)
        }

    }
}