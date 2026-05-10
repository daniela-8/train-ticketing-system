package controller;

import domain.Station;
import network.dto.DtoMapper;
import network.dto.StationDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.ITicketingService;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private static final Logger logger = LogManager.getLogger(StationController.class);
    private final ITicketingService ticketingService;

    @Autowired
    public StationController(ITicketingService ticketingService) {
        this.ticketingService = ticketingService;
    }

    @GetMapping
    public ResponseEntity<List<StationDto>> getAllStations() {
        logger.info("REST Request: GET /api/stations");

        List<Station> stations = ticketingService.getAllStations();

        List<StationDto> dtos = stations.stream()
                .map(DtoMapper::toDto)
                .toList();

        return ResponseEntity.ok(dtos);
    }
}