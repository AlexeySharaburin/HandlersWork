package ru.netology;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class Server {

    private final int portNumber;
    private final Map<String, Map<String, Handler>> handlers;



//    private final Handler notFoundHandler = (request, out) -> {
//        try {
//            out.write((
//                    "HTTP/1.1 404 Not Found\r\n" +
//                            "Content-Length: 0\r\n" +
//                            "Connection: close\r\n" +
//                            "\r\n"
//            ).getBytes());
//            out.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    };

    public Server(int portNumber) {
        this.portNumber = portNumber;
        handlers = new ConcurrentHashMap<>();
    }

    public void connectServer() throws IOException {
        final var serverSocket = new ServerSocket(portNumber);
        final var threadPool = Executors.newFixedThreadPool(64);
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

        System.out.println("Сервер начал работать!");

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    final var serverThread = new ServerThread(socket, validPaths, handlers);
                    threadPool.submit(serverThread);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        threadPool.shutdown();
    }

    public void addHandler(String method, String path, Handler handler) {
        Optional.ofNullable(handlers.get(method))
                .ifPresentOrElse(pathHandlerMap -> pathHandlerMap.put(path, handler),
                        () -> handlers.put(method, new ConcurrentHashMap<>(Map.of(path, handler))));
    }

}

