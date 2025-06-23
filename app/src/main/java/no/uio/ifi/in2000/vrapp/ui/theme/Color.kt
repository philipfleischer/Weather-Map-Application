package no.uio.ifi.in2000.vrapp.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

val LightColors = lightColorScheme(
    primary = Color(0xFF4D5071),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE9E7EF),
    onPrimaryContainer = Color(0xFF000F5D),

    secondary = Color(0xFF625B71),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),

    tertiary = Color(0xFF7D5260),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),

    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),

    background = Color(0xFFDDDAE8),
    onBackground = Color(0xFF1C1B1F),

    surface = Color(0xFFFFFFFD),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE2E0EC),
    onSurfaceVariant = Color(0xFF49454F),

    outline = Color(0xFF79747E),
    inverseOnSurface = Color(0xFFC8C2CE),
    inverseSurface = Color(0xFF313033),
    inversePrimary = Color(0xFFD0BCFF),

    surfaceTint = Color(0xFF403D57),
)


val DarkColors = darkColorScheme(
    primary = Color(0xFFC3CAE5),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF111013),
    onPrimaryContainer = Color(0xFFEADDFF),

    secondary = Color(0xFFCCC2DC), // home
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),

    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),

    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),

    background = Color(0xFF1C1B1F), // fave + home
    onBackground = Color(0xFFE6E1E5), //home

    surface = Color(0xFF2B292F), // fave
    onSurface = Color(0xFFE6E1E5), //top + home
    surfaceVariant = Color(0xFF49454F), //top + home
    onSurfaceVariant = Color(0xFFCAC4D0),

    outline = Color(0xFF938F99),
    inverseOnSurface = Color(0xFF242328),
    inverseSurface = Color(0xFFE6E1E5),
    inversePrimary = Color(0xFF6750A4),

    surfaceTint = Color(0xFFABA0BF),
)

