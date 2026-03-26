package tickets.v5;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class TicketServerAtomic {
    private static final int PORT = 8081;
    
    // El uso de AtomicInteger previene la condición de carrera asegurando que la operación 
    // de incremento es atómica por hardware (usando Compare-And-Swap),
    // sin necesidad de bloqueos o cerrojos, ofreciendo así mejor rendimiento en alta concurrencia.
    private static final AtomicInteger ticketCounter = new AtomicInteger(1);

    public static void main(String[] args) {
        System.out.println("Servidor de Tickets (AtomicInteger) iniciado en el puerto " + PORT);
        ExecutorService pool = Executors.newFixedThreadPool(10);
        
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
                
                int myTicket = ticketCounter.getAndIncrement();
                
                try {
                    // Aunque haya retraso y solapamiento, getAndIncrement ya fue atómico. 
                    // No habrá colisión en la entrega de números.
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
                
                out.println("Tu ticket es: " + myTicket);
                System.out.println("Hilo [" + Thread.currentThread().getName() + "] repartió ticket atómico: " + myTicket);
                
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
