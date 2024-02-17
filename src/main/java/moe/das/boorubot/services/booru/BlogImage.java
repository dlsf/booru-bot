package moe.das.boorubot.services.booru;

/**
 * Represents an image of a {@link BlogPost}.
 * It is not directly provided via the RSS feed and therefore has to be scraped.
 *
 * @param entryUrl the URL to the blog post this image belongs to
 * @param imageUrl the URL to the image
 */
public record BlogImage(String entryUrl, String imageUrl) {
}
