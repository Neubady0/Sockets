package tickets.v2;

import java.io.*;
import java.net.*;

public class TicketServerHilos {
    private static final int PORT = 8081;
    
    // Condición de carrera intencionada
    private static int ticketCounter = 1;

    public static void main(String[] args) {
        System.out.println("Servidor de Tickets con Hilos iniciado (Puerto " + PORT + ")");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Ahora no se bloquea, un telnet abierto no bloqueará los demás.
                // Sin embargo pueden darse problemas de condición de carrera con los tickets.
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Error de servidor: " + e.getMessage());
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String message = in.readLine();
            if ("Dame ticket".equalsIgnoreCase(message)) {
                
                int myTicket = ticketCounter;
                // Pequeño delay artificial para forzar el solapamiento de hilos (condición de carrera)
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
                
                ticketCounter = myTicket + 1;
                
                out.println("Tu ticket es: " + myTicket);
                System.out.println("Hilo [" + Thread.currentThread().getName() + "] entregó ticket: " + myTicket);
                
            } else {
                out.println("Comando inválido");
            }
        } catch (Exception e) {
            System.err.println("Error manejando cliente: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) { }
        }
    }
}
