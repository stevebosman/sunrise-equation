package uk.co.stevebosman.sunrise

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class CalculateCurrentJulianDayTest {
companion object {
    const val TT_OFFSET = 69.184 / (24 * 3600)
}

    @Test
    fun whenStartOfJ2000EpochThenTTOffset() {
        assertEquals(
            TT_OFFSET,
            calculateCurrentJulianDay(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 1),
                    LocalTime.of(12, 0),
                    ZoneId.of("Europe/London")
                )
            )
        )
    }
    @Test
    fun whenWithinOneDayOfStartOfJ2000EpochThenTTOffset() {
        assertEquals(
            TT_OFFSET,
            calculateCurrentJulianDay(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 2),
                    LocalTime.of(11, 59,59),
                    ZoneId.of("Europe/London")
                )
            )
        )
    }
}