# File Management System — Design & Architecture Specification

## Overview

The **File Management System** is a modern, high-performance desktop file explorer application built entirely in **Java (JavaFX 21)**. It bridges the core file operations backend (`src/filesystem/FileSystem.java`) with a rich, interactive graphical interface and a dedicated **File Context Window & Metadata System** (`JavaProjectContextWindow` / `com.filemanager`).

---

## 1. System Architecture

```
FileManagementSystem/
├── pom.xml                                  # Maven project configuration
├── build.gradle                             # Gradle project configuration
├── gemini.md                                # Project design & specification documentation
└── src/
    ├── filesystem/                          # Backend File Service Layer
    │   ├── FileManagerService.java          # Service interface for all file operations
    │   └── FileSystem.java                  # Concrete implementation (NIO.2, WalkTree, Streams)
    ├── models/                              # Core Domain Entities
    │   ├── FileSystemItem.java              # Abstract base item with metadata & naming logic
    │   ├── FileItem.java                    # File representation with desktop open & copy
    │   └── FolderItem.java                  # Directory representation with child listing & recursion
    ├── com/filemanager/                     # File Context Window & Metadata Engine
    │   ├── model/
    │   │   └── FileMetadata.java            # Rich metadata model (attributes, sizes, permissions)
    │   ├── service/
    │   │   └── MetadataService.java         # NIO attribute reader & MIME/extension categorizer
    │   └── ui/
    │       ├── FileContextMenu.java        # Standard context menu handler
    │       └── PropertiesDialog.java        # Dedicated File Context / Properties modal window
    └── app/                                 # JavaFX Graphical User Interface (GUI)
        ├── MainApp.java                     # Application entry point, window, & shortcut routing
        ├── ExplorerController.java          # Central MVC coordinator connecting backend to UI
        ├── FileViewMode.java                # View mode enumeration (TABLE, TILES, LIST, GRAPH)
        ├── NavigationHistory.java           # Back & Forward folder history stack
        ├── IconFactory.java                 # Vector SVG & badge icon generator for all file types
        ├── BreadcrumbBar.java               # Interactive path breadcrumbs, direct path editor, & search
        ├── ActionToolbar.java               # Quick actions (+Folder, +File, Cut, Copy, Paste, etc.)
        ├── SidebarView.java                 # Quick Access shortcuts & Workspace Directory Tree
        ├── FileViewContainer.java           # Multi-view viewport host
        ├── DetailedTableView.java           # Sortable table view with columns for name, type, size, modified, perm
        ├── TileGridView.java                # Visual card grid view with 48px icons & badges
        ├── FileListView.java                # Compact row-based list view
        ├── FolderGraphView.java             # Interactive radial Obsidian-style folder graph visualizer
        ├── GraphZoomController.java         # Pan & zoom controller for the radial graph
        ├── FileContextInspector.java        # Right-hand live File Context & Metadata inspector panel
        ├── StatusBar.java                   # Bottom metrics (item counts, selection size, status)
        ├── ItemContextMenu.java             # Right-click context menus for items and background
        └── styles.css                       # Modern dark & light themes styling
```

---

## 2. Backend Integration (`FileSystem.java`)

All file operations in the GUI are executed directly through the `filesystem.FileSystem` backend:

- **Workspace Management**: `fileSystem.openWorkspace(Path)`
- **Folder Navigation & Listing**: `fileSystem.listContents(Path)`
- **Creation**: `fileSystem.createFile(Path, String)`, `fileSystem.createFolder(Path, String)`
- **Renaming**: `fileSystem.rename(FileSystemItem, String)`
- **Moving & Copying**: `fileSystem.move(FileSystemItem, Path)`, `fileSystem.duplicate(FileSystemItem)`
- **Clipboard Management**:
  - `fileSystem.copy(ArrayList<FileSystemItem>)`
  - `fileSystem.cut(ArrayList<FileSystemItem>)`
  - `fileSystem.canPaste()`
  - `fileSystem.paste(Path)`
- **Deletion**: Recursive directory deletion via `fileSystem.delete(FileSystemItem)`
- **System Launch**: `fileSystem.open(FileItem)` to open files in default OS apps
- **Full-Text & Wildcard Search**: `fileSystem.search(String query)` traversing workspace paths

---

## 3. File Context Window & Metadata System

Every file and directory in the GUI displays its context and metadata matching the structure built in `JavaProjectContextWindow`:

### Live File Context Inspector (Right Sidebar)
Whenever any item is clicked or selected, the right panel dynamically renders:
1. **Large Type Icon**: SVG vector representation tailored to file extension.
2. **File Name & Classification**: Descriptive type (e.g., *Java Source*, *PNG Image*, *Directory*).
3. **Formatted Size**: Human-readable bytes, KB, MB, GB, or TB.
4. **Path & Hierarchy**: Current parent location and full absolute path with a 1-click **"Copy Path"** button.
5. **Timestamps**:
   - Date Created
   - Date Last Modified
   - Date Last Accessed
6. **Permissions & Flags**:
   - `Readable` (Active/Inactive pill badge)
   - `Writable` (Active/Inactive pill badge)
   - `Executable` (Active/Inactive pill badge)
   - `Hidden` (Flag pill badge)
   - `Directory` (Flag pill badge)
7. **Action Triggers**: Quick buttons to open the file or open the detailed modal Properties Dialog.

### Context / Properties Dialog (`PropertiesDialog.java`)
Accessible via `Cmd+I`, Right-Click -> `Properties`, or the Action Toolbar:
- Displays a dedicated modal window with all filesystem attributes, zone-formatted dates, and full paths.
- Matches the active theme (Dark / Light).

---

## 4. Multi-View Architecture

The user can toggle between 4 distinct views at any moment using `Cmd+1` through `Cmd+4` or toolbar buttons:

1. **Detailed Table View (`DetailedTableView`)**:
   - Columns: **Name** (with file icon), **Type**, **Size**, **Date Modified**, **Permissions**.
   - Fully sortable columns with intelligent folder-first ordering.
2. **Tile / Grid View (`TileGridView`)**:
   - Visual cards showing 48px icons, filenames, and item sizes.
   - Smooth hover elevation and border glow.
3. **Compact List View (`FileListView`)**:
   - Streamlined single-row view designed for high-density directory browsing.
4. **Obsidian-Style Radial Graph View (`FolderGraphView` & `GraphZoomController`)**:
   - Visualizes directories and files as an interactive node-link radial diagram.
   - Click folder nodes to expand/collapse child trees.
   - Zoom in/out, pan, and reset zoom via toolbar or mouse gestures (`Ctrl + Wheel` / pinch).
   - Right-click any node to access the context menu or properties.

---

## 5. Navigation & Exploration Features

- **Breadcrumbs Bar**:
  - Click any segment in the breadcrumb path to jump directly to that ancestor folder.
  - Double-click or click edit to enter manual path typing mode with auto-focus.
- **Search System**:
  - Live query field with clear (`✕`) button.
  - Recursively searches within the workspace and returns matching files and folders.
- **Sidebar Quick Access**:
  - One-click shortcuts to **Workspace**, **Home**, **Desktop**, **Documents**, and **Downloads**.
  - Interactive **Directory Tree** with lazy directory expansion.
- **Multi-Selection**:
  - Supports `Cmd/Ctrl + Click` and `Shift + Click` for multi-item selection.
  - Status bar updates in real-time with total selected items and cumulative byte size.

---

## 6. Keyboard Shortcuts

| Shortcut | Description |
|---|---|
| `Cmd + N` / `Ctrl + N` | Create New File |
| `Cmd + Shift + N` / `Ctrl + Shift + N` | Create New Folder |
| `Cmd + C` / `Ctrl + C` | Copy Selected Item(s) |
| `Cmd + X` / `Ctrl + X` | Cut Selected Item(s) |
| `Cmd + V` / `Ctrl + V` | Paste Item(s) from Clipboard |
| `Cmd + D` / `Ctrl + D` | Duplicate Selected Item(s) |
| `F2` | Rename Selected Item |
| `Delete` / `Cmd + Backspace` | Delete Selected Item(s) |
| `Cmd + R` / `F5` | Refresh Current Directory |
| `Cmd + F` | Focus Search Bar |
| `Cmd + I` / `Alt + Enter` | Show File Context / Properties Dialog |
| `Cmd + 1` | Switch to Detailed Table View |
| `Cmd + 2` | Switch to Grid / Tile View |
| `Cmd + 3` | Switch to Compact List View |
| `Cmd + 4` | Switch to Radial Folder Graph View |
| `Cmd + B` | Toggle File Context Inspector Panel |
| `Alt + Left` | Navigate Back in History |
| `Alt + Right` | Navigate Forward in History |
| `Alt + Up` | Navigate Up to Parent Directory |

---

## 7. How to Run

### Via Maven
```bash
export PATH="/opt/homebrew/opt/openjdk/bin:$PATH"
export JAVA_HOME="/opt/homebrew/opt/openjdk"

mvn clean compile javafx:run
```

### Via Gradle
```bash
gradle run
```
