package app;

import com.filemanager.model.FileMetadata;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import models.FileItem;
import models.FileSystemItem;
import models.FolderItem;

import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;

/**
 * Detailed Table View showing files with sortable columns: Name, Type, Size, Modified Date, and Permissions.
 */
public class DetailedTableView extends TableView<FileSystemItem> {

    private final ExplorerController controller;

    public DetailedTableView(ExplorerController controller) {
        this.controller = controller;
        this.getStyleClass().add("detailed-table-view");
        this.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        setupColumns();
        setupEvents();

        this.setItems(controller.getItems());

        // Sync selection with controller
        this.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<FileSystemItem>) c -> {
            controller.getSelectedItems().setAll(this.getSelectionModel().getSelectedItems());
        });

        // Context menu on empty table background
        this.setOnContextMenuRequested(event -> {
            if (this.getSelectionModel().isEmpty()) {
                ContextMenu menu = ItemContextMenu.createForBackground(controller, getScene().getWindow());
                menu.show(this, event.getScreenX(), event.getScreenY());
            }
        });

        // Placeholder for empty folder
        Label emptyLabel = new Label("This folder is empty");
        emptyLabel.getStyleClass().add("empty-folder-label");
        this.setPlaceholder(emptyLabel);
    }

    private void setupColumns() {
        // 1. Name Column (with Icon)
        TableColumn<FileSystemItem, FileSystemItem> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(280);
        nameCol.setMinWidth(150);
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue()));
        nameCol.setCellFactory(col -> new TableCell<FileSystemItem, FileSystemItem>() {
            @Override
            protected void updateItem(FileSystemItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox box = new HBox(8);
                    box.setAlignment(Pos.CENTER_LEFT);
                    Label nameLbl = new Label(item.getName());
                    nameLbl.getStyleClass().add(item instanceof FolderItem ? "folder-name-label" : "file-name-label");
                    box.getChildren().addAll(IconFactory.getIconForItem(item, 18), nameLbl);
                    setGraphic(box);
                    setText(null);
                }
            }
        });
        nameCol.setComparator(Comparator.comparing(item -> {
            boolean isFolder = item instanceof FolderItem;
            return (isFolder ? "0_" : "1_") + item.getName().toLowerCase();
        }));

        // 2. Type Column
        TableColumn<FileSystemItem, String> typeCol = new TableColumn<>("Type");
        typeCol.setPrefWidth(130);
        typeCol.setCellValueFactory(data -> {
            FileSystemItem item = data.getValue();
            if (item instanceof FolderItem) {
                return new SimpleStringProperty("Folder");
            }
            FileMetadata meta = controller.getMetadataFor(item);
            return new SimpleStringProperty(meta != null ? meta.getType() : "File");
        });

        // 3. Size Column
        TableColumn<FileSystemItem, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setPrefWidth(110);
        sizeCol.setCellValueFactory(data -> {
            FileSystemItem item = data.getValue();
            if (item instanceof FolderItem) {
                return new SimpleStringProperty("--");
            }
            FileMetadata meta = controller.getMetadataFor(item);
            return new SimpleStringProperty(meta != null ? meta.getFormattedSize() : "");
        });
        sizeCol.setComparator((s1, s2) -> {
            if (s1.equals("--") && s2.equals("--")) return 0;
            if (s1.equals("--")) return -1;
            if (s2.equals("--")) return 1;
            return s1.compareTo(s2);
        });

        // 4. Date Modified Column
        TableColumn<FileSystemItem, String> dateCol = new TableColumn<>("Date Modified");
        dateCol.setPrefWidth(180);
        dateCol.setCellValueFactory(data -> {
            FileMetadata meta = controller.getMetadataFor(data.getValue());
            return new SimpleStringProperty(meta != null ? meta.getFormattedModified() : "");
        });

        // 5. Permissions Column
        TableColumn<FileSystemItem, String> permCol = new TableColumn<>("Permissions");
        permCol.setPrefWidth(90);
        permCol.setCellValueFactory(data -> {
            FileMetadata meta = controller.getMetadataFor(data.getValue());
            return new SimpleStringProperty(meta != null ? meta.getPermissionsString() : "");
        });

        this.getColumns().setAll(nameCol, typeCol, sizeCol, dateCol, permCol);
    }

    private void setupEvents() {
        // Row factory for double click and context menu
        this.setRowFactory(tv -> {
            TableRow<FileSystemItem> row = new TableRow<>();

            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    controller.openItem(row.getItem());
                }
            });

            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    if (!this.getSelectionModel().isSelected(row.getIndex())) {
                        this.getSelectionModel().clearAndSelect(row.getIndex());
                    }
                    ContextMenu menu = ItemContextMenu.createForItems(controller, getScene().getWindow());
                    menu.show(row, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            });

            return row;
        });

        // Keyboard shortcuts
        this.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                var selected = this.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    controller.openItem(selected);
                }
            } else if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
                controller.deleteSelected(getScene().getWindow());
            } else if (event.getCode() == KeyCode.F2) {
                var selected = this.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    controller.promptRename(selected, getScene().getWindow());
                }
            }
        });
    }
}
