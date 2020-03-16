package uk.co.stevebosman.sunrise

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class CalculateJulianMidnightDateTest {
    private val JDATE_BASE_1_JAN_2000_MIDNIGHT = 2451544.5
    private val JDATE_BASE_1_JAN_2000_MIDDAY = 2451545.0

    @Test
    fun whenMiddayLondon1Jan2000ReturnsBasePlus0() {
        val actual =
            calculateJulianDate(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 1),
                    LocalTime.of(12, 0),
                    ZoneId.of("Europe/London")
                )
            )
        assertEquals(JDATE_BASE_1_JAN_2000_MIDNIGHT, actual)
    }

    @Test
    fun whenMidnightLondon2Jan2000ReturnsBasePlusOne() {
        val actual =
            calculateJulianDate(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 2),
                    LocalTime.of(0, 0),
                    ZoneId.of("Europe/London")
                )
            )
        assertEquals(JDATE_BASE_1_JAN_2000_MIDNIGHT + 1, actual)
    }

    @Test
    fun whenMiddayLondon2Jan2000ReturnsMidnightBasePlusOne() {
        val actual =
            calculateJulianDate(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 2),
                    LocalTime.of(12, 0),
                    ZoneId.of("Europe/London")
                )
            )
        assertEquals(JDATE_BASE_1_JAN_2000_MIDNIGHT + 1, actual)
    }

    @Test
    fun whenMiddayLondon2Jan2000WithTimeReturnsMiddayBasePlusOne() {
        val actual =
            calculateJulianDate(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 2),
                    LocalTime.of(12, 0),
                    ZoneId.of("Europe/London")
                ),
                true
            )
        assertEquals(JDATE_BASE_1_JAN_2000_MIDDAY + 1, actual)
    }

    @Test
    fun whenMiddayChicago2Jan2000ReturnsBasePlusOne() {
        val actual =
            calculateJulianDate(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 2),
                    LocalTime.of(12, 0),
                    ZoneId.of("America/Chicago")
                )
            )
        assertEquals(JDATE_BASE_1_JAN_2000_MIDNIGHT + 1, actual)
    }

    @Test
    fun whenMiddayChicago2Jan2000WithTimeReturnsBasePlusOne() {
        val actual =
            calculateJulianDate(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 2),
                    LocalTime.of(12, 0),
                    ZoneId.of("America/Chicago")
                ), true
            )
        assertEquals(JDATE_BASE_1_JAN_2000_MIDDAY + 30.0/24, actual)
    }
}
