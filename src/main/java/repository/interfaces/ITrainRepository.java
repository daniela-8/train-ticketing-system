package repository.interfaces;

import domain.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ITrainRepository extends JpaRepository<Train, Long> {
}