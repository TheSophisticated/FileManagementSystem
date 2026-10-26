package app;

import com.filemanager.model.FileMetadata;
import com.filemanager.service.MetadataService;
import com.filemanager.ui.PropertiesDialog;
import filesystem.FileSystem;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Window;
import models.FileItem;
import models.FileSystemItem;
import models.FolderItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main application controller managing file system operations, navigation, selection, and view state.
 */
public class ExplorerController {

    private final FileSystem fileSystem = new FileSystem();
    private final MetadataService metadataService = new MetadataService();
    private final PropertiesDialog propertiesDialog = new PropertiesDialog();
    private final NavigationHistory history = new NavigationHistory();

    // Observable States
    private final ObjectProperty<Path> currentPath = new SimpleObjectProperty<>();
    private final ObjectProperty<FolderItem> currentFolderItem = new SimpleObjectProperty<>();
    private final ObjectProperty<FolderItem> workspace = new SimpleObjectProperty<>();
    private final ObservableList<FileSystemItem> items = FXCollections.observableArrayList();
    private final ObservableList<FileSystemItem> selectedItems = FXCollections.observableArrayList();
    private final ObjectProperty<FileViewMode> viewMode = new SimpleObjectProperty<>(FileViewMode.TABLE);
    private final BooleanProperty darkMode = new SimpleBooleanProperty(true);
    private final BooleanProperty detailsVisible = new SimpleBooleanProperty(true);
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");
    private final BooleanProperty canGoBack = new SimpleBooleanProperty(false);
    private final BooleanProperty canGoForward = new SimpleBooleanProperty(false);
    private final BooleanProperty canGoUp = new SimpleBooleanProperty(false);
    private final BooleanProperty canPaste = new SimpleBooleanProperty(false);
    private final BooleanProperty searchActive = new SimpleBooleanProperty(false);
    private final StringProperty searchQuery = new SimpleStringProperty("");

    public ExplorerController() {
        darkMode.addListener((obs, oldVal, newVal) -> propertiesDialog.setDarkMode(newVal));
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public MetadataService getMetadataService() {
        return metadataService;
    }

    public PropertiesDialog getPropertiesDialog() {
        return propertiesDialog;
    }

    // Property Getters
    public ObjectProperty<Path> currentPathProperty() { return currentPath; }
    public ObjectProperty<FolderItem> currentFolderItemProperty() { return currentFolderItem; }
    public ObjectProperty<FolderItem> workspaceProperty() { return workspace; }
    public ObservableList<FileSystemItem> getItems() { return items; }
    public ObservableList<FileSystemItem> getSelectedItems() { return selectedItems; }
    public ObjectProperty<FileViewMode> viewModeProperty() { return viewMode; }
    public BooleanProperty darkModeProperty() { return darkMode; }
    public BooleanProperty detailsVisibleProperty() { return detailsVisible; }
    public StringProperty statusMessageProperty() { return statusMessage; }
    public BooleanProperty canGoBackProperty() { return canGoBack; }
    public BooleanProperty canGoForwardProperty() { return canGoForward; }
    public BooleanProperty canGoUpProperty() { return canGoUp; }
    public BooleanProperty canPasteProperty() { return canPaste; }
    public BooleanProperty searchActiveProperty() { return searchActive; }
    public StringProperty searchQueryProperty() { return searchQuery; }

    public void openWorkspace(Path root) {
        try {
            FolderItem ws = fileSystem.openWorkspace(root);
            workspace.set(ws);
            navigateTo(root);
            statusMessage.set("Workspace opened: " + root.getFileName());
        } catch (IOException e) {
            showError("Could not open workspace", e);
        }
    }

    public void navigateTo(Path path) {
        if (path == null) return;
        path = path.toAbsolutePath().normalize();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            statusMessage.set("Folder not found: " + path);
            return;
        }

        history.navigateTo(path);
        loadDirectory(path);
    }

    public void navigateBack() {
        if (history.canGoBack()) {
            Path prev = history.goBack();
            loadDirectory(prev);
        }
    }

    public void navigateForward() {
        if (history.canGoForward()) {
            Path next = history.goForward();
            loadDirectory(next);
        }
    }

    public void navigateUp() {
        Path curr = currentPath.get();
        if (curr != null && curr.getParent() != null) {
            navigateTo(curr.getParent());
        }
    }

    public void refresh() {
        if (searchActive.get()) {
            search(searchQuery.get());
        } else if (currentPath.get() != null) {
            loadDirectory(currentPath.get());
        }
    }

    private void loadDirectory(Path path) {
        try {
            searchActive.set(false);
            searchQuery.set("");
            currentPath.set(path);
            currentFolderItem.set(new FolderItem(path));

            ArrayList<FileSystemItem> contents = fileSystem.listContents(path);
            items.setAll(contents);
            selectedItems.clear();

            updateNavState();
            statusMessage.set("Loaded " + contents.size() + " item" + (contents.size() == 1 ? "" : "s"));
        } catch (IOException e) {
            showError("Could not read directory contents", e);
        }
    }

    private void updateNavState() {
        canGoBack.set(history.canGoBack());
        canGoForward.set(history.canGoForward());
        Path curr = currentPath.get();
        canGoUp.set(curr != null && curr.getParent() != null);
        canPaste.set(fileSystem.canPaste());
    }

    public void search(String query) {
        if (query == null || query.isBlank()) {
            clearSearch();
            return;
        }

        try {
            // Ensure workspace is initialized if not yet set
            if (workspace.get() == null && currentPath.get() != null) {
                fileSystem.openWorkspace(currentPath.get());
                workspace.set(new FolderItem(currentPath.get()));
            }

            searchActive.set(true);
            searchQuery.set(query.trim());
            ArrayList<FileSystemItem> results = fileSystem.search(query.trim());
            items.setAll(results);
            selectedItems.clear();
            statusMessage.set("Found " + results.size() + " matching item" + (results.size() == 1 ? "" : "s") + " for \"" + query + "\"");
        } catch (Exception e) {
            statusMessage.set("Search error: " + e.getMessage());
        }
    }

    public void clearSearch() {
        if (searchActive.get()) {
            searchActive.set(false);
            searchQuery.set("");
            if (currentPath.get() != null) {
                loadDirectory(currentPath.get());
            }
        }
    }

    public void openItem(FileSystemItem item) {
        if (item instanceof FolderItem) {
            navigateTo(item.getPath());
        } else if (item instanceof FileItem fileItem) {
            try {
                fileSystem.open(fileItem);
                statusMessage.set("Opened: " + item.getName());
            } catch (Exception e) {
                showError("Could not open file", e);
            }
        }
    }

    public void promptCreateFolder(Window owner) {
        Path target = currentPath.get();
        if (target == null) return;

        TextInputDialog dialog = new TextInputDialog("New Folder");
        dialog.setTitle("New Folder");
        dialog.setHeaderText("Create a new folder in " + target.getFileName());
        dialog.setContentText("Folder name:");
        if (owner != null) dialog.initOwner(owner);

        dialog.showAndWait().ifPresent(name -> {
            if (name.isBlank()) return;
            try {
                FolderItem created = fileSystem.createFolder(target, name.trim());
                refresh();
                selectedItems.setAll(created);
                statusMessage.set("Created folder: " + name);
            } catch (IOException e) {
                showError("Could not create folder", e);
            }
        });
    }

    public void promptCreateFile(Window owner) {
        Path target = currentPath.get();
        if (target == null) return;

        TextInputDialog dialog = new TextInputDialog("new_file.txt");
        dialog.setTitle("New File");
        dialog.setHeaderText("Create a new file in " + target.getFileName());
        dialog.setContentText("File name:");
        if (owner != null) dialog.initOwner(owner);

        dialog.showAndWait().ifPresent(name -> {
            if (name.isBlank()) return;
            try {
                FileItem created = fileSystem.createFile(target, name.trim());
                refresh();
                selectedItems.setAll(created);
                statusMessage.set("Created file: " + name);
            } catch (IOException e) {
                showError("Could not create file", e);
            }
        });
    }

    public void promptRename(FileSystemItem item, Window owner) {
        if (item == null) return;

        TextInputDialog dialog = new TextInputDialog(item.getName());
        dialog.setTitle("Rename");
        dialog.setHeaderText("Rename: " + item.getName());
        dialog.setContentText("New name:");
        if (owner != null) dialog.initOwner(owner);

        dialog.showAndWait().ifPresent(newName -> {
            if (newName.isBlank() || newName.equals(item.getName())) return;
            try {
                fileSystem.rename(item, newName.trim());
                refresh();
                statusMessage.set("Renamed to: " + newName);
            } catch (IOException e) {
                showError("Could not rename item", e);
            }
        });
    }

    public void deleteSelected(Window owner) {
        if (selectedItems.isEmpty()) return;

        int count = selectedItems.size();
        String itemNames = count == 1 ? selectedItems.get(0).getName() : count + " items";

        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + itemNames + "?\nThis action cannot be undone.",
                ButtonType.YES, ButtonType.NO
        );
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + itemNames);
        if (owner != null) confirm.initOwner(owner);

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            List<FileSystemItem> toDelete = new ArrayList<>(selectedItems);
            int deleted = 0;
            for (FileSystemItem item : toDelete) {
                try {
                    fileSystem.delete(item);
                    deleted++;
                } catch (IOException e) {
                    showError("Could not delete " + item.getName(), e);
                }
            }
            refresh();
            statusMessage.set("Deleted " + deleted + " item" + (deleted == 1 ? "" : "s"));
        }
    }

    public void copySelected() {
        if (selectedItems.isEmpty()) return;
        fileSystem.copy(new ArrayList<>(selectedItems));
        canPaste.set(true);
        statusMessage.set("Copied " + selectedItems.size() + " item(s) to clipboard");
    }

    public void cutSelected() {
        if (selectedItems.isEmpty()) return;
        fileSystem.cut(new ArrayList<>(selectedItems));
        canPaste.set(true);
        statusMessage.set("Cut " + selectedItems.size() + " item(s) to clipboard");
    }

    public void pasteItems() {
        Path target = currentPath.get();
        if (target == null || !fileSystem.canPaste()) return;

        try {
            ArrayList<FileSystemItem> pasted = fileSystem.paste(target);
            canPaste.set(fileSystem.canPaste());
            refresh();
            selectedItems.setAll(pasted);
            statusMessage.set("Pasted " + pasted.size() + " item(s)");
        } catch (IOException e) {
            showError("Could not paste items", e);
        }
    }

    public void duplicateSelected(Window owner) {
        if (selectedItems.isEmpty()) return;
        try {
            List<FileSystemItem> duplicates = new ArrayList<>();
            for (FileSystemItem item : selectedItems) {
                duplicates.add(fileSystem.duplicate(item));
            }
            refresh();
            selectedItems.setAll(duplicates);
            statusMessage.set("Duplicated " + duplicates.size() + " item(s)");
        } catch (IOException e) {
            showError("Could not duplicate item", e);
        }
    }

    public void showProperties(FileSystemItem item, Window owner) {
        if (item == null) {
            if (currentPath.get() != null) {
                propertiesDialog.show(currentPath.get(), owner);
            }
            return;
        }
        propertiesDialog.show(item.getPath(), owner);
    }

    public void showProperties(Path path, Window owner) {
        if (path != null) {
            propertiesDialog.show(path, owner);
        }
    }

    public FileMetadata getMetadataFor(FileSystemItem item) {
        if (item == null || item.getPath() == null) return null;
        try {
            return metadataService.read(item.getPath());
        } catch (Exception e) {
            return null;
        }
    }

    public FileMetadata getCurrentFolderMetadata() {
        Path p = currentPath.get();
        if (p == null) return null;
        try {
            return metadataService.read(p);
        } catch (Exception e) {
            return null;
        }
    }

    public void toggleTheme() {
        darkMode.set(!darkMode.get());
    }

    public void toggleDetailsPanel() {
        detailsVisible.set(!detailsVisible.get());
    }

    private void showError(String header, Exception exception) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("File Management Error");
            alert.setHeaderText(header);
            alert.setContentText(exception != null ? exception.getMessage() : "Unknown error");
            alert.showAndWait();
        });
    }
}
