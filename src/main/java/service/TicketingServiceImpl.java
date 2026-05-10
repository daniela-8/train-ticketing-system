package service;

import domain.*;
import repository.*;
import repository.jdbc.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class TicketingServiceImpl implements ITicketingService {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final RouteRepository routeRepository;
    private final RideRepository rideRepository;
    private final TicketRepository ticketRepository;

    public TicketingServiceImpl(UserRepository userRepository, StationRepository stationRepository,
                                TrainRepository trainRepository, RouteRepository routeRepository,
                                RideRepository rideRepository, TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.trainRepository = trainRepository;
        this.routeRepository = routeRepository;
        this.rideRepository = rideRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public User authenticate(String email) throws TicketingException {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new TicketingException("User with email " + email + " not found."));
    }

    @Override
    public List<Station> getAllStations() {
        return StreamSupport.stream(stationRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public List<Train> getAllTrains() {
        return StreamSupport.stream(trainRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @Override
    public Train addTrain(String name, int totalCapacity) {
        Train train = new Train(null, name, totalCapacity);
        return trainRepository.save(train);
    }

    @Override
    public List<Ticket> getBookingsForRide(Long rideId) {
        return StreamSupport.stream(ticketRepository.findAll().spliterator(), false)
                .filter(t -> t.getRide().getId().equals(rideId))
                .collect(Collectors.toList());
    }


    @Override
    public Ticket bookTicket(String userEmail, Long rideId, Long departureStationId, Long arrivalStationId, int numberOfSeats) throws TicketingException {
        return null;
    }

    @Override
    public List<Ride> findRoutes(Long departureStationId, Long arrivalStationId) throws TicketingException {
        return null;
    }

    @Override
    public void delayRide(Long rideId, int delayMinutes) throws TicketingException {
    }
}