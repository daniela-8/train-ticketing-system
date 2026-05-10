package network.dto;

import java.io.Serializable;

public record TrainDto(Long id, String name, int totalCapacity) implements Serializable {}