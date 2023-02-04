package ru.netology;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        final var port = 9999;

        Server server = new Server();
        server.start(port);
        //server.end();
    }
}


