package network.dto;

public record TicketDto(
        Long id,
        String userEmail,
        Long rideId,
        Long departureStationId,
        Long arrivalStationId,
        int numberOfSeats,
        String status
) {}