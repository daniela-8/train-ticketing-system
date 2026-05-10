package network.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record RideSegmentDto(
        Long id,
        StationDto fromStation,
        StationDto toStation,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        int availableSeats
) {}