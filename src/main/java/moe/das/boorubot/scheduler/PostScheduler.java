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

/**
 * Service responsible for combining the {@link BooruService} and {@link AnilistService}.
 * Periodically fetches the RSS feed, checks for new blog posts and schedules their respective anilist posts.
 * <p>
 * It uses a stack internally so the posts are in the correct order during the first startup.
 */
public class PostScheduler {
    private static final Logger logger = LoggerFactory.getLogger(PostScheduler.class);
    private final BooruService booruService;
    private final AnilistService anilistService;
    private final StorageService storageService;
    private final Stack<BlogPost> stack;
    private final Duration cooldownDuration;
    private long nextPostTimestamp = 0;

    /**
     * Service responsible for combining the {@link BooruService} and {@link AnilistService}.
     *
     * @param booruService the SakugaBooru service for fetching new blog entries
     * @param anilistService the anilist service for posting the blog entries
     * @param storageService the storage service for persistent information about each blog entry
     * @param cooldownTime the minimum time between two successful anilist posts
     */
    public PostScheduler(BooruService booruService, AnilistService anilistService, StorageService storageService, long cooldownTime) {
        this.stack = new Stack<>();
        this.booruService = booruService;
        this.anilistService = anilistService;
        this.storageService = storageService;
        this.cooldownDuration = Duration.of(cooldownTime, ChronoUnit.MINUTES);
    }

    /**
     * Starts the schedulers needed for this service.
     */
    public void start() {
        try (var executorService = Executors.newSingleThreadScheduledExecutor()) {
            executorService.scheduleWithFixedDelay(this::checkForNewPosts, 0, 15, TimeUnit.MINUTES);
            executorService.scheduleWithFixedDelay(this::checkQueue, 10, 10, TimeUnit.SECONDS);

            logger.info("Successfully scheduled tasks!");
        }
    }

    /**
     * Checks if there have been any new blog posts not present in the {@link StorageService}
     * and schedules them to be posted. It also handles the manual scraping of the image URLs.
     */
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

    /**
     * Schedules a post to be posted to anilist.
     * Calling this method does not have any immediate consequences, since the stack/queue is checked periodically.
     * {@link BlogPost}s that are already scheduled to be posted are being ignored.
     *
     * @param blogPost the blog post that should be posted to anilist
     */
    public void schedulePost(BlogPost blogPost) {
        if (stack.contains(blogPost)) return;

        stack.add(blogPost);
        storageService.setPostStatus(blogPost, PostStatus.PENDING);
        logger.info("Scheduled new blog entry post... " + blogPost);
    }

    /**
     * Checks for newly scheduled blog posts and posts them on anilist if the service is not on cooldown.
     * The name of this method is misleading because it uses a stack internally, but this sounds way cooler.
     */
    private void checkQueue() {
        // Don't run anything if there's nothing to be posted or the cooldown is not up
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
}
