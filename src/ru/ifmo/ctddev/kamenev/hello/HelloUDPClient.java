package ru.ifmo.ctddev.kamenev.hello;

import java.io.IOException;
import info.kgeorgiy.java.advanced.hello.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;
/**
 * Class to perform sending packets on UDP protocol.
 */
public class HelloUDPClient implements HelloClient{
    /**
     * Creates instance of class.
     */
    public HelloUDPClient() {
    }

    /**
     * Runs a specified number of {@code threads} each sending number of {@code requests}
     * to a server that is located on {@code host} and {@code port} that starts with {@code prefix}
     *
     * @param host     hostname or ip where server is running
     * @param port     port number to send to
     * @param prefix   prefix of requests
     * @param requests number of requests
     * @param threads  number of threads
     */
    public void start(String host, int port, String prefix, int requests, int threads) {
        new Pimpl(host, port, prefix, requests, threads).action();
    }

    private class Pimpl {
        private final SocketAddress to;
        private final String prefix;
        private final int requests;
        private final int threads;
        private final ExecutorService workerPool;

        public Pimpl(String host, int port, String prefix, int requests, int threads) {
            this.requests = requests;
            this.threads = threads;
            this.prefix = prefix;
            this.to = new InetSocketAddress(host, port);
            this.workerPool = Executors.newFixedThreadPool(threads);
        }

        public void action() {
            for (int threadNum = 0; threadNum < threads; threadNum++) {
                final int workerNum = threadNum;
                workerPool.submit(() -> {
                    try(DatagramSocket threadSocket = new DatagramSocket()) {
                        threadSocket.setSoTimeout(100);
                        final int socketBuff = threadSocket.getReceiveBufferSize();
                        DatagramPacket pack = new DatagramPacket(new byte[socketBuff], socketBuff);
                        for (int i = 0; i < requests; i++) {
                            String request = prefix + workerNum + "_" + i;
                            byte data[] = request.getBytes(StandardCharsets.UTF_8);
                            DatagramPacket req = new DatagramPacket(data, data.length, to);
                            while (!Thread.interrupted()) {
                                try {
                                    threadSocket.send(req);
                                    try {
                                        threadSocket.receive(pack);
                                        String responseStr = new String(pack.getData(), pack.getOffset(), pack.getLength(), StandardCharsets.UTF_8);
                                        String referenceStr = "Hello, " + request;
                                        if (responseStr.equals(referenceStr)) {
                                            break;
                                        }
                                        //System.out.println(responseStr);
                                    } catch (IOException e) {
                                        System.err.println("Unable to receive answer: " + e.getMessage());
                                    }
                                } catch (IOException e) {
                                    System.err.println("Unable to send request: " + e.getMessage());
                                }
                            }
                        }
                        return null;
                    }   
                });
            }
        }
    }

    /**
     * @param args args passed to command line
     */
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Invalid amount of arguments provided");
            System.out.println("Usage: [host] [port] [prefix] [threads] [requests]");
            return;
        }
        String host = args[0];
        String prefix = args[2];
        try {
            int port = Integer.parseInt(args[1]);
            int threads = Integer.parseInt(args[3]);
            int requests = Integer.parseInt(args[4]);
            new HelloUDPClient().start(host, port, prefix, requests, threads);
        } catch (NumberFormatException e) {
            System.out.println("Usage: [host] [port] [prefix] [threads] [requests]");
        }
    }
}
