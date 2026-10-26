package app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Top navigation bar featuring history buttons, interactive breadcrumbs, direct path editor, and search.
 */
public class BreadcrumbBar extends HBox {

    private final ExplorerController controller;
    private final Button backBtn = new Button();
    private final Button forwardBtn = new Button();
    private final Button upBtn = new Button();
    private final Button refreshBtn = new Button();

    private final HBox crumbsContainer = new HBox(4);
    private final ScrollPane crumbsScrollPane = new ScrollPane(crumbsContainer);
    private final TextField pathField = new TextField();
    private final StackPane pathBox = new StackPane();

    private final TextField searchField = new TextField();
    private final Button clearSearchBtn = new Button();
    private final HBox searchBox = new HBox(6);

    public BreadcrumbBar(ExplorerController controller) {
        super(10);
        this.controller = controller;
        this.setAlignment(Pos.CENTER_LEFT);
        this.setPadding(new Insets(10, 16, 10, 16));
        this.getStyleClass().add("breadcrumb-bar");

        setupNavButtons();
        setupPathEditor();
        setupSearchBox();

        Region spacer = new Region();
        HBox.setHgrow(pathBox, Priority.ALWAYS);

        this.getChildren().addAll(
                backBtn, forwardBtn, upBtn, refreshBtn,
                pathBox,
                searchBox
        );

        // Listen for directory changes
        controller.currentPathProperty().addListener((obs, oldVal, newVal) -> updateCrumbs(newVal));
        controller.searchActiveProperty().addListener((obs, oldVal, newVal) -> {
            clearSearchBtn.setVisible(newVal);
            clearSearchBtn.setManaged(newVal);
            if (!newVal) {
                searchField.clear();
            }
        });
        controller.darkModeProperty().addListener((obs, oldVal, newVal) -> updateTheme(newVal));

        updateTheme(controller.darkModeProperty().get());
    }

    private void setupNavButtons() {
        backBtn.setGraphic(IconFactory.getActionIcon("back", 14, Color.web("#94a3b8")));
        backBtn.setTooltip(new Tooltip("Back (Alt+Left)"));
        backBtn.getStyleClass().add("nav-icon-btn");
        backBtn.disableProperty().bind(controller.canGoBackProperty().not());
        backBtn.setOnAction(e -> controller.navigateBack());

        forwardBtn.setGraphic(IconFactory.getActionIcon("forward", 14, Color.web("#94a3b8")));
        forwardBtn.setTooltip(new Tooltip("Forward (Alt+Right)"));
        forwardBtn.getStyleClass().add("nav-icon-btn");
        forwardBtn.disableProperty().bind(controller.canGoForwardProperty().not());
        forwardBtn.setOnAction(e -> controller.navigateForward());

        upBtn.setGraphic(IconFactory.getActionIcon("up", 14, Color.web("#94a3b8")));
        upBtn.setTooltip(new Tooltip("Up to Parent Folder (Alt+Up)"));
        upBtn.getStyleClass().add("nav-icon-btn");
        upBtn.disableProperty().bind(controller.canGoUpProperty().not());
        upBtn.setOnAction(e -> controller.navigateUp());

        refreshBtn.setGraphic(IconFactory.getActionIcon("refresh", 14, Color.web("#94a3b8")));
        refreshBtn.setTooltip(new Tooltip("Refresh (F5)"));
        refreshBtn.getStyleClass().add("nav-icon-btn");
        refreshBtn.setOnAction(e -> controller.refresh());
    }

    private void setupPathEditor() {
        crumbsContainer.setAlignment(Pos.CENTER_LEFT);
        crumbsContainer.setPadding(new Insets(2, 6, 2, 6));

        crumbsScrollPane.setFitToHeight(true);
        crumbsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        crumbsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        crumbsScrollPane.getStyleClass().add("crumbs-scroll-pane");

        pathField.setVisible(false);
        pathField.setManaged(false);
        pathField.getStyleClass().add("direct-path-field");
        pathField.setPromptText("Enter folder path...");

        // Double click crumbs to edit path directly
        crumbsScrollPane.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 || e.getTarget() == crumbsContainer) {
                switchToPathEdit();
            }
        });

        pathField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String text = pathField.getText();
                if (text != null && !text.isBlank()) {
                    controller.navigateTo(Path.of(text.trim()));
                }
                switchToCrumbs();
            } else if (e.getCode() == KeyCode.ESCAPE) {
                switchToCrumbs();
            }
        });

        pathField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                switchToCrumbs();
            }
        });

        pathBox.getChildren().addAll(crumbsScrollPane, pathField);
        pathBox.getStyleClass().add("path-box");
        pathBox.setAlignment(Pos.CENTER_LEFT);
    }

    private void setupSearchBox() {
        searchField.setPromptText("Search folder...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(200);

        searchField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                controller.search(searchField.getText());
            } else if (e.getCode() == KeyCode.ESCAPE) {
                controller.clearSearch();
            }
        });

        clearSearchBtn.setGraphic(IconFactory.getActionIcon("clear", 12, Color.web("#94a3b8")));
        clearSearchBtn.getStyleClass().add("clear-search-btn");
        clearSearchBtn.setVisible(false);
        clearSearchBtn.setManaged(false);
        clearSearchBtn.setOnAction(e -> {
            searchField.clear();
            controller.clearSearch();
        });

        searchBox.setAlignment(Pos.CENTER_RIGHT);
        searchBox.getStyleClass().add("search-box");
        searchBox.getChildren().addAll(
                IconFactory.getActionIcon("search", 14, Color.web("#94a3b8")),
                searchField,
                clearSearchBtn
        );
    }

    private void switchToPathEdit() {
        Path curr = controller.currentPathProperty().get();
        pathField.setText(curr != null ? curr.toString() : "");
        crumbsScrollPane.setVisible(false);
        crumbsScrollPane.setManaged(false);
        pathField.setVisible(true);
        pathField.setManaged(true);
        pathField.requestFocus();
        pathField.selectAll();
    }

    private void switchToCrumbs() {
        pathField.setVisible(false);
        pathField.setManaged(false);
        crumbsScrollPane.setVisible(true);
        crumbsScrollPane.setManaged(true);
    }

    private void updateCrumbs(Path path) {
        crumbsContainer.getChildren().clear();
        if (path == null) {
            return;
        }

        List<Path> segments = new ArrayList<>();
        Path p = path;
        while (p != null) {
            segments.add(p);
            p = p.getParent();
        }
        Collections.reverse(segments);

        for (int i = 0; i < segments.size(); i++) {
            Path seg = segments.get(i);
            String name = seg.getFileName() == null ? seg.toString() : seg.getFileName().toString();

            Button crumbBtn = new Button(name);
            crumbBtn.getStyleClass().add("crumb-btn");
            if (i == segments.size() - 1) {
                crumbBtn.getStyleClass().add("crumb-btn-active");
            }
            crumbBtn.setOnAction(e -> controller.navigateTo(seg));
            crumbsContainer.getChildren().add(crumbBtn);

            if (i < segments.size() - 1) {
                Label sep = new Label("›");
                sep.getStyleClass().add("crumb-separator");
                crumbsContainer.getChildren().add(sep);
            }
        }
    }

    public void focusSearch() {
        searchField.requestFocus();
        searchField.selectAll();
    }

    private void updateTheme(boolean dark) {
        Color iconColor = dark ? Color.web("#94a3b8") : Color.web("#64748b");
        backBtn.setGraphic(IconFactory.getActionIcon("back", 14, iconColor));
        forwardBtn.setGraphic(IconFactory.getActionIcon("forward", 14, iconColor));
        upBtn.setGraphic(IconFactory.getActionIcon("up", 14, iconColor));
        refreshBtn.setGraphic(IconFactory.getActionIcon("refresh", 14, iconColor));
    }
}
