package com.muzzley.app.location

import java.util.*

class Loc(
        latitude: Double,
        longitude: Double,
        provider: String?,
        timestamp: Date,
        elapsed_realtime_nanos: Long,
        device_id: String?,
        horizontal_accuracy: Float?,
        altitude: Double?,
        bearing: Float?,
        speed: Float?,
        satellites: Int?,
        total_satellites: Int?,
        vertical_accuracy: Float?,
        bearing_accuracy: Float?,
        speed_accuracy: Float?
)
