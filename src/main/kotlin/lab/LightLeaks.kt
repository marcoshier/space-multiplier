package lab

import RabbitControlServer
import lib.Pixelate
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.fx.Post
import org.openrndr.extra.fx.blur.LaserBlur
import org.openrndr.extra.fx.distort.Perturb
import org.openrndr.extra.fx.dither.ADither
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.addTo
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.viewbox.viewBox
import org.openrndr.math.Vector2
import org.openrndr.poissonfill.PoissonFill
import org.openrndr.shape.LineSegment
import kotlin.math.cos
import kotlin.math.sin

fun main() = application {
    configure {
        width = 720
        height = 720
        windowAlwaysOnTop = true
    }

    oliveProgram {
        val vb = viewBox(drawer.bounds) {
            lightLeaks() // directly inside mapperElement
        }

        extend {
            vb.draw()
        }
    }
}

fun Program.lightLeaks(showRC: Boolean = true) {

    class Stage {

        @IntParameter("numero luci", 1, 20)
        var n = 5

        @DoubleParameter("radius", 0.1, 20.0)
        var radius = 5.0

        @DoubleParameter("larghezza", 0.0, 400.0)
        var xOffset = 10.0

        @DoubleParameter("buio", 0.0, 300.0)
        var strokeWeight = 2.0

        @DoubleParameter("movimento Y", 0.0, 1.0)
        var movY = 0.5

        @DoubleParameter("velocità movimento Y", 0.0, 300.0)
        var speedY = 2.0

        @DoubleParameter("variazione movimento Y", 0.0, 50.0, precision = 3)
        var offsetY = 2.0

        @DoubleParameter("movimento X", 0.0, 1.0)
        var movX = 0.5

        @DoubleParameter("velocità movimento X", 0.0, 300.0)
        var speedX = 2.0

        @DoubleParameter("variazione movimento X", 0.0, 50.0, precision = 3)
        var offsetX = 2.0

        @BooleanParameter("dithering")
        var dither = false

        fun draw() {
            drawer.clear(ColorRGBa.TRANSPARENT)
            var origins = listOf(drawer.bounds.center)
            if (n > 1) {
                val bounds = drawer.bounds.offsetEdges(-xOffset)
                origins = LineSegment(
                    bounds.x,
                    height / 2.0,
                    bounds.x + bounds.width,
                    height / 2.0).contour.equidistantPositions(n).mapIndexed { i, it ->
                    it.copy(
                        x = it.x + (cos(seconds * speedX + i * offsetX) * (width / 2.0)) * movX,
                        y = sin(seconds * speedY + i * offsetY) * (height / 2.0) * movY + (height / 2.0) )
                }
            }

            drawer.stroke = null
            for (p in origins) {
                drawer.stroke = null
                drawer.fill = ColorRGBa.WHITE
                drawer.circle(p, radius)
            }

            drawer.strokeWeight = strokeWeight
            drawer.stroke = ColorRGBa.BLACK
            drawer.fill = null
            drawer.rectangle(drawer.bounds)
        }
    }

    val rc = RabbitControlServer()

    val stage = Stage().also { rc.add(it) }
    val pf = PoissonFill()
    val lb = LaserBlur().also { rc.add(it) }
    val pix = Pixelate().also { rc.add(it) }
    val pert = Perturb().also { rc.add(it) }

    if (showRC) {
        extend(rc)
    }
    extend(Post()) {
        post { input, output ->
            val i0 = intermediate[0]
            val i1 = intermediate[1]
            pf.apply(input, i0)
            pert.apply(i0, i1)
            if(stage.dither || pix.resolution == 0.0) {
                val i2 = intermediate[2]
                lb.apply(i1, i2)
                pix.apply(i2, output)
            } else {
                lb.apply(i1, output)
            }

        }
    }

    extend {

        stage.draw()

    }
}