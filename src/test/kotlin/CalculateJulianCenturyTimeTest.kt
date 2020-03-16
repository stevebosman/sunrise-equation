package uk.co.stevebosman.sunrise

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CalculateJulianCenturyTimeTest {
    private val JDATE_BASE_1_JAN_2000_MIDDAY = 2451545.0

    @Test
    fun whenMidday1Jan2000Returns0() {
        val actual =
            calculateTimeJulianCentury(JDATE_BASE_1_JAN_2000_MIDDAY)
        Assertions.assertEquals(0.0, actual)
    }

    @Test
    fun whenMidday1Jan2004Returns4Percent() {
        val actual =
            calculateTimeJulianCentury(JDATE_BASE_1_JAN_2000_MIDDAY + (365.25 * 4))
        Assertions.assertEquals(0.04, actual)
    }
}