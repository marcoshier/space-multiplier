package lab

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import org.openrndr.application
import java.io.File

fun main() {
    val start = System.currentTimeMillis()
    embeddedServer(Netty, port = 9999, host = "192.168.0.101") {
        install(WebSockets)
        routing {

            staticResources("/resources", "assets")
            staticFiles("/static", File("static")) {

            }
            webSocket("/ws") {
                while (true) {
                    val t = listOf(
                        (((System.currentTimeMillis() - start) / 5000.0)).mod(6.0 / 5.0),
                        (((System.currentTimeMillis() - start) / 1000.0)).mod(6.0).toInt(), 5.0)

                    send(Frame.Text(t.joinToString(" ")))
                    delay(10)
                }
            }

        }
    }.start(wait = true)
}