package network.server;

import domain.Ride;
import domain.Station;
import domain.User;
import network.dto.DtoMapper;
import network.dto.RideDto;
import network.dto.StationDto;
import network.dto.UserDto;
import network.Request;
import network.Response;
import network.ResponseType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.ITicketingService;
import service.TicketingException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientWorker implements Runnable {
    private static final Logger logger = LogManager.getLogger(ClientWorker.class);

    private final Socket connection;
    private final ITicketingService ticketingService;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private volatile boolean connected;

    public ClientWorker(Socket connection, ITicketingService ticketingService) {
        this.connection = connection;
        this.ticketingService = ticketingService;
        try {
            this.output = new ObjectOutputStream(connection.getOutputStream());
            this.output.flush();
            this.input = new ObjectInputStream(connection.getInputStream());
            this.connected = true;
            logger.info("New client worker initialized for {}", connection.getRemoteSocketAddress());
        } catch (IOException e) {
            logger.error("Failed to initialize client worker streams", e);
        }
    }

    @Override
    public void run() {
        while (connected) {
            try {
                Object requestObj = input.readObject();
                if (requestObj instanceof Request request) {
                    Response response = handleRequest(request);
                    sendResponse(response);
                }
            } catch (IOException | ClassNotFoundException e) {
                logger.warn("Client disconnected or stream error: {}", e.getMessage());
                connected = false;
            }
        }
        closeConnection();
    }

    private Response handleRequest(Request request) {
        logger.debug("Handling request type: {}", request.getType());
        try {
            switch (request.getType()) {
                case AUTHENTICATE -> {
                    String email = (String) request.getData();
                    User user = ticketingService.authenticate(email);
                    UserDto userDto = DtoMapper.toDto(user);
                    return new Response.Builder().type(ResponseType.OK).data(userDto).build();
                }
                case GET_ALL_STATIONS -> {
                    List<Station> stations = ticketingService.getAllStations();
                    List<StationDto> dtos = stations.stream().map(DtoMapper::toDto).collect(Collectors.toList());
                    return new Response.Builder().type(ResponseType.OK).data(dtos).build();
                }
                case FIND_ROUTES -> {
                    @SuppressWarnings("unchecked")
                    Map<String, Long> payload = (Map<String, Long>) request.getData();
                    List<Ride> rides = ticketingService.findRoutes(payload.get("departureId"), payload.get("arrivalId"));
                    List<RideDto> rideDtos = rides.stream().map(DtoMapper::toDto).collect(Collectors.toList());
                    return new Response.Builder().type(ResponseType.OK).data(rideDtos).build();
                }
                default -> {
                    return new Response.Builder().type(ResponseType.ERROR).data("Unknown request type").build();
                }
            }
        } catch (TicketingException e) {
            logger.warn("Business logic exception: {}", e.getMessage());
            return new Response.Builder().type(ResponseType.ERROR).data(e.getMessage()).build();
        } catch (Exception e) {
            logger.error("Unexpected server error", e);
            return new Response.Builder().type(ResponseType.ERROR).data("Internal server error").build();
        }
    }

    private void sendResponse(Response response) throws IOException {
        output.writeObject(response);
        output.flush();
    }

    private void closeConnection() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (connection != null) connection.close();
            logger.info("Connection closed for {}", connection.getRemoteSocketAddress());
        } catch (IOException e) {
            logger.error("Error closing connection", e);
        }
    }
}