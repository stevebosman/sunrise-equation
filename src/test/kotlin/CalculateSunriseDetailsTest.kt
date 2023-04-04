package uk.co.stevebosman.sunrise

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.co.stevebosman.angles.Angle
import uk.co.stevebosman.difference.isClose
import java.time.ZonedDateTime

class CalculateSunriseDetailsTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "2023-01-01T10:15:30+01:00[Europe/Paris], 48.8566, 2.3522, NORMAL, 2023-01-01T08:44:03+01:00[Europe/Paris], 2023-01-01T17:04:11+01:00[Europe/Paris], 2023-01-01T12:54:02.130+01:00[Europe/Paris], 0.3210294615644784",
            "2023-01-01T10:15:30+01:00, 88, 2.3522, POLAR_NIGHT, 2023-03-14T10:55:19+01:00, 2022-09-29T14:43:47+01:00, 2023-01-01T12:54:02.130+01:00, 0.3210294615644784",
            "2023-01-01T10:15:30+01:00, -88, 2.3522, MIDNIGHT_SUN, 2022-09-26T01:08:13+01:00, 2023-03-18T22:29:07+01:00, 2023-01-01T12:54:02.130+01:00, 0.3210294615644784",
        ]
    )
    fun verifyCalculateSunriseDetails(
        dateText: String,
        latitude: Double,
        longitude: Double,
        expectedDaylightTypeText: String,
        expectedSunriseTimeText: String,
        expectedSunsetTimeText: String,
        expectedSolarNoonTimeText: String,
        expectedMoonPhase: Double,
    ) {
        val date = ZonedDateTime.parse(dateText)
        val expectedSunriseTime = ZonedDateTime.parse(expectedSunriseTimeText)
        val expectedSunsetTime = ZonedDateTime.parse(expectedSunsetTimeText)
        val expectedSolarNoonTime = ZonedDateTime.parse(expectedSolarNoonTimeText)
        val expectedSunsetType = DaylightType.valueOf(expectedDaylightTypeText.replaceBefore("/", "").trim('/'))
        val expectedSunriseType = DaylightType.valueOf(expectedDaylightTypeText.replaceAfter("/", "").trim('/'))

        val lat = Angle.fromDegrees(latitude)
        val long = Angle.fromDegrees(longitude)
        val actual = calculateSunriseDetails(date, long, lat)
        println("sunrise ($date): $actual")

        Assertions.assertEquals(expectedSunriseType, actual.sunriseType)
        Assertions.assertEquals(expectedSunsetType, actual.sunsetType)
        Assertions.assertEquals(expectedSunriseTime, actual.sunriseTime)
        Assertions.assertEquals(expectedSunsetTime, actual.sunsetTime)
        Assertions.assertEquals(expectedSolarNoonTime, actual.solarNoonTime)
        Assertions.assertTrue(isClose(expectedMoonPhase, actual.moonPhase))
    }
}