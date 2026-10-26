package com.filemanager.service;

import com.filemanager.model.FileMetadata;
import models.FileSystemItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;

/**
 * Service for reading file metadata and properties.
 */
public class MetadataService {

    public FileMetadata read(FileSystemItem item) throws IOException {
        if (item == null || item.getPath() == null) {
            throw new IOException("Item or path is null");
        }
        return read(item.getPath());
    }

    public FileMetadata read(Path path) throws IOException {
        if (path == null || !Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IOException("File does not exist: " + path);
        }

        BasicFileAttributes attrs = Files.readAttributes(
                path,
                BasicFileAttributes.class,
                LinkOption.NOFOLLOW_LINKS
        );

        Path fileName = path.getFileName();
        String name = fileName == null ? path.toString() : fileName.toString();

        boolean directory = attrs.isDirectory();
        boolean symbolicLink = attrs.isSymbolicLink();

        String extension = "";
        if (!directory && !symbolicLink) {
            int dot = name.lastIndexOf('.');
            if (dot > 0 && dot < name.length() - 1) {
                extension = name.substring(dot + 1).toLowerCase();
            }
        }

        String type;
        if (symbolicLink) {
            type = "Symbolic Link";
        } else if (directory) {
            type = "Folder";
        } else if (!extension.isEmpty()) {
            type = getDescriptiveFileType(extension);
        } else {
            type = "File";
        }

        Instant created = attrs.creationTime() != null ? attrs.creationTime().toInstant() : Instant.EPOCH;
        Instant modified = attrs.lastModifiedTime() != null ? attrs.lastModifiedTime().toInstant() : Instant.EPOCH;
        Instant accessed = attrs.lastAccessTime() != null ? attrs.lastAccessTime().toInstant() : Instant.EPOCH;

        boolean hidden = false;
        try {
            hidden = Files.isHidden(path);
        } catch (Exception ignored) {}

        return new FileMetadata(
                name,
                type,
                extension,
                path.toAbsolutePath().normalize(),
                attrs.size(),
                created,
                modified,
                accessed,
                directory,
                hidden,
                Files.isReadable(path),
                Files.isWritable(path),
                Files.isExecutable(path)
        );
    }

    private String getDescriptiveFileType(String ext) {
        return switch (ext.toLowerCase()) {
            case "java" -> "Java Source";
            case "class" -> "Java Bytecode";
            case "jar" -> "Java Archive (JAR)";
            case "py" -> "Python Source";
            case "c" -> "C Source";
            case "cpp", "cc", "cxx" -> "C++ Source";
            case "h", "hpp" -> "Header File";
            case "js" -> "JavaScript";
            case "ts" -> "TypeScript";
            case "html", "htm" -> "HTML Document";
            case "css" -> "CSS Stylesheet";
            case "json" -> "JSON Document";
            case "xml" -> "XML Document";
            case "yaml", "yml" -> "YAML Document";
            case "md", "markdown" -> "Markdown Document";
            case "txt" -> "Plain Text";
            case "pdf" -> "PDF Document";
            case "doc", "docx" -> "Word Document";
            case "xls", "xlsx" -> "Excel Spreadsheet";
            case "ppt", "pptx" -> "PowerPoint Presentation";
            case "png" -> "PNG Image";
            case "jpg", "jpeg" -> "JPEG Image";
            case "gif" -> "GIF Image";
            case "svg" -> "SVG Vector Image";
            case "webp" -> "WebP Image";
            case "mp3" -> "MP3 Audio";
            case "wav" -> "WAV Audio";
            case "mp4" -> "MP4 Video";
            case "mkv" -> "Matroska Video";
            case "zip" -> "ZIP Archive";
            case "tar", "gz" -> "Tarball Archive";
            case "sh" -> "Shell Script";
            case "exe" -> "Executable";
            case "dmg" -> "macOS Disk Image";
            default -> ext.toUpperCase() + " File";
        };
    }
}
