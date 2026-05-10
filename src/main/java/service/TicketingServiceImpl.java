package service;

import domain.*;
import domain.enums.TicketStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.interfaces.*;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketingServiceImpl implements ITicketingService {

    private static final Logger logger = LogManager.getLogger(TicketingServiceImpl.class);

    private final IUserRepository userRepository;
    private final IStationRepository stationRepository;
    private final ITrainRepository trainRepository;
    private final IRouteRepository routeRepository;
    private final IRideRepository rideRepository;
    private final ITicketRepository ticketRepository;
    private final NotificationService notificationService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TicketingServiceImpl(IUserRepository userRepository, IStationRepository stationRepository,
                                ITrainRepository trainRepository, IRouteRepository routeRepository,
                                IRideRepository rideRepository, ITicketRepository ticketRepository,
                                NotificationService notificationService) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.trainRepository = trainRepository;
        this.routeRepository = routeRepository;
        this.rideRepository = rideRepository;
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
        logger.info("TicketingServiceImpl initialized with Spring Data JPA.");
    }

    @Override
    public User authenticate(String email) throws TicketingException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new TicketingException("User with email " + email + " not found."));
    }

    @Override
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    @Override
    public Train addTrain(String name, int totalCapacity) {
        return trainRepository.save(new Train(null, name, totalCapacity));
    }

    @Override
    public List<Ticket> getBookingsForRide(Long rideId) {
        return ticketRepository.findByRideId(rideId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Ticket bookTicket(String userEmail, Long rideId, Long departureStationId, Long arrivalStationId, int numberOfSeats) throws TicketingException {
        User customer = authenticate(userEmail);

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new TicketingException("Ride not found."));

        List<RideSegment> allSegments = ride.getSegments();

        int startIndex = -1;
        int endIndex = -1;

        for (int i = 0; i < allSegments.size(); i++) {
            RideSegment segment = allSegments.get(i);
            if (segment.getFromStation().getId().equals(departureStationId)) startIndex = i;
            if (segment.getToStation().getId().equals(arrivalStationId)) endIndex = i;
        }

        if (startIndex == -1 || endIndex == -1 || startIndex > endIndex) {
            throw new TicketingException("Invalid route selected.");
        }

        for (int i = startIndex; i <= endIndex; i++) {
            RideSegment segment = allSegments.get(i);
            if (segment.getAvailableSeats() < numberOfSeats) {
                throw new TicketingException("Booking failed: Not enough seats available.");
            }
            segment.setAvailableSeats(segment.getAvailableSeats() - numberOfSeats);
        }

        Station depStation = stationRepository.findById(departureStationId).orElseThrow();
        Station arrStation = stationRepository.findById(arrivalStationId).orElseThrow();

        Ticket newTicket = new Ticket(null, customer, ride, depStation, arrStation, numberOfSeats, TicketStatus.CONFIRMED);
        Ticket savedTicket = ticketRepository.save(newTicket);

        notificationService.sendBookingEmail(userEmail, savedTicket.getId());
        return savedTicket;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ride> findRoutes(Long departureStationId, Long arrivalStationId) throws TicketingException {
        logger.debug("Searching for routes between station ID {} and {}", departureStationId, arrivalStationId);

        List<Ride> allRides = rideRepository.findAll();

        return allRides.stream().filter(ride -> {
            List<RideSegment> segments = ride.getSegments();
            int startIndex = -1;
            int endIndex = -1;

            for (int i = 0; i < segments.size(); i++) {
                if (segments.get(i).getFromStation().getId().equals(departureStationId)) startIndex = i;
                if (segments.get(i).getToStation().getId().equals(arrivalStationId)) endIndex = i;
            }

            if (startIndex != -1 && endIndex != -1 && startIndex <= endIndex) {
                for (int i = startIndex; i <= endIndex; i++) {
                    if (segments.get(i).getAvailableSeats() <= 0) return false;
                }
                return true;
            }
            return false;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delayRide(Long rideId, int delayMinutes) throws TicketingException {
        logger.info("Admin Action: Applying {} min delay to Ride {}", delayMinutes, rideId);

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new TicketingException("Ride not found."));

        ride.setDelayMinutes(ride.getDelayMinutes() + delayMinutes);

        for (RideSegment segment : ride.getSegments()) {
            segment.setDepartureTime(segment.getDepartureTime().plusMinutes(delayMinutes));
            segment.setArrivalTime(segment.getArrivalTime().plusMinutes(delayMinutes));
        }

        List<Ticket> bookings = ticketRepository.findByRideId(rideId);
        for (Ticket t : bookings) {
            notificationService.sendDelayNotification(t.getCustomer().getEmail(), rideId, delayMinutes);
        }
        String delayPayload = "{\"rideId\": " + rideId + ", \"delayMinutes\": " + delayMinutes + "}";
        messagingTemplate.convertAndSend("/topic/delays", delayPayload);

        logger.info("Real-time WebSocket broadcast sent for Ride ID {}", rideId);
    }

    @Override
    public void deleteTrain(Long id) {
        logger.info("Admin Action: Deleting train ID {}", id);
        trainRepository.deleteById(id);
    }

    @Override
    public Route addRoute(String name) {
        logger.info("Admin Action: Adding new route: {}", name);
        return routeRepository.save(new Route(null, name));
    }

    @Override
    public List<Ticket> getAllBookings() {
        logger.info("Admin Action: Fetching all system bookings.");
        return ticketRepository.findAll();
    }
}