package app;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Manages back and forward navigation history for folder paths.
 */
public class NavigationHistory {
    private final Deque<Path> backStack = new ArrayDeque<>();
    private final Deque<Path> forwardStack = new ArrayDeque<>();
    private Path currentPath;

    public void navigateTo(Path newPath) {
        if (newPath == null || newPath.equals(currentPath)) {
            return;
        }
        if (currentPath != null) {
            backStack.push(currentPath);
        }
        currentPath = newPath;
        forwardStack.clear();
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public boolean canGoBack() {
        return !backStack.isEmpty();
    }

    public boolean canGoForward() {
        return !forwardStack.isEmpty();
    }

    public Path goBack() {
        if (!canGoBack()) {
            return null;
        }
        forwardStack.push(currentPath);
        currentPath = backStack.pop();
        return currentPath;
    }

    public Path goForward() {
        if (!canGoForward()) {
            return null;
        }
        backStack.push(currentPath);
        currentPath = forwardStack.pop();
        return currentPath;
    }

    public void clear() {
        backStack.clear();
        forwardStack.clear();
        currentPath = null;
    }
}
