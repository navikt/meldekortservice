FROM navikt/java:13-appdynamics
COPY build/libs/meldekortservice.jar /app/app.jar
EXPOSE 8090