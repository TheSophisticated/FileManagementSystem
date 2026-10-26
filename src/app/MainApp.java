package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;

/**
 * Main application entry point for File Management System.
 */
public class MainApp extends Application {

    private final ExplorerController controller = new ExplorerController();

    @Override
    public void start(Stage stage) {
        // Layout components
        BreadcrumbBar breadcrumbBar = new BreadcrumbBar(controller);
        ActionToolbar actionToolbar = new ActionToolbar(controller);
        VBox topContainer = new VBox(breadcrumbBar, actionToolbar);
        topContainer.getStyleClass().add("top-bar-container");

        SidebarView sidebarView = new SidebarView(controller);
        StatusBar statusBar = new StatusBar(controller);
        FileViewContainer fileViewContainer = new FileViewContainer(controller, statusBar);
        FileContextInspector contextInspector = new FileContextInspector(controller);

        SplitPane centerSplit = new SplitPane();
        centerSplit.getItems().addAll(sidebarView, fileViewContainer, contextInspector);
        centerSplit.setDividerPositions(0.20, 0.76);

        // Control inspector visibility in split pane
        controller.detailsVisibleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (!centerSplit.getItems().contains(contextInspector)) {
                    centerSplit.getItems().add(contextInspector);
                    centerSplit.setDividerPositions(0.20, 0.76);
                }
            } else {
                centerSplit.getItems().remove(contextInspector);
            }
        });

        BorderPane rootLayout = new BorderPane();
        rootLayout.getStyleClass().add("main-window-layout");
        rootLayout.setTop(topContainer);
        rootLayout.setCenter(centerSplit);
        rootLayout.setBottom(statusBar);

        Scene scene = new Scene(rootLayout, 1200, 760);

        // Load stylesheet
        var cssUrl = getClass().getResource("styles.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Theme binding
        controller.darkModeProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                rootLayout.getStyleClass().remove("light-theme");
            } else {
                rootLayout.getStyleClass().add("light-theme");
            }
        });

        // Dynamic stage title
        controller.currentPathProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                String folderName = newVal.getFileName() != null ? newVal.getFileName().toString() : newVal.toString();
                stage.setTitle("File Management System - " + folderName);
            } else {
                stage.setTitle("File Management System");
            }
        });

        setupKeyboardShortcuts(scene, stage, breadcrumbBar);

        // Open initial workspace (current working directory)
        Path initialPath = Path.of(".").toAbsolutePath().normalize();
        controller.openWorkspace(initialPath);

        stage.setScene(scene);
        stage.setTitle("File Management System");
        stage.setMinWidth(860);
        stage.setMinHeight(560);
        stage.show();
    }

    private void setupKeyboardShortcuts(Scene scene, Stage stage, BreadcrumbBar breadcrumbBar) {
        KeyCombination keyNewFile = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyNewFolder = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
        KeyCombination keyCopy = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyCut = new KeyCodeCombination(KeyCode.X, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyPaste = new KeyCodeCombination(KeyCode.V, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyDuplicate = new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyProperties = new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keySearch = new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyRefresh = new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyViewTable = new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyViewTiles = new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyViewList = new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyViewGraph = new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN);
        KeyCombination keyToggleInspector = new KeyCodeCombination(KeyCode.B, KeyCombination.SHORTCUT_DOWN);

        scene.setOnKeyPressed(event -> {
            if (keyNewFolder.match(event)) {
                controller.promptCreateFolder(stage);
                event.consume();
            } else if (keyNewFile.match(event)) {
                controller.promptCreateFile(stage);
                event.consume();
            } else if (keyCopy.match(event)) {
                controller.copySelected();
                event.consume();
            } else if (keyCut.match(event)) {
                controller.cutSelected();
                event.consume();
            } else if (keyPaste.match(event)) {
                controller.pasteItems();
                event.consume();
            } else if (keyDuplicate.match(event)) {
                controller.duplicateSelected(stage);
                event.consume();
            } else if (keyProperties.match(event)) {
                var selected = controller.getSelectedItems();
                if (selected.size() == 1) {
                    controller.showProperties(selected.get(0), stage);
                } else if (controller.currentPathProperty().get() != null) {
                    controller.showProperties(controller.currentPathProperty().get(), stage);
                }
                event.consume();
            } else if (keySearch.match(event)) {
                breadcrumbBar.focusSearch();
                event.consume();
            } else if (keyRefresh.match(event) || event.getCode() == KeyCode.F5) {
                controller.refresh();
                event.consume();
            } else if (keyViewTable.match(event)) {
                controller.viewModeProperty().set(FileViewMode.TABLE);
                event.consume();
            } else if (keyViewTiles.match(event)) {
                controller.viewModeProperty().set(FileViewMode.TILES);
                event.consume();
            } else if (keyViewList.match(event)) {
                controller.viewModeProperty().set(FileViewMode.LIST);
                event.consume();
            } else if (keyViewGraph.match(event)) {
                controller.viewModeProperty().set(FileViewMode.GRAPH);
                event.consume();
            } else if (keyToggleInspector.match(event)) {
                controller.toggleDetailsPanel();
                event.consume();
            } else if (event.isAltDown() && event.getCode() == KeyCode.LEFT) {
                controller.navigateBack();
                event.consume();
            } else if (event.isAltDown() && event.getCode() == KeyCode.RIGHT) {
                controller.navigateForward();
                event.consume();
            } else if (event.isAltDown() && event.getCode() == KeyCode.UP) {
                controller.navigateUp();
                event.consume();
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
