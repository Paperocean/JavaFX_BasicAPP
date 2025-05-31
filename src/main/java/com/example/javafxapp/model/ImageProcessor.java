package com.example.javafxapp.model;

import javafx.scene.image.*;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

public class ImageProcessor {

    /**
     * Konwersja JavaFX Image do BufferedImage bez SwingFXUtils
     */
    public BufferedImage imageToBufferedImage(Image image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();

        int[] pixels = new int[width * height];
        PixelReader pixelReader = image.getPixelReader();

        WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();
        pixelReader.getPixels(0, 0, width, height, format, pixels, 0, width);

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        bufferedImage.setRGB(0, 0, width, height, pixels, 0, width);

        return bufferedImage;
    }

    /**
     * Konwersja BufferedImage do JavaFX Image bez SwingFXUtils
     */
    public Image bufferedImageToImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            throw new IllegalArgumentException("BufferedImage cannot be null");
        }

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int[] pixels = new int[width * height];
        bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);

        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        WritablePixelFormat<IntBuffer> format = PixelFormat.getIntArgbInstance();
        pixelWriter.setPixels(0, 0, width, height, format, pixels, 0, width);

        return writableImage;
    }

    /**
     * Zastosowanie negatywu obrazu z zachowaniem kanału alfa
     */
    public BufferedImage applyNegative(BufferedImage original) {
        if (original == null) {
            throw new IllegalArgumentException("Original image cannot be null");
        }

        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage negative = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = original.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = 255 - ((rgba >> 16) & 0xFF);
                int g = 255 - ((rgba >> 8) & 0xFF);
                int b = 255 - (rgba & 0xFF);
                int negativeRgb = (a << 24) | (r << 16) | (g << 8) | b;
                negative.setRGB(x, y, negativeRgb);
            }
        }

        return negative;
    }

    /**
     * Progowanie obrazu z zachowaniem kanału alfa
     */
    /**
     * Progowanie obrazu z zachowaniem kanału alfa - poprawiona wersja
     */
    public BufferedImage applyThreshold(BufferedImage original, int threshold) {
        if (original == null) {
            throw new IllegalArgumentException("Original image cannot be null");
        }
        if (threshold < 0 || threshold > 255) {
            throw new IllegalArgumentException("Threshold must be between 0 and 255");
        }

        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage thresholded = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = original.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;

                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);

                int newValue = gray > threshold ? 255 : 0;
                int newRgb = (a << 24) | (newValue << 16) | (newValue << 8) | newValue;

                thresholded.setRGB(x, y, newRgb);
            }
        }

        return thresholded;
    }


    /**
     * Detekcja krawędzi metodą Sobela z zachowaniem kanału alfa
     */
    public BufferedImage applyEdgeDetection(BufferedImage original) {
        if (original == null) {
            throw new IllegalArgumentException("Original image cannot be null");
        }

        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage edges = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        int[][] sobelX = {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}};
        int[][] sobelY = {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}};

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                    edges.setRGB(x, y, 0xFF000000);
                }
            }
        }

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int gx = 0, gy = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        int rgba = original.getRGB(x + j, y + i);
                        int r = (rgba >> 16) & 0xFF;
                        int g = (rgba >> 8) & 0xFF;
                        int b = rgba & 0xFF;
                        int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);

                        gx += gray * sobelX[i + 1][j + 1];
                        gy += gray * sobelY[i + 1][j + 1];
                    }
                }

                int magnitude = (int) Math.sqrt(gx * gx + gy * gy);
                magnitude = Math.min(255, magnitude);

                int edgeRgb = (0xFF << 24) | (magnitude << 16) | (magnitude << 8) | magnitude;
                edges.setRGB(x, y, edgeRgb);
            }
        }

        return edges;
    }

    /**
     * Obrót obrazu z zachowaniem jakości i kanału alfa
     */
    public BufferedImage rotateImage(BufferedImage original, int degrees) {
        if (original == null) {
            throw new IllegalArgumentException("Original image cannot be null");
        }

        double radians = Math.toRadians(degrees);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));

        int width = original.getWidth();
        int height = original.getHeight();

        int newWidth = (int) Math.ceil(width * cos + height * sin);
        int newHeight = (int) Math.ceil(height * cos + width * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setComposite(AlphaComposite.Clear);
        g2d.fillRect(0, 0, newWidth, newHeight);
        g2d.setComposite(AlphaComposite.SrcOver);

        AffineTransform at = new AffineTransform();
        at.translate((newWidth - width) / 2.0, (newHeight - height) / 2.0);
        at.rotate(radians, width / 2.0, height / 2.0);

        g2d.setTransform(at);
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    /**
     * Skalowanie obrazu z wysoką jakością interpolacji
     */
    public BufferedImage scaleImage(BufferedImage original, int newWidth, int newHeight) {
        if (original == null) {
            throw new IllegalArgumentException("Original image cannot be null");
        }
        if (newWidth <= 0 || newHeight <= 0) {
            throw new IllegalArgumentException("New dimensions must be positive");
        }
        if (newWidth > 3000 || newHeight > 3000) {
            throw new IllegalArgumentException("New dimensions cannot exceed 3000 pixels");
        }

        BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        return scaled;
    }

    /**
     * Zapis obrazu z obsługą różnych formatów
     */
    public void saveImage(BufferedImage image, File file) throws IOException {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        String fileName = file.getName().toLowerCase();
        String format;

        if (fileName.endsWith(".png")) {
            format = "png";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            format = "jpg";
            if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
                BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = rgbImage.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
                image = rgbImage;
            }
        } else {
            format = "jpg";
            if (image.getType() == BufferedImage.TYPE_INT_ARGB) {
                BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = rgbImage.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
                g2d.drawImage(image, 0, 0, null);
                g2d.dispose();
                image = rgbImage;
            }
        }

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        ImageIO.write(image, format, file);
    }

    /**
     * Metoda pomocnicza do tworzenia kopii obrazu
     */
    public BufferedImage copyImage(BufferedImage original) {
        if (original == null) {
            throw new IllegalArgumentException("Original image cannot be null");
        }

        BufferedImage copy = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
        Graphics2D g2d = copy.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        return copy;
    }

    /**
     * Sprawdzenie czy obraz ma kanał alfa
     */
    public boolean hasAlphaChannel(BufferedImage image) {
        if (image == null) {
            return false;
        }
        return image.getColorModel().hasAlpha();
    }

    /**
     * Konwersja obrazu do skali szarości
     */
    public BufferedImage convertToGrayscale(BufferedImage original) {
        if (original == null) {
            throw new IllegalArgumentException("Original image cannot be null");
        }

        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage grayscale = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgba = original.getRGB(x, y);
                int a = (rgba >> 24) & 0xFF;
                int r = (rgba >> 16) & 0xFF;
                int g = (rgba >> 8) & 0xFF;
                int b = rgba & 0xFF;

                int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
                int grayRgb = (a << 24) | (gray << 16) | (gray << 8) | gray;

                grayscale.setRGB(x, y, grayRgb);
            }
        }

        return grayscale;
    }

    /**
     * Automatyczne obliczenie progu metodą Otsu
     */
    public int calculateOtsuThreshold(BufferedImage image) {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        // Konwersja do skali szarości jeśli potrzeba
        BufferedImage grayImage = convertToGrayscale(image);

        int[] histogram = new int[256];
        int totalPixels = grayImage.getWidth() * grayImage.getHeight();

        // Obliczenie histogramu
        for (int y = 0; y < grayImage.getHeight(); y++) {
            for (int x = 0; x < grayImage.getWidth(); x++) {
                int rgb = grayImage.getRGB(x, y);
                int gray = rgb & 0xFF; // Wartość składowej niebieskiej (wszystkie są równe w skali szarości)
                histogram[gray]++;
            }
        }

        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0;
        int wB = 0, wF = 0;
        double maxVariance = 0;
        int threshold = 0;

        for (int i = 0; i < 256; i++) {
            wB += histogram[i];
            if (wB == 0) continue;

            wF = totalPixels - wB;
            if (wF == 0) break;

            sumB += i * histogram[i];
            double mB = sumB / wB;
            double mF = (sum - sumB) / wF;
            double variance = wB * wF * (mB - mF) * (mB - mF);

            if (variance > maxVariance) {
                maxVariance = variance;
                threshold = i;
            }
        }

        return threshold;
    }
}
