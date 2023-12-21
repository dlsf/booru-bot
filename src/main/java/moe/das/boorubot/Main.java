package moe.das.boorubot;

import moe.das.boorubot.services.anilist.AnilistService;
import moe.das.boorubot.services.booru.BooruService;
import moe.das.boorubot.services.scheduler.PostScheduler;
import moe.das.boorubot.services.storage.StorageService;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting service...");

        var config = new YamlFile("config.yml");
        try {
            config.createOrLoad();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        var authToken = config.getString("auth-token");
        logger.info("Found anilist auth token");

        var anilistService = new AnilistService(authToken);
        var booruService = new BooruService();
        var storageService = new StorageService();
        var postScheduler = new PostScheduler(booruService, anilistService, storageService);
        postScheduler.scheduleTasks();

        logger.info("Scheduled tasks");
    }

}
