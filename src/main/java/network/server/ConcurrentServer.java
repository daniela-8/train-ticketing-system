package network.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.ITicketingService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentServer {
    private static final Logger logger = LogManager.getLogger(ConcurrentServer.class);

    private final int port;
    private final ITicketingService ticketingService;
    private final ExecutorService threadPool;
    private boolean isRunning;

    public ConcurrentServer(int port, ITicketingService ticketingService) {
        this.port = port;
        this.ticketingService = ticketingService;
        this.threadPool = Executors.newFixedThreadPool(10);
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            isRunning = true;
            logger.info("Concurrent RPC Server started on port {}", port);

            while (isRunning) {
                logger.info("Waiting for clients to connect.");
                Socket clientSocket = serverSocket.accept();
                logger.info("Client connected from {}", clientSocket.getRemoteSocketAddress());

                ClientWorker worker = new ClientWorker(clientSocket, ticketingService);
                threadPool.execute(worker);
            }
        } catch (IOException e) {
            logger.error("Server exception on port {}", port, e);
        } finally {
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        threadPool.shutdown();
        logger.info("Server stopped.");
    }
}