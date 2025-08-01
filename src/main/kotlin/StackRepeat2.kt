import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Filter1to1
import org.openrndr.draw.Filter2to1
import org.openrndr.draw.MagnifyingFilter
import org.openrndr.draw.MinifyingFilter
import org.openrndr.extra.fx.mppFilterShader
import org.openrndr.extra.parameters.Description
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.parameters.Vector2Parameter
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import kotlin.collections.getValue
import kotlin.collections.setValue

@Description("Stack repeat")
class StackRepeat2 : Filter2to1(mppFilterShader(fx_stack_repeat2, "stack-repeat")) {
    @DoubleParameter("zoom", -1.0, 1.0, order = 0)
    var zoom: Double by parameters
    var origin: Vector2 by parameters // Replaces xOrigin, yOrigin
    var offset: Vector2 by parameters // Replaces xOffset, yOffset

    @DoubleParameter("rotation", -180.0, 180.0, order = 5)
    var rotation: Double by parameters

    @IntParameter("repeats", 0, 16, order = 6)
    var repeats: Int by parameters

    @IntParameter("shapeType", 0, 3, order = 7)
    var shapeType: Int by parameters

    @DoubleParameter("shapeSize", 0.0, 1.0, order = 5)
    var shapeSize: Double by parameters

    @DoubleParameter("shapeSize", 0.0, 1.0, order = 5)
    var shapeSmoothness: Double by parameters

    @IntParameter("repeats", 0, 1, order = 6)
    var maskMode: Int by parameters

    @IntParameter("repeats", 0, 4, order = 6)
    var maskChannel: Int by parameters

    @DoubleParameter("maskScale", 0.0, 1.0, order = 5)
    var maskScale: Double by parameters

    @DoubleParameter("maskRotation", 0.0, 1.0, order = 5)
    var maskRotation: Double by parameters

    var time = 0.0

    @DoubleParameter("delayAmount", 0.0, 1.0, order = 5)
    var delayAmount: Double by parameters

    @IntParameter("repeats", 0, 2, order = 6)
    var delayMode: Double by parameters
    // Add this parameter'




    @Vector2Parameter("ss", 0.0, 1920.0, order = 5)
    var screenSize: Vector2 by parameters

    var maskBoundsPosition: Vector2 by parameters
    var maskBoundsSize: Vector2 by parameters

    init {
        screenSize = Vector2(1280.0, 720.0)
        maskBoundsPosition = Vector2.ZERO
        maskBoundsSize = Vector2(100.0, 100.0)

        origin = Vector2.ZERO
        offset = Vector2.ZERO
        zoom = 0.0
        repeats = 10
        shapeSmoothness = 0.05
    }



}
