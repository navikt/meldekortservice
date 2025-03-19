FROM gcr.io/distroless/java21

ENV LANG='nb_NO.UTF-8' LANGUAGE='nb_NO:nb' LC_ALL='nb:NO.UTF-8' TZ="Europe/Oslo"

COPY build/libs/meldekortservice-all.jar /app.jar
CMD ["/app.jar"]
