package app;

import com.filemanager.model.FileMetadata;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import models.FileSystemItem;
import models.FolderItem;

/**
 * Compact vertical list view showing icon, name, type, and formatted size.
 */
public class FileListView extends ListView<FileSystemItem> {

    private final ExplorerController controller;

    public FileListView(ExplorerController controller) {
        this.controller = controller;
        this.getStyleClass().add("file-list-view");
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        this.setItems(controller.getItems());

        this.setCellFactory(lv -> new ListCell<FileSystemItem>() {
            @Override
            protected void updateItem(FileSystemItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox row = new HBox(10);
                    row.setAlignment(Pos.CENTER_LEFT);

                    Label nameLabel = new Label(item.getName());
                    nameLabel.getStyleClass().add(item instanceof FolderItem ? "folder-name-label" : "file-name-label");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    String metaText;
                    if (item instanceof FolderItem) {
                        metaText = "Folder";
                    } else {
                        FileMetadata meta = controller.getMetadataFor(item);
                        metaText = meta != null ? meta.getType() + "  ·  " + meta.getFormattedSize() : "";
                    }

                    Label metaLabel = new Label(metaText);
                    metaLabel.getStyleClass().add("list-item-meta");

                    row.getChildren().addAll(IconFactory.getIconForItem(item, 20), nameLabel, spacer, metaLabel);
                    setGraphic(row);
                    setText(null);
                }
            }
        });

        // Sync selection with controller
        this.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<FileSystemItem>) c -> {
            controller.getSelectedItems().setAll(this.getSelectionModel().getSelectedItems());
        });

        // Double click to open
        this.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                FileSystemItem selected = this.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    controller.openItem(selected);
                }
            }
        });

        // Context menu
        this.setOnContextMenuRequested(event -> {
            if (this.getSelectionModel().isEmpty()) {
                ContextMenu menu = ItemContextMenu.createForBackground(controller, getScene().getWindow());
                menu.show(this, event.getScreenX(), event.getScreenY());
            } else {
                ContextMenu menu = ItemContextMenu.createForItems(controller, getScene().getWindow());
                menu.show(this, event.getScreenX(), event.getScreenY());
            }
        });

        Label emptyLabel = new Label("This folder is empty");
        emptyLabel.getStyleClass().add("empty-folder-label");
        this.setPlaceholder(emptyLabel);
    }
}
