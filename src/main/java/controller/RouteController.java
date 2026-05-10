package controller;

import domain.Ride;
import network.dto.DtoMapper;
import network.dto.RideDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import service.ITicketingService;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private static final Logger logger = LogManager.getLogger(RouteController.class);
    private final ITicketingService ticketingService;

    @Autowired
    public RouteController(ITicketingService ticketingService) {
        this.ticketingService = ticketingService;
    }

    @GetMapping
    public ResponseEntity<List<RideDto>> findRoutes(@RequestParam Long departureId, @RequestParam Long arrivalId) {
        logger.info("REST Request: GET /api/routes?dep={}&arr={}", departureId, arrivalId);

        List<Ride> rides = ticketingService.findRoutes(departureId, arrivalId);

        List<RideDto> dtos = rides.stream()
                .map(DtoMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }
}