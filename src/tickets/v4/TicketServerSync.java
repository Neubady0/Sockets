package tickets.v4;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class TicketServerSync {
    private static final int PORT = 8081;
    private static int ticketCounter = 1;

    public static void main(String[] args) {
        System.out.println("Servidor de Tickets (Synchronized) iniciado en el puerto " + PORT);
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

    // El uso de la palabra reservada synchronized provee un cerrojo a nivel del método estático,
    // garantizando la exclusión mutua en este bloque de código crítico y solucionando la condición de carrera
    private static synchronized int getNextTicket() {
        int currentTicket = ticketCounter;
        try {
            // Este retraso ya no causará problemas de colisión porque ningún otro hilo puede entrar aquí a la vez.
            Thread.sleep(10);
        } catch (InterruptedException e) {}
        ticketCounter++;
        return currentTicket;
    }

    private static void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            String message = in.readLine();
            if ("Dame ticket".equalsIgnoreCase(message)) {
                
                int myTicket = getNextTicket();
                
                out.println("Tu ticket es: " + myTicket);
                System.out.println("Hilo [" + Thread.currentThread().getName() + "] repartió el ticket sincronizado: " + myTicket);
                
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
