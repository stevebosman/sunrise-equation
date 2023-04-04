package uk.co.stevebosman.sunrise

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.co.stevebosman.angles.Angle

class CalculateSunriseSunsetTest {
    @ParameterizedTest
    @CsvSource(
        value = [
            "-7, 0, 70, 670, 765, NORMAL",
            "-6, 0, 70, 687, 748, NORMAL",
            "-5, 0, 70, 88547, -691, POLAR_NIGHT",
            "-4, 0, 70, 87107, -2131, POLAR_NIGHT",
            "-3, 0, 70, 85667, -3571, POLAR_NIGHT",
            "54, 0, 70, 3587, -85651, POLAR_NIGHT",
            "55, 0, 70, 2147, -87091, POLAR_NIGHT",
            "56, 0, 70, 707, 765, NORMAL",
            "57, 0, 70, 691, 782, NORMAL",
            "173, 0, 70, 33, 1386, NORMAL",
            "174, 0, 70, 16, 109400, NORMAL/MIDNIGHT_SUN",
            "175, 0, 70, -1423, 107960, MIDNIGHT_SUN",
            "176, 0, 70, -2863, 106520, MIDNIGHT_SUN",
            "247, 0, 70, -105103, 4280, MIDNIGHT_SUN",
            "248, 0, 70, -106543, 2840, MIDNIGHT_SUN", // feels wrong
            "249, 0, 70, 25, 1401, NORMAL", // feels wrong
            "250, 0, 70, 48, 1386, NORMAL",
            "359, 0, 70, 682, 753, NORMAL",
            "360, 0, 70, 89974, -686, POLAR_NIGHT",
            "361, 0, 70, 88534, -2126, POLAR_NIGHT",
            "362, 0, 70, 87094, -3566, POLAR_NIGHT",
            "420, 0, 70, 3574, -87086, POLAR_NIGHT",
            "421, 0, 70, 2134, -88526, POLAR_NIGHT",
            "422, 0, 70, 694, 778, NORMAL",
            "423, 0, 70, 681, 791, NORMAL",

            "-7, 180, 70, -55, 51, NORMAL",
            "-6, 180, 70, -41, 37, NORMAL",
            "-5, 180, 70, -19, 16, NORMAL",
            "-4, 180, 70, 86401, -1423, POLAR_NIGHT",
            "-3, 180, 70, 84961, -2863, POLAR_NIGHT",
            "54, 180, 70, 2881, -84943, POLAR_NIGHT",
            "55, 180, 70, 1441, -86383, POLAR_NIGHT",
            "56, 180, 70, 1, 31, NORMAL",
            "57, 180, 70, -21, 54, NORMAL",
            "173, 180, 70, -680, 657, NORMAL",
            "174, 180, 70, -694, 679, NORMAL",
            "175, 180, 70, -2134, 107250, MIDNIGHT_SUN",
            "176, 180, 70, -3574, 105810, MIDNIGHT_SUN",
            "247, 180, 70, -105814, 3570, MIDNIGHT_SUN",
            "248, 180, 70, -107254, 2130, MIDNIGHT_SUN", // feels wrong
            "249, 180, 70, -108694, 690, MIDNIGHT_SUN/NORMAL", // feels wrong
            "250, 180, 70, -680, 673, NORMAL",
            "359, 180, 70, -45, 41, NORMAL",
            "360, 180, 70, -26, 23, NORMAL",
            "361, 180, 70, 87822, -1416, POLAR_NIGHT",
            "362, 180, 70, 86382, -2856, POLAR_NIGHT",
            "420, 180, 70, 2862, -86376, POLAR_NIGHT",
            "421, 180, 70, 1422, -87816, POLAR_NIGHT",
            "422, 180, 70, -17, 50, NORMAL",
            "423, 180, 70, -32, 65, NORMAL",

            "0, 0, 0, 356, 1083, NORMAL",
            "60, 0, 0, 372, 1099, NORMAL",
            "120, 0, 0, 354, 1080, NORMAL",
            "180, 0, 0, 338, 1065, NORMAL",
            "240, 0, 0, 358, 1085, NORMAL",
            "300, 0, 0, 358, 1084, NORMAL",
            "360, 0, 0, 355, 1082, NORMAL"
        ]
    )
    fun calculateRefinedSunriseUtc(
        julianMidnight: Double,
        longitude: Double,
        latitude: Double,
        sunriseMinutesSinceMidnight: Int,
        sunsetMinutesSinceMidnight: Int,
        daylightTypeText: String,
    ) {
        val expectedSunsetType = DaylightType.valueOf(daylightTypeText.replaceBefore("/", "").trim('/'))
        val expectedSunriseType = DaylightType.valueOf(daylightTypeText.replaceAfter("/", "").trim('/'))

        val actualSunrise = calculateRefinedSunriseSetUTC(
            true, julianMidnight, Angle.fromDegrees(latitude), Angle.fromDegrees(longitude)
        )
        println("sunrise ($julianMidnight): $actualSunrise")

        val actualSunset = calculateRefinedSunriseSetUTC(
            false, julianMidnight, Angle.fromDegrees(latitude), Angle.fromDegrees(longitude)
        )
        println("sunset ($julianMidnight): $actualSunset")

        Assertions.assertEquals(sunriseMinutesSinceMidnight, actualSunrise.first.toInt())
        Assertions.assertEquals(sunsetMinutesSinceMidnight, actualSunset.first.toInt())

        Assertions.assertEquals(expectedSunriseType, actualSunrise.second)
        Assertions.assertEquals(expectedSunsetType, actualSunset.second)
    }
}