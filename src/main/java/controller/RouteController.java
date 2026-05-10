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
import service.TicketingException;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<?> findRoutes(@RequestParam Long departureId, @RequestParam Long arrivalId) {
        logger.info("REST Request received: GET /api/routes?departureId={}&arrivalId={}", departureId, arrivalId);
        try {
            List<Ride> rides = ticketingService.findRoutes(departureId, arrivalId);

            List<RideDto> dtos = rides.stream()
                    .map(DtoMapper::toDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(dtos);
        } catch (TicketingException e) {
            logger.warn("Route search failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}