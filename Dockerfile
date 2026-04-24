FROM gradle:8.12-jdk21 AS build

WORKDIR /app
COPY . .
RUN gradle clean :containers:monolith:build --no-daemon

FROM eclipse-temurin:21-jre-alpine

RUN mkdir /files

WORKDIR /app
COPY --from=build /app/containers/monolith/build/libs/monolith-all.jar ./monolith.jar
ENTRYPOINT ["java", "-jar", "monolith.jar"]
