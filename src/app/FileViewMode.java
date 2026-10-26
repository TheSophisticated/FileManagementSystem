package app;

/**
 * Display modes for the central file explorer view.
 */
public enum FileViewMode {
    TABLE("Table View"),
    TILES("Grid View"),
    LIST("List View"),
    GRAPH("Radial Graph");

    private final String displayName;

    FileViewMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
