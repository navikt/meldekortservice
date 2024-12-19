FROM ghcr.io/navikt/baseimages/temurin:21

COPY build/libs/meldekortservice-all.jar /app/app.jar
