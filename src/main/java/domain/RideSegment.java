package domain;

import java.util.Objects;

public class RideSegment implements Entity<Long> {
    private Long id;
    private Station fromStation;
    private Station toStation;
    private int availableSeats;

    public RideSegment(Long id, Station fromStation, Station toStation, int availableSeats) {
        this.id = id;
        this.fromStation = fromStation;
        this.toStation = toStation;
        this.availableSeats = availableSeats;
    }

    @Override
    public Long getId() { return id; }
    @Override
    public void setId(Long id) { this.id = id; }

    public Station getFromStation() { return fromStation; }
    public void setFromStation(Station fromStation) { this.fromStation = fromStation; }

    public Station getToStation() { return toStation; }
    public void setToStation(Station toStation) { this.toStation = toStation; }

    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int availableSeats) { this.availableSeats = availableSeats; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RideSegment that = (RideSegment) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
