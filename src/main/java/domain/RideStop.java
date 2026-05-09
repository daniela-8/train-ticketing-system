package domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class RideStop implements Entity<Long> {
    private Long id;
    private Station station;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;

    public RideStop(Long id, Station station, LocalDateTime arrivalTime, LocalDateTime departureTime) {
        this.id = id;
        this.station = station;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    @Override
    public Long getId() { return id; }
    @Override
    public void setId(Long id) { this.id = id; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RideStop rideStop = (RideStop) o;
        return Objects.equals(id, rideStop.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
