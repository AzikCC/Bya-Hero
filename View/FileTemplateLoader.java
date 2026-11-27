package com.byahero.view;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Reads template .html files from a folder on disk. */
public class FileTemplateLoader implements TemplateLoader {

    private final String folder;

    public FileTemplateLoader(String folder) {
        this.folder = folder;
    }

    @Override
    public String load(String fileName) {
        Path path = Paths.get(folder, fileName);
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Template not found: " + path);
        }
    }
}
