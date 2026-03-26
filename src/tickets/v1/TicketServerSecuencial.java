package tickets.v1;

import java.io.*;
import java.net.*;

public class TicketServerSecuencial {
    private static final int PORT = 8081;
    private static int ticketCounter = 1;

    public static void main(String[] args) {
        System.out.println("Servidor Secuencial de Tickets iniciado (Puerto " + PORT + ")");
        System.out.println("OJO: Si conectas con telnet y no envías nada, bloquearás a los demás.");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Acepta un cliente, y procesa su petición completamente antes de aceptar otro
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                    
                    System.out.println("Cliente conectado: " + clientSocket.getInetAddress() + ". Esperando comando...");
                    
                    // Bloqueante: si no lee, no avanza el programa
                    String message = in.readLine();
                    if ("Dame ticket".equalsIgnoreCase(message)) {
                        // Simulamos un leve retraso para visualizar condiciones de carrera si las hubiera más fácilmente
                        Thread.sleep(10);
                        out.println("Tu ticket es: " + ticketCounter);
                        System.out.println("Ticket " + ticketCounter + " entregado.");
                        ticketCounter++;
                    } else {
                        out.println("Comando inválido");
                    }
                } catch (Exception e) {
                    System.err.println("Error manejando cliente: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error de servidor: " + e.getMessage());
        }
    }
}
