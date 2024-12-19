FROM gcr.io/distroless/java21-debian12

COPY build/libs/meldekortservice-all.jar /app.jar
CMD ["/app.jar"]
