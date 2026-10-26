package app;

import com.filemanager.model.FileMetadata;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import models.FileSystemItem;
import models.FolderItem;

import java.nio.file.Path;

/**
 * Live File Context and Metadata Inspector panel showing file properties, timestamps, and permissions.
 */
public class FileContextInspector extends ScrollPane {

    private final ExplorerController controller;
    private final VBox container = new VBox(12);

    private final StackPane iconBox = new StackPane();
    private final Label nameLabel = new Label();
    private final Label typeLabel = new Label();
    private final Label sizeLabel = new Label();

    private final Label pathValue = new Label();
    private final Label locationValue = new Label();
    private final Label createdValue = new Label();
    private final Label modifiedValue = new Label();
    private final Label accessedValue = new Label();

    private final FlowPane permissionsPane = new FlowPane();
    private final Button copyPathBtn = new Button("Copy Path");
    private final Button openItemBtn = new Button("Open");
    private final Button fullPropertiesBtn = new Button("More Properties...");

    public FileContextInspector(ExplorerController controller) {
        this.controller = controller;
        this.setFitToWidth(true);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.getStyleClass().add("context-inspector-scroll");
        this.setMinWidth(220);
        this.setPrefWidth(280);
        this.setMaxWidth(380);

        container.setPadding(new Insets(16, 14, 16, 14));
        container.getStyleClass().add("context-inspector-container");
        this.setContent(container);

        setupUI();

        // Listen for selection changes or directory changes
        controller.getSelectedItems().addListener((javafx.collections.ListChangeListener<FileSystemItem>) c -> updateInspector());
        controller.currentPathProperty().addListener((obs, oldVal, newVal) -> updateInspector());

        updateInspector();
    }

    private void setupUI() {
        Label header = new Label("FILE CONTEXT");
        header.getStyleClass().add("inspector-section-header");

        nameLabel.getStyleClass().add("inspector-name-label");
        nameLabel.setWrapText(true);
        nameLabel.setAlignment(Pos.CENTER);

        typeLabel.getStyleClass().add("inspector-type-label");
        typeLabel.setAlignment(Pos.CENTER);

        sizeLabel.getStyleClass().add("inspector-size-label");
        sizeLabel.setAlignment(Pos.CENTER);

        VBox headerCard = new VBox(8, iconBox, nameLabel, typeLabel, sizeLabel);
        headerCard.setAlignment(Pos.CENTER);
        headerCard.getStyleClass().add("inspector-header-card");
        headerCard.setPadding(new Insets(14));

        // Details grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(6, 0, 6, 0));

        int row = 0;
        addDetailRow(grid, row++, "Location", locationValue);
        addDetailRow(grid, row++, "Full Path", pathValue);
        addDetailRow(grid, row++, "Created", createdValue);
        addDetailRow(grid, row++, "Modified", modifiedValue);
        addDetailRow(grid, row++, "Accessed", accessedValue);

        // Permissions section
        Label permHeader = new Label("PERMISSIONS & FLAGS");
        permHeader.getStyleClass().add("inspector-section-header");

        permissionsPane.setHgap(6);
        permissionsPane.setVgap(6);

        // Actions
        copyPathBtn.setMaxWidth(Double.MAX_VALUE);
        copyPathBtn.getStyleClass().add("inspector-action-btn");
        copyPathBtn.setOnAction(e -> {
            String p = pathValue.getText();
            if (p != null && !p.isBlank()) {
                ClipboardContent content = new ClipboardContent();
                content.putString(p);
                Clipboard.getSystemClipboard().setContent(content);
                copyPathBtn.setText("Path Copied!");
                new javafx.animation.Timeline(new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(2),
                        ev -> copyPathBtn.setText("Copy Path")
                )).play();
            }
        });

        openItemBtn.setMaxWidth(Double.MAX_VALUE);
        openItemBtn.getStyleClass().add("inspector-action-btn-primary");
        openItemBtn.setOnAction(e -> {
            var selected = controller.getSelectedItems();
            if (selected.size() == 1) {
                controller.openItem(selected.get(0));
            }
        });

        fullPropertiesBtn.setMaxWidth(Double.MAX_VALUE);
        fullPropertiesBtn.getStyleClass().add("inspector-action-btn");
        fullPropertiesBtn.setOnAction(e -> {
            var selected = controller.getSelectedItems();
            if (selected.size() == 1) {
                controller.showProperties(selected.get(0), getScene().getWindow());
            } else if (controller.currentPathProperty().get() != null) {
                controller.showProperties(controller.currentPathProperty().get(), getScene().getWindow());
            }
        });

        VBox actionBox = new VBox(6, openItemBtn, copyPathBtn, fullPropertiesBtn);

        container.getChildren().addAll(
                header,
                headerCard,
                new Separator(),
                grid,
                new Separator(),
                permHeader,
                permissionsPane,
                new Separator(),
                actionBox
        );
    }

    private void addDetailRow(GridPane grid, int row, String labelText, Label valueLabel) {
        Label lbl = new Label(labelText);
        lbl.getStyleClass().add("inspector-field-label");

        valueLabel.getStyleClass().add("inspector-field-value");
        valueLabel.setWrapText(true);
        valueLabel.setMaxWidth(160);

        grid.add(lbl, 0, row);
        grid.add(valueLabel, 1, row);
    }

    private void updateInspector() {
        var selected = controller.getSelectedItems();

        if (selected.size() == 1) {
            FileSystemItem item = selected.get(0);
            FileMetadata meta = controller.getMetadataFor(item);
            displayItemMetadata(item, meta);
        } else if (selected.size() > 1) {
            displayMultiSelect(selected.size());
        } else {
            // Nothing selected: show current folder metadata
            FileMetadata meta = controller.getCurrentFolderMetadata();
            displayFolderMetadata(meta);
        }
    }

    private void displayItemMetadata(FileSystemItem item, FileMetadata meta) {
        iconBox.getChildren().setAll(IconFactory.getIconForItem(item, 52));
        nameLabel.setText(item.getName());

        if (meta != null) {
            typeLabel.setText(meta.getType());
            sizeLabel.setText(item instanceof FolderItem ? "Folder" : meta.getFormattedSize());

            locationValue.setText(meta.getPath().getParent() != null ? meta.getPath().getParent().toString() : "/");
            pathValue.setText(meta.getPath().toString());
            createdValue.setText(meta.getFormattedCreated());
            modifiedValue.setText(meta.getFormattedModified());
            accessedValue.setText(meta.getFormattedAccessed());

            updatePermissionBadges(meta);
        } else {
            typeLabel.setText(item instanceof FolderItem ? "Folder" : "File");
            sizeLabel.setText("");
            locationValue.setText(item.getPath().getParent() != null ? item.getPath().getParent().toString() : "");
            pathValue.setText(item.getPath().toString());
            createdValue.setText("--");
            modifiedValue.setText("--");
            accessedValue.setText("--");
            permissionsPane.getChildren().clear();
        }

        openItemBtn.setVisible(true);
        openItemBtn.setManaged(true);
        openItemBtn.setText(item instanceof FolderItem ? "Open Folder" : "Open File");
        fullPropertiesBtn.setText("Properties Dialog");
    }

    private void displayFolderMetadata(FileMetadata meta) {
        iconBox.getChildren().setAll(IconFactory.getFolderIcon(52));
        Path curr = controller.currentPathProperty().get();
        String name = curr != null ? (curr.getFileName() != null ? curr.getFileName().toString() : curr.toString()) : "No Folder";
        nameLabel.setText(name);
        typeLabel.setText("Current Directory");
        int count = controller.getItems().size();
        sizeLabel.setText(count + " item" + (count == 1 ? "" : "s"));

        if (meta != null) {
            locationValue.setText(meta.getPath().getParent() != null ? meta.getPath().getParent().toString() : "/");
            pathValue.setText(meta.getPath().toString());
            createdValue.setText(meta.getFormattedCreated());
            modifiedValue.setText(meta.getFormattedModified());
            accessedValue.setText(meta.getFormattedAccessed());
            updatePermissionBadges(meta);
        } else if (curr != null) {
            locationValue.setText(curr.getParent() != null ? curr.getParent().toString() : "/");
            pathValue.setText(curr.toString());
            createdValue.setText("--");
            modifiedValue.setText("--");
            accessedValue.setText("--");
            permissionsPane.getChildren().clear();
        }

        openItemBtn.setVisible(false);
        openItemBtn.setManaged(false);
        fullPropertiesBtn.setText("Folder Properties");
    }

    private void displayMultiSelect(int count) {
        iconBox.getChildren().setAll(IconFactory.getIconForPath(Path.of("multi"), 52));
        nameLabel.setText(count + " items selected");
        typeLabel.setText("Multiple Selection");

        long totalSize = 0;
        for (FileSystemItem item : controller.getSelectedItems()) {
            FileMetadata m = controller.getMetadataFor(item);
            if (m != null && !m.isDirectory()) {
                totalSize += m.getSize();
            }
        }
        sizeLabel.setText(formatBytes(totalSize));

        Path curr = controller.currentPathProperty().get();
        locationValue.setText(curr != null ? curr.toString() : "");
        pathValue.setText(count + " items in " + (curr != null ? curr.getFileName() : ""));
        createdValue.setText("--");
        modifiedValue.setText("--");
        accessedValue.setText("--");

        permissionsPane.getChildren().clear();
        openItemBtn.setVisible(false);
        openItemBtn.setManaged(false);
        fullPropertiesBtn.setText("Selection Properties");
    }

    private void updatePermissionBadges(FileMetadata meta) {
        permissionsPane.getChildren().clear();

        addBadge("Readable", meta.isReadable(), "#10b981", "#ef4444");
        addBadge("Writable", meta.isWritable(), "#3b82f6", "#ef4444");
        addBadge("Executable", meta.isExecutable(), "#8b5cf6", "#64748b");
        if (meta.isHidden()) {
            addBadge("Hidden", true, "#f59e0b", "#64748b");
        }
        if (meta.isDirectory()) {
            addBadge("Directory", true, "#06b6d4", "#64748b");
        }
    }

    private void addBadge(String text, boolean active, String activeColor, String inactiveColor) {
        Label badge = new Label(text);
        badge.getStyleClass().add("inspector-badge");
        String color = active ? activeColor : inactiveColor;
        badge.setStyle("-fx-background-color: " + color + "22; -fx-text-fill: " + color + "; -fx-border-color: " + color + "55;");
        permissionsPane.getChildren().add(badge);
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double sz = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unit = 0;
        while (sz >= 1024 && unit < units.length - 1) {
            sz /= 1024;
            unit++;
        }
        return String.format(java.util.Locale.ROOT, "%.2f %s", sz, units[unit]);
    }
}
