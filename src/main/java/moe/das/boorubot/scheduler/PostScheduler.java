package moe.das.boorubot.scheduler;

import moe.das.boorubot.services.anilist.AnilistService;
import moe.das.boorubot.services.booru.BlogPost;
import moe.das.boorubot.services.booru.BooruService;
import moe.das.boorubot.storage.PostStatus;
import moe.das.boorubot.storage.StorageService;
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
    private final BooruService booruService;
    private final AnilistService anilistService;
    private final StorageService storageService;
    private final Stack<BlogPost> stack;
    private final Duration cooldownDuration;
    private long nextPostTimestamp = 0;

    public PostScheduler(BooruService booruService, AnilistService anilistService, StorageService storageService, long cooldownTime) {
        this.stack = new Stack<>();
        this.booruService = booruService;
        this.anilistService = anilistService;
        this.storageService = storageService;
        this.cooldownDuration = Duration.of(cooldownTime, ChronoUnit.MINUTES);
    }

    public void start() {
        try (var executorService = Executors.newSingleThreadScheduledExecutor()) {
            executorService.scheduleWithFixedDelay(this::checkQueue, 10, 10, TimeUnit.SECONDS);
            executorService.scheduleWithFixedDelay(this::checkForNewPosts, 0, 15, TimeUnit.MINUTES);

            logger.info("Successfully scheduled tasks!");
        }
    }

    public void schedulePost(BlogPost blogPost) {
        if (stack.contains(blogPost)) return;

        stack.add(blogPost);
        storageService.setPostStatus(blogPost, PostStatus.PENDING);
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
                logger.info("Successfully published blog post activity! Writing to storage...");
                storageService.setPostStatus(post, PostStatus.POSTED);
                logger.info("Wrote to storage");
            } else {
                logger.error("Failed to post status, retrying...");
                nextPostTimestamp = System.currentTimeMillis();
            }
        } catch (IOException exception) {
            logger.error("Failed to post activity", exception);
            nextPostTimestamp = System.currentTimeMillis();
        }
    }

    private void checkForNewPosts() {
        try {
            var images = booruService.scrapeImages();
            booruService.fetchBlogPosts()
                    .peek(blogPost -> {
                        // Try to assign manually scraped images to every blog post
                        for (var image : images) {
                            if (!blogPost.getUrl().contains(image.entryUrl())) continue;
                            blogPost.setImageUrl(image.imageUrl());
                        }
                    })
                    .filter(blogPost -> storageService.getPostStatus(blogPost) != PostStatus.POSTED)
                    .filter(blogPost -> !stack.contains(blogPost))
                    .forEach(this::schedulePost);
            logger.info("Checked for new posts");
        } catch (IOException exception) {
            logger.error("Failed to check for new blog posts", exception);
        }
    }
}
