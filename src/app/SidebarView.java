package app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import models.FileSystemItem;
import models.FolderItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Left sidebar providing Quick Access shortcuts and an expandable Workspace Directory Tree.
 */
public class SidebarView extends VBox {

    private final ExplorerController controller;
    private final TreeView<Path> directoryTree = new TreeView<>();
    private final VBox quickAccessBox = new VBox(2);
    private final Button openFolderBtn = new Button("Open Folder...");

    public SidebarView(ExplorerController controller) {
        super(12);
        this.controller = controller;
        this.setPadding(new Insets(14, 10, 14, 10));
        this.getStyleClass().add("sidebar-view");
        this.setMinWidth(180);
        this.setPrefWidth(240);
        this.setMaxWidth(360);

        setupQuickAccess();
        setupDirectoryTree();

        Label quickAccessLabel = new Label("QUICK ACCESS");
        quickAccessLabel.getStyleClass().add("sidebar-section-header");

        Label treeHeaderLabel = new Label("FOLDERS");
        treeHeaderLabel.getStyleClass().add("sidebar-section-header");

        VBox treeSection = new VBox(6, treeHeaderLabel, directoryTree);
        VBox.setVgrow(treeSection, Priority.ALWAYS);
        VBox.setVgrow(directoryTree, Priority.ALWAYS);

        this.getChildren().addAll(
                openFolderBtn,
                quickAccessLabel,
                quickAccessBox,
                new Separator(),
                treeSection
        );

        // Listen for current folder changes to synchronize tree selection
        controller.currentPathProperty().addListener((obs, oldVal, newVal) -> selectPathInTree(newVal));
        controller.workspaceProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                buildTreeRoot(newVal.getPath());
            }
        });
    }

    private void setupQuickAccess() {
        openFolderBtn.setMaxWidth(Double.MAX_VALUE);
        openFolderBtn.setGraphic(IconFactory.getActionIcon("plus_folder", 14, Color.web("#ffffff")));
        openFolderBtn.getStyleClass().add("sidebar-open-btn");
        openFolderBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose Folder to Open");
            Path curr = controller.currentPathProperty().get();
            if (curr != null && Files.exists(curr)) {
                chooser.setInitialDirectory(curr.toFile());
            }
            Window window = getScene() != null ? getScene().getWindow() : null;
            File selected = chooser.showDialog(window);
            if (selected != null) {
                controller.openWorkspace(selected.toPath());
            }
        });

        addQuickAccessItem("Current Workspace", "workspace", () -> {
            FolderItem ws = controller.workspaceProperty().get();
            if (ws != null) {
                controller.navigateTo(ws.getPath());
            } else if (controller.currentPathProperty().get() != null) {
                controller.navigateTo(controller.currentPathProperty().get());
            }
        });

        String userHome = System.getProperty("user.home");
        Path homePath = Path.of(userHome);
        Path desktopPath = homePath.resolve("Desktop");
        Path documentsPath = homePath.resolve("Documents");
        Path downloadsPath = homePath.resolve("Downloads");

        if (Files.exists(desktopPath)) {
            addQuickAccessItem("Desktop", "desktop", () -> controller.navigateTo(desktopPath));
        }
        if (Files.exists(documentsPath)) {
            addQuickAccessItem("Documents", "documents", () -> controller.navigateTo(documentsPath));
        }
        if (Files.exists(downloadsPath)) {
            addQuickAccessItem("Downloads", "downloads", () -> controller.navigateTo(downloadsPath));
        }
        addQuickAccessItem("Home", "home", () -> controller.navigateTo(homePath));
    }

    private void addQuickAccessItem(String title, String iconType, Runnable action) {
        Button itemBtn = new Button(title);
        itemBtn.setGraphic(IconFactory.getQuickAccessIcon(iconType, 16));
        itemBtn.setMaxWidth(Double.MAX_VALUE);
        itemBtn.setAlignment(Pos.CENTER_LEFT);
        itemBtn.getStyleClass().add("quick-access-btn");
        itemBtn.setOnAction(e -> action.run());
        quickAccessBox.getChildren().add(itemBtn);
    }

    private void setupDirectoryTree() {
        directoryTree.setShowRoot(true);
        directoryTree.getStyleClass().add("sidebar-tree");

        directoryTree.setCellFactory(tv -> new TreeCell<Path>() {
            @Override
            protected void updateItem(Path path, boolean empty) {
                super.updateItem(path, empty);
                if (empty || path == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String name = path.getFileName() == null ? path.toString() : path.getFileName().toString();
                    setText(name);
                    setGraphic(IconFactory.getFolderIcon(16));
                }
            }
        });

        directoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null) {
                Path selectedPath = newVal.getValue();
                Path current = controller.currentPathProperty().get();
                if (current == null || !current.equals(selectedPath)) {
                    controller.navigateTo(selectedPath);
                }
            }
        });
    }

    public void buildTreeRoot(Path rootPath) {
        if (rootPath == null || !Files.exists(rootPath)) return;

        LazyTreeItem rootItem = new LazyTreeItem(rootPath);
        rootItem.setExpanded(true);
        directoryTree.setRoot(rootItem);
    }

    private void selectPathInTree(Path path) {
        if (path == null || directoryTree.getRoot() == null) return;
        TreeItem<Path> target = findTreeItem(directoryTree.getRoot(), path);
        if (target != null) {
            directoryTree.getSelectionModel().select(target);
        }
    }

    private TreeItem<Path> findTreeItem(TreeItem<Path> current, Path target) {
        if (current == null) return null;
        if (target.equals(current.getValue())) return current;

        for (TreeItem<Path> child : current.getChildren()) {
            TreeItem<Path> found = findTreeItem(child, target);
            if (found != null) return found;
        }
        return null;
    }

    /**
     * Lazy-loading TreeItem for directories to keep navigation fast.
     */
    private static class LazyTreeItem extends TreeItem<Path> {
        private boolean isFirstChildren = true;
        private boolean isFirstLeaf = true;
        private boolean isLeaf = false;

        LazyTreeItem(Path path) {
            super(path);
        }

        @Override
        public javafx.collections.ObservableList<TreeItem<Path>> getChildren() {
            if (isFirstChildren) {
                isFirstChildren = false;
                buildChildren();
            }
            return super.getChildren();
        }

        @Override
        public boolean isLeaf() {
            if (isFirstLeaf) {
                isFirstLeaf = false;
                Path path = getValue();
                isLeaf = !Files.isDirectory(path);
            }
            return isLeaf;
        }

        private void buildChildren() {
            Path path = getValue();
            if (path != null && Files.isDirectory(path)) {
                try (Stream<Path> stream = Files.list(path)) {
                    stream.filter(p -> {
                                if (!Files.isDirectory(p)) return false;
                                String name = p.getFileName() != null ? p.getFileName().toString() : "";
                                return !name.startsWith(".") && !name.equals("target") && !name.equals("build") && !name.equals("bin");
                            })
                            .sorted((p1, p2) -> {
                                String n1 = p1.getFileName() != null ? p1.getFileName().toString() : "";
                                String n2 = p2.getFileName() != null ? p2.getFileName().toString() : "";
                                return n1.compareToIgnoreCase(n2);
                            })
                            .forEach(p -> super.getChildren().add(new LazyTreeItem(p)));
                } catch (Exception ignored) {}
            }
        }
    }
}
