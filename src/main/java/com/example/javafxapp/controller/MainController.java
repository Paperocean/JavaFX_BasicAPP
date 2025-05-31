package com.example.javafxapp.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.example.javafxapp.model.ImageProcessor;
import com.example.javafxapp.util.Logger;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController implements Initializable {

    @FXML private ImageView originalImageView;
    @FXML private ImageView processedImageView;
    @FXML private ComboBox<String> operationComboBox;
    @FXML private Button executeButton;
    @FXML private Button loadButton;
    @FXML private Button saveButton;
    @FXML private Button rotateLeftButton;
    @FXML private Button rotateRightButton;
    @FXML private Button scaleButton;
    @FXML private Label statusLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Label authorLabel;
    @FXML private ImageView logoImageView;

    private BufferedImage originalImage;
    private BufferedImage processedImage;
    private ImageProcessor imageProcessor;
    private ExecutorService executorService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        imageProcessor = new ImageProcessor();
        executorService = Executors.newFixedThreadPool(4);

        setupUI();
        setupEventHandlers();

        Logger.getInstance().log("INFO", "Kontroler zainicjalizowany");
    }

    private void setupUI() {
        operationComboBox.getItems().addAll(
                "Negatyw", "Progowanie", "Konturowanie"
        );

        originalImageView.setFitWidth(400);
        originalImageView.setFitHeight(300);
        originalImageView.setPreserveRatio(true);

        processedImageView.setFitWidth(400);
        processedImageView.setFitHeight(300);
        processedImageView.setPreserveRatio(true);

        // Logo PWr
        try {
            Image logo = new Image(getClass().getResourceAsStream("/com/example/javafxapp/images/logo.png"));
            logoImageView.setImage(logo);
        } catch (Exception e) {
            Logger.getInstance().log("ERROR", "Nie można załadować logo: " + e.getMessage());
        }

        authorLabel.setText("© 2025 - Michał Wróblewski, Politechnika Wrocławska");

        executeButton.setDisable(true);
        saveButton.setDisable(true);
        rotateLeftButton.setDisable(true);
        rotateRightButton.setDisable(true);
        scaleButton.setDisable(true);

        progressBar.setVisible(false);
        statusLabel.setText("Gotowy do pracy");
    }

    private void setupEventHandlers() {
        loadButton.setOnAction(e -> loadImage());
        executeButton.setOnAction(e -> executeOperation());
        saveButton.setOnAction(e -> saveImage());
        rotateLeftButton.setOnAction(e -> rotateImage(-90));
        rotateRightButton.setOnAction(e -> rotateImage(90));
        scaleButton.setOnAction(e -> showScaleDialog());

        operationComboBox.setOnAction(e -> {
            executeButton.setDisable(operationComboBox.getValue() == null || originalImage == null);
        });
    }

    @FXML
    private void loadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Wybierz obraz");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JPEG Images", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) loadButton.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                Image image = new Image(file.toURI().toString());
                originalImageView.setImage(image);
                originalImage = imageProcessor.imageToBufferedImage(image);

                enableButtons(true);
                showToast("Obraz załadowany pomyślnie", "success");
                Logger.getInstance().log("INFO", "Załadowano obraz: " + file.getName());

            } catch (Exception e) {
                showToast("Błąd podczas ładowania obrazu", "error");
                Logger.getInstance().log("ERROR", "Błąd ładowania obrazu: " + e.getMessage());
            }
        }
    }

    @FXML
    private void executeOperation() {
        String operation = operationComboBox.getValue();
        if (operation == null || originalImage == null) {
            showToast("Wybierz operację i załaduj obraz", "warning");
            return;
        }

        int threshold = -1;
        if ("Progowanie".equals(operation)) {
            threshold = showThresholdDialog();
            if (threshold == -1) {
                showToast("Anulowano operację progowania", "warning");
                return;
            }
        }

        progressBar.setVisible(true);
        statusLabel.setText("Przetwarzanie...");
        executeButton.setDisable(true);

        final int finalThreshold = threshold;

        Task<BufferedImage> task = new Task<BufferedImage>() {
            @Override
            protected BufferedImage call() throws Exception {
                switch (operation) {
                    case "Negatyw":
                        return imageProcessor.applyNegative(originalImage);
                    case "Progowanie":
                        return imageProcessor.applyThreshold(originalImage, finalThreshold);
                    case "Konturowanie":
                        return imageProcessor.applyEdgeDetection(originalImage);
                    default:
                        return null;
                }
            }

            @Override
            protected void succeeded() {
                BufferedImage result = getValue();
                if (result != null) {
                    processedImage = result;
                    Image fxImage = imageProcessor.bufferedImageToImage(result);
                    processedImageView.setImage(fxImage);
                    saveButton.setDisable(false);
                    showToast("Operacja wykonana pomyślnie", "success");
                    Logger.getInstance().log("INFO", "Wykonano operację: " + operation);
                }
                progressBar.setVisible(false);
                statusLabel.setText("Gotowy do pracy");
                executeButton.setDisable(false);
            }

            @Override
            protected void failed() {
                showToast("Błąd podczas przetwarzania", "error");
                progressBar.setVisible(false);
                statusLabel.setText("Błąd");
                executeButton.setDisable(false);
                Logger.getInstance().log("ERROR", "Błąd przetwarzania: " + getException().getMessage());
            }
        };

        executorService.submit(task);
    }

    private void rotateImage(int degrees) {
        if (originalImage == null) return;

        Task<BufferedImage> task = new Task<BufferedImage>() {
            @Override
            protected BufferedImage call() throws Exception {
                return imageProcessor.rotateImage(originalImage, degrees);
            }

            @Override
            protected void succeeded() {
                originalImage = getValue();
                Image fxImage = imageProcessor.bufferedImageToImage(originalImage);
                originalImageView.setImage(fxImage);
                showToast("Obraz obrócony", "success");
                Logger.getInstance().log("INFO", "Obrócono obraz o " + degrees + " stopni");
            }

            @Override
            protected void failed() {
                showToast("Błąd podczas obracania obrazu", "error");
                Logger.getInstance().log("ERROR", "Błąd obracania: " + getException().getMessage());
            }
        };

        executorService.submit(task);
    }

    private void showScaleDialog() {
        if (originalImage == null) {
            showToast("Najpierw załaduj obraz", "warning");
            return;
        }

        Dialog<int[]> dialog = new Dialog<>();
        dialog.setTitle("Skalowanie obrazu");
        dialog.setHeaderText("Wprowadź nowe wymiary:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField widthField = new TextField(String.valueOf(originalImage.getWidth()));
        TextField heightField = new TextField(String.valueOf(originalImage.getHeight()));

        grid.add(new Label("Szerokość:"), 0, 0);
        grid.add(widthField, 1, 0);
        grid.add(new Label("Wysokość:"), 0, 1);
        grid.add(heightField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    int width = Integer.parseInt(widthField.getText());
                    int height = Integer.parseInt(heightField.getText());
                    if (width > 0 && width <= 3000 && height > 0 && height <= 3000) {
                        return new int[]{width, height};
                    }
                } catch (NumberFormatException e) {
                    // Invalid input
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(dimensions -> {
            Task<BufferedImage> task = new Task<BufferedImage>() {
                @Override
                protected BufferedImage call() throws Exception {
                    return imageProcessor.scaleImage(originalImage, dimensions[0], dimensions[1]);
                }

                @Override
                protected void succeeded() {
                    originalImage = getValue();
                    Image fxImage = imageProcessor.bufferedImageToImage(originalImage);
                    originalImageView.setImage(fxImage);
                    showToast("Obraz przeskalowany", "success");
                    Logger.getInstance().log("INFO", "Przeskalowano obraz do " + dimensions[0] + "x" + dimensions[1]);
                }

                @Override
                protected void failed() {
                    showToast("Błąd podczas skalowania", "error");
                    Logger.getInstance().log("ERROR", "Błąd skalowania: " + getException().getMessage());
                }
            };

            executorService.submit(task);
        });
    }

    private int showThresholdDialog() {
        TextInputDialog dialog = new TextInputDialog("128");
        dialog.setTitle("Progowanie");
        dialog.setHeaderText("Wprowadź wartość progu (0-255):");
        dialog.setContentText("Próg:");

        return dialog.showAndWait()
                .map(s -> {
                    try {
                        int value = Integer.parseInt(s);
                        return (value >= 0 && value <= 255) ? value : -1;
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                })
                .orElse(-1);
    }

    @FXML
    private void saveImage() {
        if (processedImage == null) {
            showToast("Brak obrazu do zapisania", "warning");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Zapisz obraz");
        dialog.setHeaderText("Wprowadź nazwę pliku (bez rozszerzenia):");
        dialog.setContentText("Nazwa:");

        dialog.showAndWait().ifPresent(filename -> {
            if (filename.length() >= 3 && filename.length() <= 100) {
                try {
                    String userHome = System.getProperty("user.home");
                    String picturesPath = userHome + File.separator + "Pictures";
                    File picturesDir = new File(picturesPath);

                    if (!picturesDir.exists()) {
                        picturesDir.mkdirs();
                    }

                    File file = new File(picturesPath, filename + ".jpg");

                    if (file.exists()) {
                        showToast("Plik o tej nazwie już istnieje", "warning");
                        return;
                    }

                    imageProcessor.saveImage(processedImage, file);
                    showToast("Obraz zapisany pomyślnie", "success");
                    Logger.getInstance().log("INFO", "Zapisano obraz: " + file.getAbsolutePath());

                } catch (Exception e) {
                    showToast("Błąd podczas zapisywania", "error");
                    Logger.getInstance().log("ERROR", "Błąd zapisywania: " + e.getMessage());
                }
            } else {
                showToast("Nazwa musi mieć 3-100 znaków", "warning");
            }
        });
    }

    private void enableButtons(boolean enable) {
        executeButton.setDisable(!enable || operationComboBox.getValue() == null);
        rotateLeftButton.setDisable(!enable);
        rotateRightButton.setDisable(!enable);
        scaleButton.setDisable(!enable);
    }

    private void showToast(String message, String type) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.getStyleClass().removeAll("success", "error", "warning");
            statusLabel.getStyleClass().add(type);

            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                statusLabel.setText("Gotowy do pracy");
                statusLabel.getStyleClass().removeAll("success", "error", "warning");
            }));
            timeline.play();
        });
    }

    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            Logger.getInstance().log("INFO", "ExecutorService zamknięty");
        }
    }
}
