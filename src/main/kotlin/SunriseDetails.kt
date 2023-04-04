package uk.co.stevebosman.sunrise

import java.time.ZonedDateTime

class SunriseDetails(
    val sunriseType: DaylightType,
    val sunsetType: DaylightType,
    val solarNoonTime: ZonedDateTime,
    val sunriseTime: ZonedDateTime,
    val sunsetTime: ZonedDateTime,
    val moonPhase: Double
) {
    override fun toString(): String {
        return "SunriseDetails(sunriseType=$sunriseType, sunsetType=$sunsetType, solarNoonTime=$solarNoonTime, sunriseTime=$sunriseTime, sunsetTime=$sunsetTime, moonPhase=$moonPhase)"
    }
}