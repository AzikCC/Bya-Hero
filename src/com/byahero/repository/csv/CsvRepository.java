package com.byahero.repository.csv;

import com.byahero.repository.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * The shared "read the whole file -> change the list -> write the whole file back"
 * logic for every CSV repository.
 *
 * A child class only says three things about its own data:
 *   fromRow(...) how one CSV row becomes an object
 *   toLine(...)  how one object becomes a CSV line
 *   idOf(...)    which text identifies the object
 *
 * SINGLE RESPONSIBILITY: this class knows about CSV storage and nothing else.
 * "synchronized" makes sure only one request uses the file at a time.
 */
public abstract class CsvRepository<T> implements Repository<T> {

    private final String filePath;
    private final String header;
    private final int columnCount;
    private final String entityName; // used in warning messages, like "fare"

    protected CsvRepository(String filePath, String header, int columnCount, String entityName) {
        this.filePath = filePath;
        this.header = header;
        this.columnCount = columnCount;
        this.entityName = entityName;
        CsvUtil.ensureFile(filePath, header); // create the file if it is missing
    }

    // ---------- what each child must provide ----------

    protected abstract T fromRow(String[] row);

    protected abstract String toLine(T item);

    protected abstract String idOf(T item);

    // ---------- messages a child can change ----------

    protected String alreadyExistsMessage(T item) {
        return "Item " + idOf(item) + " already exists.";
    }

    protected String notFoundMessage(T item) {
        return "No item found with id " + idOf(item) + ".";
    }

    // ---------- helpers the children can use ----------

    /** Reads the file and returns a list of objects. Broken rows are skipped with a warning. */
    protected List<T> readFile() {
        List<T> items = new ArrayList<T>();
        List<String[]> rows = CsvUtil.readRows(filePath, header, columnCount);

        for (int i = 0; i < rows.size(); i++) {
            try {
                items.add(fromRow(rows.get(i)));
            } catch (RuntimeException e) {
                System.err.println("WARNING: skipping bad " + entityName + " row (" + e.getMessage() + ")");
            }
        }
        return items;
    }

    /** Writes the list of objects back into the file. */
    protected void writeFile(List<T> items) {
        List<String> lines = new ArrayList<String>();
        for (int i = 0; i < items.size(); i++) {
            lines.add(toLine(items.get(i)));
        }
        CsvUtil.writeAll(filePath, header, lines);
    }

    /** Finds the position of the FIRST item with this id. Returns -1 if not found. */
    protected int indexOf(List<T> items, String id) {
        if (id == null) {
            return -1;
        }
        for (int i = 0; i < items.size(); i++) {
            if (idOf(items.get(i)).equalsIgnoreCase(id.trim())) {
                return i;
            }
        }
        return -1;
    }

    // ---------- methods from the Repository interface ----------

    @Override
    public synchronized List<T> findAll() {
        return readFile();
    }

    @Override
    public synchronized T findById(String id) {
        List<T> items = readFile();
        int index = indexOf(items, id);
        if (index == -1) {
            return null;
        }
        return items.get(index);
    }

    @Override
    public synchronized void save(T item) {
        List<T> items = readFile();
        if (indexOf(items, idOf(item)) != -1) {
            throw new IllegalArgumentException(alreadyExistsMessage(item));
        }
        items.add(item);
        writeFile(items);
    }

    @Override
    public synchronized void update(T item) {
        List<T> items = readFile();
        int index = indexOf(items, idOf(item));
        if (index == -1) {
            throw new IllegalArgumentException(notFoundMessage(item));
        }
        items.set(index, item);
        writeFile(items);
    }

    @Override
    public synchronized boolean delete(String id) {
        List<T> items = readFile();
        int index = indexOf(items, id);
        if (index == -1) {
            return false;
        }
        items.remove(index);
        writeFile(items);
        return true;
    }
}
