package app;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Window;
import models.FileItem;
import models.FileSystemItem;
import models.FolderItem;

import java.nio.file.Path;

/**
 * Unified context menu for files, folders, and background area.
 */
public class ItemContextMenu {

    public static ContextMenu createForItems(ExplorerController controller, Window window) {
        ContextMenu menu = new ContextMenu();

        var selected = controller.getSelectedItems();
        boolean single = selected.size() == 1;
        FileSystemItem primaryItem = single ? selected.get(0) : null;

        MenuItem open = new MenuItem(primaryItem instanceof FolderItem ? "Open Folder" : "Open");
        open.setGraphic(IconFactory.getActionIcon("view_list", 14, javafx.scene.paint.Color.web("#3b82f6")));
        open.setOnAction(e -> {
            if (primaryItem != null) {
                controller.openItem(primaryItem);
            }
        });

        MenuItem cut = new MenuItem("Cut");
        cut.setGraphic(IconFactory.getActionIcon("cut", 14, javafx.scene.paint.Color.web("#f59e0b")));
        cut.setOnAction(e -> controller.cutSelected());

        MenuItem copy = new MenuItem("Copy");
        copy.setGraphic(IconFactory.getActionIcon("copy", 14, javafx.scene.paint.Color.web("#3b82f6")));
        copy.setOnAction(e -> controller.copySelected());

        MenuItem paste = new MenuItem("Paste Here");
        paste.setGraphic(IconFactory.getActionIcon("paste", 14, javafx.scene.paint.Color.web("#10b981")));
        paste.setDisable(!controller.canPasteProperty().get());
        paste.setOnAction(e -> controller.pasteItems());

        MenuItem duplicate = new MenuItem("Duplicate");
        duplicate.setGraphic(IconFactory.getActionIcon("duplicate", 14, javafx.scene.paint.Color.web("#8b5cf6")));
        duplicate.setOnAction(e -> controller.duplicateSelected(window));

        MenuItem rename = new MenuItem("Rename...");
        rename.setGraphic(IconFactory.getActionIcon("rename", 14, javafx.scene.paint.Color.web("#06b6d4")));
        rename.setDisable(!single);
        rename.setOnAction(e -> {
            if (primaryItem != null) {
                controller.promptRename(primaryItem, window);
            }
        });

        MenuItem delete = new MenuItem("Delete");
        delete.setGraphic(IconFactory.getActionIcon("delete", 14, javafx.scene.paint.Color.web("#ef4444")));
        delete.setOnAction(e -> controller.deleteSelected(window));

        MenuItem properties = new MenuItem("Properties");
        properties.setGraphic(IconFactory.getActionIcon("properties", 14, javafx.scene.paint.Color.web("#6366f1")));
        properties.setOnAction(e -> {
            if (primaryItem != null) {
                controller.showProperties(primaryItem, window);
            } else if (controller.currentPathProperty().get() != null) {
                controller.showProperties(controller.currentPathProperty().get(), window);
            }
        });

        menu.getItems().addAll(
                open,
                new SeparatorMenuItem(),
                cut,
                copy,
                paste,
                duplicate,
                new SeparatorMenuItem(),
                rename,
                delete,
                new SeparatorMenuItem(),
                properties
        );

        return menu;
    }

    public static ContextMenu createForBackground(ExplorerController controller, Window window) {
        ContextMenu menu = new ContextMenu();

        MenuItem newFolder = new MenuItem("New Folder");
        newFolder.setGraphic(IconFactory.getActionIcon("plus_folder", 14, javafx.scene.paint.Color.web("#f59e0b")));
        newFolder.setOnAction(e -> controller.promptCreateFolder(window));

        MenuItem newFile = new MenuItem("New File");
        newFile.setGraphic(IconFactory.getActionIcon("plus_file", 14, javafx.scene.paint.Color.web("#3b82f6")));
        newFile.setOnAction(e -> controller.promptCreateFile(window));

        MenuItem paste = new MenuItem("Paste");
        paste.setGraphic(IconFactory.getActionIcon("paste", 14, javafx.scene.paint.Color.web("#10b981")));
        paste.setDisable(!controller.canPasteProperty().get());
        paste.setOnAction(e -> controller.pasteItems());

        MenuItem refresh = new MenuItem("Refresh");
        refresh.setGraphic(IconFactory.getActionIcon("refresh", 14, javafx.scene.paint.Color.web("#64748b")));
        refresh.setOnAction(e -> controller.refresh());

        MenuItem properties = new MenuItem("Folder Properties");
        properties.setGraphic(IconFactory.getActionIcon("properties", 14, javafx.scene.paint.Color.web("#6366f1")));
        properties.setOnAction(e -> {
            Path current = controller.currentPathProperty().get();
            if (current != null) {
                controller.showProperties(current, window);
            }
        });

        menu.getItems().addAll(
                newFolder,
                newFile,
                new SeparatorMenuItem(),
                paste,
                refresh,
                new SeparatorMenuItem(),
                properties
        );

        return menu;
    }
}
