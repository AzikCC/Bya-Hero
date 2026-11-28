package com.byahero.repository;

/**
 * ABSTRACTION: the full repository = read + write.
 * Only classes that really need both (check-in, admin) use this one.
 * Everyone else asks for the smaller ReadRepository or WriteRepository.
 */
public interface Repository<T> extends ReadRepository<T>, WriteRepository<T> {
}
