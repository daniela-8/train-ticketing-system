package domain;

import domain.enums.TicketStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "Tickets")
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "departure_station_id", nullable = false)
    private Station departureStation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "arrival_station_id", nullable = false)
    private Station arrivalStation;

    @Column(name = "number_of_seats", nullable = false)
    private int numberOfSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    public Ticket() {}

    public Ticket(Long id, User customer, Ride ride, Station departureStation, Station arrivalStation, int numberOfSeats, TicketStatus status) {
        this.id = id;
        this.customer = customer;
        this.ride = ride;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.numberOfSeats = numberOfSeats;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public Ride getRide() { return ride; }
    public void setRide(Ride ride) { this.ride = ride; }
    public Station getDepartureStation() { return departureStation; }
    public void setDepartureStation(Station station) { this.departureStation = station; }
    public Station getArrivalStation() { return arrivalStation; }
    public void setArrivalStation(Station station) { this.arrivalStation = station; }
    public int getNumberOfSeats() { return numberOfSeats; }
    public void setNumberOfSeats(int seats) { this.numberOfSeats = seats; }
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
}