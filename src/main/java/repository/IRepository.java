package repository;

import domain.Entity;

import java.util.Optional;

public interface IRepository<ID, E extends Entity<ID>> {
    Optional<E> findOne(ID id);
    Iterable<E> findAll();
    E save(E entity);
    void delete(ID id);
    E update(E entity);
}
