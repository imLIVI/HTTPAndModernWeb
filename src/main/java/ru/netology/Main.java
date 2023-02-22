package ru.netology;

import java.io.BufferedOutputStream;

public class Main {
    public static final int PORT = 9999;

    public static void main(String[] args) {

        Server server = new Server();

        // добавление хендлеров (обработчиков)
        server.addHandler("GET", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
                server.badRequest(responseStream);
            }
        });
        server.addHandler("POST", "/messages", new Handler() {
            public void handle(Request request, BufferedOutputStream responseStream) {
                // TODO: handlers code
                server.badRequest(responseStream);
            }
        });

        server.start(PORT);
    }
}


