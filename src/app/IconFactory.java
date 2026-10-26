package app;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import models.FileItem;
import models.FileSystemItem;
import models.FolderItem;

import java.nio.file.Path;

/**
 * Factory for creating crisp, modern vector icons for files, folders, and actions.
 */
public class IconFactory {

    // SVG Path Constants
    private static final String SVG_FOLDER =
            "M10 4H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2h-8l-2-2z";

    private static final String SVG_FOLDER_OPEN =
            "M20 6h-8l-2-2H4c-1.1 0-1.99.9-1.99 2L2 18c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V8c0-1.1-.9-2-2-2zm0 12H4V8h16v10z";

    private static final String SVG_FILE =
            "M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z";

    private static final String SVG_CODE =
            "M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z";

    private static final String SVG_IMAGE =
            "M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z";

    private static final String SVG_MUSIC =
            "M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z";

    private static final String SVG_VIDEO =
            "M17 10.5V7c0-.55-.45-1-1-1H4c-.55 0-1 .45-1 1v10c0 .55.45 1 1 1h12c.55 0 1-.45 1-1v-3.5l4 4v-11l-4 4z";

    private static final String SVG_ARCHIVE =
            "M20 6h-4V4c0-1.11-.89-2-2-2h-4c-1.11 0-2 .89-2 2v2H4c-1.11 0-1.99.89-1.99 2L2 19c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-6 0h-4V4h4v2z";

    private static final String SVG_HOME =
            "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z";

    private static final String SVG_DESKTOP =
            "M21 2H3c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h7l-2 3v1h8v-1l-2-3h7c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 12H3V4h18v10z";

    private static final String SVG_DOCUMENT =
            "M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z";

    private static final String SVG_DOWNLOAD =
            "M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z";

    private static final String SVG_WORKSPACE =
            "M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H8V4h12v12z";

    private static final String SVG_BACK =
            "M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z";

    private static final String SVG_FORWARD =
            "M12 4l-1.41 1.41L16.17 11H4v2h12.17l-5.58 5.59L12 20l8-8z";

    private static final String SVG_UP =
            "M4 12l1.41 1.41L11 7.83V20h2V7.83l5.58 5.59L20 12l-8-8-8 8z";

    private static final String SVG_REFRESH =
            "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z";

    private static final String SVG_SEARCH =
            "M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z";

    private static final String SVG_CLEAR =
            "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z";

    private static final String SVG_PLUS_FOLDER =
            "M20 6h-8l-2-2H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V8c0-1.11-.89-2-2-2zm-1 8h-3v3h-2v-3h-3v-2h3v-3h2v3h3v2z";

    private static final String SVG_PLUS_FILE =
            "M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 14h-3v3h-2v-3H8v-2h3v-3h2v3h3v2zm-3-7V3.5L18.5 9H13z";

    private static final String SVG_CUT =
            "M9.64 7.64c.23-.5.36-1.05.36-1.64 0-2.21-1.79-4-4-4S2 3.79 2 6s1.79 4 4 4c.59 0 1.14-.13 1.64-.36L10 12l-2.36 2.36C7.14 14.13 6.59 14 6 14c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4c0-.59-.13-1.14-.36-1.64L12 14l7 7h3v-1L9.64 7.64zM6 8c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm0 12c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm6-7.5c-.28 0-.5-.22-.5-.5s.22-.5.5-.5.5.22.5.5-.22.5-.5.5zM19 3l-6 6 2 2 7-7V3h-3z";

    private static final String SVG_COPY =
            "M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z";

    private static final String SVG_PASTE =
            "M19 2h-4.18C14.4 .84 13.3 0 12 0c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm7 18H5V4h2v3h10V4h2v16z";

    private static final String SVG_DUPLICATE =
            "M15 1H4c-1.1 0-2 .9-2 2v13c0 .55.45 1 1 1s1-.45 1-1V4c0-.55.45-1 1-1h10c.55 0 1-.45 1-1s-.45-1-1-1zm4 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm-1 16H9c-.55 0-1-.45-1-1V8c0-.55.45-1 1-1h9c.55 0 1 .45 1 1v12c0 .55-.45 1-1 1z";

    private static final String SVG_RENAME =
            "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z";

    private static final String SVG_DELETE =
            "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z";

    private static final String SVG_PROPERTIES =
            "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z";

    private static final String SVG_VIEW_TABLE =
            "M4 3h16c1.1 0 2 .9 2 2v14c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V5c0-1.1.9-2 2-2zm0 4h16V5H4v2zm0 4h16V9H4v2zm0 4h16v-2H4v2zm0 4h16v-2H4v2z";

    private static final String SVG_VIEW_TILES =
            "M4 11h5V5H4v6zm0 7h5v-6H4v6zm6 0h5v-6h-5v6zm6 0h5v-6h-5v6zm-6-7h5V5h-5v6zm6-6v6h5V5h-5z";

    private static final String SVG_VIEW_LIST =
            "M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z";

    private static final String SVG_VIEW_GRAPH =
            "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm-5 11c0-1.66 1.34-3 3-3 .24 0 .47.03.7.08l1.3-2.08c-.62-.48-1-1.22-1-2.08 0-1.46 1.18-2.65 2.63-2.73V5h.74v2.17C15.82 7.25 17 8.44 17 9.9c0 .86-.38 1.6-1 2.08l1.3 2.08c.23-.05.46-.08.7-.08 1.66 0 3 1.34 3 3v1H7v-1z";

    private static final String SVG_THEME =
            "M20 8.69V4h-4.69L12 .69 8.69 4H4v4.69L.69 12 4 15.31V20h4.69L12 23.31 15.31 20H20v-4.69L23.31 12 20 8.69zM12 18c-3.31 0-6-2.69-6-6s2.69-6 6-6 6 2.69 6 6-2.69 6-6 6zm0-10c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4z";

    private static final String SVG_DETAILS =
            "M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 14h-2v-4h2v4zm0-6h-2V7h2v4zm5 6h-2v-2h2v2zm0-4h-2V7h2v6z";

    public static Node getIconForItem(FileSystemItem item, double size) {
        if (item instanceof FolderItem) {
            return createSvgIcon(SVG_FOLDER, Color.web("#f59e0b"), size);
        } else if (item instanceof FileItem) {
            return getIconForPath(item.getPath(), size);
        }
        return createSvgIcon(SVG_FILE, Color.web("#64748b"), size);
    }

    public static Node getIconForPath(Path path, double size) {
        String name = path.getFileName() == null ? path.toString() : path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        String ext = (dot > 0 && dot < name.length() - 1) ? name.substring(dot + 1).toLowerCase() : "";

        return switch (ext) {
            case "java" -> createBadgeIcon("JAVA", Color.web("#ea580c"), size);
            case "class", "jar" -> createBadgeIcon("JAR", Color.web("#d97706"), size);
            case "py" -> createBadgeIcon("PY", Color.web("#3b82f6"), size);
            case "js", "ts", "jsx", "tsx" -> createBadgeIcon("JS", Color.web("#eab308"), size);
            case "html", "htm", "css" -> createBadgeIcon("WEB", Color.web("#06b6d4"), size);
            case "json", "xml", "yaml", "yml" -> createBadgeIcon("DATA", Color.web("#10b981"), size);
            case "md", "markdown", "txt" -> createBadgeIcon("TXT", Color.web("#8b5cf6"), size);
            case "pdf" -> createBadgeIcon("PDF", Color.web("#ef4444"), size);
            case "png", "jpg", "jpeg", "gif", "svg", "webp" -> createSvgIcon(SVG_IMAGE, Color.web("#10b981"), size);
            case "mp3", "wav", "flac", "ogg" -> createSvgIcon(SVG_MUSIC, Color.web("#ec4899"), size);
            case "mp4", "mkv", "mov", "avi" -> createSvgIcon(SVG_VIDEO, Color.web("#8b5cf6"), size);
            case "zip", "tar", "gz", "rar", "7z" -> createSvgIcon(SVG_ARCHIVE, Color.web("#f59e0b"), size);
            case "sh", "bat", "exe", "dmg" -> createBadgeIcon("EXE", Color.web("#6366f1"), size);
            case "c", "cpp", "h", "hpp" -> createBadgeIcon("C++", Color.web("#0284c7"), size);
            default -> createSvgIcon(SVG_FILE, Color.web("#94a3b8"), size);
        };
    }

    public static Node getFolderIcon(double size) {
        return createSvgIcon(SVG_FOLDER, Color.web("#f59e0b"), size);
    }

    public static Node getOpenFolderIcon(double size) {
        return createSvgIcon(SVG_FOLDER_OPEN, Color.web("#f59e0b"), size);
    }

    public static Node getQuickAccessIcon(String shortcut, double size) {
        return switch (shortcut.toLowerCase()) {
            case "workspace" -> createSvgIcon(SVG_WORKSPACE, Color.web("#3b82f6"), size);
            case "desktop" -> createSvgIcon(SVG_DESKTOP, Color.web("#6366f1"), size);
            case "documents" -> createSvgIcon(SVG_DOCUMENT, Color.web("#10b981"), size);
            case "downloads" -> createSvgIcon(SVG_DOWNLOAD, Color.web("#06b6d4"), size);
            case "home" -> createSvgIcon(SVG_HOME, Color.web("#f59e0b"), size);
            default -> createSvgIcon(SVG_FOLDER, Color.web("#64748b"), size);
        };
    }

    public static Node getActionIcon(String action, double size, Color color) {
        String path = switch (action.toLowerCase()) {
            case "back" -> SVG_BACK;
            case "forward" -> SVG_FORWARD;
            case "up" -> SVG_UP;
            case "refresh" -> SVG_REFRESH;
            case "search" -> SVG_SEARCH;
            case "clear" -> SVG_CLEAR;
            case "plus_folder" -> SVG_PLUS_FOLDER;
            case "plus_file" -> SVG_PLUS_FILE;
            case "cut" -> SVG_CUT;
            case "copy" -> SVG_COPY;
            case "paste" -> SVG_PASTE;
            case "duplicate" -> SVG_DUPLICATE;
            case "rename" -> SVG_RENAME;
            case "delete" -> SVG_DELETE;
            case "properties" -> SVG_PROPERTIES;
            case "view_table" -> SVG_VIEW_TABLE;
            case "view_tiles" -> SVG_VIEW_TILES;
            case "view_list" -> SVG_VIEW_LIST;
            case "view_graph" -> SVG_VIEW_GRAPH;
            case "theme" -> SVG_THEME;
            case "details" -> SVG_DETAILS;
            default -> SVG_FILE;
        };
        return createSvgIcon(path, color, size);
    }

    private static Node createSvgIcon(String svgContent, Color fill, double targetSize) {
        SVGPath path = new SVGPath();
        path.setContent(svgContent);
        path.setFill(fill);

        // Normalize default 24x24 SVG
        double scale = targetSize / 24.0;
        path.setScaleX(scale);
        path.setScaleY(scale);

        StackPane pane = new StackPane(path);
        pane.setMinSize(targetSize, targetSize);
        pane.setPrefSize(targetSize, targetSize);
        pane.setMaxSize(targetSize, targetSize);
        pane.setAlignment(Pos.CENTER);
        return pane;
    }

    private static Node createBadgeIcon(String text, Color baseColor, double size) {
        SVGPath doc = new SVGPath();
        doc.setContent(SVG_FILE);
        doc.setFill(baseColor);
        double scale = size / 24.0;
        doc.setScaleX(scale);
        doc.setScaleY(scale);

        Label label = new Label(text);
        label.setStyle("-fx-font-size: " + Math.max(7, size * 0.22) + "px; -fx-font-weight: bold; -fx-text-fill: white;");

        StackPane badge = new StackPane(doc, label);
        badge.setMinSize(size, size);
        badge.setPrefSize(size, size);
        badge.setMaxSize(size, size);
        badge.setAlignment(Pos.CENTER);
        StackPane.setAlignment(label, Pos.BOTTOM_CENTER);
        label.setTranslateY(-size * 0.12);
        return badge;
    }
}
