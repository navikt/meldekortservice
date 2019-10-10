FROM navikt/java:8
COPY build/libs/meldekortservice.jar /app/app.jar
EXPOSE 8090