package moe.das.boorubot.services.booru;

import com.apptasticsoftware.rssreader.Item;

public class BlogPost {
    private final String title;
    private final String description;
    private final String url;
    private String imageUrl;

    private BlogPost(String title, String description, String url) {
        this.title = title;
        this.description = description;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean hasImage() {
        return imageUrl != null;
    }

    public static BlogPost fromRssItem(Item item) {
        var titel = item.getTitle().orElseThrow();
        var description = item.getDescription().orElseThrow().replaceAll("<span class=\"screen-reader-text\">.*</span>", "");
        var link = item.getLink().orElseThrow();

        return new BlogPost(titel, description, link);
    }

    @Override
    public String toString() {
        return "BlogPost{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
