package moe.das.boorubot.services.booru;

public class BlogImage {
    private final String entryUrl;
    private final String imageUrl;

    public BlogImage(String entryUrl, String imageUrl) {
        this.entryUrl = entryUrl;
        this.imageUrl = imageUrl;
    }

    public String getEntryUrl() {
        return entryUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
