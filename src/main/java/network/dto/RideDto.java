package network.dto;

import java.io.Serializable;
import java.util.List;

public record RideDto(
        Long id,
        TrainDto train,
        String routeName,
        List<RideSegmentDto> segments,
        int delayMinutes
) implements Serializable {}