package service;

import domain.*;
import domain.enums.TicketStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.jdbc.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class TicketingServiceImpl implements ITicketingService {

    private static final Logger logger = LogManager.getLogger(TicketingServiceImpl.class);

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final TrainRepository trainRepository;
    private final RouteRepository routeRepository;
    private final RideRepository rideRepository;
    private final TicketRepository ticketRepository;
    private final RideSegmentRepository segmentRepository;
    private final NotificationService notificationService;

    @Autowired
    public TicketingServiceImpl(UserRepository userRepository,
                                StationRepository stationRepository,
                                TrainRepository trainRepository,
                                RouteRepository routeRepository,
                                RideRepository rideRepository,
                                TicketRepository ticketRepository,
                                RideSegmentRepository segmentRepository,
                                NotificationService notificationService) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.trainRepository = trainRepository;
        this.routeRepository = routeRepository;
        this.rideRepository = rideRepository;
        this.ticketRepository = ticketRepository;
        this.segmentRepository = segmentRepository;
        this.notificationService = notificationService;
        logger.info("TicketingServiceImpl initialized with Advanced Features (Async & Transactions).");
    }

    @Override
    public User authenticate(String email) throws TicketingException {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .filter(u -> u.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new TicketingException("User not found: " + email));
    }

    @Override
    public List<Station> getAllStations() {
        return StreamSupport.stream(stationRepository.findAll().spliterator(), false).toList();
    }

    @Override
    public List<Train> getAllTrains() {
        return StreamSupport.stream(trainRepository.findAll().spliterator(), false).toList();
    }

    @Override
    public Train addTrain(String name, int totalCapacity) {
        return trainRepository.save(new Train(null, name, totalCapacity));
    }

    @Override
    public List<Ticket> getBookingsForRide(Long rideId) {
        return StreamSupport.stream(ticketRepository.findAll().spliterator(), false)
                .filter(t -> t.getRide().getId().equals(rideId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Ticket bookTicket(String userEmail, Long rideId, Long departureStationId, Long arrivalStationId, int numberOfSeats) throws TicketingException {
        logger.info("Processing ACID Booking: {} seats for Ride ID {}", numberOfSeats, rideId);

        User customer = authenticate(userEmail);

        Ride ride = rideRepository.findOne(rideId)
                .orElseThrow(() -> new TicketingException("Ride not found."));

        List<RideSegment> allSegments = segmentRepository.findByRideId(rideId);

        int startIndex = -1;
        int endIndex = -1;

        for (int i = 0; i < allSegments.size(); i++) {
            RideSegment segment = allSegments.get(i);
            if (segment.getFromStation().getId().equals(departureStationId)) startIndex = i;
            if (segment.getToStation().getId().equals(arrivalStationId)) endIndex = i;
        }

        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            throw new TicketingException("Invalid route selection.");
        }

        for (int i = startIndex; i <= endIndex; i++) {
            if (allSegments.get(i).getAvailableSeats() < numberOfSeats) {
                logger.warn("Concurrency Block: Not enough seats on segment index {}", i);
                throw new TicketingException("Booking failed: Seats no longer available.");
            }
        }

        for (int i = startIndex; i <= endIndex; i++) {
            RideSegment segment = allSegments.get(i);
            segment.setAvailableSeats(segment.getAvailableSeats() - numberOfSeats);
            segmentRepository.update(segment);
        }

        Station depStation = stationRepository.findOne(departureStationId).orElseThrow();
        Station arrStation = stationRepository.findOne(arrivalStationId).orElseThrow();

        Ticket newTicket = new Ticket(null, customer, ride, depStation, arrStation, numberOfSeats, TicketStatus.CONFIRMED);
        Ticket savedTicket = ticketRepository.save(newTicket);

        notificationService.sendBookingEmail(userEmail, savedTicket.getId());

        logger.info("Booking transaction committed successfully for Ticket #{}", savedTicket.getId());
        return savedTicket;
    }

    @Override
    public List<Ride> findRoutes(Long departureStationId, Long arrivalStationId) throws TicketingException {
        List<Ride> validRides = new ArrayList<>();
        Iterable<Ride> allRides = rideRepository.findAll();

        for (Ride ride : allRides) {
            List<RideSegment> segments = segmentRepository.findByRideId(ride.getId());
            int start = -1, end = -1;

            for (int i = 0; i < segments.size(); i++) {
                if (segments.get(i).getFromStation().getId().equals(departureStationId)) start = i;
                if (segments.get(i).getToStation().getId().equals(arrivalStationId)) end = i;
            }

            if (start != -1 && end != -1 && start <= end) {
                boolean available = true;
                for (int i = start; i <= end; i++) {
                    if (segments.get(i).getAvailableSeats() <= 0) {
                        available = false;
                        break;
                    }
                }
                if (available) {
                    validRides.add(new Ride(ride.getId(), ride.getTrain(), ride.getRoute(), segments, ride.getDelayMinutes()));
                }
            }
        }
        if (validRides.isEmpty()) throw new TicketingException("No routes found.");
        return validRides;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delayRide(Long rideId, int delayMinutes) throws TicketingException {
        logger.info("Applying {} min delay to Ride {}", delayMinutes, rideId);

        Ride ride = rideRepository.findOne(rideId)
                .orElseThrow(() -> new TicketingException("Ride not found."));

        ride.setDelayMinutes(ride.getDelayMinutes() + delayMinutes);
        rideRepository.update(ride);

        List<RideSegment> segments = segmentRepository.findByRideId(rideId);
        for (RideSegment segment : segments) {
            segment.setDepartureTime(segment.getDepartureTime().plusMinutes(delayMinutes));
            segment.setArrivalTime(segment.getArrivalTime().plusMinutes(delayMinutes));
            segmentRepository.update(segment);
        }

        List<Ticket> bookings = getBookingsForRide(rideId);
        for (Ticket t : bookings) {
            notificationService.sendDelayNotification(t.getCustomer().getEmail(), rideId, delayMinutes);
        }
    }
}