package com.imjustdoom.justpermissions.util;

import com.imjustdoom.justpermissions.JustPermissions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

    /**
     * Checks if a file exists
     * @param filename - name of the file
     * @return - does it exist?
     */
    public static boolean doesFileExist(String filename) {
        Path path = Path.of(filename);
        return Files.exists(path);
    }

    /**
     * Creates a file
     * @param filename - name of the file
     */
    public static void createFile(String filename) throws IOException {
        Path path = Path.of(filename);
        File file = new File(String.valueOf(path));
        file.createNewFile();
    }

    /**
     * Creates a directory
     * @param path - path of the directory
     */
    public static void createDirectory(String path) throws IOException {
        Files.createDirectory(Path.of(path));
    }

    /**
     * Copies and pastes the default config
     * @param filename - new files name
     */
    public static void addConfig(String filename) throws IOException {
        Path path = Path.of(filename);
        InputStream stream = JustPermissions.class.getResourceAsStream("/config.yml");
        Files.copy(stream, path);
    }
}