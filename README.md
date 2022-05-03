# Meldekortservice

Microservice / proxy som henter data fra meldekort ORDS (Arena DB).

## Kom i gang
1. Bygg meldekortservice ved å kjøre `gradle clean build`. Dette vil også kjøre testene.
2. Start lokal instans av Postgres ved å kjøre `docker-compose up -d`.
3. Start appen ved å kjøre Server.kt sin main-metode eller kjør `gradle runServer`.
4. Appen starter på http://localhost:8090. Sjekk for eksempel at ping svarer på http://localhost:8090/meldekortservice/internal/ping.

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
Swagger er tilgjengelig på `http://localhost:8090/meldekortservice/internal/apidocs/index.html?url=swagger.json`.  
Om Meldekort journalføring: https://confluence.adeo.no/pages/viewpage.action?pageId=431009242

For å sjekke om det finnes nye versjoner av avhengigheter, kan man kjøre: `./gradlew dependencyUpdates`
