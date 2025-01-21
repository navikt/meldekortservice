# Meldekortservice

Microservice / proxy som henter data fra meldekort ORDS (Arena DB).

## Kom i gang
Bygg meldekortservice ved å kjøre `./gradlew clean build`. Dette vil også kjøre testene.
Det er også mulig å kjøre `gradle clean build`, men da må man ha en riktig versjon av gradle installert (som støtter Java 21)

## Lokal kjøring
0. Ikke nødvendig: for å være sikker på at man får en ny tom database, kan man kjøre kommandoen: `docker-compose down -v`.
1. Start lokal instans av Postgres ved å kjøre `docker-compose up -d`.
2. Start appen ved å kjøre `./gradlew runServerTest`.
Det er også mulig å kjøre Server.kt sin main-metode eller `./gradlew runServer`, men da må man sette miljøvariablene:
TOKEN_X_WELL_KNOWN_URL
TOKEN_X_CLIENT_ID
For å kjøre mot f.eks Q1 kan man enten sette riktige miljøvariabler (manuelt eller ved hjelp av bat/bash script) eller midlertidig skrive disse inn i Environment.kt i stedet for defaultValue'er.  
For eksempel, for å bruke ORDS i Q1 må man erstatte
```
val ordsUrl: String = ordsSettings["ORDS_URI"] ?: DUMMY_URL,
val ordsClientId: String = ordsSettings["CLIENT_ID"] ?: "cLiEnTiD",
val ordsClientSecret: String = ordsSettings["CLIENT_SECRET"] ?: "cLiEnTsEcReT",
```
med
```
val ordsUrl: String = "https://arena-ords-q1.dev.adeo.no/arena",
val ordsClientId: String = "%CLIENT_ID_FRA_VAULT%",
val ordsClientSecret: String = "%CLIENT_SECRET_FRA_VAULT%",
```
Appen starter på http://localhost:8090. Sjekk for eksempel at ping svarer på http://localhost:8090/meldekortservice/internal/ping.  
Swagger er tilgjengelig på http://localhost:8090/meldekortservice/internal/apidocs/index.html

## Feilsøking
For å være sikker på at man får en ny tom database, kan man kjøre kommandoen: `docker-compose down -v`.

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan rettes mot https://github.com/orgs/navikt/teams/meldekort.

## For Nav-ansatte
Interne henvendelser kan sendes via Slack i kanalen #meldekort.

## Dokumentasjon
Dokumentasjon finnes i [Confluence](https://confluence.adeo.no/display/TMP/Meldekort-api)

For å sjekke om det finnes nye versjoner av avhengigheter, kan man kjøre: `./gradlew dependencyUpdates`
