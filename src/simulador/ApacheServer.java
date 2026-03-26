package simulador;

import java.io.*;
import java.net.*;

public class ApacheServer {
    private static final int PORT = 8082;
    private static int activeConnections = 0;

    public static void main(String[] args) {
        System.out.println("Simulador de Apache (Modelo Bloqueante de Hilos) iniciado en puerto " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Cada conexión lanza un hilo, bloqueando la memoria y un hilo de SO.
                new Thread(() -> handleConnection(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error del servidor Apache: " + e.getMessage());
        }
    }

    private static void handleConnection(Socket clientSocket) {
        synchronized (ApacheServer.class) {
            activeConnections++;
            if (activeConnections % 100 == 0 || activeConnections == 500) {
                System.out.println("[Apache] Hilos bloqueados concurrentemente: " + activeConnections);
            }
        }
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            // OPERACIÓN BLOQUEANTE:
            // Apache tradicional usa I/O bloqueante. Si el cliente tarda en enviar 'GET /',
            // el hilo entero asociado se queda bloqueado durmiendo y desperdiciando RAM.
            String request = in.readLine();
            
            if (request != null) {
                // Procesar (Ej: leer de disco)
                try { Thread.sleep(10); } catch (Exception e) {} 
                out.println("HTTP/1.1 200 OK");
                out.println("");
                out.println("Hola desde Apache Simulado");
            }
        } catch (Exception e) {
        } finally {
            synchronized (ApacheServer.class) {
                activeConnections--;
            }
            try { clientSocket.close(); } catch (IOException e) { }
        }
    }
}
