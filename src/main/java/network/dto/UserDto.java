package network.dto;

import java.io.Serializable;

public record UserDto(Long id, String name, String email, String role) implements Serializable {}