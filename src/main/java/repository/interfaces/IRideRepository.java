package repository.interfaces;

import domain.Ride;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IRideRepository extends JpaRepository<Ride, Long> {
}