package com.byahero.repository.csv;

import com.byahero.exception.CsvStorageException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Basic CSV file helpers that all three repositories use. */
public class CsvUtil {

    /** If the file (or its folder) does not exist, create it with just the header row. */
    public static void ensureFile(String path, String header) {
        File file = new File(path);

        File folder = file.getParentFile();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }

        if (!file.exists() || file.length() == 0) {
            writeAll(path, header, new ArrayList<String>());
        }
    }

    /**
     * Reads the file and returns each good row as an array of columns.
     * Blank lines, the header line, and rows with the wrong number of columns
     * are skipped (a warning is printed instead of crashing).
     */
    public static List<String[]> readRows(String path, String header, int expectedColumns) {
        ensureFile(path, header);
        List<String[]> rows = new ArrayList<String[]>();

        try (BufferedReader reader = new BufferedReader(new FileReader(path, StandardCharsets.UTF_8))) {
            int lineNumber = 0;
            String line = reader.readLine();
            while (line != null) {
                lineNumber++;

                boolean isBlank = line.trim().isEmpty();
                boolean isHeader = line.trim().equalsIgnoreCase(header);

                if (!isBlank && !isHeader) {
                    String[] columns = line.split(",", -1);
                    if (columns.length == expectedColumns) {
                        for (int i = 0; i < columns.length; i++) {
                            columns[i] = columns[i].trim();
                        }
                        rows.add(columns);
                    } else {
                        System.err.println("WARNING: skipping bad row " + lineNumber + " in " + path + ": " + line);
                    }
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            throw new CsvStorageException("Could not read " + path, e);
        }
        return rows;
    }

    /** Replaces the whole file with the header row followed by the given lines. */
    public static void writeAll(String path, String header, List<String> lines) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, StandardCharsets.UTF_8)))) {
            out.println(header);
            for (int i = 0; i < lines.size(); i++) {
                out.println(lines.get(i));
            }
            // PrintWriter hides write errors, so we ask it if something went wrong
            if (out.checkError()) {
                throw new CsvStorageException("Could not write " + path);
            }
        } catch (IOException e) {
            throw new CsvStorageException("Could not write " + path, e);
        }
    }

    /** Removes commas and line breaks from a value so it cannot break the CSV columns. */
    public static String clean(String value) {
        if (value == null) {
            return "";
        }
        value = value.replace(",", " ");
        value = value.replace("\r", " ");
        value = value.replace("\n", " ");
        return value.trim();
    }
}
