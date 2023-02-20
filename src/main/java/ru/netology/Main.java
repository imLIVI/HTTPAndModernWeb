package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class Main {
    public static final int PORT = 9999;
    private static final String NOT_FOUND_CODE = "404";
    private static final String NOT_FOUND_DESCRIPTION = "Not Found";

    public static void main(String[] args) {

        Server server = new Server();

        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
                server.responseBuilder(responseStream, NOT_FOUND_CODE, NOT_FOUND_DESCRIPTION, "", 0);
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
                server.responseBuilder(responseStream, NOT_FOUND_CODE, NOT_FOUND_DESCRIPTION, "", 0);
            }
        });
        server.addHandler("GET", "/index.html", new Handler() {
            public void handle(Request request, BufferedOutputStream out) {
                // TODO: handlers code
                try {
                    final var filePath = Path.of(".", "public", request.getPath());
                    final var mimeType = Files.probeContentType(filePath);

                    // special case for classic
                    if (request.getPath().equals("/classic.html")) {
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
        });

        server.start(PORT);
    }
}


