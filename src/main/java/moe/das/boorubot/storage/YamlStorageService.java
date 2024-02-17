package moe.das.boorubot.storage;

import moe.das.boorubot.services.booru.BlogPost;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class YamlStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(YamlStorageService.class);
    private final YamlFile yamlFile;

    public YamlStorageService() {
        this.yamlFile = new YamlFile("data.yml");

        try {
            this.yamlFile.createOrLoad();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void setPostStatus(BlogPost blogPost, PostStatus postStatus) {
        this.yamlFile.set(blogPost.getId(), postStatus.name());

        try {
            this.yamlFile.save();
        } catch (IOException exception) {
            logger.error("Failed to save to storage", exception);
        }
    }

    @Override
    public PostStatus getPostStatus(BlogPost blogPost) {
        var status = this.yamlFile.getString(blogPost.getId());
        if (status == null) return PostStatus.UNKNOWN;

        return PostStatus.valueOf(status);
    }
}
