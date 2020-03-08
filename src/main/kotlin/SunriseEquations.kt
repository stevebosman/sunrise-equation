package uk.co.stevebosman.sunrise

import uk.co.stevebosman.angles.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

val J2000_EPOCH_DATE:ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(2000, 1, 1),
    LocalTime.of(12, 0),
    ZoneId.of("UTC")
)
const val J2000_EPOCH = 2451545
const val DAYS_PER_CENTURY = 36525.0
/**
 * Used to take into account refraction
 */
val SolarZenithAtSunRiseSunSet = Angle.fromDegrees(90, 50.0)
private const val MINUTES_PER_DAY = 24.0 * 60.0

private const val SECONDS_PER_DAY = 24 * 60 * 60
private const val MILLISECONDS_PER_DAY = SECONDS_PER_DAY * 1000.0
private const val TT_OFFSET = 69.184/(SECONDS_PER_DAY)

/**
 * The Julian date (JD) of any instant is the Julian day number for the preceding noon in Universal Time
 * plus the fraction of the day since that instant.
 * Calculate the continuous count of days since the beginning of the Julian Period, see https://en.wikipedia.org/wiki/Julian_day
 * @param dateTime
 * @return continuous count of days since the beginning of the Julian Period
 */
fun calculateJulianDate(dateTime: ZonedDateTime): Double {
    val days =
        ChronoUnit.DAYS.between(J2000_EPOCH_DATE.truncatedTo(ChronoUnit.DAYS), dateTime.truncatedTo(ChronoUnit.DAYS))
    val adjustedDay = dateTime.withYear(2000).withDayOfYear(1)
    val millis = ChronoUnit.MILLIS.between(J2000_EPOCH_DATE, adjustedDay)
    return J2000_EPOCH + days + (millis / MILLISECONDS_PER_DAY)
}

/**
 * Days since 1-Jan-2000, 12:00
 * @param dateTime date time
 * @return Julian Day
 */
fun calculateCurrentJulianDay(dateTime: ZonedDateTime) = calculateJulianDate(dateTime).toInt() - J2000_EPOCH + TT_OFFSET

/**
 * Determine sunrise or sunset time for the given day at the given latitude or longitude
 * @param rise true - calculate sunrise; otherwise calculate sunset
 * @param dateTime date time to calculate for
 * @param latitude latitude on earth
 * @param longitude longitude on earth
 * @param zone time zone
 * @return Sunrise or sunset time for given day
 */
fun calculateSunriseSetUTCTime(rise: Boolean, dateTime: ZonedDateTime, latitude: Angle, longitude: Angle, zone: ZoneId? = null): ZonedDateTime? {
    val jd = calculateJulianDate(dateTime)
    val timeUTC = calculateSunriseSetUTC(rise, jd, latitude, longitude)
    val currentJulianDay = calculateCurrentJulianDay(dateTime)
    val cjd = currentJulianDay + (timeUTC / MINUTES_PER_DAY)
    val seconds = ((cjd % 1) * SECONDS_PER_DAY).toLong()
    return J2000_EPOCH_DATE.plusDays(cjd.toLong()).plusSeconds(seconds - (SECONDS_PER_DAY / 2)).withZoneSameInstant(zone ?: dateTime.zone)
}

/**
 * Determine minutes since midnight of sunrise or sunset for the given day at the given latitude or longitude
 * @param rise true - calculate sunrise; otherwise calculate sunset
 * @param jd Julian Date
 * @param latitude latitude on earth
 * @param longitude longitude on earth
 * @return Sunrise or sunset time for given day in minutes since midnight
 */
fun calculateSunriseSetUTC(rise: Boolean, jd: Double, latitude: Angle, longitude: Angle): Double {
    val t = calculateTimeJulianCentury(jd)
    val eqTime = calculateEquationOfTime(t)
    val solarDec = calculateSunDeclination(t)
    var hourAngle = calculateHourAngleSunrise(latitude, solarDec)
    if (!rise) hourAngle = -hourAngle
    val delta = longitude + hourAngle
    return 720 - (4.0 * delta.degrees) - eqTime
}

/**
 * Julian centuries since J2000.0 from Julian day
 * @param jd Julian Day
 * @return Julian Century time
 */
fun calculateTimeJulianCentury(jd: Double): Double {
    return (jd - J2000_EPOCH) / DAYS_PER_CENTURY
}

/**
 * Geometric mean longitude of the sun.
 * @param t Julian centuries since J2000.0
 * @return Geometric mean longitude of the sun.
 */
fun calculateGeometricMeanLongitudeSun(t: Double): Angle {
    val l0 = 280.46646 + t * (36000.76983 + t * (0.0003032))
    return Angle.fromDegrees(l0).simplify()
}

/**
 * Geometric mean anomaly of the sun.
 * @param t Julian centuries since J2000.0
 * @return Geometric mean anomaly
 */
fun calculateGeometricMeanAnomalySun(t: Double): Angle {
    val m = 357.52911 + t * (35999.05029 - 0.0001537 * t)
    return Angle.fromDegrees(m)
}

/**
 * Eccentricity of Earth's orbit
 * @param t Julian centuries since J2000.0
 * @return eccentricity
 */
fun calculateEccentricityEarthOrbit(t: Double): Double {
    return 0.016708634 - t * (0.000042037 + 0.0000001267 * t)
}

/**
 * Equation of center of the sun.
 * @param t Julian centuries since J2000.0
 * @return Center
 */
fun calculateSunEquationOfCenter(t: Double): Angle {
    val m = calculateGeometricMeanAnomalySun(t)
    val sinm = sin(m)
    val sin2m = sin(m + m)
    val sin3m = sin(m + m + m)
    val c = sinm * (1.914602 - t * (0.004817 + 0.000014 * t)) + sin2m * (0.019993 - 0.000101 * t) + sin3m * 0.000289
    return Angle.fromDegrees(c)
}

/**
 * True longitude of the sun.
 * @param t Julian centuries since J2000.0
 * @return True longitude
 */
fun calculateSunTrueLongitude(t: Double): Angle {
    val l0 = calculateGeometricMeanLongitudeSun(t)
    val c = calculateSunEquationOfCenter(t)
    return l0 + c
}

/**
 * Apparent longitude of the sun. (Right ascension).
 * @param t Julian centuries since J2000.0
 * @return Apparent longitude
 */
fun calculateSunApparentLongitude(t: Double): Angle {
    val o = calculateSunTrueLongitude(t)
    val omega = Angle.fromDegrees(125.04 - 1934.136 * t)
    val lambda = o.degrees - 0.00569 - 0.00478 * sin(omega)
    return Angle.fromDegrees(lambda)
}

/**
 * Mean inclination of Earth's equator with respect to the ecliptic,
 * i.e. ignoring nutation of the equator.
 * See https://en.wikipedia.org/wiki/Ecliptic#Obliquity_of_the_ecliptic
 * @param t Julian centuries since J2000.0
 */
fun calculateMeanObliquityOfEcliptic(t: Double): Angle {
    val seconds = 21.448 - t * (46.8150 + t * (0.00059 - t * (0.001813)))
    return Angle.fromDegrees(23, 26.0, seconds)
}

/**
 * Corrected obliquity of the ecliptic.
 * @param t Julian centuries since J2000.0
 * @return Corrected obliquity
 */
fun calculateObliquityCorrection(t: Double): Angle {
    val e0 = calculateMeanObliquityOfEcliptic(t)
    val omega = Angle.fromDegrees(125.04 - 1934.136 * t)
    val e = e0.degrees + 0.00256 * cos(omega)
    return Angle.fromDegrees(e)        
}

/**
 * Declination of the sun,
 * i.e. latitude at which the sun is directly overhead
 * @param t Julian centuries since J2000.0
 * @return declination
 */
fun calculateSunDeclination(t: Double): Angle {
    val e = calculateObliquityCorrection(t)
    val lambda = calculateSunApparentLongitude(t)

    val sint = sin(e) * sin(lambda)
    return asin(sint)
}

/**
 * Difference between true solar time and mean solar time.
 * @param t Julian centuries since J2000.0
 * @return minutes of time
 */
fun calculateEquationOfTime(t: Double): Double {
    val epsilon = calculateObliquityCorrection(t)
    val l0 = calculateGeometricMeanLongitudeSun(t)
    val e = calculateEccentricityEarthOrbit(t)
    val m = calculateGeometricMeanAnomalySun(t)

    var y = tan(epsilon / 2.0)
    y *= y

    val sin2l0 = sin(2.0 * l0)
    val sinm = sin(m)
    val cos2l0 = cos(2.0 * l0)
    val sin4l0 = sin(4.0 * l0)
    val sin2m = sin(2.0 * m)

    val eTime = y * sin2l0 - 2.0 * e * sinm + 4.0 * e * y * sinm * cos2l0 - 0.5 * y * y * sin4l0 - 1.25 * e * e * sin2m
    return radiansToDegrees(eTime) * 4.0
}

/**
 * Hour Angle for sunrise at location on earth.
 * @param latitude Latitude of location
 * @param solarDeclination declination
 * @return Hour Angle
 */
fun calculateHourAngleSunrise(latitude: Angle, solarDeclination: Angle): Angle {
    val hourAngleArg = (cos(SolarZenithAtSunRiseSunSet) / (cos(latitude) * cos(solarDeclination)) - tan(latitude) * tan(solarDeclination))
    return acos(hourAngleArg)
}

