package repository.interfaces;

import domain.Station;

import java.util.Optional;

public interface IStationRepository extends IRepository<Long, Station> {
    Optional<Station> findByName(String name);
}