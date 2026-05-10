import network.client.TicketingServiceProxy;
import service.ITicketingService;

public class ClientMain {
    public static void main(String[] args) {
        System.out.println("Starting Client.");

        ITicketingService service = new TicketingServiceProxy("localhost", 5555);

        try {
            System.out.println("Fetching stations from server.");
            service.getAllStations().forEach(station ->
                    System.out.println("Received Station: " + station.getName())
            );
        } catch (Exception e) {
            System.err.println("Client Error: " + e.getMessage());
        }
    }
}