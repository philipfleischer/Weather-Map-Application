ARCHITECTURE.md
Dette dokumentet er ment for utviklere som skal sette seg inn i appen vår, forstå arkitekturen og videreføre kodestilen og prinsippene vi har fulgt.
--------------------------------------------------------
Overordnet arkitektur:

Applikasjonen er utviklet i Kotlin med Jetpack Compose for brukergrensesnittet. Den følger en MVVM-arkitektur (Model-View-ViewModel) kombinert med Repository Pattern og manuell Dependency Injection (DI). Applikasjonen er bygget med Unidirectional Data Flow (UDF) for å sikre forutsigbar og testbar logikk.

--------------------------------------------------------
Mappestruktur og ansvar:

Prosjektet er delt inn i følgende hovedlag, med tilhørende mappeinndeling:
    - data/: Håndterer datatilgang, inkludert API-kall (via Ktor), lokale databaser (Room), preferanselagring (DataStore), og verktøy for lokasjon og GPS. Inneholder også AppContainer for manuell DI.
    - dependencyinjection/: Inneholder manuell fil for dependency injection (AppContainer.kt).
    - domain/: Inneholder felles datamodeller som representerer applikasjonens forretningslogikk.
    - ui/: Inneholder ViewModels og Compose-baserte skjermfiler, organisert per funksjonell modul (home/, map/, longterm/, favourite/). Inneholder også komponenter og tema som brukes på tvers av appen.

--------------------------------------------------------
Objektorienterte prinsipper og design patterns:
    - Lav kobling: ViewModels kjenner kun til repositories, og repositories kjenner kun til datasources. Dette gjør det enkelt å bytte ut eller teste komponenter isolert.
    - Høy kohesjon: Hver komponent har ett klart ansvar, som gjør det lett å forstå, teste og videreutvikle.
    - MVVM & UDF: Data flyter én vei – brukerinteraksjon sendes til ViewModel, som oppdaterer UiState eksponert via StateFlow, som UI observerer og rendrer basert på.

--------------------------------------------------------
Teknologivalg og API-nivå:
    - Språk: Kotlin
    - UI: Jetpack Compose
    - API-kall: Ktor
    - Lagring: Room, DataStore
    - Kart: MapLibre GL
    - Asynkronitet: Kotlin Coroutines + Flow
    - Testing: JUnit, MockK
    - Dependency Injection: Manuell via AppContainer.kt
    - Minimum SDK: 24 (Android 7.0 Nougat)
    - Target SDK: 35 (Android 14)

Valgt API-nivå sikrer støtte for moderne funksjoner samtidig som kompatibilitet med eldre enheter opprettholdes.

--------------------------------------------------------
Anbefalinger for videreutvikling:
    - Fortsett å bruke MVVM, Repository Pattern og UDF.
    - Følg eksisterende mappestruktur og legg nye funksjoner i egne moduler.
    - Nye API-integrasjoner legges som egne datasources og kobles via Repository.
    - Navigasjon utvides via WeatherNavHost.kt.
    - Vurder å implementere Hilt ved behov for å forenkle dependency injection i større skala.
