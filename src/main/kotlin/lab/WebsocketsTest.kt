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
fun main() {
    val obsControl = OBSControl()

    embeddedServer(Netty, port = 9999) {
        install(WebSockets)
        routing {
            val start = System.currentTimeMillis()
            webSocket("/ws") {
                while (true) {
                    send(Frame.Text(((System.currentTimeMillis() - start) / 1000.0).toString()))
                    delay(10)
                }
            }
        }
    }.start(wait = true)
}