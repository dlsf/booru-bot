# BooruBot

> [!IMPORTANT]
> [This project has been moved to Codeberg](https://codeberg.org/das/booru-bot)

A Java application which automatically reposts SakugaBooru RSS feed entries to AL via the GraphQL API.

## Running the bot

### Via docker-compose (preferred)
Check the provided [docker-compose.yml](docker-compose.yml) or manually build the image using the [Dockerfile](Dockerfile).

### Building from source via Gradle
Run `./gradlew installDist`, afterwards the application with all its dependencies is located in `build/install/BooruBot`. You can run it via the sh (Unix) & batch (Windows) files in the `bin` directory.

## Configuring the bot

Copy the [config.yml.example](config.yml.example) into the directory of the application as `config.yml`, adjust the values and start the bot.

## Contributing

Issues and pull requests are always welcome! :)
