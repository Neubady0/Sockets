package chat;

import java.io.*;
import java.net.*;

public class ChatServer {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        System.out.println("Servidor de Chat iniciado en el puerto " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println("Bienvenido al Chat. Escribe 'salir' para desconectarte.");
                String mensaje;

                while ((mensaje = in.readLine()) != null) {
                    System.out.println("Mensaje recibido de " + socket.getInetAddress() + ": " + mensaje);
                    if ("salir".equalsIgnoreCase(mensaje)) {
                        out.println("¡Hasta luego!");
                        break;
                    }
                    out.println("Servidor echo: " + mensaje);
                }

                System.out.println("Cliente ordenadamente desconectado: " + socket.getInetAddress());

            } catch (IOException e) {
                System.err.println("Error manejando cliente: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error al cerrar socket del cliente: " + e.getMessage());
                }
            }
        }
    }
}
