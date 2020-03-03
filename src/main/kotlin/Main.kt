import uk.co.stevebosman.angles.Angle
import uk.co.stevebosman.sunrise.calculateCurrentJulianDay
import uk.co.stevebosman.sunrise.calculateJulianDate
import uk.co.stevebosman.sunrise.calculateSunriseSetUTC
import uk.co.stevebosman.sunrise.calculateSunriseSetUTCTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun main() {
    val now = ZonedDateTime.now()
    val julianDate = calculateJulianDate(now)
    println("JulianDate: $julianDate")
    println("CurrentJulianDay: " + calculateCurrentJulianDay(now))
    println("Sunrise: " + calculateSunriseSetUTC(true, julianDate, Angle.fromDegrees(52.4862), Angle.fromDegrees(-1.8904)) / 60)
    println("Sunset: " + calculateSunriseSetUTC(false, julianDate, Angle.fromDegrees(52.4862), Angle.fromDegrees(-1.8904)) / 60)
    println("Sunrise: " + calculateSunriseSetUTCTime(true, now, Angle.fromDegrees(52.4862), Angle.fromDegrees(-1.8904)))
    println("Sunset: " + calculateSunriseSetUTCTime(false, now, Angle.fromDegrees(52.4862), Angle.fromDegrees(-1.8904)))
    println("Sunrise: " + calculateSunriseSetUTCTime(true, now, Angle.fromDegrees(64.1466), Angle.fromDegrees(21.9426), ZoneId.of("Atlantic/Reykjavik")))
    println("Sunset: " + calculateSunriseSetUTCTime(false, now, Angle.fromDegrees(64.1466), Angle.fromDegrees(21.9426), ZoneId.of("Atlantic/Reykjavik")))
    println("Sunrise: " + calculateSunriseSetUTCTime(true, now, Angle.fromDegrees(34.0522), Angle.fromDegrees(-118.2437), ZoneId.of("America/Los_Angeles")))
    println("Sunset: " + calculateSunriseSetUTCTime(false, now, Angle.fromDegrees(34.0522), Angle.fromDegrees(-118.2437), ZoneId.of("America/Los_Angeles")))
    println("Sunrise: " + calculateSunriseSetUTCTime(true, now, Angle.fromDegrees(5.6037), Angle.fromDegrees(0.1870), ZoneId.of("Africa/Accra")))
    println("Sunset: " + calculateSunriseSetUTCTime(false, now, Angle.fromDegrees(5.6037), Angle.fromDegrees(0.1870), ZoneId.of("Africa/Accra")))
}
