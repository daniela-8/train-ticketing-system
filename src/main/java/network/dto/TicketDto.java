package network.dto;

import java.io.Serializable;

public record TicketDto(
        Long id,
        UserDto customer,
        RideDto ride,
        StationDto departureStation,
        StationDto arrivalStation,
        int numberOfSeats,
        String status
) implements Serializable {}