package moe.das.boorubot.storage;

import moe.das.boorubot.services.booru.BlogPost;

/**
 * Interface for a persistent storage service. Holds information about {@link BlogPost}s and their {@link PostStatus}.
 */
public interface StorageService {
    /**
     * Updates the {@link PostStatus} of the {@link BlogPost}, overwriting and ignoring the previous value.
     *
     * @param blogPost the blog post whose status should be updated
     * @param postStatus the new status of the blog post
     */
    void setPostStatus(BlogPost blogPost, PostStatus postStatus);

    /**
     * Returns the current {@link PostStatus} of the blog post.
     *
     * @param blogPost the blog post whose status should be checked
     * @return the status of the blog post
     */
    PostStatus getPostStatus(BlogPost blogPost);
}
