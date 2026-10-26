package app;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import models.FolderItem;

import java.io.IOException;

/**
 * Central container that hosts and seamlessly switches between Table, Tiles, List, and Graph views.
 */
public class FileViewContainer extends StackPane {

    private final ExplorerController controller;
    private final DetailedTableView tableView;
    private final TileGridView tileGridView;
    private final FileListView fileListView;

    private final FolderGraphView graphView;
    private final ScrollPane graphScrollPane;
    private final GraphZoomController zoomController;

    public FileViewContainer(ExplorerController controller, StatusBar statusBar) {
        this.controller = controller;
        this.getStyleClass().add("file-view-container");

        // Initialize views
        this.tableView = new DetailedTableView(controller);
        this.tileGridView = new TileGridView(controller);
        this.fileListView = new FileListView(controller);

        this.graphView = new FolderGraphView(
                controller.getFileSystem(),
                err -> controller.statusMessageProperty().set("Graph error: " + err.getMessage()),
                msg -> controller.statusMessageProperty().set(msg)
        );
        this.graphView.setController(controller);

        this.graphScrollPane = new ScrollPane(graphView);
        graphScrollPane.setPannable(true);
        graphScrollPane.setFitToWidth(false);
        graphScrollPane.setFitToHeight(false);
        graphScrollPane.getStyleClass().add("graph-scroll-pane");

        this.zoomController = new GraphZoomController(graphScrollPane, graphView, statusBar.getZoomPercentLabel());
        this.zoomController.bindControls(statusBar.getZoomOutBtn(), statusBar.getZoomInBtn(), statusBar.getZoomResetBtn());
        this.zoomController.attachGestures();

        this.getChildren().addAll(tableView, tileGridView, fileListView, graphScrollPane);

        // View mode listener
        controller.viewModeProperty().addListener((obs, oldVal, newVal) -> updateActiveView(newVal));

        // Workspace / directory listener for graph view
        controller.currentFolderItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                try {
                    graphView.setWorkspace(newVal);
                } catch (IOException ignored) {}
            }
        });

        controller.darkModeProperty().addListener((obs, oldVal, newVal) -> graphView.setDarkMode(newVal));

        updateActiveView(controller.viewModeProperty().get());
    }

    private void updateActiveView(FileViewMode mode) {
        tableView.setVisible(mode == FileViewMode.TABLE);
        tableView.setManaged(mode == FileViewMode.TABLE);

        tileGridView.setVisible(mode == FileViewMode.TILES);
        tileGridView.setManaged(mode == FileViewMode.TILES);

        fileListView.setVisible(mode == FileViewMode.LIST);
        fileListView.setManaged(mode == FileViewMode.LIST);

        graphScrollPane.setVisible(mode == FileViewMode.GRAPH);
        graphScrollPane.setManaged(mode == FileViewMode.GRAPH);

        if (mode == FileViewMode.GRAPH) {
            FolderItem curr = controller.currentFolderItemProperty().get();
            if (curr != null) {
                try {
                    graphView.setWorkspace(curr);
                } catch (IOException ignored) {}
            }
        }
    }

    public DetailedTableView getTableView() { return tableView; }
    public TileGridView getTileGridView() { return tileGridView; }
    public FileListView getFileListView() { return fileListView; }
    public FolderGraphView getGraphView() { return graphView; }
}
