package moe.das.boorubot.services.scheduler;

import moe.das.boorubot.services.anilist.AnilistService;
import moe.das.boorubot.services.booru.BlogPost;
import moe.das.boorubot.services.booru.BooruService;
import moe.das.boorubot.services.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PostScheduler {
    private static final Logger logger = LoggerFactory.getLogger(PostScheduler.class);
    private final Stack<BlogPost> stack;
    private final BooruService booruService;
    private final AnilistService anilistService;
    private final StorageService storageService;
    private final Duration cooldownDuration;
    private long nextPostTimestamp = 0;

    public PostScheduler(BooruService booruService, AnilistService anilistService, StorageService storageService, long cooldownTime) {
        this.stack = new Stack<>();
        this.booruService = booruService;
        this.anilistService = anilistService;
        this.storageService = storageService;
        this.cooldownDuration = Duration.of(cooldownTime, ChronoUnit.MINUTES);
    }

    public void scheduleTasks() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this::checkQueue, 10, 10, TimeUnit.SECONDS);
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(this::checkForNewPosts, 0, 15, TimeUnit.MINUTES);
    }

    public void schedulePost(BlogPost blogPost) {
        stack.add(blogPost);
        storageService.setPostStatus(blogPost, StorageService.PostStatus.PENDING);
        logger.info("Scheduled new blog entry post... " + blogPost);
    }

    private void checkQueue() {
        if (stack.isEmpty()) return;
        if (System.currentTimeMillis() < nextPostTimestamp) return;

        nextPostTimestamp = System.currentTimeMillis() + cooldownDuration.toMillis();

        try {
            var post = stack.pop();
            var success = anilistService.postBlogEntry(Objects.requireNonNull(post));

            if (success) {
                logger.info("Successfully published blog post activity");
                logger.info("Writing to storage...");
                storageService.setPostStatus(post, StorageService.PostStatus.POSTED);
                logger.info("Wrote to storage");
            } else {
                logger.error("Failed to post status");
                nextPostTimestamp = System.currentTimeMillis();
            }
        } catch (IOException exception) {
            logger.error("Failed to post activity", exception);
            nextPostTimestamp = System.currentTimeMillis();
        }
    }

    private void checkForNewPosts() {
        try {
            var images = booruService.extractImages();
            booruService.fetchBlogPosts()
                    .peek(blogPost -> {
                        for (var image : images) {
                            if (!blogPost.getUrl().contains(image.getEntryUrl())) continue;
                            blogPost.setImageUrl(image.getImageUrl());
                        }
                    })
                    .filter(blogPost -> storageService.getPostStatus(blogPost) != StorageService.PostStatus.POSTED)
                    .forEach(this::schedulePost);
        } catch (IOException exception) {
            logger.error("Failed to check for new blog posts", exception);
        }
    }
}
