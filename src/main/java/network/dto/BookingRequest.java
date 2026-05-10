package network.dto;

public record BookingRequest(
        String userEmail,
        Long rideId,
        Long departureStationId,
        Long arrivalStationId,
        int numberOfSeats
) {}