package uk.co.stevebosman.sunrise

import java.time.ZonedDateTime

class SunriseDetails(
    val daylightType: DaylightType,
    val solarNoonTime: ZonedDateTime,
    val sunriseTime: ZonedDateTime,
    val sunsetTime: ZonedDateTime,
    val moonPhase: Double
)