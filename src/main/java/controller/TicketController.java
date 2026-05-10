package controller;

import domain.Ticket;
import network.dto.BookingRequest;
import network.dto.DtoMapper;
import network.dto.TicketDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.ITicketingService;
import service.TicketingException;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private static final Logger logger = LogManager.getLogger(TicketController.class);
    private final ITicketingService ticketingService;

    @Autowired
    public TicketController(ITicketingService ticketingService) {
        this.ticketingService = ticketingService;
    }

    @PostMapping("/book")
    public ResponseEntity<?> bookTicket(@RequestBody BookingRequest request) {
        logger.info("REST Request received: POST /api/tickets/book for user: {}", request.userEmail());

        try {
            Ticket bookedTicket = ticketingService.bookTicket(
                    request.userEmail(),
                    request.rideId(),
                    request.departureStationId(),
                    request.arrivalStationId(),
                    request.numberOfSeats()
            );

            TicketDto responseDto = DtoMapper.toDto(bookedTicket);

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

        } catch (TicketingException e) {
            logger.warn("Booking failed via REST: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during booking", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }
}