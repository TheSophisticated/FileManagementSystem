package com.filemanager.ui;

import com.filemanager.model.FileMetadata;
import com.filemanager.service.MetadataService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Properties Dialog for displaying detailed file metadata.
 */
public class PropertiesDialog {

    private final MetadataService metadataService = new MetadataService();
    private boolean darkMode = true;

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public void show(Path path, Window owner) {
        FileMetadata metadata;
        try {
            metadata = metadataService.read(path);
        } catch (IOException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Properties Error");
            alert.setHeaderText("Cannot read file properties");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
            return;
        }

        Stage stage = new Stage();
        stage.setTitle("Properties - " + metadata.getName());
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            stage.initOwner(owner);
        }

        String bg = darkMode ? "#181b22" : "#ffffff";
        String cardBg = darkMode ? "#222734" : "#f4f6f8";
        String text = darkMode ? "#f0f6fc" : "#1e293b";
        String mutedText = darkMode ? "#8b949e" : "#64748b";
        String borderColor = darkMode ? "#30363d" : "#e2e8f0";

        // Header
        Label nameLabel = new Label(metadata.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + text + ";");
        nameLabel.setWrapText(true);

        Label typeLabel = new Label(metadata.getType() + (metadata.isDirectory() ? "" : " (" + metadata.getFormattedSize() + ")"));
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3b82f6; -fx-font-weight: bold;");

        VBox heading = new VBox(4, nameLabel, typeLabel);
        heading.setPadding(new Insets(12));
        heading.setStyle("-fx-background-color: " + cardBg + "; -fx-background-radius: 8; -fx-border-color: " + borderColor + "; -fx-border-radius: 8;");

        // Grid
        GridPane grid = new GridPane();
        grid.setHgap(16);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 4, 10, 4));

        int row = 0;
        addRow(grid, row++, "Name", metadata.getName(), text, mutedText);
        addRow(grid, row++, "Type", metadata.getType(), text, mutedText);
        addRow(grid, row++, "Extension", metadata.getExtension().isEmpty() ? "None" : "." + metadata.getExtension(), text, mutedText);

        // Path with copy button
        Label fullPathLabel = new Label(metadata.getPath().toString());
        fullPathLabel.setWrapText(true);
        fullPathLabel.setMaxWidth(300);
        fullPathLabel.setStyle("-fx-text-fill: " + text + "; -fx-font-family: monospace; -fx-font-size: 11px;");

        Button copyPathBtn = new Button("Copy");
        copyPathBtn.setStyle("-fx-font-size: 10px; -fx-padding: 3 8 3 8; -fx-background-radius: 6; -fx-cursor: hand;");
        copyPathBtn.setOnAction(e -> {
            ClipboardContent content = new ClipboardContent();
            content.putString(metadata.getPath().toString());
            Clipboard.getSystemClipboard().setContent(content);
            copyPathBtn.setText("Copied!");
        });
        HBox pathBox = new HBox(8, fullPathLabel, copyPathBtn);
        pathBox.setAlignment(Pos.CENTER_LEFT);

        Label pathKeyLabel = new Label("Full Path");
        pathKeyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + mutedText + "; -fx-font-size: 12px;");
        grid.add(pathKeyLabel, 0, row);
        grid.add(pathBox, 1, row++);

        addRow(grid, row++, "Location", metadata.getPath().getParent() == null ? "/" : metadata.getPath().getParent().toString(), text, mutedText);
        addRow(grid, row++, "Size", metadata.getFormattedSize() + (metadata.isDirectory() ? "" : " (" + metadata.getSize() + " bytes)"), text, mutedText);
        addRow(grid, row++, "Created", metadata.getFormattedCreated(), text, mutedText);
        addRow(grid, row++, "Last Modified", metadata.getFormattedModified(), text, mutedText);
        addRow(grid, row++, "Last Accessed", metadata.getFormattedAccessed(), text, mutedText);
        addRow(grid, row++, "Permissions", metadata.getPermissionsString() + " (Readable: " + yesNo(metadata.isReadable()) + ", Writable: " + yesNo(metadata.isWritable()) + ", Executable: " + yesNo(metadata.isExecutable()) + ")", text, mutedText);
        addRow(grid, row++, "Hidden", yesNo(metadata.isHidden()), text, mutedText);

        Button closeButton = new Button("Close");
        closeButton.setDefaultButton(true);
        closeButton.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8 20 8 20; -fx-cursor: hand;");
        closeButton.setOnAction(event -> stage.close());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox buttonBox = new HBox(spacer, closeButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(14, heading, new Separator(), grid, new Separator(), buttonBox);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: " + bg + ";");

        Scene scene = new Scene(root, 560, 580);
        stage.setScene(scene);
        stage.setMinWidth(480);
        stage.setMinHeight(520);
        stage.showAndWait();
    }

    private void addRow(GridPane grid, int row, String key, String value, String textColor, String keyColor) {
        Label keyLabel = new Label(key);
        keyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " + keyColor + "; -fx-font-size: 12px;");

        Label valueLabel = new Label(value);
        valueLabel.setWrapText(true);
        valueLabel.setMaxWidth(380);
        valueLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 12px;");

        grid.add(keyLabel, 0, row);
        grid.add(valueLabel, 1, row);
    }

    private String yesNo(boolean value) {
        return value ? "Yes" : "No";
    }
}
