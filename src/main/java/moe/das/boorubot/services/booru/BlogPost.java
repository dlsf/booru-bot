package moe.das.boorubot.services.booru;

import com.apptasticsoftware.rssreader.Item;

import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Represents a SakugaBooru blog post.
 */
public class BlogPost {
    private static final Pattern idPattern = Pattern.compile("https://blog.sakugabooru.com/\\d{4}/\\d{2}/\\d{2}/(?<id>.+)/(?:\\?.+)?");
    private final String title;
    private final String description;
    private final String url;
    private String imageUrl;

    /**
     * Represents a SakugaBooru blog post.
     *
     * @param title the full, human-readable title of the blog post
     * @param description the short description of the blog post
     * @param url the URL to the blog post
     */
    private BlogPost(String title, String description, String url) {
        this.title = title;
        this.description = description;
        this.url = url;
    }

    /**
     * Takes an item from an RSS feed and converts it into its respective representation as a blog post.
     *
     * @param item the RSS item which should be converted into a blog post
     * @return the {@link BlogPost} which represents the RSS item
     * @throws NoSuchElementException if the RSS item is missing required fields. Should never be thrown.
     */
    public static BlogPost fromRssItem(Item item) throws NoSuchElementException {
        var titel = item.getTitle().orElseThrow();
        var description = item.getDescription().orElseThrow().replaceAll("<span class=\"screen-reader-text\">.*</span>", "");
        var link = item.getLink().orElseThrow();

        return new BlogPost(titel, description, link);
    }

    /**
     * The (hopefully) unique representation of this blog post.
     * Tries to extract a readable ID from the blog URL and falls back to the title.
     *
     * @return the ID of this blog post
     */
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
