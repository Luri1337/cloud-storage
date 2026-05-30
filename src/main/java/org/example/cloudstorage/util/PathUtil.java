package org.example.cloudstorage.util;

public class PathUtil {
    public static String buildFullPath(Long userId, String path) {
        String root = "user-" + userId;
        if (path == null || path.isEmpty()) {
            return root;
        }
        String cleanPath = path.startsWith("/") ? path.substring(1) : path;
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
        }
        return root + "/" + cleanPath;
    }
}
