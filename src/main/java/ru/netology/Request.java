package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

public class Request {
    private String method;
    private String path;
    private List<String> headers;
    private String body;
    private List<NameValuePair> queryParameters;

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

    public void getQueryParams() {
        try {
            URI uri = new URI(path);
            queryParameters = URLEncodedUtils.parse(uri, "UTF-8");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public List<String> getQueryParam(String name) {
        if (!queryParameters.isEmpty())
            return queryParameters.stream()
                    .filter(o -> o.getName().startsWith(name))
                    .map(NameValuePair::getValue)
                    .collect(Collectors.toList());
        else
            return null;
    }
}
