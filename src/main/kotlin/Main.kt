package uk.co.stevebosman.sunrise
import uk.co.stevebosman.angles.Angle
import java.time.ZoneId
import java.time.ZonedDateTime

fun main() {
    println("Near North Pole")
    calculate(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")), Angle.fromDegrees(-1.7), Angle.fromDegrees(86))
    println("Birmingham")
    calculate(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Europe/London")), Angle.fromDegrees(-1.8904), Angle.fromDegrees(52.4862) )
    println("Birmingham 2")
    calculate(ZonedDateTime.now().plusDays(1).withZoneSameInstant(ZoneId.of("Europe/London")), Angle.fromDegrees(-1.8904), Angle.fromDegrees(52.4862) )
    println("Birmingham 3")
    calculate(ZonedDateTime.now().plusDays(2).withZoneSameInstant(ZoneId.of("Europe/London")), Angle.fromDegrees(-1.8904), Angle.fromDegrees(52.4862) )
    println("Birmingham 4")
    calculate(ZonedDateTime.now().plusDays(3).withZoneSameInstant(ZoneId.of("Europe/London")), Angle.fromDegrees(-1.8904), Angle.fromDegrees(52.4862) )
    println("Near South Pole")
    calculate(ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC")), Angle.fromDegrees(-5), Angle.fromDegrees(-89))
}

fun calculate(date: ZonedDateTime, longitude:Angle, latitude:Angle) {
    val details = calculateSunriseDetails(date, longitude, latitude)
    println("Date: ${date}")
    println("Type: ${details.daylightType}")
    println("SolNoon: ${details.solarNoonTime}")
    println("Sunrise: ${details.sunriseTime}")
    println("Sunset: ${details.sunsetTime}")
    println("*****")
}

