package moe.das.boorubot.services.anilist;

import moe.das.boorubot.services.booru.BlogPost;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Handles the communication with the public anilist GraphQL API.
 */
public class AnilistService {
    private static final Logger logger = LoggerFactory.getLogger(AnilistService.class);
    private static final OkHttpClient client = new OkHttpClient.Builder().build();
    private final String authToken;
    private final String infoUrl;

    /**
     * Handles the communication with the public anilist GraphQL API.
     *
     * @param authToken the auth token provided by anilist
     * @param infoUrl an url to further information about the bot
     */
    public AnilistService(String authToken, String infoUrl) {
        this.authToken = authToken;
        this.infoUrl = infoUrl;
    }

    /**
     * Immediately posts the provided {@link BlogPost} to anilist.
     *
     * @param blogPost the blog post which should be posted
     * @return true if the operation was successful
     * @throws IOException if there was an exception while communicating with the anilist API
     */
    public boolean postBlogEntry(BlogPost blogPost) throws IOException {
        if (blogPost.hasImage()) {
            return postAnilistStatus(String.format("<center><h1>%s</h1><hr>%simg500(%s)<p><h6>[About this post](%s)</h6></center>", blogPost.getTitle(), blogPost.getDescription(), blogPost.getImageUrl(), infoUrl));
        } else {
            return postAnilistStatus(String.format("<center><h1>%s</h1><hr>%s<p><h6>[About this post](%s)</h6></center>", blogPost.getTitle(), blogPost.getDescription(), infoUrl));
        }
    }

    /**
     * Creates a new status post with the provided text on anilist via the GraphQL API.
     *
     * @param text the text that should be sent
     * @return true if the operation was successful
     * @throws IOException if there was an exception while communicating with the anilist API
     */
    private boolean postAnilistStatus(String text) throws IOException {
        var query = """
                mutation($text: String) {
                  SaveTextActivity(text: $text) {
                    id
                    siteUrl
                  }
                }
                """;
        String variables = String.format("{\"text\": \"%s\"}", StringEscapeUtils.escapeJson(text));
        return callGraphQL(query, variables);
    }

    /**
     * Performs a raw GraphQL call to the anilist API.
     *
     * @param query the GraphQL query of this call
     * @param variables the variables that belong to the query
     * @return true if the operation was successful
     * @throws IOException if there was an exception while communicating with the anilist API
     */
    private boolean callGraphQL(String query, String variables) throws IOException {
        var requestBody = new FormBody.Builder()
                .add("query", query)
                .add("variables", variables)
                .build();

        var request = new Request.Builder()
                .url("https://graphql.anilist.co/")
                .header("Authorization", "Bearer " + authToken)
                .post(requestBody)
                .build();

        var call = client.newCall(request);
        try (var response = call.execute()) {
            var responseBody = response.body();
            if (response.code() == 200 && responseBody != null) {
                responseBody.string();
                logger.info("Successfully posted data to AL");
                return true;
            } else {
                logger.error("Failed to post status. Got error " + response.code() + " with body " + (responseBody == null ? null : responseBody.string()));
                return false;
            }
        }
    }
}
