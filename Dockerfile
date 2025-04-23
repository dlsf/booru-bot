FROM eclipse-temurin:21.0.7_6-jdk-jammy as build

COPY . .
RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:21.0.7_6-jdk-jammy as runtime

WORKDIR /app

COPY --from=build /build/install/BooruBot/bin/BooruBot bin/BooruBot
COPY --from=build /build/install/BooruBot/lib/ lib/

ENTRYPOINT ["bin/BooruBot"]
