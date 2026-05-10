package network.dto;

import domain.*;
import java.util.stream.Collectors;

public class DtoMapper {

    public static StationDto toDto(Station station) {
        if (station == null) return null;
        return new StationDto(station.getId(), station.getName());
    }

    public static TrainDto toDto(Train train) {
        if (train == null) return null;
        return new TrainDto(train.getId(), train.getName(), train.getTotalCapacity());
    }

    public static UserDto toDto(User user) {
        if (user == null) return null;
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public static RideSegmentDto toDto(RideSegment segment) {
        if (segment == null) return null;
        return new RideSegmentDto(
                segment.getId(),
                toDto(segment.getFromStation()),
                toDto(segment.getToStation()),
                segment.getDepartureTime(),
                segment.getArrivalTime(),
                segment.getAvailableSeats()
        );
    }

    public static RideDto toDto(Ride ride) {
        if (ride == null) return null;
        return new RideDto(
                ride.getId(),
                toDto(ride.getTrain()),
                ride.getRoute() != null ? ride.getRoute().getName() : "Unknown Route",
                ride.getSegments().stream().map(DtoMapper::toDto).collect(Collectors.toList()),
                ride.getDelayMinutes()
        );
    }

    public static TicketDto toDto(Ticket ticket) {
        return new TicketDto(
                ticket.getId(),
                ticket.getCustomer().getEmail(),
                ticket.getRide().getId(),
                ticket.getDepartureStation().getId(),
                ticket.getArrivalStation().getId(),
                ticket.getNumberOfSeats(),
                ticket.getStatus().name()
        );
    }
}