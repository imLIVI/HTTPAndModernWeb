package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final byte[] headersDelimiter = new byte[]{'\r', '\n', '\r', '\n'};
    private final byte[] requestLineDelimiter = new byte[]{'\r', '\n'};

    private final ExecutorService executorService;
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
                final var in = new BufferedInputStream(socket.getInputStream());
                final var out = new BufferedOutputStream(socket.getOutputStream())
        ) {

            // лимит на request line + заголовки
            final var limit = 4096;

            in.mark(limit);
            final byte[] buffer = new byte[limit];
            final int read = in.read(buffer);

            // ищем и читаем request line
            String[] requestLine = getRequestLine(buffer, read);
            if (requestLine == null) {
                badRequest(out);
                return;
            }

            String method = requestLine[0];
            String path = requestLine[1];
            if (!path.startsWith("/")) {
                badRequest(out);
                return;
            }

            List<String> headers = getHeaders(buffer, read, in);
            if (headers == null) {
                badRequest(out);
                return;
            }

            String body = getBody(method, headers, in);
            Request request = new Request(method, path, headers, body);


            /*Request request = Request.parse(in);
            if (request == null) {
                badRequest(out);
                return;
            }*/

            if (!handlers.containsKey(request.getMethod())) {
                badRequest(out);
                return;
            }

            var methods = handlers.get(request.getMethod());
            if (!methods.containsKey(request.getPath())) {
                badRequest(out);
                return;
            }

            var handler = methods.get(request.getPath());
            handler.handle(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBody(String method, List<String> headers, BufferedInputStream in) {
        String body = null;
        // для GET тела нет
        if (!method.equals("GET")) {
            try {
                in.skip(headersDelimiter.length);
                // вычитываем Content-Length, чтобы прочитать body
                final var contentLength = extractHeader(headers, "Content-Length");
                if (contentLength.isPresent()) {
                    final var length = Integer.parseInt(contentLength.get());
                    final var bodyBytes = in.readNBytes(length);

                    body = new String(bodyBytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return body;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    public List<String> getHeaders(byte[] buffer, int read, BufferedInputStream in) {
        // ищем заголовки
        List<String> headers = null;
        final int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);

        final var headersStart = requestLineEnd + requestLineDelimiter.length;
        final var headersEnd = indexOf(buffer, headersDelimiter, headersStart, read);
        if (headersEnd == -1) {
            return null;
        }

        try {
            // отматываем на начало буфера
            in.reset();
            // пропускаем requestLine
            in.skip(headersStart);

            var headersBytes = in.readNBytes(headersEnd - headersStart);
            headers = Arrays.asList(new String(headersBytes).split("\r\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return headers;
    }

    public String[] getRequestLine(byte[] buffer, int read) {
        // ищем request line
        final int requestLineEnd = indexOf(buffer, requestLineDelimiter, 0, read);
        if (requestLineEnd == -1) {
            return null;
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != 3) {
            return null;
        }
        return requestLine;
    }

    // from google guava with modifications
    private static int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }

    public static void badRequest(BufferedOutputStream out) {
        try {
            out.write((
                    "HTTP/1.1 400 Bad Request\r\n" +
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
