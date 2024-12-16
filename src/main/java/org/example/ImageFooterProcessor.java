package org.example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageFooterProcessor {
    // Church and Pastor Details
    private static final String PASTOR_NAME = "Rev. Dr. Paul Raju";
    private static final String CHURCH_NAME = "Telugu Christian Gateway";
    private static final String CHURCH_PHONE = "+91 9876543210";
    private static final String CHURCH_ADDRESS = "Hyderabad, Telangana";

    public static BufferedImage addAdvancedFooter(BufferedImage originalImage) {
        // Define footer dimensions
        int footerHeight = 200;
        int newImageWidth = originalImage.getWidth();
        int newImageHeight = originalImage.getHeight() + footerHeight;

        // Create new buffered image with extended height
        BufferedImage extendedImage = new BufferedImage(
                newImageWidth,
                newImageHeight,
                BufferedImage.TYPE_INT_ARGB
        );

        // Get graphics context
        Graphics2D g2d = extendedImage.createGraphics();

        // Set rendering hints for better text quality
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
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString(PASTOR_NAME, 50, originalImage.getHeight() + 60);

        // Church Name - Slightly smaller
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(CHURCH_NAME, 50, originalImage.getHeight() + 90);

        // Contact Details
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("Contact: " + CHURCH_PHONE, 50, originalImage.getHeight() + 120);
        g2d.drawString(CHURCH_ADDRESS, 50, originalImage.getHeight() + 150);

        // Right side - Pastor Photo
        try {
            BufferedImage pastorPhoto = ImageIO.read(new File("pastor_photo1.jpg"));

            // Resize and crop photo to square
            int photoSize = 150;
            BufferedImage squarePhoto = new BufferedImage(photoSize, photoSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D photoG2d = squarePhoto.createGraphics();

            // Calculate scaling and cropping
            double scale = Math.max(
                    (double)photoSize / pastorPhoto.getWidth(),
                    (double)photoSize / pastorPhoto.getHeight()
            );

            int scaledWidth = (int)(pastorPhoto.getWidth() * scale);
            int scaledHeight = (int)(pastorPhoto.getHeight() * scale);

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
                    newImageWidth - 200,
                    originalImage.getHeight() + 25,
                    null
            );

        } catch (IOException e) {
            System.out.println("Pastor photo not found. Skipping photo.");
        }

        // Vertical separator line
        g2d.setColor(new Color(200, 200, 200));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(newImageWidth - 250, originalImage.getHeight(),
                newImageWidth - 250, originalImage.getHeight() + footerHeight);

        g2d.dispose();
        return extendedImage;
    }

    public static void main(String[] args) {
        try {
            // Load original image
            BufferedImage originalImage = ImageIO.read(new File("original_image.png"));

            // Process image with footer
            BufferedImage finalImage = addAdvancedFooter(originalImage);

            // Save processed image
            ImageIO.write(finalImage, "png", new File("final_image_with_footer.png"));

            System.out.println("Image processing complete!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}