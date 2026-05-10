package controller;

import domain.Route;
import domain.Ticket;
import domain.Train;
import network.dto.DtoMapper;
import network.dto.TicketDto;
import network.dto.TrainDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.ITicketingService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LogManager.getLogger(AdminController.class);
    private final ITicketingService ticketingService;

    @Autowired
    public AdminController(ITicketingService ticketingService) {
        this.ticketingService = ticketingService;
    }


    @PostMapping("/trains")
    public ResponseEntity<TrainDto> addTrain(@RequestBody Train trainRequest) {
        logger.info("Admin Request: Add Train {}", trainRequest.getName());
        Train saved = ticketingService.addTrain(trainRequest.getName(), trainRequest.getTotalCapacity());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toDto(saved));
    }

    @DeleteMapping("/trains/{id}")
    public ResponseEntity<Void> deleteTrain(@PathVariable Long id) {
        logger.info("Admin Request: Delete Train ID {}", id);
        ticketingService.deleteTrain(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/routes")
    public ResponseEntity<Route> addRoute(@RequestBody Route routeRequest) {
        logger.info("Admin Request: Add Route {}", routeRequest.getName());
        Route saved = ticketingService.addRoute(routeRequest.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }


    @GetMapping("/bookings")
    public ResponseEntity<List<TicketDto>> getAllBookings() {
        logger.info("Admin Request: View all bookings");
        List<Ticket> tickets = ticketingService.getAllBookings();
        List<TicketDto> dtos = tickets.stream().map(DtoMapper::toDto).toList();
        return ResponseEntity.ok(dtos);
    }


    @PostMapping("/rides/{rideId}/delay")
    public ResponseEntity<String> reportDelay(@PathVariable Long rideId, @RequestParam int minutes) {
        logger.info("Admin Request: Report {} min delay for Ride {}", minutes, rideId);
        ticketingService.delayRide(rideId, minutes);
        return ResponseEntity.ok("Delay reported and customers notified via async email.");
    }
}