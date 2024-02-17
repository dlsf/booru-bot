package moe.das.boorubot.storage;

/**
 * Represents the status of a {@link moe.das.boorubot.services.booru.BlogPost}.
 */
public enum PostStatus {
    /**
     * The blog post has never been seen before and should be posted eventually.
     */
    UNKNOWN,

    /**
     * The blog post is scheduled to be posted.
     */
    PENDING,

    /**
     * The blog post has been posted successfully.
     */
    POSTED
}
