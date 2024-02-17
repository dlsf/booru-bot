package moe.das.boorubot.storage;

import moe.das.boorubot.services.booru.BlogPost;
import org.simpleyaml.configuration.file.YamlFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Service handling persistent storage via a YAML file. Holds information about {@link BlogPost}s and their {@link PostStatus}.
 */
public class YamlStorageService implements StorageService {
    private static final Logger logger = LoggerFactory.getLogger(YamlStorageService.class);
    private final YamlFile yamlFile;

    /**
     * Service handling persistent storage via a YAML file.
     */
    public YamlStorageService() {
        this.yamlFile = new YamlFile("data.yml");

        try {
            this.yamlFile.createOrLoad();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Updates the {@link PostStatus} of the {@link BlogPost}, overwriting and ignoring the previous value.
     *
     * @param blogPost the blog post whose status should be updated
     * @param postStatus the new status of the blog post
     */
    @Override
    public void setPostStatus(BlogPost blogPost, PostStatus postStatus) {
        this.yamlFile.set(blogPost.getId(), postStatus.name());

        try {
            this.yamlFile.save();
        } catch (IOException exception) {
            logger.error("Failed to save to storage", exception);
        }
    }

    /**
     * Returns the current {@link PostStatus} of the blog post.
     *
     * @param blogPost the blog post whose status should be checked
     * @return the status of the blog post
     */
    @Override
    public PostStatus getPostStatus(BlogPost blogPost) {
        var status = this.yamlFile.getString(blogPost.getId());
        if (status == null) return PostStatus.UNKNOWN;

        return PostStatus.valueOf(status);
    }
}
