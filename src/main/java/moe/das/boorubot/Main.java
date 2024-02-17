package moe.das.boorubot;

import moe.das.boorubot.scheduler.PostScheduler;
import moe.das.boorubot.services.anilist.AnilistService;
import moe.das.boorubot.services.booru.BooruService;
import moe.das.boorubot.storage.YamlStorageService;
import org.simpleyaml.configuration.file.YamlConfiguration;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * The entry point of this app.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * The entry point of this app.
     */
    public static void main(String[] args) {
        logger.info("Starting service...");

        // Read config values
        var config = initConfig();
        var authToken = config.getString("auth-token");
        var cooldownTime = config.getInt("cooldown-between-posts-minutes");
        var infoUrl = config.getString("info-url");

        logger.info("Successfully read config");

        // Initialize & Start services
        var anilistService = new AnilistService(authToken, infoUrl);
        var booruService = new BooruService();
        var storageService = new YamlStorageService();
        var postScheduler = new PostScheduler(booruService, anilistService, storageService, cooldownTime);
        postScheduler.start();

        logger.info("Initialized services");
    }

    /**
     * Loads the YAML configuration of this application, creating an empty one if it's not present.
     *
     * @return the {@link YamlConfiguration} of this application
     */
    private static YamlConfiguration initConfig() {
        var config = new YamlFile("config.yml");

        try {
            config.createOrLoad();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        return config;
    }
}
