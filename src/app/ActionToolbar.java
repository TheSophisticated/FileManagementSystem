package app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Window;

/**
 * Secondary toolbar providing file actions (+Folder, +File, Cut, Copy, Paste, etc.), view switcher, and toggles.
 */
public class ActionToolbar extends HBox {

    private final ExplorerController controller;

    // Action Buttons
    private final Button newFolderBtn = new Button("New Folder");
    private final Button newFileBtn = new Button("New File");
    private final Button cutBtn = new Button("Cut");
    private final Button copyBtn = new Button("Copy");
    private final Button pasteBtn = new Button("Paste");
    private final Button duplicateBtn = new Button("Duplicate");
    private final Button renameBtn = new Button("Rename");
    private final Button deleteBtn = new Button("Delete");
    private final Button propertiesBtn = new Button("Properties");

    // View Switchers
    private final ToggleButton tableViewBtn = new ToggleButton();
    private final ToggleButton tilesViewBtn = new ToggleButton();
    private final ToggleButton listViewBtn = new ToggleButton();
    private final ToggleButton graphViewBtn = new ToggleButton();
    private final ToggleGroup viewGroup = new ToggleGroup();

    // Side toggles
    private final ToggleButton detailsToggleBtn = new ToggleButton();
    private final Button themeToggleBtn = new Button();

    public ActionToolbar(ExplorerController controller) {
        super(8);
        this.controller = controller;
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(6, 16, 6, 16));
        this.getStyleClass().add("action-toolbar");

        setupActionButtons();
        setupViewSwitchers();
        setupToggles();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actionsGroup = new HBox(6,
                newFolderBtn, newFileBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                cutBtn, copyBtn, pasteBtn, duplicateBtn, renameBtn, deleteBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL),
                propertiesBtn
        );
        actionsGroup.setAlignment(Pos.CENTER_LEFT);

        HBox viewSwitcherGroup = new HBox(2,
                tableViewBtn, tilesViewBtn, listViewBtn, graphViewBtn
        );
        viewSwitcherGroup.setAlignment(Pos.CENTER);
        viewSwitcherGroup.getStyleClass().add("segmented-button-bar");

        HBox sideToggles = new HBox(6,
                detailsToggleBtn, themeToggleBtn
        );
        sideToggles.setAlignment(Pos.CENTER_RIGHT);

        this.getChildren().addAll(actionsGroup, spacer, viewSwitcherGroup, new Separator(javafx.geometry.Orientation.VERTICAL), sideToggles);

        // Bindings
        controller.canPasteProperty().addListener((obs, oldVal, newVal) -> pasteBtn.setDisable(!newVal));
        pasteBtn.setDisable(!controller.canPasteProperty().get());

        controller.getSelectedItems().addListener((javafx.collections.ListChangeListener<models.FileSystemItem>) c -> updateSelectionStates());
        updateSelectionStates();

        controller.darkModeProperty().addListener((obs, oldVal, newVal) -> updateTheme(newVal));
        updateTheme(controller.darkModeProperty().get());
    }

    private void setupActionButtons() {
        Window window = null; // retrieved on click

        newFolderBtn.setGraphic(IconFactory.getActionIcon("plus_folder", 14, Color.web("#f59e0b")));
        newFolderBtn.getStyleClass().add("action-btn");
        newFolderBtn.setTooltip(new Tooltip("Create New Folder (Cmd+Shift+N)"));
        newFolderBtn.setOnAction(e -> controller.promptCreateFolder(getScene().getWindow()));

        newFileBtn.setGraphic(IconFactory.getActionIcon("plus_file", 14, Color.web("#3b82f6")));
        newFileBtn.getStyleClass().add("action-btn");
        newFileBtn.setTooltip(new Tooltip("Create New File (Cmd+N)"));
        newFileBtn.setOnAction(e -> controller.promptCreateFile(getScene().getWindow()));

        cutBtn.setGraphic(IconFactory.getActionIcon("cut", 14, Color.web("#f59e0b")));
        cutBtn.getStyleClass().add("action-btn");
        cutBtn.setTooltip(new Tooltip("Cut Selected Items (Cmd+X)"));
        cutBtn.setOnAction(e -> controller.cutSelected());

        copyBtn.setGraphic(IconFactory.getActionIcon("copy", 14, Color.web("#3b82f6")));
        copyBtn.getStyleClass().add("action-btn");
        copyBtn.setTooltip(new Tooltip("Copy Selected Items (Cmd+C)"));
        copyBtn.setOnAction(e -> controller.copySelected());

        pasteBtn.setGraphic(IconFactory.getActionIcon("paste", 14, Color.web("#10b981")));
        pasteBtn.getStyleClass().add("action-btn");
        pasteBtn.setTooltip(new Tooltip("Paste from Clipboard (Cmd+V)"));
        pasteBtn.setOnAction(e -> controller.pasteItems());

        duplicateBtn.setGraphic(IconFactory.getActionIcon("duplicate", 14, Color.web("#8b5cf6")));
        duplicateBtn.getStyleClass().add("action-btn");
        duplicateBtn.setTooltip(new Tooltip("Duplicate Selected Items (Cmd+D)"));
        duplicateBtn.setOnAction(e -> controller.duplicateSelected(getScene().getWindow()));

        renameBtn.setGraphic(IconFactory.getActionIcon("rename", 14, Color.web("#06b6d4")));
        renameBtn.getStyleClass().add("action-btn");
        renameBtn.setTooltip(new Tooltip("Rename (F2)"));
        renameBtn.setOnAction(e -> {
            var selected = controller.getSelectedItems();
            if (selected.size() == 1) {
                controller.promptRename(selected.get(0), getScene().getWindow());
            }
        });

        deleteBtn.setGraphic(IconFactory.getActionIcon("delete", 14, Color.web("#ef4444")));
        deleteBtn.getStyleClass().add("action-btn-danger");
        deleteBtn.setTooltip(new Tooltip("Delete Selected Items (Delete)"));
        deleteBtn.setOnAction(e -> controller.deleteSelected(getScene().getWindow()));

        propertiesBtn.setGraphic(IconFactory.getActionIcon("properties", 14, Color.web("#6366f1")));
        propertiesBtn.getStyleClass().add("action-btn");
        propertiesBtn.setTooltip(new Tooltip("View File Context & Properties (Cmd+I)"));
        propertiesBtn.setOnAction(e -> {
            var selected = controller.getSelectedItems();
            if (selected.size() == 1) {
                controller.showProperties(selected.get(0), getScene().getWindow());
            } else if (controller.currentPathProperty().get() != null) {
                controller.showProperties(controller.currentPathProperty().get(), getScene().getWindow());
            }
        });
    }

    private void setupViewSwitchers() {
        tableViewBtn.setToggleGroup(viewGroup);
        tilesViewBtn.setToggleGroup(viewGroup);
        listViewBtn.setToggleGroup(viewGroup);
        graphViewBtn.setToggleGroup(viewGroup);

        tableViewBtn.setGraphic(IconFactory.getActionIcon("view_table", 14, Color.web("#94a3b8")));
        tableViewBtn.setTooltip(new Tooltip("Detailed Table View (Cmd+1)"));
        tableViewBtn.getStyleClass().add("segmented-btn");

        tilesViewBtn.setGraphic(IconFactory.getActionIcon("view_tiles", 14, Color.web("#94a3b8")));
        tilesViewBtn.setTooltip(new Tooltip("Tile / Grid View (Cmd+2)"));
        tilesViewBtn.getStyleClass().add("segmented-btn");

        listViewBtn.setGraphic(IconFactory.getActionIcon("view_list", 14, Color.web("#94a3b8")));
        listViewBtn.setTooltip(new Tooltip("List View (Cmd+3)"));
        listViewBtn.getStyleClass().add("segmented-btn");

        graphViewBtn.setGraphic(IconFactory.getActionIcon("view_graph", 14, Color.web("#94a3b8")));
        graphViewBtn.setTooltip(new Tooltip("Radial Graph View (Cmd+4)"));
        graphViewBtn.getStyleClass().add("segmented-btn");

        tableViewBtn.setSelected(true);

        tableViewBtn.setOnAction(e -> controller.viewModeProperty().set(FileViewMode.TABLE));
        tilesViewBtn.setOnAction(e -> controller.viewModeProperty().set(FileViewMode.TILES));
        listViewBtn.setOnAction(e -> controller.viewModeProperty().set(FileViewMode.LIST));
        graphViewBtn.setOnAction(e -> controller.viewModeProperty().set(FileViewMode.GRAPH));

        controller.viewModeProperty().addListener((obs, oldVal, newVal) -> {
            switch (newVal) {
                case TABLE -> tableViewBtn.setSelected(true);
                case TILES -> tilesViewBtn.setSelected(true);
                case LIST -> listViewBtn.setSelected(true);
                case GRAPH -> graphViewBtn.setSelected(true);
            }
        });
    }

    private void setupToggles() {
        detailsToggleBtn.setGraphic(IconFactory.getActionIcon("details", 14, Color.web("#94a3b8")));
        detailsToggleBtn.setTooltip(new Tooltip("Toggle File Context Inspector"));
        detailsToggleBtn.getStyleClass().add("action-icon-btn");
        detailsToggleBtn.setSelected(controller.detailsVisibleProperty().get());
        detailsToggleBtn.setOnAction(e -> controller.detailsVisibleProperty().set(detailsToggleBtn.isSelected()));
        controller.detailsVisibleProperty().addListener((obs, oldVal, newVal) -> detailsToggleBtn.setSelected(newVal));

        themeToggleBtn.setGraphic(IconFactory.getActionIcon("theme", 14, Color.web("#f59e0b")));
        themeToggleBtn.setTooltip(new Tooltip("Toggle Dark / Light Theme"));
        themeToggleBtn.getStyleClass().add("action-icon-btn");
        themeToggleBtn.setOnAction(e -> controller.toggleTheme());
    }

    private void updateSelectionStates() {
        var selected = controller.getSelectedItems();
        boolean hasSelection = !selected.isEmpty();
        boolean single = selected.size() == 1;

        cutBtn.setDisable(!hasSelection);
        copyBtn.setDisable(!hasSelection);
        duplicateBtn.setDisable(!hasSelection);
        renameBtn.setDisable(!single);
        deleteBtn.setDisable(!hasSelection);
    }

    private void updateTheme(boolean dark) {
        Color iconColor = dark ? Color.web("#cbd5e1") : Color.web("#475569");
        tableViewBtn.setGraphic(IconFactory.getActionIcon("view_table", 14, iconColor));
        tilesViewBtn.setGraphic(IconFactory.getActionIcon("view_tiles", 14, iconColor));
        listViewBtn.setGraphic(IconFactory.getActionIcon("view_list", 14, iconColor));
        graphViewBtn.setGraphic(IconFactory.getActionIcon("view_graph", 14, iconColor));
        detailsToggleBtn.setGraphic(IconFactory.getActionIcon("details", 14, iconColor));
    }
}
