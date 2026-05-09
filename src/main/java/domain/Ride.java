package domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Ride implements Entity<Long> {
    private Long id;
    private Train train;
    private Route route;
    private List<RideSegment> segments = new ArrayList<>();
    private int delayMinutes;

    public Ride(Long id, Train train, Route route, List<RideSegment> segments, int delayMinutes) {
        this.id = id;
        this.train = train;
        this.route = route;
        if (segments != null) {
            this.segments = segments;
        }
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

    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }

    public List<RideSegment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    public void addSegment(RideSegment segment) {
        this.segments.add(segment);
    }

    public void removeSegment(RideSegment segment) {
        this.segments.remove(segment);
    }

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