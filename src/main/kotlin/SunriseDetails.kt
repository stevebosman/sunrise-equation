package uk.co.stevebosman.sunrise

import uk.co.stevebosman.sunrise.DaylightType
import java.time.ZonedDateTime

class SunriseDetails(
    val daylightType: DaylightType,
    val solarNoonTime: ZonedDateTime,
    val sunrise: ZonedDateTime,
    val sunset: ZonedDateTime
)