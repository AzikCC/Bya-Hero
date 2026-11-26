package com.byahero.repository;

import java.util.List;

/**
 * INTERFACE SEGREGATION: the "read" half of a repository.
 * A class that only looks things up (the board, the fare calculator...)
 * depends on THIS interface, so it cannot accidentally save or delete anything.
 */
public interface ReadRepository<T> {

    List<T> findAll();

    /** Returns the item with this id, or null if there is none. */
    T findById(String id);
}
