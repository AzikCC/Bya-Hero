package com.byahero.fakes;

import com.byahero.repository.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * A fake repository that keeps everything in a list (no files).
 * It is only possible because the services depend on the Repository INTERFACE.
 * (idOf says how to get the id of an item, like Bus::getPlate)
 */
public class InMemoryRepository<T> implements Repository<T> {

    public interface IdReader<T> {
        String idOf(T item);
    }

    private final List<T> items = new ArrayList<T>();
    private final IdReader<T> idReader;

    public InMemoryRepository(IdReader<T> idReader) {
        this.idReader = idReader;
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<T>(items);
    }

    @Override
    public T findById(String id) {
        T found = null;
        for (int i = 0; i < items.size(); i++) {
            if (idReader.idOf(items.get(i)).equalsIgnoreCase(id)) {
                found = items.get(i); // last one wins (newest)
            }
        }
        return found;
    }

    @Override
    public void save(T item) {
        items.add(item);
    }

    @Override
    public void update(T item) {
        for (int i = 0; i < items.size(); i++) {
            if (idReader.idOf(items.get(i)).equalsIgnoreCase(idReader.idOf(item))) {
                items.set(i, item);
            }
        }
    }

    @Override
    public boolean delete(String id) {
        boolean removed = false;
        for (int i = items.size() - 1; i >= 0; i--) {
            if (idReader.idOf(items.get(i)).equalsIgnoreCase(id)) {
                items.remove(i);
                removed = true;
            }
        }
        return removed;
    }
}
