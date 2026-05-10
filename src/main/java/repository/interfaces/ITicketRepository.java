package repository.interfaces;

import domain.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ITicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByRideId(Long rideId);
}