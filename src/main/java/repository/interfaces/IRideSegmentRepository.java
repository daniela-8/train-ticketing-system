package repository.interfaces;

import domain.RideSegment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface IRideSegmentRepository extends JpaRepository<RideSegment, Long> {
    List<RideSegment> findByRideId(Long rideId);
}