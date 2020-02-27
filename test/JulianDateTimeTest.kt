import org.junit.jupiter.api.Test
import uk.co.stevebosman.sunrise.calculateJulianDate
import java.time.*
import kotlin.test.assertEquals

internal class JulianDateTimeTest {
    private val JDATE_BASE_1_JAN_2020 = 2451545.0

    @Test
    fun whenMiddday1Jan2020ReturnsBasePlus0() {
        val actual =
            calculateJulianDate(ZonedDateTime.of(LocalDate.of(2020, 1, 1), LocalTime.of(12, 0), ZoneId.of("Europe/London")))
        assertEquals(JDATE_BASE_1_JAN_2020, actual);
    }
    @Test
    fun whenMidnight2Jan2020ReturnsBasePlusHalf() {
        val actual =
            calculateJulianDate(ZonedDateTime.of(LocalDate.of(2020, 1, 2), LocalTime.of(0, 0), ZoneId.of("Europe/London")))
        assertEquals(JDATE_BASE_1_JAN_2020 + 0.5, actual);
    }
    @Test
    fun whenMiddday2Jan2020ReturnsBasePlus1() {
        val actual =
            calculateJulianDate(ZonedDateTime.of(LocalDate.of(2020, 1, 2), LocalTime.of(12, 0), ZoneId.of("Europe/London")))
        assertEquals(JDATE_BASE_1_JAN_2020 + 1, actual);
    }
    @Test
    fun whenMidddayNewYork2Jan2020ReturnsBasePlus1() {
        val actual =
            calculateJulianDate(ZonedDateTime.of(LocalDate.of(2020, 1, 2), LocalTime.of(12, 0), ZoneId.of("America/Chicago")))
        assertEquals(JDATE_BASE_1_JAN_2020 + 1.25, actual);
    }
}