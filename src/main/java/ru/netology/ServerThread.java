package ru.netology;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServerThread extends Thread {

    private final Socket socket;
    private final List<String> validPaths;
    private InputStream in;
    private BufferedOutputStream out;
    Map<String, Map<String, Handler>> handlers;

    private final Handler notFoundHandler = (request, out) -> {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public ServerThread(Socket socket, List<String> validPaths, Map<String, Map<String, Handler>> handlers) {
        this.socket = socket;
        this.validPaths = validPaths;
        this.handlers = handlers;
    }

    @Override
    public void run() {

        try {
            while (true) {
                in = socket.getInputStream();
                out = new BufferedOutputStream(socket.getOutputStream());
                processingRequest();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }


    public void processingRequest() {

        while (true) {
            try {
                var request = Request.fromInputStream(in);

                Optional.ofNullable(handlers.get(request.getMethod()))
                        .map(pathToHandlerMap -> pathToHandlerMap.get(request.getPath()))
                        .ifPresentOrElse(handler -> handler.handle(request, out),
                                () -> notFoundHandler.handle(request, out));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}



