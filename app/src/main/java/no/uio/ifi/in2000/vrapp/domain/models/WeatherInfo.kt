package no.uio.ifi.in2000.vrapp.domain.models

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.pow

//Represents weather data with properties like temperature and forecasts
@RequiresApi(Build.VERSION_CODES.O)
@Serializable
data class WeatherInfo(
    @SerialName("properties") val properties: Properties,
    val location: Location? = null
) {

    private val calendar: Calendar by lazy {
        Calendar.getInstance(TimeZone.getTimeZone("Europe/Oslo"))
    }

    //Date formatting
    fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
    }

    val currentTemp: String =
        properties.timeSeries.firstOrNull()?.data?.instant?.details?.temperature?.toInt()
            ?.toString()
            ?: "N/A"

    //Caching weather condition for each timestamp
    private val weatherConditions: Map<String, String> by lazy {
        properties.timeSeries.associate { timeSeries ->
            val time = timeSeries.time
            val condition = getWeatherCondition(
                details = timeSeries.data.instant.details,
                symbolCode = timeSeries.data.next1Hour?.summary?.symbolCode
                    ?: timeSeries.data.next6Hours?.summary?.symbolCode
                    ?: timeSeries.data.next12Hours?.summary?.symbolCode
            )
            time to condition
        }
    }

    val hourlyForecast: List<String> by lazy {
        properties.timeSeries.take(24).mapIndexed { index, timeSeries ->
            val hour = (calendar.get(Calendar.HOUR_OF_DAY) + index) % 24
            val condition = weatherConditions[timeSeries.time]
            val temp = timeSeries.data.instant.details.temperature.toInt()
            "$hour $condition ${temp}°C"
        }
    }

    val weeklyForecast: List<String> by lazy {
        properties.timeSeries
            .groupBy { it.time.substring(0, 10) }
            .values
            .take(7)
            .mapIndexed { dayIndex, dayEntries ->
                val condition = getDailyCondition(dayEntries)
                val temps = dayEntries.map { it.data.instant.details.temperature }
                val minTemp = temps.minOrNull()?.toInt() ?: 0
                val maxTemp = temps.maxOrNull()?.toInt() ?: 0
                "$condition ${minTemp}°C | ${maxTemp}°C"
            }
    }

    //Determines weather condition for a days entries
    private fun getDailyCondition(dayEntries: List<TimeSeries>): String {
        val rainEntry = dayEntries.firstOrNull { entry ->
            (entry.data.next6Hours?.details?.precipitation?.let { it > 0 } == true) ||
                    (entry.data.next1Hour?.details?.precipitation?.let { it > 0 } == true)
        }
        rainEntry?.let {
            return weatherConditions[it.time] ?: getWeatherCondition(
                symbolCode = it.data.next6Hours?.summary?.symbolCode
                    ?: it.data.next1Hour?.summary?.symbolCode
            )
        }

        val morningEntry = dayEntries.firstOrNull { it.time.contains("T06:00") }
        morningEntry?.data?.next12Hours?.summary?.symbolCode?.let {
            return weatherConditions[morningEntry.time] ?: getWeatherCondition(symbolCode = it)
        }

        val middayEntry =
            dayEntries.firstOrNull { it.time.contains("T12:00") } ?: dayEntries.first()
        return weatherConditions[middayEntry.time] ?: getWeatherCondition(
            symbolCode = middayEntry.data.next12Hours?.summary?.symbolCode
                ?: middayEntry.data.next6Hours?.summary?.symbolCode
        )
    }

    //Returns a icon representing the weather condition
    fun getWeatherCondition(
        details: WeatherDetails? = null,
        symbolCode: String? = null
    ): String {
        Log.d("WeatherDebug", "SymbolCode: $symbolCode, CloudFraction: ${details?.cloudCover}")

        symbolCode?.takeUnless { it.isBlank() }?.let { code ->
            val lowerCode = code.lowercase()
            val isNight = "night" in lowerCode

            if (isNight) {
                // Nighttime priority: rain/snow/sleet → heavy clouds → general clouds → moon
                return when {
                    "rain" in lowerCode -> "🌧️"
                    "snow" in lowerCode -> "❄️"
                    "sleet" in lowerCode -> "🌨️"
                    details?.isHeavyClouds() == true -> "☁️"
                    "cloud" in lowerCode -> "☁️"
                    else -> "🌙"
                }
            } else {
                // Daytime conditions
                return when {
                    "sun" in lowerCode || "clear" in lowerCode -> "☀️"
                    "thunder" in lowerCode -> "⛈️"
                    "fog" in lowerCode -> "🌫️"
                    "rain" in lowerCode -> "🌧️"
                    "snow" in lowerCode -> "❄️"
                    "sleet" in lowerCode -> "🌨️"
                    "fair" in lowerCode -> "🌤️"
                    "partly" in lowerCode -> "⛅"
                    "cloud" in lowerCode -> "☁️"
                    else -> details?.getCloudEmoji() ?: "�"
                }
            }
        }

        // Fallback to cloud fraction if no symbol code
        return details?.getCloudEmoji() ?: run {
            Log.w("WeatherDebug", "No weather data available")
            "�"
        }
    }

    // Extension functions for cleaner logic
    private fun WeatherDetails?.isHeavyClouds(): Boolean {
        return this?.cloudCover?.let { it >= 0.8 } ?: false
    }

    private fun WeatherDetails?.getCloudEmoji(): String {
        return when (this?.cloudCover) {
            null -> "�"
            in 0.0..0.2 -> "☀️"
            in 0.2..0.5 -> "🌤️"
            in 0.5..0.8 -> "⛅"
            else -> "☁️"
        }
    }

    val feelsLikeTemp: String by lazy {
        val details = properties.timeSeries.firstOrNull()?.data?.instant?.details ?: run {
            Log.d("FeelsLike", "No weather details available")
            return@lazy "N/A"
        }
        val temp = details.temperature
        val windSpeed = details.windSpeed
        val humidity = details.humidity

        Log.d(
            "FeelsLike",
            "Inputs - Temp: ${temp}°C, Wind: ${windSpeed}m/s, Humidity: ${humidity}%"
        )

        when {
            temp <= 10 && windSpeed > 3 -> {
                val windChill = calculateWindChill(temp, windSpeed)
                Log.d("FeelsLike", "Wind Chill: ${temp}°C→${windChill.toInt()}°C")
                windChill.toInt().toString()
            }

            temp in 10.0..15.0 && windSpeed > 2.0 -> {
                (temp - (windSpeed * 0.7)).toInt().toString()
            }

            temp >= 20 && humidity > 50 -> {
                val heatIndex = calculateHumidex(temp, humidity)
                Log.d("FeelsLike", "Heat Index: ${temp}°C→${heatIndex.toInt()}°C")
                heatIndex.toInt().toString()
            }

            else -> temp.toInt().toString()
        }
    }

    //Calculations of windchill and humidity
    private fun calculateWindChill(temp: Double, windSpeed: Double): Double {
        return if (temp <= 10 && windSpeed >= 4.8) {
            13.12 + 0.6215 * temp - 11.37 * windSpeed.pow(0.16) +
                    0.3965 * temp * windSpeed.pow(0.16)
        } else {
            temp
        }
    }

    private fun calculateHumidex(temp: Double, humidity: Double): Double {
        val e = 6.112 * 10.0.pow(7.5 * temp / (237.7 + temp)) * humidity / 100
        return temp + 0.5555 * (e - 10.0)
    }

    //Returns the precipitation probability for a specific day
    fun getDayPrecipitationProbability(dayIndex: Int): Double {
        val groupedByDay = properties.timeSeries
            .groupBy { it.time.substring(0, 10) }
            .values
            .toList()

        val dayGroup = groupedByDay.getOrNull(dayIndex)
        return dayGroup?.maxOfOrNull { it.data.next6Hours?.details?.precipProbability ?: 0.0 }
            ?: 0.0
    }

    val structuredHourlyForecasts: List<List<HourlyData>> by lazy {
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val cestZone = TimeZone.getTimeZone("Europe/Oslo")

        properties.timeSeries
            .groupBy {
                //Parse timezone with ZonedDateTime and get date as CEST
                val zonedDateTime = ZonedDateTime.parse(it.time, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                zonedDateTime.withZoneSameInstant(cestZone.toZoneId()).toLocalDate().toString()
            }
            .values
            .take(7)
            .mapIndexed { dayIndex, dayEntries ->
                if (dayIndex < 2) {
                    if (dayIndex == 0) {
                        dayEntries.dropWhile {
                            //Parse hour with ZonedDatetime and get CEST time
                            val hour = ZonedDateTime.parse(it.time, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                            .withZoneSameInstant(cestZone.toZoneId()).hour
                            hour < currentHour }
                            .take(24)
                    } else {
                        dayEntries.take(24)
                    }.map { createHourlyData(it) }
                } else {
                    listOf(0, 6, 12, 18).mapNotNull { baseHour ->
                        dayEntries.find {entry ->
                            //Converting back from Cest to UTC to check if the hours align (last minute solution)
                            val utcHour = ZonedDateTime.parse(entry.time, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                                .withZoneSameInstant(ZoneId.of("UTC")).hour
                            utcHour == baseHour
                        }?.let {
                            HourlyData(
                                hour = baseHour,
                                hourRange = "${String.format(Locale.ROOT, "%02d", baseHour)}-${
                                    String.format(Locale.ROOT, "%02d", (baseHour + 6) % 24)
                                }",
                                condition = weatherConditions[it.time] ?: getWeatherCondition(
                                    details = it.data.instant.details,
                                    symbolCode = it.data.next6Hours?.summary?.symbolCode
                                ),
                                temperature = it.data.instant.details.temperature.toInt(),
                                windSpeed = it.data.instant.details.windSpeed,
                                windDirection = it.data.instant.details.windDirection,
                                precipitation = it.data.next6Hours?.details?.precipitation ?: 0.0
                            )
                        }
                    }
                }
            }
    }

    //Creates an HourlyData object from a TimeSeries entry
    private fun createHourlyData(timeSeries: TimeSeries): HourlyData {
        return HourlyData(
            hour = timeSeries.time.substring(11, 13).toInt(),
            condition = weatherConditions[timeSeries.time] ?: getWeatherCondition(
                details = timeSeries.data.instant.details,
                symbolCode = timeSeries.data.next1Hour?.summary?.symbolCode
            ),
            temperature = timeSeries.data.instant.details.temperature.toInt(),
            windSpeed = timeSeries.data.instant.details.windSpeed,
            precipitation = timeSeries.data.next1Hour?.details?.precipitation ?: 0.0,
            windDirection = timeSeries.data.instant.details.windDirection
        )
    }
}
//Data Classes:

data class FavoriteWeather(
    val weatherInfo: WeatherInfo,
    val maxTemp: String,
    val minTemp: String
)


@Serializable
data class HourlyData(
    val hour: Int,
    val condition: String,
    val temperature: Int,
    val windSpeed: Double,
    val precipitation: Double,
    val windDirection: Double?,
    val hourRange: String = ""
)

@Serializable
data class Properties(
    @SerialName("timeseries") val timeSeries: List<TimeSeries>
)

@Serializable
data class TimeSeries(
    @SerialName("time") var time: String,
    @SerialName("data") val data: ForecastData
)

@Serializable
data class ForecastData(
    // Only include what you actually use
    @SerialName("instant") val instant: InstantData,
    @SerialName("next_1_hours") val next1Hour: HourlyForecast? = null,
    @SerialName("next_6_hours") val next6Hours: HourlyForecast? = null,
    @SerialName("next_12_hours") val next12Hours: HourlyForecast? = null,


    )

@Serializable
data class InstantData(
    @SerialName("details") val details: WeatherDetails
)

@Serializable
data class WeatherDetails(
    @SerialName("air_pressure_at_sea_level") val pressure: Double,
    @SerialName("air_temperature") val temperature: Double,
    @SerialName("cloud_area_fraction") val cloudCover: Double,
    @SerialName("relative_humidity") val humidity: Double,
    @SerialName("wind_from_direction") val windDirection: Double,
    @SerialName("wind_speed") val windSpeed: Double,
    @SerialName("precipitation_amount") val precipitation: Double? = null
)

@Serializable
data class HourlyForecast(
    @SerialName("summary") val summary: Summary? = null,
    @SerialName("details") val details: ForecastDetails? = null
)

@Serializable
data class Summary(
    @SerialName("symbol_code") val symbolCode: String
)

@Serializable
data class ForecastDetails(
    @SerialName("precipitation_amount") val precipitation: Double? = null,
    @SerialName("precipitation_amount_max") val maxPrecipitation: Double? = null,
    @SerialName("precipitation_amount_min") val minPrecipitation: Double? = null,
    @SerialName("probability_of_precipitation") val precipProbability: Double? = null,
    @SerialName("probability_of_thunder") val thunderProbability: Double? = null,
    @SerialName("air_temperature_max") val maxTemperature: Double? = null,
    @SerialName("air_temperature_min") val minTemperature: Double? = null
)
