package webserver;

import java.io.*;
import java.net.*;
import java.nio.file.*;

public class HttpHandler implements Runnable {
    private final Socket socket;
    private final Config config;

    public HttpHandler(Socket socket, Config config) {
        this.socket = socket;
        this.config = config;
    }

    @Override
    public void run() {
        try (InputStream input = socket.getInputStream();
             OutputStream output = socket.getOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {

            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isEmpty()) return;

            // Simple "log" de consola parecido a apache access.log
            System.out.println("[" + socket.getInetAddress() + "] " + requestLine);
            
            String[] parts = requestLine.split(" ");
            if (parts.length >= 2 && parts[0].equals("GET")) {
                String requestedPath = parts[1];
                if (requestedPath.equals("/")) {
                    requestedPath = "/" + config.getDefaultFile();
                }
                // Evitamos Directory Traversal básico (solo a nivel de prueba)
                requestedPath = requestedPath.replace("..", "");
                
                serveFile(output, requestedPath);
            } else {
                sendResponse(output, 501, "Not Implemented", "Sólo GET es soportado.");
            }

        } catch (IOException e) {
            System.err.println("Error procesando HTTP: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException e) {}
        }
    }

    private void serveFile(OutputStream output, String requestedPath) throws IOException {
        File file = new File(config.getDocumentRoot(), requestedPath);
        if (file.exists() && !file.isDirectory()) {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            sendResponseHeaders(output, 200, "OK", getContentType(requestedPath), fileContent.length);
            output.write(fileContent);
        } else {
            sendResponse(output, 404, "Not Found", "<h1>Error 404</h1><p>Archivo " + requestedPath + " no encontrado o sin permisos.</p>");
        }
        output.flush();
    }

    private void sendResponse(OutputStream out, int statusCode, String statusText, String body) throws IOException {
        byte[] bodyBytes = body.getBytes("UTF-8");
        sendResponseHeaders(out, statusCode, statusText, "text/html; charset=UTF-8", bodyBytes.length);
        out.write(bodyBytes);
        out.flush();
    }

    private void sendResponseHeaders(OutputStream out, int statusCode, String statusText, String contentType, int contentLength) throws IOException {
        PrintWriter writer = new PrintWriter(out, false);
        writer.printf("HTTP/1.1 %d %s\r\n", statusCode, statusText);
        writer.printf("Server: MiniApache/1.0\r\n");
        writer.printf("Content-Type: %s\r\n", contentType);
        writer.printf("Content-Length: %d\r\n", contentLength);
        writer.printf("Connection: close\r\n");
        writer.printf("\r\n");
        writer.flush();
    }

    private String getContentType(String path) {
        if (path.endsWith(".html") || path.endsWith(".htm")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        return "text/plain";
    }
}
