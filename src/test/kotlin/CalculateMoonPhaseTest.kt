package uk.co.stevebosman.sunrise

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class CalculateMoonPhaseTest {
    @Test
    fun whenMiddayLondon1Jan2000ReturnsBasePlus0() {
        val actual =
            calculateMoonPhase(
                ZonedDateTime.of(
                    LocalDate.of(2000, 1, 6),
                    LocalTime.of(12, 24, 1),
                    ZoneId.of("Europe/London")
                )
            )
        Assertions.assertEquals(0.0, actual)
    }

}