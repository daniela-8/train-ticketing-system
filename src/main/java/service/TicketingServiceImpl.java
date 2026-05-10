package service;

import domain.*;
import domain.enums.TicketStatus;
import repository.*;
import repository.interfaces.*;
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
    private final RideSegmentRepository segmentRepository;

    public TicketingServiceImpl(UserRepository userRepository, StationRepository stationRepository,
                                TrainRepository trainRepository, RouteRepository routeRepository,
                                RideRepository rideRepository, TicketRepository ticketRepository, RideSegmentRepository segmentRepository) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.trainRepository = trainRepository;
        this.routeRepository = routeRepository;
        this.rideRepository = rideRepository;
        this.ticketRepository = ticketRepository;
        this.segmentRepository = segmentRepository;
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
        User customer = authenticate(userEmail);

        Ride ride = rideRepository.findOne(rideId)
                .orElseThrow(() -> new TicketingException("Ride not found."));

        List<RideSegment> allSegments = segmentRepository.findByRideId(rideId);
        if (allSegments.isEmpty()) {
            throw new TicketingException("This ride has no scheduled segments.");
        }

        int startIndex = -1;
        int endIndex = -1;

        for (int i = 0; i < allSegments.size(); i++) {
            RideSegment segment = allSegments.get(i);
            if (segment.getFromStation().getId().equals(departureStationId)) {
                startIndex = i;
            }
            if (segment.getToStation().getId().equals(arrivalStationId)) {
                endIndex = i;
            }
        }

        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            throw new TicketingException("Invalid route selected. Please check your departure and arrival stations.");
        }

        for (int i = startIndex; i <= endIndex; i++) {
            if (allSegments.get(i).getAvailableSeats() < numberOfSeats) {
                throw new TicketingException("Booking failed: Not enough seats available between " +
                        allSegments.get(i).getFromStation().getId() + " and " +
                        allSegments.get(i).getToStation().getId());
            }
        }

        for (int i = startIndex; i <= endIndex; i++) {
            RideSegment segment = allSegments.get(i);
            segment.setAvailableSeats(segment.getAvailableSeats() - numberOfSeats);
            segmentRepository.update(segment);
        }

        Station depStation = new Station(departureStationId, null);
        Station arrStation = new Station(arrivalStationId, null);

        Ticket newTicket = new Ticket(null, customer, ride, depStation, arrStation, numberOfSeats, TicketStatus.CONFIRMED);

        return ticketRepository.save(newTicket);
    }

    @Override
    public List<Ride> findRoutes(Long departureStationId, Long arrivalStationId) throws TicketingException {
        return null;
    }

    @Override
    public void delayRide(Long rideId, int delayMinutes) throws TicketingException {
    }
}