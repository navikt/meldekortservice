# Meldekortservice

Microservice / proxy som henter data fra meldekort ORDS (Arena DB).

# Kom i gang
1. Bygg meldekortservice ved å kjøre `gradle clean build`. Dette vil også kjøre testene.
2. Start lokal instans av Postgres ved å kjøre `docker-compose up -d`.
3. Start appen ved å kjøre Server.kt sin main-metode eller kjør `gradle runServer`.
4. Appen starter på http://localhost:8090. Sjekk for eksempel at ping svarer på http://localhost:8090/meldekortservice/internal/ping.

# Feilsøking
For å være sikker på at man får en ny tom database kan man kjøre kommandoen: `docker-compose down -v`.

# Henvendelser
Spørsmål knyttet til koden eller prosjektet kan rettes mot https://github.com/orgs/navikt/teams/meldekort.

## For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #meldekort.

## Dokumentasjon
Dokumentasjon finnes i [Confluence](https://confluence.adeo.no/display/TMP/Meldekort-api).  
Swagger er tilgjengelig på `meldekortservice/internal/apidocs`.  
Om Meldekort journalføring: https://confluence.adeo.no/x/uY6wGQ