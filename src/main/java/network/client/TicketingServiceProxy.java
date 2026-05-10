package network.client;

import domain.Ride;
import domain.Station;
import domain.Train;
import domain.User;
import domain.Ticket;
import network.dto.DtoMapper;
import network.dto.StationDto;
import network.dto.UserDto;
import network.dto.RideDto;
import network.Request;
import network.RequestType;
import network.Response;
import network.ResponseType;
import service.ITicketingService;
import service.TicketingException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TicketingServiceProxy implements ITicketingService {
    private final String host;
    private final int port;

    public TicketingServiceProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }

    private Response sendRequest(Request request) throws TicketingException {
        try (Socket socket = new Socket(host, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush();

            Response response = (Response) in.readObject();
            if (response.getType() == ResponseType.ERROR) {
                throw new TicketingException((String) response.getData());
            }
            return response;

        } catch (IOException | ClassNotFoundException e) {
            throw new TicketingException("Network error: " + e.getMessage(), e);
        }
    }

    @Override
    public User authenticate(String email) throws TicketingException {
        Request req = new Request.Builder().type(RequestType.AUTHENTICATE).data(email).build();
        Response res = sendRequest(req);
        UserDto dto = (UserDto) res.getData();
        return new User(dto.id(), dto.name(), dto.email(), domain.enums.Role.valueOf(dto.role()));
    }

    @Override
    public List<Station> getAllStations() {
        Request req = new Request.Builder().type(RequestType.GET_ALL_STATIONS).build();
        Response res = sendRequest(req);
        @SuppressWarnings("unchecked")
        List<StationDto> dtos = (List<StationDto>) res.getData();
        return dtos.stream().map(dto -> new Station(dto.id(), dto.name())).collect(Collectors.toList());
    }

    @Override
    public List<Train> getAllTrains() { return null; }
    @Override
    public Train addTrain(String name, int totalCapacity) { return null; }
    @Override
    public void delayRide(Long rideId, int delayMinutes) throws TicketingException {}
    @Override
    public List<Ticket> getBookingsForRide(Long rideId) { return null; }
    @Override
    public Ticket bookTicket(String userEmail, Long rideId, Long depId, Long arrId, int seats) throws TicketingException { return null; }
    @Override
    public List<Ride> findRoutes(Long departureStationId, Long arrivalStationId) throws TicketingException { return null; }
    @Override
    public void deleteTrain(Long id) {
        throw new UnsupportedOperationException("Admin operations are only supported via the REST API.");
    }

    @Override
    public domain.Route addRoute(String name) {
        throw new UnsupportedOperationException("Admin operations are only supported via the REST API.");
    }

    @Override
    public java.util.List<domain.Ticket> getAllBookings() {
        throw new UnsupportedOperationException("Admin operations are only supported via the REST API.");
    }
}