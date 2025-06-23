![Prosjektlogo](images/tittelheaderh.png)

## 💛 VÆR APP FOR FOLKET - TEAM 28 IN2000 UIO 💛

#### Innholdsfortegnelse:

[Introduksjon](#🌞-om-oss)  •  [Bibloteker](#📚-bibloteker)  •  [Innstallasjon](#⚙️-innstallasjon)



---
![bilde](images/poster.png)
---

### 🌞 Om Oss

Vi er en gruppe studenter som tar IN2000: software engineering med prosjektarbeid.

**Team 28:**

- Mia Anneli Pulkkanen [@miaap](https://github.com/miaap)
- Linn Thoresen [@linnthor](https://github.com/linnthor)
- Amanda Linnea Slang Rödström [@alrodstr](https://github.com/alrodstr)
- Hans Magnus Salve Malm Haga [@hmhaga](https://github.com/hmhaga)
- David Hovde [@davidhov](https://github.com/davidhov)
- Philip Elias Fleischer [@philipef](https://github.com/philipef)

---

### 📱 Om Appen

Vi valgte case nr 4: **Yr – Kart til folket!** Vi syntes det var et spennende case, som var både
konkret og med rom til å utforske.

Vi har laget en vær-app som har en værtabell, langtidsvarsel og værkart. Værtabell og langtidsvarsel
viser værmelding for de neste dagene, med en dynamisk bakgrunn som er representativ for været på
valgt lokasjon. Værkartet viser forskjellige værfohold over et kart. Vi har laget et interaktivt
værkart, som oppfordrer brukere til å utforske værforhold og steder i hele norge. Du kan finne
lokasjoner av å bruke søkefeltet, eller holde inne på ønsket sted i værkartet. Du kan også lagre
favoritt lokasjoner for rask tilgang, og enkel oversikt i værkartet.

    Case-kravene vi oppfyller med applikasjonen er:

        • Legge tiles for temperatur og nedbør på kart

        • Lage transparens for data under gitt terskelverdi (fx regn)

        • Justere fargepalett

        • Sette opp en backend som rendrer ferdige karttiles som kan brukes direkte i klienten

    Tekniske krav:
    
        • Ryddig arkitektur med logisk mappestruktur og MVVM
        
        • Koden er robust og pålitelig

        • Enhetstester for vedlikehold

        • Grensesnittet er tilpassbart, adaptivt og universlt utformet
        
        • Funksjonalitet er representert i diagrammen, se MODELING.md

---

## 📚 API OG BIBLIOTEKER

#### ☀️ API

Vi har i hovedsak bygget appen rundt MET sine APIer for å finne værdata:

- Beta.yr-maps.met.no/api/air-temperature
- Beta.yr-maps.met.no/api/cloud-area-fraction
- Beta.yr-maps.met.no/api/percipitation-amount
- Beta.yr-maps.met.no/api/percipitation-nowcast
- Beta.yr-maps.met.no/api/percipitation-observations
- Beta.yr-maps.met.no/api/wind/available.json
- In2000.api.met.no/weatherapi/locationforecast/2.0/complete
- In2000.api.met.no/weatherapi/subseasonal/1.0/complete
- In2000.api.met.no/weatherapi/sunrise/3.0/moon
- In2000.api.met.no/weatherapi/sunrise/3.0/sun

Og vi har hentet API for å finne stedsnavn og lokasjoner:

- Api.kartverket.no/stedsnavn/v1/navn?
- Api.kartverket.no/stedsnavn/v1/punkt?

---

#### 📎 Biblioteker

| Bibliotek                         | Beskrivelse                                                                     |
|-----------------------------------|---------------------------------------------------------------------------------|
| **Maplibre**                      | Brukes for å vise værdata som lag (tiles) på et interaktivt kart.               |
| **Retrofit**                      | For å hente værdata fra MET og andre API-er på en effektiv og strukturert måte. |
| **Ktor**                          | Alternativ HTTP-klient brukt For asynkrone nettvekrskall                        |
| **Gson**                          | For å konvertere JSON-data fra API-er til Kotlin-objekter.                      |
| **Room**                          | Lokal database som lagrer brukerens favorittsteder og innstillinger.            |
| **Google Play Services Location** | Henter brukerens sanntidsposisjon via GPS.                                      |
| **Accompanist Permissions**       | For å håndtere tillatelser, som posisjonstilgang, i Compose.                    |
| **Jetpack Compose**               | UI-rammeverket som bygger hele appens grensesnitt – moderne og deklarativt.     |
| **Material 3**                    | Brukes til visuelle komponenter som knapper, kort og menyer.                    |

Vi brukte TileConvert fra Yr som inspirasjon

## ⚙️ INNSTALLASJON

### 📂 Forutsetninger

    - Innstallert Android Studios 

    - Tilgang til internett

    - Minimum SDK på 24, anbefalt 34 eller mer

    - Lokasjon i Norge. (Kun lokasjoner i Norge er implementert - ikke inkludert svalbard)

    - Norsk tastatur må være aktivert i systeminnstillinger på enhet/emulator (for å skrive stedsnavn med æ,ø eller å)

    - At backend hostes, for å visualisere kartet

---

### 🔌 Kloning og bygging

For å kjøre applikasjonen må filene lastes ned riktig, og kjøres i Android Studios. Her er en guide
for å åpne koden, med 3 alternative metoder. Deretter en guide for å laste ned emulatoren, vi
anbefaler API 34 eller mer.

**Slik åpner du prosjektet fra en zip-fil:**

1. Pakk ut ZIP-filen et sted på datamaskinen din (f.eks. Nedlastinger eller Dokumenter).

2. Åpne Android Studio.

3. Klikk på "Open" (åpne et prosjekt).

4. Bla til mappen du nettopp pakket ut, og velg den.

**Slik åpner du prosjektet og lagrer koden med terminal:**

```bash
git clone https://github.uio.no/IN2000-V25/team-28.git
cd team-28
./gradlew build
```

1. Åpne Android Studios.

2. Velg Open an existing project og naviger til mappen du klonet (team-28).

3. Vent til prosjektet lastes inn og alle avhengigheter er synkronisert (du vil se "Gradle sync
   finished" nederst).

4. Hvis du blir spurt, velg å bruke standard Gradle-wrapper.

---

**Slik åpner du prosjektet direkte i Android Studios med git-lenke**

1. Åpne Android Studios.

2. Velg "Get from VCS" (VCS = Version Control System) på startsiden.

3. I feltet for "URL", lim inn GitHub-lenken til prosjektet:

```bash
https://github.uio.no/IN2000-V25/team-28.git

```

4. Velg hvor du vil lagre prosjektet på maskinen din, og trykk Clone.

---


**For å laste ned emulator:**

1. Gå til Tools → Device Manager i menyen.

2. Klikk på Create Device for å lage en ny emulator hvis du ikke allerede har én.

3. Velg modell f.eks. Pixel 5 og trykk Next.

4. Velg en type Android 7.0 (API nivå 24) eller nyere for å fungere. Vi anbefaler API 34 eller mer
   for optimal ytelse.

5. Klikk Download hvis det ikke er lastet ned enda, og trykk Finish.

6. Start emulatoren ved å trykke på den grønne ▶-knappen ved siden av navnet på emulatoren.


---

### 💾 Backend

Vi satte opp en backend som rendrer ferdige karttiles som kan brukes direkte i klienten. Backenden
gjør pixelmanipuleringen, slik at bildene i værkartet blir mer presist og komplekst.

Som fullstendig app ville backend-server ville blitt kjørt av oss på enten egen server eller via en
tredjeparts hosting-tjeneste. Men istedenfor å hoste en server i 4 uker i strekk legger vi ved en
instruksjon om hvordan dette kan settes opp og kjøres lokalt.

Backend-serveren ligger i mappen TileServer. Den inneholder følgende filer:

- TileServer.java
- ServerHttpHandler.java
- TileConvert.java
- info.md

**For å kjøre gjør følgende:**
**Localhost i android emulator:**

1. Åpne terminalvindu, og finn plasseringen av mappen TileServer. Deretter skriv:

```bash
javac *.java
java TileServer //dette starter serveren
```

2. Da får du opp at serveren er startet, for å stanse serveren trykk Ctrl + C i terminalvinduet.
3. Nå er servern klar. Det vil si at nå kan Android emulatoren aksessere maskinens localhost via
   ip’en 10.0.2.2.
4. Nå kan du kjøre applikasjonen i ved å trykke på den grønne Run ▶-knappen.

**På fysisk enhet eller server og android studio på to ulike enheter:**
Når man kjører applikasjonen med backend på en fysisk enhet kan ikke appen aksessere localhost. Da
må man legge inn riktig IP-addresse direkte i koden.

1. Endre streng-argumenter «localhost» på linje 12 i TileServer.java byttes ut med
   InetAddress.getByName(«<ip-en på enheten som kjører server>»).
2. I tillegg må linje 21 i MapDataSource.kt settes lik denne ip’en som du får.
3. Lagre endringene du skrev i java-koden.
4. I terminal, finn plasseringen av mappen TileServer, og deretter skriv:

```bash
javac *.java
java TileServer //dette starter serveren
```

5. Da vil du mest sannsynlig få en beskjed om at java trenger brannmurstilgang, trykk OK. Dette er
   nødvendig for at denne konfigurasjonen skal fungere og kan fjernes etterpå.
6. Da får du opp at serveren er startet, for å stanse serveren trykk Ctrl + C i terminalvinduet.
3. Nå er servern klar og kjører på IP-adressen til enheten serveren kjøres på.
4. Koble til enheten du vil kjøre applikasjonen på fra Device Manager, og kjør applikasjonen som
   vanlig

---


## Appen er nå klar! Takk for Oss! 🌞
---

![Prosjektlogo](images/phonepic.png)

---
![Prosjektlogo](images/tittelheaderh.png)
