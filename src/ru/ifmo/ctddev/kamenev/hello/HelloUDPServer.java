package ru.ifmo.ctddev.kamenev.hello;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.*;

/**
 * Class that implements server that listens and responses to packets on UDP protocol.
 */
public class HelloUDPServer implements AutoCloseable {

    private final BlockingQueue<Pimpl> runningServers;

    /**
     * Starts server on UDP protocol on specified port with specified number of threads.
     *
     * @param args arguments passed in command line
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Invalid amount of arguments provided");
            System.out.println("Usage: [port] [threads]");
            return;
        }
        try {
            int port = Integer.parseInt(args[0]);
            int threads = Integer.parseInt(args[1]);
            HelloUDPServer server = new HelloUDPServer();
            server.start(port, threads);

        } catch (NumberFormatException e) {
            System.out.println("Usage: [port] [threads]");
        }
    }

    private class Pimpl {
        private final DatagramSocket socket;
        private final ExecutorService workerThreads;
        private final int buffSize;
        private final int threads;

        public Pimpl(int port, int threads) throws SocketException {
            this.threads = threads;
            this.socket = new DatagramSocket(port);
            this.workerThreads = Executors.newFixedThreadPool(threads);
            this.buffSize = socket.getReceiveBufferSize();
        }

        public void start() {
            for (int i = 0; i < threads; i++) {
                workerThreads.submit(() -> {
                    DatagramPacket request = new DatagramPacket(new byte[buffSize], buffSize);
                    while (!Thread.interrupted()) {
                        try {
                            socket.receive(request);
                            String requestString = new String(request.getData(), request.getOffset(), request.getLength());
                            String responseString = "Hello, " + requestString;

                            byte[] data = responseString.getBytes();
                            DatagramPacket response = new DatagramPacket(data, data.length, request.getAddress(), request.getPort());
                            try {
                                socket.send(response);
                            } catch (IOException e) {
                                System.out.println("Unable to send response: " + e.getMessage());
                            }
                        } catch (IOException e) {
                            System.out.println("Failed to receive packet: " + e.getMessage());
                        }
                    }
                });
            }

        }

        public void close() {
            workerThreads.shutdownNow();
            socket.close();
        }
    }

    /**
     * Creates instance of class.
     * Created class is ready to perform responding using  {@link #start(int, int)}
     */
    public HelloUDPServer() {
        runningServers = new LinkedBlockingQueue<>();
    }

    /**
     * Starts listening on {@code port} using {@code threads} number of threads.
     *
     * @param port    port to listen to
     * @param threads number of threads that listen
     */
    public void start(int port, int threads) {
        try {
            Pimpl server = new Pimpl(port, threads);
            runningServers.add(server);
            server.start();
        } catch (SocketException e) {
            System.out.println("Failed to start server: " + e.getMessage());
        }

    }

    /**
     * Closes all sockets and stops all threads.
     */
    @Override
    public void close() {
        runningServers.forEach(Pimpl::close);
    }
}
