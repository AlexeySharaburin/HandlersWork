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

//    public void processingRequest() {
//
//        while (true) {
//            try {
//                final var requestLine = in.readLine();
//                final var parts = requestLine.split(" ");
//
//                if (parts.length != 3) {
//                    continue;
//                }
//
//                final var path = parts[1];
//                if (!validPaths.contains(path)) {
//                    out.write((
//                            "HTTP/1.1 404 Not Found\r\n" +
//                                    "Content-Length: 0\r\n" +
//                                    "Connection: close\r\n" +
//                                    "\r\n"
//                    ).getBytes());
//                    out.flush();
//                    continue;
//                }
//
//                final var filePath = Paths.get(".", "public", path);
//                final var mimeType = Files.probeContentType(filePath);
//
//                // special case for classic
//                if (path.equals("/classic.html")) {
//                    final var template = Files.readString(filePath);
//                    final var content = template.replace(
//                            "{time}",
//                            LocalDateTime.now().toString()
//                    ).getBytes();
//                    out.write((
//                            "HTTP/1.1 200 OK\r\n" +
//                                    "Content-Type: " + mimeType + "\r\n" +
//                                    "Content-Length: " + content.length + "\r\n" +
//                                    "Connection: close\r\n" +
//                                    "\r\n"
//                    ).getBytes());
//                    out.write(content);
//                    out.flush();
//                    return;
//                }
//
//                final var lenght = Files.size(filePath);
//                out.write((
//                        "HTTP/1.1 200 OK\r\n" +
//                                "Content-Type: " + mimeType + "\r\n" +
//                                "Content-Length: " + lenght + "\r\n" +
//                                "Connection: close\r\n" +
//                                "\r\n"
//                ).getBytes());
//                Files.copy(filePath, out);
//                out.flush();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }

