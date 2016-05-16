package ru.ifmo.ctddev.kamenev.hello;

import info.kgeorgiy.java.advanced.hello.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import java.util.concurrent.*;

/**
 * Class that implements server that listens and responses to packets on UDP protocol.
 */
public class HelloUDPServer implements HelloServer {
    private final static int TRIES = 10;
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
            HelloUDPServer server = new HelloUDPServer();
            int threads = Integer.parseInt(args[1]);
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
        private final int port;

        public Pimpl(int port, int threads) throws SocketException {
            this.threads = threads;
            this.port = port;
            this.socket = new DatagramSocket(port);
            this.workerThreads = Executors.newFixedThreadPool(threads);
            this.buffSize = socket.getReceiveBufferSize();
        }

        public void start() {
            for (int i = 0; i < threads; i++) {
                workerThreads.submit(() -> {
                    DatagramPacket pack = new DatagramPacket(new byte[buffSize], buffSize);
                    while (!Thread.interrupted()) {
                        try {
                            socket.receive(pack);
                            String requestString = new String(pack.getData(), pack.getOffset(), pack.getLength(), StandardCharsets.UTF_8);
                            String responseString = "Hello, " + requestString;

                            //byte[] data = responseString.getBytes(StandardCharsets.UTF_8);
                            //DatagramPacket response = new DatagramPacket(data, data.length, request.getAddress(), request.getPort());
                            pack.setData(responseString.getBytes());
                            for(int ii = 0; ii < TRIES; ++ii) {
                                try {
                                    socket.send(pack);
                                    break;
                                } catch (IOException e) {
                                    System.err.println("Unable to send response: " + e.getMessage() + ". Retrying");
                                }
                            }

                        } catch (IOException e) {
                            System.err.println("Failed to receive packet: " + e.getMessage());
                        }
                        
                    }
                });
            }
            System.out.println("Started listening on " + this.port + " in " + this.threads + " threads");
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
