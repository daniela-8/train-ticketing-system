package repository.interfaces;

import domain.RideSegment;
import java.util.List;

public interface IRideSegmentRepository extends IRepository<Long, RideSegment> {
    List<RideSegment> findByRideId(Long rideId);
}