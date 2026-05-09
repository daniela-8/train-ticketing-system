package domain;

import domain.enums.TicketStatus;

import java.util.Objects;

public class Ticket implements Entity<Long> {
    private Long id;
    private User customer;
    private Ride ride;
    private Station departureStation;
    private Station arrivalStation;
    private int numberOfSeats;
    private TicketStatus status;

    public Ticket(Long id, User customer, Ride ride, Station departureStation, Station arrivalStation, int numberOfSeats, TicketStatus status) {
        this.id = id;
        this.customer = customer;
        this.ride = ride;
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.numberOfSeats = numberOfSeats;
        this.status = status;
    }

    @Override
    public Long getId() { return id; }
    @Override
    public void setId(Long id) { this.id = id; }

    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }

    public Ride getRide() { return ride; }
    public void setRide(Ride ride) { this.ride = ride; }

    public Station getDepartureStation() { return departureStation; }
    public void setDepartureStation(Station departureStation) { this.departureStation = departureStation; }

    public Station getArrivalStation() { return arrivalStation; }
    public void setArrivalStation(Station arrivalStation) { this.arrivalStation = arrivalStation; }

    public int getNumberOfSeats() { return numberOfSeats; }
    public void setNumberOfSeats(int numberOfSeats) { this.numberOfSeats = numberOfSeats; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ticket ticket = (Ticket) o;
        return Objects.equals(id, ticket.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
