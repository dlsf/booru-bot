package moe.das.boorubot.services.booru;

import com.apptasticsoftware.rssreader.RssReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Handles the communication with the public SakugaBooru RSS feed.
 */
public class BooruService {
    private static final Pattern imagePattern = Pattern.compile("<a[^<>]*href=\"(?<link>[^\"]+)\"[^<]*<img[^<>]*src=\"(?<img>[^\"]+)\"[^<]*</a>");

    /**
     * Checks the RSS feed and contains a list of the (by default 10) latest blog posts.
     *
     * @return the latest blog posts
     * @throws IOException if the communication with SakugaBooru failed
     */
    public Stream<BlogPost> fetchBlogPosts() throws IOException {
        var rssReader = new RssReader();
        return rssReader.read("https://blog.sakugabooru.com/feed/").map(BlogPost::fromRssItem);
    }

    /**
     * Attempts to scrape images for each blog entry from the HTML of the SakugaBooru website.
     * This is prone to break, but currently the only way.
     *
     * @return a list of all {@link BlogImage}s that can be found on the homepage of SakugaBooru
     * @throws IOException if the communication with SakugaBooru failed
     */
    public List<BlogImage> scrapeImages() throws IOException {
        final OkHttpClient client = new OkHttpClient.Builder().build();
        var request = new Request.Builder()
                .url("https://blog.sakugabooru.com/")
                .get()
                .build();

        try (var response = client.newCall(request).execute()) {
            var htmlBody = response.body();
            if (htmlBody == null) return List.of();
            var html = htmlBody.string().replace("\n", "");

            var images = new ArrayList<BlogImage>();
            var matcher = imagePattern.matcher(html);
            while (matcher.find()) {
                images.add(new BlogImage(matcher.group("link"), matcher.group("img")));
            }

            return images;
        }
    }
}
