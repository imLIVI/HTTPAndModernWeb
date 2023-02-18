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
    private final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js");
    private final String BAD_REQUEST_CODE = "400";
    private final String BAD_REQUEST_DESCRIPTION = "Bad Request";
    private final String NOT_FOUND_CODE = "404";
    private final String NOT_FOUND_DESCRIPTION = "Not Found";

    private final ConcurrentHashMap<String, Map<String, Handler>> handlers;

    public Server() {
        this.executorService = Executors.newFixedThreadPool(64);
        handlers = new ConcurrentHashMap();
    }

    public void start(int port) {
        try {
            final var serverSocket = new ServerSocket(port);
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
            // read only request line for simplicity
            // must be in form GET /path HTTP/1.1
            final var requestLine = in.readLine();
            final var parts = requestLine.split(" ");

            if (parts.length != 3) {
                // just close socket
                socket.close();
                return;
            }

            final var method = parts[0];
            final var path = parts[1];
            if (!method.equals(null)) {
                Request request = new Request(method, path);
            } else {
                responseBuilder(out, BAD_REQUEST_CODE, BAD_REQUEST_DESCRIPTION);
                return;
            }

            if (!validPaths.contains(path)) {
                responseBuilder(out, NOT_FOUND_CODE, NOT_FOUND_DESCRIPTION);
                return;
            }

            final var filePath = Path.of(".", "public", path);
            final var mimeType = Files.probeContentType(filePath);

            // special case for classic
            if (path.equals("/classic.html")) {
                final var template = Files.readString(filePath);
                final var content = template.replace(
                        "{time}",
                        LocalDateTime.now().toString()
                ).getBytes();
                out.write((
                        "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: " + mimeType + "\r\n" +
                                "Content-Length: " + content.length + "\r\n" +
                                "Connection: close\r\n" +
                                "\r\n"
                ).getBytes());
                out.write(content);
                out.flush();
                return;
            }

            final var length = Files.size(filePath);
            out.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, out);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void responseBuilder(BufferedOutputStream out,
                                   String respCode,
                                   String respDescription) {
        try {
            out.write((
                    "HTTP/1.1 " + respCode + " " + respDescription + "\r\n" +
                            "Content-Type: none\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
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
