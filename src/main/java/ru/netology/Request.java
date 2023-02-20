package ru.netology;

import java.io.BufferedReader;
import java.io.IOException;

public class Request {
    private static String method;
    private String path;
    private String body;

    public Request(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public static Request parse(BufferedReader in) {
        // read only request line for simplicity
        // must be in form GET /path HTTP/1.1
        String requestLine;
        String method = null, path = null;
        try {
            requestLine = in.readLine();
            var parts = requestLine.split(" ");
            if (parts.length != 3)
                return null;
            method = parts[0];
            path = parts[1];

        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Request(method, path);
    }

}
