package lab

import lib.cross2d
import lib.inverseBilinear
import lib.sortedClockwise
import org.openrndr.application
import org.openrndr.draw.defaultFontMap
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.olive.OliveScriptHost
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Polar
import org.openrndr.shape.Rectangle
import org.openrndr.shape.ShapeContour
import kotlin.math.sin
import kotlin.random.Random

fun main() = application {
    configure {
        width = 960
        height = 720
    }
    oliveProgram(scriptHost = OliveScriptHost.JSR223) {

        val points = Rectangle.fromCenter(drawer.bounds.center, 200.0, 200.0).contour.segments.map { it.start }
        val img = loadImage("data/images/cheeta.jpg")

        extend {

           // val mPositions = points.reversed()

            val mPositions = points.mapIndexed { i, it ->
                val pol = Polar(
                    seconds + Double.uniform(0.0, 10.0, Random(i)),
                    sin(seconds + Double.uniform(0.0, 60.0, Random(i))) * 100.0 + 150.0
                )
                it + pol.cartesian
            }.reversed()

            val shape = ShapeContour.fromPoints(mPositions, true).shape

            drawer.shadeStyle = shadeStyle {

                fragmentPreamble = cross2d + inverseBilinear

                fragmentTransform = """
                      vec2 uv = invBilinear(va_position, p_pos[0], p_pos[1], p_pos[2], p_pos[3]);
                      
                      if( uv.x > -0.5 ) {  
                        x_fill.xyz = texture(p_img, uv).xyz; 
                      }

                      x_fill = texture(p_img, uv.xy);
                    """.trimIndent()
                parameter("img", img)
                parameter("pos", mPositions.toTypedArray())
            }


            drawer.shape(shape)

            drawer.shadeStyle = null
            drawer.fontMap = defaultFontMap
            mPositions.forEachIndexed { i, v -> drawer.text("$i", v) }


        }
    }
}