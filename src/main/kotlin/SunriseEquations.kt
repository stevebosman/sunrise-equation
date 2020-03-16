package uk.co.stevebosman.sunrise

import uk.co.stevebosman.angles.*
import uk.co.stevebosman.close.isClose
import java.time.*
import java.time.temporal.ChronoUnit
import kotlin.math.floor

private val J2000_EPOCH_DATE: ZonedDateTime = ZonedDateTime.of(
    LocalDate.of(2000, 1, 1),
    LocalTime.of(12, 0),
    ZoneId.of("UTC")
)
private const val J2000_EPOCH = 2451545
private const val DAYS_PER_CENTURY = 36525.0
private const val SECONDS_PER_MINUTE = 60
private const val MINUTES_PER_DEGREE = 4.0
private const val MINUTES_PER_DAY = 24 * 60
private const val SECONDS_PER_DAY = MINUTES_PER_DAY * SECONDS_PER_MINUTE
private const val MILLISECONDS_PER_DAY = SECONDS_PER_DAY * 1000

/**
 * Used to take into account refraction
 */
val SolarZenithAtSunRiseSunSet = Angle.fromDegrees(90, 50.0)

/**
 * The Julian date (JD) of any instant is the Julian day number for the preceding noon in Universal Time
 * plus the fraction of the day since that instant.
 * Calculate the continuous count of days since the beginning of the Julian Period, see https://en.wikipedia.org/wiki/Julian_day
 * @param dateTime
 * @return continuous count of days since the beginning of the Julian Period
 */
fun calculateJulianMidnightDate(dateTime: ZonedDateTime, includeTime: Boolean = false): Double {
    val days =
        ChronoUnit.DAYS.between(J2000_EPOCH_DATE.truncatedTo(ChronoUnit.DAYS), dateTime.truncatedTo(ChronoUnit.DAYS))
    val adjustedDay = dateTime.withYear(2000).withDayOfYear(1)
    return J2000_EPOCH + days + if (includeTime) (ChronoUnit.MILLIS.between(
        J2000_EPOCH_DATE,
        adjustedDay
    ) / MILLISECONDS_PER_DAY.toDouble()) else -0.5
}

/**
 * Determine sunrise or sunset time for the given day at the given latitude or longitude
 * @param rise true - calculate sunrise; otherwise calculate sunset
 * @param dateTime date time to calculate for
 * @param latitude latitude on earth
 * @param longitude longitude on earth
 * @return Sunrise or sunset time for given day
 */
fun calculateSunriseSetTime(rise: Boolean, dateTime: ZonedDateTime, latitude: Angle, longitude: Angle): ZonedDateTime {
    val jday = calculateJulianMidnightDate(dateTime, false)
    val midnightUtc: ZonedDateTime = dateTime.withZoneSameInstant(ZoneId.of("UTC")).truncatedTo(ChronoUnit.DAYS)

    val minutesAfterMidnight = calculateRefinedSunriseSetUTC(rise, jday, latitude, longitude)
    val secondsAfterMidnight = SECONDS_PER_MINUTE * minutesAfterMidnight
    return midnightUtc.plusSeconds(secondsAfterMidnight.toLong()).withZoneSameInstant(dateTime.zone)
}

/**
 * Determine minutes since midnight of sunrise or sunset for the given day at the given latitude or longitude.
 * This method refines the result from [calculateSunriseSetUTC] to determine time date in polar latitudes.
 * @param rise true - calculate sunrise; otherwise calculate sunset
 * @param jd Julian Date
 * @param latitude latitude on earth
 * @param longitude longitude on earth
 * @return Sunrise or sunset time for given day in minutes since midnight
 */
fun calculateRefinedSunriseSetUTC(rise: Boolean, jd: Double, latitude: Angle, longitude: Angle): Double {
    var estimate = calculateSunriseSetUTC(rise, jd, latitude, longitude)
    var i = 0
    if (estimate.isNaN()) {
        val doy = calculateDayOfYearFromJulianDay(jd)
        val increment: Int
        if (((latitude.degrees > 66.4) && (doy > 79) && (doy < 267)) ||
            ((latitude.degrees < -66.4) && ((doy < 83) || (doy > 263)))
        ) {   //previous sunrise/next sunset
            if (rise) { // find previous sunrise
                increment = -1
            } else { // find next sunset
                increment = 1
            }
        } else {   //previous sunset/next sunrise
            if (rise) { // find previous sunrise
                increment = -1
            } else { // find next sunset
                increment = 1
            }

        }
        while (estimate.isNaN()) {
            i += increment
            estimate = calculateSunriseSetUTC(rise, jd + i, latitude, longitude)
            estimate = calculateSunriseSetUTC(rise, jd + i + estimate / MINUTES_PER_DAY, latitude, longitude)
        }
    }
    var prevEstimate = estimate
    for (j in 1..4) {
        estimate = calculateSunriseSetUTC(rise, jd + i + estimate / MINUTES_PER_DAY, latitude, longitude)
        if (isClose(estimate, prevEstimate, 1.0 / SECONDS_PER_DAY)) {
            break
        }
        prevEstimate = estimate
    }
    return (i * MINUTES_PER_DAY) + estimate
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
    if (hourAngle.isNaN()) {
        return Double.NaN
    }
    if (!rise) hourAngle = -hourAngle
    val delta = longitude + hourAngle
    return (MINUTES_PER_DAY / 2) - (MINUTES_PER_DEGREE * delta.degrees) - eqTime    // in minutes
}

fun calculateDayOfYearFromJulianDay(julianDay: Double): Int {
    val z = floor(julianDay + 0.5).toInt()
    val f = (julianDay + 0.5) - z
    val A: Int
    if (z < 2299161) {
        A = z
    } else {
        val alpha = floor((z - 1867216.25) / 36524.25).toInt()
        A = z + 1 + alpha - floor(alpha / 4.0).toInt()
    }
    val B = A + 1524
    val C = floor((B - 122.1) / 365.25).toInt()
    val D = floor(365.25 * C).toInt()
    val E = floor((B - D) / 30.6001).toInt()
    val day = B - D - floor(30.6001 * E).toInt() + f
    val month = if (E < 14) E - 1 else E - 13
    val year = if (month > 2) C - 4716 else C - 4715

    val k = if (Year.isLeap(year.toLong())) 1 else 2
    val doy = floor((275 * month) / 9.0).toInt() - k * floor((month + 9) / 12.0).toInt() + day - 30
    return doy.toInt()
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
    return Angle.fromRadians(eTime).degrees * MINUTES_PER_DEGREE
}

/**
 * Hour Angle for sunrise at location on earth.
 * @param latitude Latitude of location
 * @param solarDeclination declination
 * @return Hour Angle
 */
fun calculateHourAngleSunrise(latitude: Angle, solarDeclination: Angle): Angle {
    try {
        val hourAngleArg =
            (cos(SolarZenithAtSunRiseSunSet) / (cos(latitude) * cos(solarDeclination)) - tan(latitude) * tan(
                solarDeclination
            ))
        return acos(hourAngleArg)
    } catch (e: IllegalArgumentException) {
        return Angle.NaN
    }
}

/**
 * Determine time of solar noon in the same timezone as [date] for the given [longitude]
 */
fun calculateSolarNoonTime(
    date: ZonedDateTime,
    longitude: Angle
): ZonedDateTime {
    val jday = calculateJulianMidnightDate(date, false)
    val midnightUtc: ZonedDateTime = date.withZoneSameInstant(ZoneId.of("UTC")).truncatedTo(ChronoUnit.DAYS)
    val solNoonTime =
        midnightUtc.plusNanos((SECONDS_PER_MINUTE * 1_000_000_000L * calculateSolarNoon(jday, longitude)).toLong()).withZoneSameInstant(date.zone)
    return solNoonTime.truncatedTo(ChronoUnit.MILLIS)
}

/**
 * Determine time of solar noon in minutes after midnight since [julianDate] for the given [longitude]
 */
fun calculateSolarNoon(julianDate: Double, longitude: Angle): Double {
    val tnoon = calculateTimeJulianCentury(julianDate - longitude.degrees / 360.0)
    var eqTime = calculateEquationOfTime(tnoon)
    var estimate = (MINUTES_PER_DAY / 2) - (longitude.degrees * MINUTES_PER_DEGREE) - eqTime
    do {
        val previousEstimate = estimate
        val newTime = calculateTimeJulianCentury(julianDate + estimate / MINUTES_PER_DAY)
        eqTime = calculateEquationOfTime(newTime)
        estimate = (MINUTES_PER_DAY / 2) - (longitude.degrees * MINUTES_PER_DEGREE) - eqTime
    } while (!isClose(estimate, previousEstimate, 1.0/ SECONDS_PER_DAY))
    return estimate
}

