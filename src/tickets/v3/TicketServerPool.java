package tickets.v3;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class TicketServerPool {
    private static final int PORT = 8081;
    private static final int MAX_THREADS = 10;
    
    // Sigue existiendo la condición de carrera
    private static int ticketCounter = 1;

    public static void main(String[] args) {
        System.out.println("Servidor de Tickets (Pool de Hilos, límite " + MAX_THREADS + ") iniciado en puerto " + PORT);
        
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Error de servidor: " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String message = in.readLine();
            if ("Dame ticket".equalsIgnoreCase(message)) {
                
                int myTicket = ticketCounter;
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
                ticketCounter = myTicket + 1;
                
                out.println("Tu ticket es: " + myTicket);
                System.out.println("Pool [" + Thread.currentThread().getName() + "] entregó ticket: " + myTicket);
                
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
