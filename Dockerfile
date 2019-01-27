FROM openjdk:8u171-jdk-alpine3.8 as builder

ADD . /app
WORKDIR /app

RUN ./gradlew :shadowJar && \
    mv build/libs/server-0.1.jar /server.jar

FROM openjdk:8u171-jre-alpine3.8 as environment
WORKDIR /app
COPY --from=builder /server.jar .
ENTRYPOINT java -jar /app/server.jar