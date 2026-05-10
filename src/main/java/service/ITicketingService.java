package service;

import domain.Ride;
import domain.Route;
import domain.Station;
import domain.Ticket;
import domain.Train;
import domain.User;

import java.util.List;

public interface ITicketingService {
    User authenticate(String email) throws TicketingException;
    List<Station> getAllStations();
    List<Train> getAllTrains();
    Train addTrain(String name, int totalCapacity);

    void deleteTrain(Long id);
    Route addRoute(String name);
    List<Ticket> getAllBookings();

    List<Ticket> getBookingsForRide(Long rideId);
    Ticket bookTicket(String userEmail, Long rideId, Long departureStationId, Long arrivalStationId, int numberOfSeats) throws TicketingException;
    List<Ride> findRoutes(Long departureStationId, Long arrivalStationId) throws TicketingException;
    void delayRide(Long rideId, int delayMinutes) throws TicketingException;
}