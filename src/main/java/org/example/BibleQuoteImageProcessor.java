package org.example;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import javax.imageio.ImageIO;

import javax.sql.DataSource;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

public class BibleQuoteImageProcessor {
    // Configuration Constants
    private static final String WEBSITE_URL = "https://teluguchristiangateway.wordpress.com/2023/03/";

    // Email Configuration
    private static final String EMAIL_FROM = "your-email@gmail.com";
    private static final String EMAIL_PASSWORD = "your-app-password";
    private static final String EMAIL_TO = "recipient@example.com";

    // Church and Pastor Details
    private static final String PASTOR_NAME = "Pas Jakariya HanumanthaRao";
    private static final String CHURCH_NAME = "Grace of Christ Ministries";
    private static final String CHURCH_PHONE = "+91 9848778617";
    private static final String CHURCH_ADDRESS = "Khammam, Telangana";

    public static String extractLatestImageUrl() throws IOException {
        Document doc = Jsoup.connect(WEBSITE_URL)
                .userAgent("Mozilla/5.0")
                .timeout(10000)
                .get();

        Element imageElement = doc.select("div > figure > img:first-of-type").first();

        if (imageElement == null) {
            throw new IOException("No image found on the page");
        }

        return imageElement.absUrl("src");
    }

    public static BufferedImage processImage(String imageUrl) throws IOException {
        // Read image from URL
        BufferedImage originalImage = ImageIO.read(new URL(imageUrl));

        // Add footer to the image
        return addAdvancedFooter(originalImage);
    }

    private static BufferedImage addAdvancedFooter(BufferedImage originalImage) {
        // Footer dimensions and processing (same as previous implementation)

        int footerHeight = 200;
        int newImageWidth = originalImage.getWidth();
        int newImageHeight = originalImage.getHeight() + footerHeight;
        Graphics2D g2dd = originalImage.createGraphics();

        // Enable anti-aliasing for smoother text
        g2dd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Set font and color for the text
        Font boldFont = new Font("Arial", Font.BOLD, 24);
        g2dd.setFont(boldFont);
        g2dd.setColor(Color.WHITE);

        // Get the current date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String dateText = dateFormat.format(new Date());

        // Measure the text size
        FontMetrics metrics = g2dd.getFontMetrics(boldFont);
        int textWidth = metrics.stringWidth(dateText);
        int textHeight = metrics.getHeight();

        // Define the background rectangle size and position
        int padding = 10;
        int rectX = 20; // Left margin
        int rectY = 20; // Top margin
        int rectWidth = textWidth + 2 * padding;
        int rectHeight = textHeight + 2 * padding;

        // Draw the background rectangle with a semi-transparent black color
        g2dd.setColor(new Color(0, 0, 0, 150)); // RGBA (black with 150 alpha for transparency)
        g2dd.fillRect(rectX, rectY, rectWidth, rectHeight);

        // Draw the date text on top of the rectangle
        g2dd.setColor(Color.WHITE);
        g2dd.drawString(dateText, rectX + padding, rectY + padding + metrics.getAscent());

        // Dispose of the graphics object
        g2dd.dispose();
        BufferedImage extendedImage = new BufferedImage(
                newImageWidth,
                newImageHeight,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = extendedImage.createGraphics();

        g2d.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR
        );

        // Draw original image
        g2d.drawImage(originalImage, 0, 0, null);

        // Footer background - light blue
        g2d.setColor(new Color(230, 240, 255));
        g2d.fillRect(0, originalImage.getHeight(), newImageWidth, footerHeight);

        // Left side - Text Details
        g2d.setColor(Color.BLACK);

        // Pastor Name - Large, Bold
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        g2d.drawString(PASTOR_NAME, 25, originalImage.getHeight() + 60);

        // Church Name - Slightly smaller
        g2d.setFont(new Font("Arial", Font.BOLD, 35));
        g2d.drawString(CHURCH_NAME, 150, originalImage.getHeight() + 110);

        // Contact Details
        g2d.setFont(new Font("Arial", Font.PLAIN, 33));
        g2d.drawString("Contact: " + CHURCH_PHONE, 170, originalImage.getHeight() + 150);
        g2d.drawString(CHURCH_ADDRESS, 190, originalImage.getHeight() + 190);

        // Right side - Placeholder for Pastor Photo
        try {
            BufferedImage pastorPhoto = ImageIO.read(BibleQuoteImageProcessor.class.getResourceAsStream("/pastor_photo.jpg"));

            // Resize and crop photo to square
            int photoSize =300;
            BufferedImage squarePhoto = new BufferedImage(photoSize, photoSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D photoG2d = squarePhoto.createGraphics();

            // Calculate scaling and cropping
            double scale = Math.max(
                    (double) photoSize / pastorPhoto.getWidth(),
                    (double) photoSize / pastorPhoto.getHeight()
            );

            int scaledWidth = (int) (pastorPhoto.getWidth()*scale );
            int scaledHeight = (int) (pastorPhoto.getHeight() *scale);

            int x = (scaledWidth - photoSize) / 2;
            int y = (scaledHeight - photoSize) / 2;

            photoG2d.drawImage(
                    pastorPhoto.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH),
                    -x, -y, null
            );
            photoG2d.dispose();

            // Draw photo on the right side
            g2d.drawImage(
                    squarePhoto,
                    newImageWidth - 300,
                    originalImage.getHeight() - 100,
                    null
            );

        } catch (IOException e) {
            System.out.println("Pastor photo not found. Skipping photo.");
        }

        // Vertical separator line
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawLine(newImageWidth - 303, originalImage.getHeight(),
                newImageWidth - 303, originalImage.getHeight() + footerHeight);

        g2d.dispose();
        return extendedImage;
    }

    public static void saveImageLocally(BufferedImage image) throws IOException {
        // Create output directory if it doesn't exist
        File outputDir = new File("daily_bible_quotes");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Generate filename with current date
        String filename = LocalDate.now().format(DateTimeFormatter.ISO_DATE) + "_bible_quote.png";
        File outputFile = new File(outputDir, filename);

        // Save the image
        ImageIO.write(image, "png", outputFile);
        System.out.println("Image saved locally: " + outputFile.getAbsolutePath());
    }

    public static void sendEmailWithImage(BufferedImage image) throws MessagingException, IOException, MessagingException {
        // Email configuration properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Create a session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_FROM, EMAIL_PASSWORD);
            }
        });

        // Create a new email message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(EMAIL_FROM));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(EMAIL_TO));
        message.setSubject("Daily Bible Quote - " + LocalDate.now());

        // Create multipart message
        Multipart multipart = new MimeMultipart();

        // Text part
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText("Daily Bible Quote Image Attached");

        // Image attachment part
        MimeBodyPart imagePart = new MimeBodyPart();

        // Convert BufferedImage to DataSource
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        DataSource dataSource = (DataSource) new ByteArrayDataSource(imageBytes, "image/png");
        imagePart.setDataHandler(new DataHandler((jakarta.activation.DataSource) dataSource));
        imagePart.setFileName("daily_bible_quote.png");

        // Add parts to multipart
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(imagePart);

        // Set content of message
        message.setContent(multipart);

        // Send the message
        Transport.send(message);
        System.out.println("Email sent successfully!");
    }

    public static void main(String[] args) {
        try {
            // Extract latest image URL
            String imageUrl = extractLatestImageUrl();

            // Process image
            BufferedImage processedImage = processImage(imageUrl);

            // Save image locally
            //saveImageLocally(processedImage);

            // Send email with the image
            sendEmailWithImage(processedImage);

            System.out.println("Daily Bible quote processed successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}