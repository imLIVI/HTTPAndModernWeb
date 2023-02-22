package ru.netology;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;

public class Request {
    private static String method;
    private String path;
    private List<String> headers;
    private String body;

    // for POST request
    public Request(String method, String path, List<String> headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    // for GET request
    public Request(String method, String path, List<String> headers) {
        this.method = method;
        this.path = path;
        this.headers = headers;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public void getQueryParam(String name) {

    }

    public void getQueryParams() {

    }
}
