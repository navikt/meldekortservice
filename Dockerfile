FROM navikt/java:12
COPY build/libs/meldekortservice.jar /app/app.jar
EXPOSE 8090