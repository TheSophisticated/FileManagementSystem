package com.filemanager.model;

import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Model representing file metadata and context attributes.
 */
public class FileMetadata {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private final String name;
    private final String type;
    private final String extension;
    private final Path path;
    private final long size;
    private final Instant created;
    private final Instant modified;
    private final Instant accessed;
    private final boolean directory;
    private final boolean hidden;
    private final boolean readable;
    private final boolean writable;
    private final boolean executable;

    public FileMetadata(
            String name,
            String type,
            String extension,
            Path path,
            long size,
            Instant created,
            Instant modified,
            Instant accessed,
            boolean directory,
            boolean hidden,
            boolean readable,
            boolean writable,
            boolean executable) {

        this.name = name;
        this.type = type;
        this.extension = extension;
        this.path = path;
        this.size = size;
        this.created = created;
        this.modified = modified;
        this.accessed = accessed;
        this.directory = directory;
        this.hidden = hidden;
        this.readable = readable;
        this.writable = writable;
        this.executable = executable;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getExtension() {
        return extension;
    }

    public Path getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public Instant getCreated() {
        return created;
    }

    public Instant getModified() {
        return modified;
    }

    public Instant getAccessed() {
        return accessed;
    }

    public boolean isDirectory() {
        return directory;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isReadable() {
        return readable;
    }

    public boolean isWritable() {
        return writable;
    }

    public boolean isExecutable() {
        return executable;
    }

    public String getFormattedSize() {
        if (directory) {
            return "Folder";
        }
        if (size < 1024) {
            return size + " B";
        }
        double sz = size;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unit = 0;
        while (sz >= 1024 && unit < units.length - 1) {
            sz /= 1024;
            unit++;
        }
        return String.format(Locale.ROOT, "%.2f %s", sz, units[unit]);
    }

    public String getFormattedModified() {
        if (modified == null) return "Unknown";
        return FORMATTER.format(modified.atZone(ZoneId.systemDefault()));
    }

    public String getFormattedCreated() {
        if (created == null) return "Unknown";
        return FORMATTER.format(created.atZone(ZoneId.systemDefault()));
    }

    public String getFormattedAccessed() {
        if (accessed == null) return "Unknown";
        return FORMATTER.format(accessed.atZone(ZoneId.systemDefault()));
    }

    public String getPermissionsString() {
        return (readable ? "r" : "-")
                + (writable ? "w" : "-")
                + (executable ? "x" : "-");
    }
}
