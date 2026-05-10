import config.AppConfig;
import service.ITicketingService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        ITicketingService service = context.getBean(ITicketingService.class);

        service.getAllStations().forEach(station ->
                System.out.println("Found Station: " + station.getName())
        );
    }
}