package uk.co.stevebosman.sunrise

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class CalculateJulianDateTest {
    private val JDATE_BASE_1_JAN_2000 = 2451545.0

    @Test
    fun whenMiddday1Jan2020ReturnsBasePlus0() {
        val actual =
            calculateJulianDate(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 1),
                    LocalTime.of(12, 0),
                    ZoneId.of("Europe/London")
                )
            )
        assertEquals(JDATE_BASE_1_JAN_2000, actual)
    }

    @Test
    fun whenMidnight2Jan2020ReturnsBasePlusHalf() {
        val actual =
            calculateJulianDate(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 2),
                    LocalTime.of(0, 0),
                    ZoneId.of("Europe/London")
                )
            )
        assertEquals(JDATE_BASE_1_JAN_2000 + 0.5, actual)
    }

    @Test
    fun whenMiddday2Jan2020ReturnsBasePlus1() {
        val actual =
            calculateJulianDate(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 2),
                    LocalTime.of(12, 0),
                    ZoneId.of("Europe/London")
                )
            )
        assertEquals(JDATE_BASE_1_JAN_2000 + 1, actual)
    }

    @Test
    fun whenMidddayNewYork2Jan2020ReturnsBasePlus1() {
        val actual =
            calculateJulianDate(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 2),
                    LocalTime.of(12, 0),
                    ZoneId.of("America/Chicago")
                )
            )
        assertEquals(JDATE_BASE_1_JAN_2000 + 1.25, actual)
    }
}
