FROM navikt/java:11
COPY build/libs/meldekortservice.jar /app/app.jar
EXPOSE 8090