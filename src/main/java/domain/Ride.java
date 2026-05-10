package domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Rides")
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "train_id")
    private Train train;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "route_id")
    private Route route;

    @OneToMany(mappedBy = "ride", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RideSegment> segments = new ArrayList<>();

    @Column(name = "delay_minutes")
    private int delayMinutes;

    public Ride() {}

    public Ride(Long id, Train train, Route route, List<RideSegment> segments, int delayMinutes) {
        this.id = id;
        this.train = train;
        this.route = route;
        this.segments = segments;
        this.delayMinutes = delayMinutes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Train getTrain() { return train; }
    public void setTrain(Train train) { this.train = train; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public List<RideSegment> getSegments() { return segments; }
    public void setSegments(List<RideSegment> segments) { this.segments = segments; }

    public int getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(int delayMinutes) { this.delayMinutes = delayMinutes; }
}