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

import java.util.ArrayList;
import java.util.List;

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

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TicketingServiceImpl(IUserRepository userRepository, IStationRepository stationRepository,
                                ITrainRepository trainRepository, IRouteRepository routeRepository,
                                IRideRepository rideRepository, ITicketRepository ticketRepository,
                                NotificationService notificationService,
                                SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.trainRepository = trainRepository;
        this.routeRepository = routeRepository;
        this.rideRepository = rideRepository;
        this.ticketRepository = ticketRepository;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
        logger.info("Enterprise TicketingServiceImpl with Changeover Engine initialized.");
    }

    @Override
    public User authenticate(String email) throws TicketingException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new TicketingException("User with email " + email + " not found."));
    }

    @Override
    public List<Station> getAllStations() { return stationRepository.findAll(); }

    @Override
    public List<Train> getAllTrains() { return trainRepository.findAll(); }

    @Override
    public Train addTrain(String name, int totalCapacity) {
        return trainRepository.save(new Train(null, name, totalCapacity));
    }

    @Override
    public List<Ticket> getBookingsForRide(Long rideId) {
        return ticketRepository.findByRideId(rideId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ride> findRoutes(Long departureStationId, Long arrivalStationId) throws TicketingException {
        logger.debug("Searching routes between {} and {}", departureStationId, arrivalStationId);
        List<Ride> allRides = rideRepository.findAll();
        List<Ride> validJourneys = new ArrayList<>();

        for (Ride ride : allRides) {
            int startIdx = getStationIndex(ride.getSegments(), departureStationId, true);
            int endIdx = getStationIndex(ride.getSegments(), arrivalStationId, false);

            if (startIdx != -1 && endIdx != -1 && startIdx <= endIdx) {
                if (checkSeats(ride.getSegments(), startIdx, endIdx)) {
                    Ride directRide = new Ride();
                    directRide.setId(ride.getId());
                    directRide.setTrain(ride.getTrain());
                    directRide.setRoute(ride.getRoute());
                    directRide.setDelayMinutes(ride.getDelayMinutes());
                    directRide.setSegments(new ArrayList<>(ride.getSegments().subList(startIdx, endIdx + 1)));

                    validJourneys.add(directRide);
                }
            }
        }

        if (validJourneys.isEmpty()) {
            for (Ride r1 : allRides) {
                for (Ride r2 : allRides) {
                    if (r1.getId().equals(r2.getId())) continue;

                    int startR1 = getStationIndex(r1.getSegments(), departureStationId, true);
                    int endR2 = getStationIndex(r2.getSegments(), arrivalStationId, false);

                    if (startR1 != -1 && endR2 != -1) {
                        for (int i = startR1; i < r1.getSegments().size(); i++) {
                            Station transferStation = r1.getSegments().get(i).getToStation();
                            int transferR2Idx = getStationIndex(r2.getSegments(), transferStation.getId(), true);

                            if (transferR2Idx != -1 && transferR2Idx <= endR2) {
                                RideSegment arriveTransfer = r1.getSegments().get(i);
                                RideSegment departTransfer = r2.getSegments().get(transferR2Idx);

                                if (arriveTransfer.getArrivalTime().isBefore(departTransfer.getDepartureTime())) {
                                    if (checkSeats(r1.getSegments(), startR1, i) && checkSeats(r2.getSegments(), transferR2Idx, endR2)) {

                                        Train mixedTrain = new Train(999L, "Transfer required at " + transferStation.getName(), 0);
                                        Route mixedRoute = new Route(999L, "Multi-Line Journey");
                                        Long compositeId = 1000000L + (r1.getId() * 1000) + r2.getId();

                                        Ride mixedRide = new Ride();
                                        mixedRide.setId(compositeId);
                                        mixedRide.setTrain(mixedTrain);
                                        mixedRide.setRoute(mixedRoute);
                                        mixedRide.setDelayMinutes(0);

                                        List<RideSegment> mixedSegments = new ArrayList<>();
                                        mixedSegments.addAll(r1.getSegments().subList(startR1, i + 1));
                                        mixedSegments.addAll(r2.getSegments().subList(transferR2Idx, endR2 + 1));

                                        mixedRide.setSegments(mixedSegments);
                                        validJourneys.add(mixedRide);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return validJourneys;
    }

    private int getStationIndex(List<RideSegment> segments, Long stationId, boolean isDeparture) {
        for (int i = 0; i < segments.size(); i++) {
            if (isDeparture && segments.get(i).getFromStation().getId().equals(stationId)) return i;
            if (!isDeparture && segments.get(i).getToStation().getId().equals(stationId)) return i;
        }
        return -1;
    }

    private boolean checkSeats(List<RideSegment> segments, int startIdx, int endIdx) {
        for (int i = startIdx; i <= endIdx; i++) {
            if (segments.get(i).getAvailableSeats() <= 0) return false;
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Ticket bookTicket(String userEmail, Long rideId, Long departureStationId, Long arrivalStationId, int numberOfSeats) throws TicketingException {
        User customer = authenticate(userEmail);

        if (rideId >= 1000000L) {
            Long r1Id = (rideId - 1000000L) / 1000L;
            Long r2Id = (rideId - 1000000L) % 1000L;

            Ride r1 = rideRepository.findById(r1Id).orElseThrow(() -> new TicketingException("Leg 1 not found"));
            Ride r2 = rideRepository.findById(r2Id).orElseThrow(() -> new TicketingException("Leg 2 not found"));

            Station transferStation = null;
            for (RideSegment s1 : r1.getSegments()) {
                for (RideSegment s2 : r2.getSegments()) {
                    if (s1.getToStation().getId().equals(s2.getFromStation().getId())) {
                        transferStation = s1.getToStation();
                    }
                }
            }

            if (transferStation == null) throw new TicketingException("Transfer invalid.");

            bookInternal(customer, r1, departureStationId, transferStation.getId(), numberOfSeats);
            Ticket finalLegTicket = bookInternal(customer, r2, transferStation.getId(), arrivalStationId, numberOfSeats);

            notificationService.sendBookingEmail(userEmail, finalLegTicket.getId());
            return finalLegTicket;
        }

        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new TicketingException("Ride not found."));
        Ticket savedTicket = bookInternal(customer, ride, departureStationId, arrivalStationId, numberOfSeats);
        notificationService.sendBookingEmail(userEmail, savedTicket.getId());
        return savedTicket;
    }

    private Ticket bookInternal(User customer, Ride ride, Long depId, Long arrId, int seats) throws TicketingException {
        int startIdx = getStationIndex(ride.getSegments(), depId, true);
        int endIdx = getStationIndex(ride.getSegments(), arrId, false);

        if (startIdx == -1 || endIdx == -1 || startIdx > endIdx) throw new TicketingException("Invalid segment selection.");

        for (int i = startIdx; i <= endIdx; i++) {
            RideSegment segment = ride.getSegments().get(i);
            if (segment.getAvailableSeats() < seats) throw new TicketingException("Booking failed: Overbooking prevented.");
            segment.setAvailableSeats(segment.getAvailableSeats() - seats);
        }

        Station depStation = stationRepository.findById(depId).orElseThrow();
        Station arrStation = stationRepository.findById(arrId).orElseThrow();
        return ticketRepository.save(new Ticket(null, customer, ride, depStation, arrStation, seats, TicketStatus.CONFIRMED));
    }

    @Override
    @Transactional
    public void delayRide(Long rideId, int delayMinutes) throws TicketingException {
        Ride ride = rideRepository.findById(rideId).orElseThrow(() -> new TicketingException("Ride not found."));
        ride.setDelayMinutes(ride.getDelayMinutes() + delayMinutes);
        for (RideSegment segment : ride.getSegments()) {
            segment.setDepartureTime(segment.getDepartureTime().plusMinutes(delayMinutes));
            segment.setArrivalTime(segment.getArrivalTime().plusMinutes(delayMinutes));
        }

        List<Ticket> bookings = ticketRepository.findByRideId(rideId);
        for (Ticket t : bookings) notificationService.sendDelayNotification(t.getCustomer().getEmail(), rideId, delayMinutes);
        messagingTemplate.convertAndSend("/topic/delays", "{\"rideId\": " + rideId + ", \"delayMinutes\": " + delayMinutes + "}");
    }

    @Override
    public void deleteTrain(Long id) { trainRepository.deleteById(id); }

    @Override
    public Route addRoute(String name) { return routeRepository.save(new Route(null, name)); }

    @Override
    public List<Ticket> getAllBookings() { return ticketRepository.findAll(); }
}