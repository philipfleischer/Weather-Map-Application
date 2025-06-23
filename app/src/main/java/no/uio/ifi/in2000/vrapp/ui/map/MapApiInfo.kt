package no.uio.ifi.in2000.vrapp.ui.map

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Content for the api info box, showing Text and maplegend. Describing the api
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun mapApiInfo(apiName: String?, viewModel: MapViewModel): Any {
    val (legendColors, legendUnit, _, background) = viewModel.mapLegend()
    return when (apiName) {
        "Temperatur" -> {
            Column {
                Text(
                    """
                        $apiName api-laget viser en global prognose for lufttemperatur i Celsius for de neste 2-3 dagene. Kartet bruker en fargeskala med intervaller på 10 grader fra -50°C til 50°C, der hvert intervall har en unik fargetone.
                            
                        Legenden nedenfor viser fargen og temperaturen i Celcius, slik det er vist på kartet.
                        """.trimIndent()
                )
                Spacer(Modifier.height(8.dp))
                Text(" $legendUnit", fontWeight = FontWeight.Bold)
                legendColors.forEach { pair: Pair<Color, String> ->
                    val (color, unit) = pair
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(color)
                                .width(8.dp)
                                .height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            unit,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    """
                        Fargetonene kan justeres på med "fargehjul" funksjonen i Instillinger.
                        
                        Dataen oppdateres hver sjette(6) time.
                        """.trimIndent()
                )
            }
        }

        "Skyer" -> {
            Column {
                Text(
                    """
                        $apiName api-laget viser en global prognose for skydekke i prosent for de neste 2-3 dagene. Kartet bruker fargen Hvit med opasitet for å representere skydekke, tettere skydekke gir høyere opasitet på hvitfargen.
                        
                        Legenden nedenfor viser fargen med opasitet og skydekke i prosent, slik det er vist på kartet.
                        
                        For å effektivt vise opasiteten til hvitfargen er bakgrunnen satt til en lys grønnfarge
                        """.trimIndent()
                )
                Spacer(Modifier.height(8.dp))
                Text(" $legendUnit", fontWeight = FontWeight.Bold)
                legendColors.forEach { pair: Pair<Color, String> ->
                    val (color, unit) = pair
                    val opacity = unit.toFloat() / 128
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .then(
                                    if (background != null) {
                                        Modifier.background(background)
                                    } else {
                                        Modifier
                                    }
                                )
                                .background(color.copy(alpha = opacity))
                                .width(8.dp)
                                .height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            unit,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    """
                        Fargetonene kan ikke justeres på i dette apiet.
                        
                        Dataen oppdateres hver sjette(6) time.
                        """.trimIndent()
                )
            }
        }

        "Nedbør langtids" -> {
            Column {
                Text(
                    """
                        $apiName api-laget viser en global prognose for nedbørsintensitet i millimeter per time for de neste 2-3 dagene. Kartet bruker en fargeskala med intervaller mellom minimalt nedbør (0.03 mm/t) og styrtregn (>= 15 mm/t), hvor hvert intervall har en unik fargetone.
                        
                        Legenden nedenfor viser fargen og nedbørsintensiteten i mm/t, slik det er vist på kartet.
                        """.trimIndent()
                )
                Spacer(Modifier.height(8.dp))
                Text(" $legendUnit", fontWeight = FontWeight.Bold)
                legendColors.forEach { pair: Pair<Color, String> ->
                    val (color, unit) = pair
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(color)
                                .height(24.dp)
                                .width(8.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            unit,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    """
                        Fargetonene kan justeres på med "fargehjul" funksjonen i Innstillinger.
                        
                        Dataen oppdateres hver sjette(6) time.
                        """.trimIndent()
                )
            }
        }

        "Nedbør radar" -> {
            Column {
                Text(
                    """
                           $apiName api-laget viser observert nedbørsmengde for de siste 90 minuttene og prognosen for de neste 90 minuttene i Norge, Sverige og Finland. Kartet bruker en fargeskala med intervaller mellom minimalt nedbør (0.03 mm/t) og styrtregn (>= 15 mm/t), hvor hvert intervall har en unik fargetone.
                           
                           Legenden nedenfor viser fargen og nedbørsintensiteten i mm/t, slik det er vist på kartet.
                    """.trimIndent()
                )
                Spacer(Modifier.height(8.dp))
                Text(" $legendUnit", fontWeight = FontWeight.Bold)
                legendColors.forEach { pair: Pair<Color, String> ->
                    val (color, unit) = pair
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(color)
                                .width(8.dp)
                                .height(24.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            unit,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    """
                        Fargetonene kan justeres på med "fargehjul" funksjonen i Innstillinger.
                        
                        Dataen er basert på radarmålinger og oppdateres hvert 5 minutt.
                        """.trimIndent()
                )
            }
        }

        "Vind" -> {
            Column {
                Text(
                    """
                        $apiName api-laget viser en global prognose for vindhastighet i meter per sekund for de neste 2-3 dagene. Retningen på vinden blir illustrert med piler. Kartet bruker en fargeskala med intervaller mellom lett bris (<5.4 m/s) og orkan (>32.6 m/s), hvor hvert intervall har en unik fargetone.
                        """.trimIndent()
                )
                Spacer(Modifier.height(8.dp))
                Text(" $legendUnit", fontWeight = FontWeight.Bold)
                legendColors.forEach { pair: Pair<Color, String> ->
                    val (color, unit) = pair
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(color)
                                .height(24.dp)
                                .width(8.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            unit,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    """
                        Fargetonene kan justeres på med "fargehjul" funksjonen i Innstillinger.
                        
                        Dataen oppdateres hver sjette(6) time.
                        """.trimIndent()
                )
            }
        }

        else -> {
            Column {
                Text(
                    """
                Laster...
            """.trimIndent()
                )
            }
        }
    }
}