import classes.Mapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadImage
import org.openrndr.extra.fx.color.ColorCorrection
import org.openrndr.extra.imageFit.imageFit
import org.openrndr.extra.jumpfill.ShapeSDF
import org.openrndr.extra.shapes.primitives.Tear
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.IntRectangle
import org.openrndr.shape.Rectangle
import org.w3c.dom.css.Rect
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import kotlin.math.PI


fun main() {
    application {
        configure {
            width = 1080
            height = 1920
            hideWindowDecorations = true
           // position = IntVector2(0, -400)
            display = displays[1]
        }

        program {

            var debug = true

            val img = loadImage("data/images/wall.jpg")
            val cc = ColorCorrection()
            cc.brightness = 0.2
            cc.contrast = 0.5
            cc.apply(img, img)


            val serverUrl = "http://localhost:5000"

            val httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()

            val json = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }

            suspend fun getRectangles(): TrackingResponse? {
                return withContext(Dispatchers.IO) {
                    try {
                        val request = HttpRequest.newBuilder()
                            .uri(URI.create("$serverUrl/rectangles"))
                            .timeout(Duration.ofSeconds(3))
                            .GET()
                            .build()

                        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

                        if (response.statusCode() == 200) {
                            json.decodeFromString<TrackingResponse>(response.body())
                        } else {
                            println("HTTP Error: ${response.statusCode()}")
                            null
                        }
                    } catch (e: Exception) {
                        println("Error fetching rectangles: ${e.message}")
                        null
                    }
                }
            }

            fun startTracking(updateIntervalMs: Long = 100, onUpdate: (List<PersonRectangle>) -> Unit) {
                CoroutineScope(Dispatchers.IO).launch {
                    while (true) {
                        val response = getRectangles()
                        response?.let {
                            onUpdate(it.rectangles)
                        }
                        delay(updateIntervalMs)
                    }
                }
            }
            var currentRectangles = listOf<Rectangle>()

         /*   startTracking(updateIntervalMs = 100) { rectangles ->
                currentRectangles = rectangles.map {
                    IntRectangle(it.x, it.y, it.w, it.h).rectangle
                }
            }*/


            val mapper = extend(Mapper()) {
                pMap {
                    mapperElement("ola", Rectangle.fromCenter(drawer.bounds.center, 1080.0, 2008.0).contour) {
                        drawer.imageFit(img, this.drawer.bounds)
                    }
                }
            }

            extend {

                if (debug) {
                    currentRectangles = listOf(
                        Rectangle.fromCenter(Vector2(mouse.position.x , 0.0), 300.0, 500.0).scaledBy(
                            0.5 + 0.5 * (mouse.position.y / height)
                        )
                    )
                }


                mapper.elements.values.first().rectangles = currentRectangles
                drawer.stroke = ColorRGBa.RED
                drawer.rectangles(currentRectangles)

            }
        }
    }
}