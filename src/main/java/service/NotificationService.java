package service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    @Async
    public void sendBookingEmail(String email, Long ticketId) {
        logger.info("ASYNC-START: Preparing booking confirmation for Ticket #{} to {}", ticketId, email);

        try {
            Thread.sleep(4000);

            logger.info("ASYNC-COMPLETE: Confirmation email successfully sent to {} for Ticket #{}", email, ticketId);
        } catch (InterruptedException e) {
            logger.error("ASYNC-ERROR: Email task for Ticket #{} was interrupted", ticketId, e);
            Thread.currentThread().interrupt();
        }
    }


    @Async
    public void sendDelayNotification(String email, Long rideId, int delayMinutes) {
        logger.info("ASYNC-START: Preparing delay notification for Ride #{} to {}", rideId, email);

        try {
            Thread.sleep(3000);

            logger.info("ASYNC-COMPLETE: Delay notification ({} min) sent to {} for Ride #{}", delayMinutes, email, rideId);
        } catch (InterruptedException e) {
            logger.error("ASYNC-ERROR: Delay notification task for Ride #{} was interrupted", rideId, e);
            Thread.currentThread().interrupt();
        }
    }
}