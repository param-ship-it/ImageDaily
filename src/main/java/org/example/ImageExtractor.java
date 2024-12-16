package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ImageExtractor {
    private static final String WEBSITE_URL = "https://teluguchristiangateway.wordpress.com/category/verse-of-the-day/";

    public static String extractLatestImageUrl() throws IOException {
        // Connect to the website
        Document doc = Jsoup.connect(WEBSITE_URL)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get();

        // Find all posts in the Verse of the Day category
        Elements posts = doc.select("article.post");

        // Variables to track the most recent post
        Element latestPost = null;
        LocalDate latestDate = null;

        // Iterate through posts to find the most recent one
        for (Element post : posts) {
            // Try to extract the date from the post
            Element dateElement = post.select("time.published").first();
            if (dateElement == null) {
                continue;
            }

            // Parse the date 
            String dateString = dateElement.attr("datetime");
            LocalDate postDate = parseWordPressDate(dateString);

            // Update latest post if this is more recent
            if (latestDate == null || postDate.isAfter(latestDate)) {
                latestDate = postDate;
                latestPost = post;
            }
        }

        // If no post found, throw an exception
        if (latestPost == null) {
            throw new IOException("No posts found in the Verse of the Day category");
        }

        // Find the image in the latest post
        Element imageElement = latestPost.select("img.wp-post-image").first();

        if (imageElement == null) {
            throw new IOException("No image found in the latest post");
        }

        // Return the absolute URL of the image
        return imageElement.absUrl("src");
    }

    private static LocalDate parseWordPressDate(String dateString) {
        try {
            // WordPress typically uses ISO 8601 format
            return LocalDate.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            // Fallback to current date if parsing fails
            System.out.println("Could not parse date: " + dateString);
            return LocalDate.now(ZoneId.of("Asia/Kolkata"));
        }
    }

    public static void main(String[] args) {
        try {
            String latestImageUrl = extractLatestImageUrl();
            System.out.println("Latest Image URL: " + latestImageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}