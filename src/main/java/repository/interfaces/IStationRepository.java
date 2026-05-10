package repository.interfaces;

import domain.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IStationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByName(String name);
}