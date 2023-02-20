package ru.netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService executorService;
    private final String BAD_REQUEST_CODE = "400";
    private final String BAD_REQUEST_DESCRIPTION = "Bad Request";

    private final ConcurrentHashMap<String, Map<String, Handler>> handlers;

    public Server() {
        this.executorService = Executors.newFixedThreadPool(64);
        handlers = new ConcurrentHashMap();
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(() -> handle(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle(Socket socket) {
        try (
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            Request request = Request.parse(in);
            if (request == null ) {
                responseBuilder(out, BAD_REQUEST_CODE, BAD_REQUEST_DESCRIPTION, null, 0);
                return;
            }

            if (!handlers.containsKey(request.getMethod())) {
                responseBuilder(out, BAD_REQUEST_CODE, BAD_REQUEST_DESCRIPTION, null, 0);
                return;
            }

            var methods = handlers.get(request.getMethod());
            if (!methods.containsKey(request.getPath())) {
                responseBuilder(out, BAD_REQUEST_CODE, BAD_REQUEST_DESCRIPTION, null, 0);
                return;
            }

            var handler = methods.get(request.getPath());
            handler.handle(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void responseBuilder(BufferedOutputStream out,
                                String respCode,
                                String respDescription,
                                String contentType,
                                long contentLength) {
        try {
            out.write((
                    "HTTP/1.1 " + respCode + " " + respDescription + "\r\n" +
                            "Content-Type: " + contentType + "\r\n" +
                            "Content-Length: " + contentLength + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addHandler(String method, String path, Handler handler) {
        if (!handlers.containsKey(method)) {
            handlers.put(method, new HashMap<>());
        }
        handlers.get(method).put(path, handler);
    }
}
