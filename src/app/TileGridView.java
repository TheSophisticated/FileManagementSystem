package app;

import com.filemanager.model.FileMetadata;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import models.FileSystemItem;
import models.FolderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Grid / Tile View displaying items as cards with large icons and metadata labels.
 */
public class TileGridView extends ScrollPane {

    private final ExplorerController controller;
    private final FlowPane flowPane = new FlowPane();
    private final ObservableList<FileSystemItem> selectedItems = FXCollections.observableArrayList();

    public TileGridView(ExplorerController controller) {
        this.controller = controller;
        this.getStyleClass().add("tile-scroll-pane");
        this.setFitToWidth(true);

        flowPane.getStyleClass().add("tile-container");
        flowPane.setHgap(16);
        flowPane.setVgap(16);
        flowPane.setPadding(new Insets(18));

        this.setContent(flowPane);

        // Click outside clears selection
        flowPane.setOnMouseClicked(event -> {
            if (event.getTarget() == flowPane) {
                clearSelection();
            }
        });

        // Context menu on empty background
        flowPane.setOnContextMenuRequested(event -> {
            if (event.getTarget() == flowPane) {
                ContextMenu menu = ItemContextMenu.createForBackground(controller, getScene().getWindow());
                menu.show(flowPane, event.getScreenX(), event.getScreenY());
            }
        });

        // Listen for items change
        controller.getItems().addListener((ListChangeListener<FileSystemItem>) c -> renderTiles());

        renderTiles();
    }

    private void renderTiles() {
        flowPane.getChildren().clear();
        clearSelection();

        List<FileSystemItem> items = controller.getItems();
        if (items.isEmpty()) {
            Label emptyLbl = new Label("This folder is empty");
            emptyLbl.getStyleClass().add("empty-folder-label");
            flowPane.getChildren().add(emptyLbl);
            return;
        }

        for (FileSystemItem item : items) {
            VBox card = createTileCard(item);
            flowPane.getChildren().add(card);
        }
    }

    private VBox createTileCard(FileSystemItem item) {
        VBox card = new VBox(6);
        card.getStyleClass().add("file-tile-card");
        card.setAlignment(Pos.CENTER);
        card.setUserData(item);
        card.setPrefWidth(120);
        card.setMinWidth(110);

        // Icon
        card.getChildren().add(IconFactory.getIconForItem(item, 44));

        // Name
        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("tile-name-label");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(105);
        nameLabel.setAlignment(Pos.CENTER);

        // Subtitle (size or type)
        String subtitleText;
        if (item instanceof FolderItem) {
            subtitleText = "Folder";
        } else {
            FileMetadata meta = controller.getMetadataFor(item);
            subtitleText = meta != null ? meta.getFormattedSize() : "";
        }
        Label subLabel = new Label(subtitleText);
        subLabel.getStyleClass().add("tile-sub-label");

        card.getChildren().addAll(nameLabel, subLabel);

        // Mouse Handlers
        card.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (event.getClickCount() == 2) {
                    controller.openItem(item);
                } else {
                    if (event.isControlDown() || event.isMetaDown()) {
                        toggleSelection(card, item);
                    } else {
                        selectSingle(card, item);
                    }
                }
            }
        });

        card.setOnContextMenuRequested(event -> {
            if (!selectedItems.contains(item)) {
                selectSingle(card, item);
            }
            ContextMenu menu = ItemContextMenu.createForItems(controller, getScene().getWindow());
            menu.show(card, event.getScreenX(), event.getScreenY());
            event.consume();
        });

        return card;
    }

    private void selectSingle(VBox card, FileSystemItem item) {
        clearVisuals();
        selectedItems.clear();
        selectedItems.add(item);
        card.getStyleClass().add("selected");
        controller.getSelectedItems().setAll(selectedItems);
    }

    private void toggleSelection(VBox card, FileSystemItem item) {
        if (selectedItems.contains(item)) {
            selectedItems.remove(item);
            card.getStyleClass().remove("selected");
        } else {
            selectedItems.add(item);
            card.getStyleClass().add("selected");
        }
        controller.getSelectedItems().setAll(selectedItems);
    }

    private void clearSelection() {
        clearVisuals();
        selectedItems.clear();
        controller.getSelectedItems().clear();
    }

    private void clearVisuals() {
        for (var node : flowPane.getChildren()) {
            node.getStyleClass().remove("selected");
        }
    }
}
