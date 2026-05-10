package service;

import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import repository.interfaces.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketingServiceImplTest {

    @Mock private IUserRepository userRepository;
    @Mock private IStationRepository stationRepository;
    @Mock private ITrainRepository trainRepository;
    @Mock private IRouteRepository routeRepository;
    @Mock private IRideRepository rideRepository;
    @Mock private ITicketRepository ticketRepository;
    @Mock private NotificationService notificationService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TicketingServiceImpl ticketingService;

    private User mockUser;
    private Station cluj, brasov, bucharest;
    private Ride directRide;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@siemens.com");

        cluj = new Station();
        cluj.setId(1L);
        cluj.setName("Cluj-Napoca");

        brasov = new Station();
        brasov.setId(2L);
        brasov.setName("Brasov");

        bucharest = new Station();
        bucharest.setId(3L);
        bucharest.setName("Bucharest");

        Train train = new Train();
        train.setId(1L);
        train.setName("Express");

        Route route = new Route();
        route.setId(1L);
        route.setName("Main Line");

        RideSegment s1 = new RideSegment();
        s1.setId(1L);
        s1.setFromStation(cluj);
        s1.setToStation(brasov);
        s1.setAvailableSeats(100);
        s1.setDepartureTime(LocalDateTime.now());
        s1.setArrivalTime(LocalDateTime.now().plusHours(2));

        RideSegment s2 = new RideSegment();
        s2.setId(2L);
        s2.setFromStation(brasov);
        s2.setToStation(bucharest);
        s2.setAvailableSeats(100);
        s2.setDepartureTime(LocalDateTime.now().plusHours(2));
        s2.setArrivalTime(LocalDateTime.now().plusHours(4));

        List<RideSegment> segments = new ArrayList<>();
        segments.add(s1);
        segments.add(s2);

        directRide = new Ride();
        directRide.setId(1L);
        directRide.setTrain(train);
        directRide.setRoute(route);
        directRide.setSegments(segments);
    }

    @Test
    @DisplayName("Should successfully book a direct ticket and deduct available seats")
    void bookTicket_Success_WhenDirectRouteHasEnoughSeats() throws TicketingException {
        when(userRepository.findByEmail("test@siemens.com")).thenReturn(Optional.of(mockUser));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(directRide));
        when(stationRepository.findById(1L)).thenReturn(Optional.of(cluj));
        when(stationRepository.findById(3L)).thenReturn(Optional.of(bucharest));

        Ticket savedMockTicket = new Ticket();
        savedMockTicket.setId(99L);
        when(ticketRepository.save(any(Ticket.class))).thenReturn(savedMockTicket);

        Ticket result = ticketingService.bookTicket("test@siemens.com", 1L, 1L, 3L, 5);

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals(95, directRide.getSegments().get(0).getAvailableSeats());
        assertEquals(95, directRide.getSegments().get(1).getAvailableSeats());
        verify(notificationService, times(1)).sendBookingEmail("test@siemens.com", 99L);
    }

    @Test
    @DisplayName("Should throw TicketingException and rollback when trying to overbook")
    void bookTicket_ThrowsException_WhenOverbooked() {
        when(userRepository.findByEmail("test@siemens.com")).thenReturn(Optional.of(mockUser));
        when(rideRepository.findById(1L)).thenReturn(Optional.of(directRide));

        TicketingException exception = assertThrows(TicketingException.class, () -> {
            ticketingService.bookTicket("test@siemens.com", 1L, 1L, 3L, 101);
        });

        assertTrue(exception.getMessage().contains("Overbooking prevented"));
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(notificationService, never()).sendBookingEmail(anyString(), anyLong());
    }

    @Test
    @DisplayName("Should stitch together a Virtual Changeover Ride when direct routes are unavailable")
    void findRoutes_ReturnsCompositeRide_WhenChangeoverRequired() throws TicketingException {
        Station timisoara = new Station();
        timisoara.setId(4L);
        timisoara.setName("Timisoara");

        Station sibiu = new Station();
        sibiu.setId(5L);
        sibiu.setName("Sibiu");

        RideSegment feedSegment = new RideSegment();
        feedSegment.setId(3L);
        feedSegment.setFromStation(timisoara);
        feedSegment.setToStation(sibiu);
        feedSegment.setAvailableSeats(50);
        feedSegment.setDepartureTime(LocalDateTime.of(2026, 5, 10, 7, 0));
        feedSegment.setArrivalTime(LocalDateTime.of(2026, 5, 10, 9, 0));

        Ride feederRide = new Ride();
        feederRide.setId(2L);
        feederRide.setSegments(List.of(feedSegment));

        RideSegment mainSegment = new RideSegment();
        mainSegment.setId(4L);
        mainSegment.setFromStation(sibiu);
        mainSegment.setToStation(bucharest);
        mainSegment.setAvailableSeats(50);
        mainSegment.setDepartureTime(LocalDateTime.of(2026, 5, 10, 10, 0));
        mainSegment.setArrivalTime(LocalDateTime.of(2026, 5, 10, 12, 0));

        Ride mainRide = new Ride();
        mainRide.setId(1L);
        mainRide.setSegments(List.of(mainSegment));

        when(rideRepository.findAll()).thenReturn(List.of(feederRide, mainRide));

        List<Ride> availableRoutes = ticketingService.findRoutes(4L, 3L);

        assertEquals(1, availableRoutes.size(), "Should find exactly 1 composite route");
        Ride compositeRide = availableRoutes.get(0);
        assertTrue(compositeRide.getId() >= 1000000L, "Composite ID should be generated (>= 1,000,000)");
        assertEquals(2, compositeRide.getSegments().size(), "Composite ride should contain exactly 2 stitched segments");
        assertEquals("Timisoara", compositeRide.getSegments().get(0).getFromStation().getName());
        assertEquals("Bucharest", compositeRide.getSegments().get(1).getToStation().getName());
    }

    @Test
    @DisplayName("Should throw exception when booking with an unregistered email")
    void bookTicket_ThrowsException_WhenUserNotFound() {
        when(userRepository.findByEmail("ghost@hacker.com")).thenReturn(Optional.empty());

        TicketingException exception = assertThrows(TicketingException.class, () -> {
            ticketingService.bookTicket("ghost@hacker.com", 1L, 1L, 3L, 1);
        });

        assertEquals("User with email ghost@hacker.com not found.", exception.getMessage());
        verify(rideRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should return empty list when absolutely no route exists between stations")
    void findRoutes_ReturnsEmptyList_WhenNoRouteExists() throws TicketingException {
        when(rideRepository.findAll()).thenReturn(List.of(directRide));

        List<Ride> availableRoutes = ticketingService.findRoutes(1L, 99L);

        assertTrue(availableRoutes.isEmpty(), "Should return an empty list when no route is possible");
    }

    @Test
    @DisplayName("Admin Delay: Should shift timetable, send emails, and broadcast WebSocket alert")
    void delayRide_Success_UpdatesTimesAndNotifiesUsers() throws TicketingException {
        int delayMinutes = 45;
        LocalDateTime originalDeparture = directRide.getSegments().get(0).getDepartureTime();

        when(rideRepository.findById(1L)).thenReturn(Optional.of(directRide));

        Ticket existingTicket = new Ticket();
        existingTicket.setId(100L);
        existingTicket.setCustomer(mockUser);
        when(ticketRepository.findByRideId(1L)).thenReturn(List.of(existingTicket));

        ticketingService.delayRide(1L, delayMinutes);

        assertEquals(delayMinutes, directRide.getDelayMinutes());
        assertTrue(directRide.getSegments().get(0).getDepartureTime().isEqual(originalDeparture.plusMinutes(delayMinutes)));

        verify(notificationService, times(1)).sendDelayNotification("test@siemens.com", 1L, delayMinutes);

        verify(messagingTemplate, times(1))
                .convertAndSend(eq("/topic/delays"), contains("\"delayMinutes\": 45"));
    }
}