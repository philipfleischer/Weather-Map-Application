package no.uio.ifi.in2000.vrapp.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


//Gets daily forecast data from long-term weather information
fun LongTermWeatherInfo.getDailyForecast(): List<DailyForecast> {
    return properties.timeseries.map { timeSeries ->
        DailyForecast(
            date = timeSeries.time,
            maxTemp = timeSeries.data.next24Hours?.details?.airTemperatureMax ?: 0.0,
            minTemp = timeSeries.data.next24Hours?.details?.airTemperatureMin ?: 0.0,
            precipitationProbability = timeSeries.data.next24Hours?.details?.probabilityOfPrecipitation ?: 0.0,
            precipitationAmount = timeSeries.data.next24Hours?.details?.precipitationAmount ?: 0.0
        )
    }
}
//Data classes:

@Serializable
data class DailyForecast(
    val date: String,
    val maxTemp: Double,
    val minTemp: Double,
    val precipitationProbability: Double,
    val precipitationAmount: Double
) {
    fun getFormattedDate(): String {
        return date.substring(0, 10)
    }
}
@Serializable
data class LongTermWeatherInfo(
    @SerialName("type") val type: String,
    @SerialName("geometry") val geometry: Geometry,
    @SerialName("properties") val properties: LongTermProperties,
    val location: Location? = null  

)

@Serializable
data class Geometry(
    @SerialName("type") val type: String,
    @SerialName("coordinates") val coordinates: List<Double>
)

@Serializable
data class LongTermProperties(
    @SerialName("meta") val meta: Meta,
    @SerialName("timeseries") val timeseries: List<LongTermTimeSeries>
)

@Serializable
data class Meta(
    @SerialName("updated_at") val updatedAt: String,
    @SerialName("units") val units: Units
)

@Serializable
data class Units(
    @SerialName("air_temperature_max") val airTemperatureMax: String,
    @SerialName("air_temperature_max_percentile_10") val airTemperatureMaxPercentile10: String,
    @SerialName("air_temperature_max_percentile_90") val airTemperatureMaxPercentile90: String,
    @SerialName("air_temperature_mean") val airTemperatureMean: String,
    @SerialName("air_temperature_mean_percentile_10") val airTemperatureMeanPercentile10: String,
    @SerialName("air_temperature_mean_percentile_90") val airTemperatureMeanPercentile90: String,
    @SerialName("air_temperature_min") val airTemperatureMin: String,
    @SerialName("air_temperature_min_percentile_10") val airTemperatureMinPercentile10: String,
    @SerialName("air_temperature_min_percentile_90") val airTemperatureMinPercentile90: String,
    @SerialName("precipitation_amount") val precipitationAmount: String,
    @SerialName("precipitation_amount_percentile_10") val precipitationAmountPercentile10: String,
    @SerialName("precipitation_amount_percentile_90") val precipitationAmountPercentile90: String,
    @SerialName("probability_of_frost") val probabilityOfFrost: String,
    @SerialName("probability_of_heavy_precipitation") val probabilityOfHeavyPrecipitation: String,
    @SerialName("probability_of_precipitation") val probabilityOfPrecipitation: String
)

@Serializable
data class LongTermTimeSeries(
    @SerialName("time") var time: String,
    @SerialName("data") val data: LongTermTimeSeriesData
)

@Serializable
data class LongTermTimeSeriesData(
    @SerialName("next_24_hours") val next24Hours: Next24Hours? = null,
    @SerialName("next_7_days") val next7Days: Next7Days? = null
)

@Serializable
data class Next24Hours(
    @SerialName("details") val details: Next24HoursDetails
)

@Serializable
data class Next24HoursDetails(
    @SerialName("air_temperature_max") val airTemperatureMax: Double,
    @SerialName("air_temperature_max_percentile_10") val airTemperatureMaxPercentile10: Double,
    @SerialName("air_temperature_max_percentile_90") val airTemperatureMaxPercentile90: Double,
    @SerialName("air_temperature_mean") val airTemperatureMean: Double,
    @SerialName("air_temperature_mean_percentile_10") val airTemperatureMeanPercentile10: Double,
    @SerialName("air_temperature_mean_percentile_90") val airTemperatureMeanPercentile90: Double,
    @SerialName("air_temperature_min") val airTemperatureMin: Double,
    @SerialName("air_temperature_min_percentile_10") val airTemperatureMinPercentile10: Double,
    @SerialName("air_temperature_min_percentile_90") val airTemperatureMinPercentile90: Double,
    @SerialName("precipitation_amount") val precipitationAmount: Double,
    @SerialName("precipitation_amount_percentile_10") val precipitationAmountPercentile10: Double,
    @SerialName("precipitation_amount_percentile_90") val precipitationAmountPercentile90: Double,
    @SerialName("probability_of_frost") val probabilityOfFrost: Double,
    @SerialName("probability_of_heavy_precipitation") val probabilityOfHeavyPrecipitation: Double,
    @SerialName("probability_of_precipitation") val probabilityOfPrecipitation: Double
)

@Serializable
data class Next7Days(
    @SerialName("details") val details: Next7DaysDetails
)

@Serializable
data class Next7DaysDetails(
    @SerialName("precipitation_amount") val precipitationAmount: Double,
    @SerialName("precipitation_amount_percentile_10") val precipitationAmountPercentile10: Double,
    @SerialName("precipitation_amount_percentile_90") val precipitationAmountPercentile90: Double,
    @SerialName("probability_of_frost") val probabilityOfFrost: Double
)
