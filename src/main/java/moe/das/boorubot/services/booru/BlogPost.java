package moe.das.boorubot.services.booru;

import com.apptasticsoftware.rssreader.Item;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public class BlogPost {
    private static final Pattern idPattern = Pattern.compile("https://blog.sakugabooru.com/\\d{4}/\\d{2}/\\d{2}/(?<id>.+)/(?:\\?.+)?");
    private final String title;
    private final String description;
    private final String url;
    private String imageUrl;

    private BlogPost(String title, String description, String url) {
        this.title = title;
        this.description = description;
        this.url = url;
    }

    public static BlogPost fromRssItem(Item item) throws NoSuchElementException {
        var titel = item.getTitle().orElseThrow();
        var description = item.getDescription().orElseThrow().replaceAll("<span class=\"screen-reader-text\">.*</span>", "");
        var link = item.getLink().orElseThrow();

        return new BlogPost(titel, description, link);
    }

    public String getId() {
        var matcher = idPattern.matcher(url);
        var extractionSuccessful = matcher.find();

        if (extractionSuccessful) {
            return matcher.group("id");
        } else {
            return title;
        }
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean hasImage() {
        return imageUrl != null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        BlogPost blogPost = (BlogPost) other;

        return title.equals(blogPost.title);
    }

    @Override
    public int hashCode() {
        return title.hashCode();
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
