package moe.das.boorubot.services.booru;

import com.apptasticsoftware.rssreader.RssReader;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BooruService {
    private final Pattern imagePattern = Pattern.compile("<a[^<>]*href=\"(?<link>[^\"]+)\"[^<]*<img[^<>]*src=\"(?<img>[^\"]+)\"[^<]*</a>");

    public Stream<BlogPost> fetchBlogPosts() throws IOException {
        var rssReader = new RssReader();
        return rssReader.read("https://blog.sakugabooru.com/feed/").map(BlogPost::fromRssItem);
    }

    public List<BlogImage> extractImages() throws IOException {
        final OkHttpClient client = new OkHttpClient.Builder().build();
        var request = new Request.Builder()
                .url("https://blog.sakugabooru.com/")
                .get()
                .build();

        var html = client.newCall(request).execute().body().string().replace("\n", "");
        var matcher = imagePattern.matcher(html);

        List<BlogImage> images = new ArrayList<>();
        while (matcher.find()) {
            images.add(new BlogImage(matcher.group("link"), matcher.group("img")));
        }

        return images;
    }
}
