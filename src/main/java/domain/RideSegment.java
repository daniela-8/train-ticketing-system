package domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RideSegments")
public class RideSegment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ride_id")
    private Ride ride;

    @ManyToOne
    @JoinColumn(name = "from_station_id")
    private Station fromStation;

    @ManyToOne
    @JoinColumn(name = "to_station_id")
    private Station toStation;

    @Column(name = "available_seats")
    private int availableSeats;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    public RideSegment() {}
    public RideSegment(Long id, Station fromStation, Station toStation, LocalDateTime departureTime, LocalDateTime arrivalTime, int availableSeats) {
        this.id = id; this.fromStation = fromStation; this.toStation = toStation;
        this.departureTime = departureTime; this.arrivalTime = arrivalTime;
        this.availableSeats = availableSeats;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Station getFromStation() { return fromStation; }
    public Station getToStation() { return toStation; }
    public int getAvailableSeats() { return availableSeats; }
    public void setAvailableSeats(int seats) { this.availableSeats = seats; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime time) { this.departureTime = time; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime time) { this.arrivalTime = time; }
}