package com.byahero.repository;

/**
 * INTERFACE SEGREGATION: the "write" half of a repository.
 * T is a placeholder for the kind of object stored (Bus, Fare, RouteStop...).
 */
public interface WriteRepository<T> {

    void save(T item);

    void update(T item);

    /** Returns true if something was removed. */
    boolean delete(String id);
}
