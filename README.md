# Meldekortservice

Microservice / proxy som henter data fra meldekort ORDS (Arena DB).

## Kom i gang
1. Bygg meldekortservice ved å kjøre `gradle clean build`. Dette vil også kjøre testene.
2. Start lokal instans av Postgres ved å kjøre `docker-compose up -d`.
3. For å være sikker på at man får en ny tom database, kan man kjøre kommandoen: `docker-compose down -v`.

## Lokal kjøring
Start appen ved å kjøre Server.kt sin main-metode eller kjør `gradle runServer`.  
For å kjøre mot f.eks Q1 kan man enten sette riktige miljøvariabler (manuelt eller ved hjelp av bat/bash script) eller midlertidig skrive disse inn i Environment.kt i stedet for defaultValue'er.  
For eksempel, for å bruke ORDS i Q1 må man erstatte
```
val ordsUrl: URL = URL(getEnvVar("ORDS_URI", DUMMY_URL)),
val ordsClientId: String = getEnvVar("CLIENT_ID", "cLiEnTiD"),
val ordsClientSecret: String = getEnvVar("CLIENT_SECRET", "cLiEnTsEcReT"),
```
med
```
val ordsUrl: URL = URL(getEnvVar("ORDS_URI", "https://arena-ords-q1.dev.adeo.no/arena")),
val ordsClientId: String = getEnvVar("CLIENT_ID", "%CLIENT_ID_FRA_VAULT%"),
val ordsClientSecret: String = getEnvVar("CLIENT_SECRET", "%CLIENT_SECRET_FRA_VAULT%"),
```
Appen starter på http://localhost:8090. Sjekk for eksempel at ping svarer på http://localhost:8090/meldekortservice/internal/ping.  
Swagger er tilgjengelig på http://localhost:8090/meldekortservice/internal/apidocs/index.html?url=swagger.json

## Feilsøking
For å være sikker på at man får en ny tom database, kan man kjøre kommandoen: `docker-compose down -v`.

## Tekster
Denne appen blant annet støtter tekstversjonering for meldekort-frontend.
Meldekort-frontend viser de versjonenne av tekstene som var gyldige i begynnelsen av meldeperioden til aktivt
meldekort. Hvis meldekort ikke er valgt, viser frontend de nyeste versjonene av tekstene.

Tekstene er lagret i en DB og derfor må frontend gå gjennom meldekort-api og meldekortservice for å nå dem.

For å endre tekstene (eller opprette nye versjoner) må man endre fil R__recreate_texts.sql her, i meldekortservice, og
deploye meldekortservice på nytt.
Dette gir mulighet å teste endringer i DEV/QA først og hindrer direkte endringer i prod DB.

## Henvendelser
Spørsmål knyttet til koden eller prosjektet kan rettes mot https://github.com/orgs/navikt/teams/meldekort.

## For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #meldekort.

## Dokumentasjon
Dokumentasjon finnes i [Confluence](https://confluence.adeo.no/display/TMP/Meldekort-api).
Om Meldekort journalføring: https://confluence.adeo.no/pages/viewpage.action?pageId=431009242

For å sjekke om det finnes nye versjoner av avhengigheter, kan man kjøre: `./gradlew dependencyUpdates`
