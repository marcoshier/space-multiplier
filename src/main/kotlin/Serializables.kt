import kotlinx.serialization.Serializable

@Serializable
data class PersonRectangle(
    val id: Int,
    val x: Int,
    val y: Int,
    val w: Int,
    val h: Int,
    val center_x: Int,
    val center_y: Int
)

@Serializable
data class TrackingResponse(
    val timestamp: Double,
    val frame_count: Int,
    val person_count: Int,
    val rectangles: List<PersonRectangle>
)
