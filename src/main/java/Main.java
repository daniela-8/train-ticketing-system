import config.AppConfig;
import network.server.ConcurrentServer;
import service.ITicketingService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        ITicketingService service = context.getBean(ITicketingService.class);

        ConcurrentServer server = new ConcurrentServer(5555, service);
        server.start();
    }
}