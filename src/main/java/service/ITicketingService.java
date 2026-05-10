package service;

import domain.*;

import java.util.List;

public interface ITicketingService {
    Ticket bookTicket(String userEmail, Long rideId, Long departureStationId, Long arrivalStationId, int numberOfSeats) throws TicketingException;

    List<Ride> findRoutes(Long departureStationId, Long arrivalStationId) throws TicketingException;

    Train addTrain(String name, int totalCapacity);
    void delayRide(Long rideId, int delayMinutes) throws TicketingException;
    List<Ticket> getBookingsForRide(Long rideId);

    User authenticate(String email) throws TicketingException;
    List<Station> getAllStations();
    List<Train> getAllTrains();
}
