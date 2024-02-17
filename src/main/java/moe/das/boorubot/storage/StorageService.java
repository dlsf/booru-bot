package moe.das.boorubot.storage;

import moe.das.boorubot.services.booru.BlogPost;

public interface StorageService {
    void setPostStatus(BlogPost blogPost, PostStatus postStatus);
    PostStatus getPostStatus(BlogPost blogPost);
}
