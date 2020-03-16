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
private const val J2000_EPOCH_NOON = 2451545
private const val GREGORIAN_PERIOD_DAY_ONE = 2299161
private const val DAYS_PER_YEAR = 365.25
private const val DAYS_PER_CENTURY = DAYS_PER_YEAR * 100
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
 *
 * If [includeTime] is false then this is the julian date representation of midnight UTC on the day [dateTime].
 * If [includeTime] is true then this is the julian date representation of [dateTime].
 *
 * Calculate the continuous count of days since the beginning of the Julian Period, see https://en.wikipedia.org/wiki/Julian_day
 * @param dateTime
 * @param includeTime
 * @return continuous count of days since the beginning of the Julian Period
 */
fun calculateJulianDate(dateTime: ZonedDateTime, includeTime: Boolean = false): Double {
    val days =
        ChronoUnit.DAYS.between(J2000_EPOCH_DATE.truncatedTo(ChronoUnit.DAYS), dateTime.truncatedTo(ChronoUnit.DAYS))
    val adjustedDay = dateTime.withYear(2000).withDayOfYear(1)
    return J2000_EPOCH_NOON + days + if (includeTime) (ChronoUnit.MILLIS.between(
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
    val julianMidnight = calculateJulianDate(dateTime, false)
    val midnightUtc: ZonedDateTime = dateTime.withZoneSameInstant(ZoneId.of("UTC")).truncatedTo(ChronoUnit.DAYS)

    val minutesAfterMidnight = calculateRefinedSunriseSetUTC(rise, julianMidnight, latitude, longitude)
    val secondsAfterMidnight = SECONDS_PER_MINUTE * minutesAfterMidnight
    return midnightUtc.plusSeconds(secondsAfterMidnight.toLong()).withZoneSameInstant(dateTime.zone)
}

/**
 * Determine minutes since midnight of sunrise or sunset for the given day at the given latitude or longitude.
 * This method refines the result from [calculateSunriseSetUTC] to determine time date in polar latitudes.
 * @param rise true - calculate sunrise; otherwise calculate sunset
 * @param julianMidnight Julian Date
 * @param latitude latitude on earth
 * @param longitude longitude on earth
 * @return Sunrise or sunset time for given day in minutes since midnight
 */
fun calculateRefinedSunriseSetUTC(rise: Boolean, julianMidnight: Double, latitude: Angle, longitude: Angle): Double {
    var estimate = calculateSunriseSetUTC(rise, julianMidnight, latitude, longitude)
    var i = 0
    if (estimate.isNaN()) {
        val doy = calculateDayOfYearFromJulianDay(julianMidnight)
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
            estimate = calculateSunriseSetUTC(rise, julianMidnight + i, latitude, longitude)
            estimate = calculateSunriseSetUTC(rise, julianMidnight + i + estimate / MINUTES_PER_DAY, latitude, longitude)
        }
    }
    var prevEstimate = estimate
    for (j in 1..4) {
        estimate = calculateSunriseSetUTC(rise, julianMidnight + i + estimate / MINUTES_PER_DAY, latitude, longitude)
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
    val centuryTimeJ2000 = calculateTimeJulianCentury(jd)
    val eqTime = calculateEquationOfTime(centuryTimeJ2000)
    val solarDec = calculateSunDeclination(centuryTimeJ2000)
    var hourAngle = calculateHourAngleSunrise(latitude, solarDec)
    if (hourAngle.isNaN()) {
        return Double.NaN
    }
    if (!rise) hourAngle = -hourAngle
    val delta = longitude + hourAngle
    return (MINUTES_PER_DAY / 2) - (MINUTES_PER_DEGREE * delta.degrees) - eqTime    // in minutes
}

/**
 * Given a Julian Day [julianDay] determine the day of the year
 */
fun calculateDayOfYearFromJulianDay(julianDay: Double): Int {
    val julianNoon = floor(julianDay + 0.5).toInt()
    val f = (julianDay + 0.5) - julianNoon
    val A: Int
    if (julianNoon < GREGORIAN_PERIOD_DAY_ONE) {
        A = julianNoon
    } else {
        val alpha = floor((julianNoon - 1867216.25) / 36524.25).toInt()
        A = julianNoon + 1 + alpha - floor(alpha / 4.0).toInt()
    }
    val B = A + 1524
    val C = floor((B - 122.1) / DAYS_PER_YEAR).toInt()
    val D = floor(DAYS_PER_YEAR * C).toInt()
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
    return (jd - J2000_EPOCH_NOON) / DAYS_PER_CENTURY
}

/**
 * Geometric mean longitude of the sun.
 * @param centuryTimeJ2000 Julian centuries since J2000.0
 * @return Geometric mean longitude of the sun.
 */
fun calculateGeometricMeanLongitudeSun(centuryTimeJ2000: Double): Angle {
    val l0 = 280.46646 + centuryTimeJ2000 * (36000.76983 + centuryTimeJ2000 * 0.0003032)
    return Angle.fromDegrees(l0).simplify()
}

/**
 * Geometric mean anomaly of the sun.
 * @param centuryTimeJ2000 Julian centuries since J2000.0
 * @return Geometric mean anomaly
 */
fun calculateGeometricMeanAnomalySun(centuryTimeJ2000: Double): Angle {
    val m = 357.52911 + centuryTimeJ2000 * (35999.05029 - centuryTimeJ2000 * 0.0001537)
    return Angle.fromDegrees(m)
}

/**
 * Eccentricity of Earth's orbit
 * @param centuryTimeJ2000 Julian centuries since J2000.0
 * @return eccentricity
 */
fun calculateEccentricityEarthOrbit(centuryTimeJ2000: Double): Double {
    return 0.016708634 - centuryTimeJ2000 * (0.000042037 + centuryTimeJ2000 * 0.0000001267)
}

/**
 * Equation of center of the sun.
 * @param centuryTimeJ2000 Julian centuries since J2000.0
 * @return Center
 */
fun calculateSunEquationOfCenter(centuryTimeJ2000: Double): Angle {
    val m = calculateGeometricMeanAnomalySun(centuryTimeJ2000)
    val sinm = sin(m)
    val sin2m = sin(m + m)
    val sin3m = sin(m + m + m)
    val c = sinm * (1.914602 - centuryTimeJ2000 * (0.004817 + 0.000014 * centuryTimeJ2000)) + sin2m * (0.019993 - 0.000101 * centuryTimeJ2000) + sin3m * 0.000289
    return Angle.fromDegrees(c)
}

/**
 * True longitude of the sun.
 * @param centuryTimeJ2000 Julian centuries since J2000.0
 * @return True longitude
 */
fun calculateSunTrueLongitude(centuryTimeJ2000: Double): Angle {
    val l0 = calculateGeometricMeanLongitudeSun(centuryTimeJ2000)
    val c = calculateSunEquationOfCenter(centuryTimeJ2000)
    return l0 + c
}

/**
 * Apparent longitude of the sun. (Right ascension).
 * @param centuryTimeJ2000 Julian centuries since J2000.0
 * @return Apparent longitude
 */
fun calculateSunApparentLongitude(centuryTimeJ2000: Double): Angle {
    val o = calculateSunTrueLongitude(centuryTimeJ2000)
    val omega = Angle.fromDegrees(125.04 - 1934.136 * centuryTimeJ2000)
    val lambda = o.degrees - 0.00569 - 0.00478 * sin(omega)
    return Angle.fromDegrees(lambda)
}

/**
 * Mean inclination of Earth's equator with respect to the ecliptic,
 * i.e. ignoring nutation of the equator.
 * See https://en.wikipedia.org/wiki/Ecliptic#Obliquity_of_the_ecliptic
 * @param centuryTimeJ2000 Julian centuries since J2000.0
 */
fun calculateMeanObliquityOfEcliptic(centuryTimeJ2000: Double): Angle {
    val seconds = 21.448 - centuryTimeJ2000 * (46.8150 + centuryTimeJ2000 * (0.00059 - centuryTimeJ2000 * (0.001813)))
    return Angle.fromDegrees(23, 26.0, seconds)
}

/**
 * Corrected obliquity of the ecliptic.
 * @param centuryTimeJ2000 Julian centuries since J2000.0
 * @return Corrected obliquity
 */
fun calculateObliquityCorrection(centuryTimeJ2000: Double): Angle {
    val e0 = calculateMeanObliquityOfEcliptic(centuryTimeJ2000)
    val omega = Angle.fromDegrees(125.04 - 1934.136 * centuryTimeJ2000)
    val e = e0.degrees + 0.00256 * cos(omega)
    return Angle.fromDegrees(e)
}

/**
 * Declination of the sun,
 * i.e. latitude at which the sun is directly overhead
 * @param centuryTimeJ2000 Julian centuries since J2000.0
 * @return declination
 */
fun calculateSunDeclination(centuryTimeJ2000: Double): Angle {
    val e = calculateObliquityCorrection(centuryTimeJ2000)
    val lambda = calculateSunApparentLongitude(centuryTimeJ2000)

    val sint = sin(e) * sin(lambda)
    return asin(sint)
}

/**
 * Difference between true solar time and mean solar time.
 * @param centuryTimeJ2000 Julian centuries since J2000.0
 * @return minutes of time*/
fun calculateEquationOfTime(centuryTimeJ2000: Double): Double {
    val epsilon = calculateObliquityCorrection(centuryTimeJ2000)
    val l0 = calculateGeometricMeanLongitudeSun(centuryTimeJ2000)
    val e = calculateEccentricityEarthOrbit(centuryTimeJ2000)
    val m = calculateGeometricMeanAnomalySun(centuryTimeJ2000)

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
    val jday = calculateJulianDate(date, false)
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

