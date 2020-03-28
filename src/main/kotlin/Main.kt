package uk.co.stevebosman.sunrise
import uk.co.stevebosman.angles.Angle
import java.time.ZoneId
import java.time.ZonedDateTime

fun main() {
    println("Near North Pole")
    calculate(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")), Angle.fromDegrees(-1.8904), Angle.fromDegrees(89))
    println("Birmingham")
    calculate(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Europe/London")), Angle.fromDegrees(-1.8904), Angle.fromDegrees(52.4862) )
    println("Near South Pole")
    calculate(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")), Angle.fromDegrees(-1.8904), Angle.fromDegrees(-89))
}

fun calculate(date: ZonedDateTime, longitude:Angle, latitude:Angle) {
    val details = calculateSunriseDetails(date, longitude, latitude)
    println("Type: ${details.daylightType}")
    println("SolNoon: ${details.solarNoonTime}")
    println("Sunrise: ${details.sunriseTime}")
    println("Sunset: ${details.sunsetTime}")
}

