package app;

import com.filemanager.model.FileMetadata;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import models.FileSystemItem;

/**
 * Bottom status bar showing item counts, selection metrics, and operational feedback.
 */
public class StatusBar extends HBox {

    private final ExplorerController controller;
    private final Label itemCountLabel = new Label("0 items");
    private final Label selectionLabel = new Label("");
    private final Label statusMessageLabel = new Label("Ready");

    // Graph Zoom Controls
    private final HBox zoomBox = new HBox(4);
    private final Button zoomOutBtn = new Button("−");
    private final Label zoomPercentLabel = new Label("100%");
    private final Button zoomInBtn = new Button("+");
    private final Button zoomResetBtn = new Button("Reset");

    public StatusBar(ExplorerController controller) {
        super(10);
        this.controller = controller;
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(6, 16, 6, 16));
        this.getStyleClass().add("status-bar");

        setupLabels();
        setupZoomControls();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(
                itemCountLabel,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                selectionLabel,
                spacer,
                statusMessageLabel,
                zoomBox
        );

        // Listen for changes
        controller.getItems().addListener((javafx.collections.ListChangeListener<FileSystemItem>) c -> updateCounts());
        controller.getSelectedItems().addListener((javafx.collections.ListChangeListener<FileSystemItem>) c -> updateCounts());
        controller.statusMessageProperty().addListener((obs, oldVal, newVal) -> statusMessageLabel.setText(newVal));

        controller.viewModeProperty().addListener((obs, oldVal, newVal) -> {
            boolean isGraph = newVal == FileViewMode.GRAPH;
            zoomBox.setVisible(isGraph);
            zoomBox.setManaged(isGraph);
        });
        zoomBox.setVisible(false);
        zoomBox.setManaged(false);

        updateCounts();
    }

    private void setupLabels() {
        itemCountLabel.getStyleClass().add("status-label");
        selectionLabel.getStyleClass().add("status-label-selection");
        statusMessageLabel.getStyleClass().add("status-label-message");
    }

    private void setupZoomControls() {
        zoomBox.setAlignment(Pos.CENTER_RIGHT);
        zoomOutBtn.getStyleClass().add("zoom-btn");
        zoomInBtn.getStyleClass().add("zoom-btn");
        zoomResetBtn.getStyleClass().add("zoom-btn");
        zoomPercentLabel.getStyleClass().add("zoom-label");

        zoomBox.getChildren().addAll(zoomOutBtn, zoomPercentLabel, zoomInBtn, zoomResetBtn);
    }

    public Button getZoomOutBtn() { return zoomOutBtn; }
    public Button getZoomInBtn() { return zoomInBtn; }
    public Button getZoomResetBtn() { return zoomResetBtn; }
    public Label getZoomPercentLabel() { return zoomPercentLabel; }

    private void updateCounts() {
        int total = controller.getItems().size();
        itemCountLabel.setText(total + " item" + (total == 1 ? "" : "s"));

        var selected = controller.getSelectedItems();
        if (selected.isEmpty()) {
            selectionLabel.setText("");
        } else {
            long bytes = 0;
            for (FileSystemItem item : selected) {
                FileMetadata meta = controller.getMetadataFor(item);
                if (meta != null && !meta.isDirectory()) {
                    bytes += meta.getSize();
                }
            }
            selectionLabel.setText(selected.size() + " selected (" + formatBytes(bytes) + ")");
        }
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
        return String.format(java.util.Locale.ROOT, "%.1f %s", sz, units[unit]);
    }
}
