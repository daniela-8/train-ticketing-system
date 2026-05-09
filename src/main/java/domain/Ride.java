package domain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Ride implements Entity<Long> {
    private Long id;
    private Train train;
    private Route route;
    private List<RideStop> stops = new ArrayList<>();
    private List<RideSegment> segments = new ArrayList<>();
    private int delayMinutes = 0;

    public Ride(Long id, Train train, Route route, int delayMinutes) {
        this.id = id;
        this.train = train;
        this.route = route;
        this.delayMinutes = delayMinutes;
    }

    @Override
    public Long getId() { return id; }
    @Override
    public void setId(Long id) { this.id = id; }

    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public List<RideStop> getStop() {
        return Collections.unmodifiableList(stops);
    }

    public void addStop(RideStop stop) {
        this.stops.add(stop);
    }

    public void removeStop(RideStop stop) {
        this.stops.remove(stop);
    }

    public List<RideSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public void addSegment(RideSegment segment) {
        this.segments.add(segment);
    }

    public void removeSegment(RideSegment segment) {
        this.segments.remove(segment);
    }

    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ride ride = (Ride) o;
        return Objects.equals(id, ride.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}