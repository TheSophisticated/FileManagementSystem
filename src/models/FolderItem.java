package models;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

public class FolderItem extends FileSystemItem{
    public FolderItem(Path path){
        super(path);
    }

    public ArrayList<FileSystemItem> getChildren() throws IOException {
        return getChildren(false);
    }

    public ArrayList<FileSystemItem> getChildren(boolean includeHidden) throws IOException {
        ArrayList<FileSystemItem> children = new ArrayList<>();
    
        try (Stream<Path> stream = Files.list(path)) {
            stream.forEach(p -> {
                String name = p.getFileName() != null ? p.getFileName().toString() : "";
                if (!includeHidden) {
                    if (name.startsWith(".") || name.equals("target") || name.equals("build") || name.equals("bin")) {
                        return;
                    }
                    try {
                        if (Files.isHidden(p)) return;
                    } catch (Exception ignored) {}
                }
                if (Files.isDirectory(p)) {
                    children.add(new FolderItem(p));
                } else {
                    children.add(new FileItem(p));
                }
            });
        }

        children.sort(
            Comparator.comparing((FileSystemItem item) -> !(item instanceof FolderItem))
            .thenComparing(item -> item.getName().toLowerCase())
        );

        return children;
    }

    public FileItem createFile(String fileName) throws IOException{
        Path newFile = Files.createFile(path.resolve(fileName));
        return new FileItem(newFile);
    }

    public FolderItem createFolder(String folderName) throws IOException{
        Path newFolder = Files.createDirectory(path.resolve(folderName));
        return new FolderItem(newFolder);
    }

    @Override
    public void delete() throws IOException{
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            @Override 
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException{
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override 
            public FileVisitResult postVisitDirectory(Path dir, IOException e) throws IOException{
                if(e == null){
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                } else{
                    throw e;
                }
            }
        });
    }

    @Override 
    public FolderItem copyTo(Path targetFolder) throws IOException{
        Path target = resolveUniqueTarget(targetFolder.toAbsolutePath());
        if(target.startsWith(path)){
            throw new IOException("Cannot copy a folder into itself!");
        }

        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            @Override 
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attr) throws IOException{
                Files.createDirectories(target.resolve(path.relativize(dir)));
                return FileVisitResult.CONTINUE;
            }
            
            @Override 
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException{
                Files.copy(file, target.resolve(path.relativize(file)));
                return FileVisitResult.CONTINUE;
            }
        });

        return new FolderItem(target);
    }

}