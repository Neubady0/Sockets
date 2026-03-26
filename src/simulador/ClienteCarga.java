package simulador;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ClienteCarga {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 8082; // Mismo puerto para ApacheServer o NginxServer
    private static final int NUM_CLIENTS = 500;

    public static void main(String[] args) {
        System.out.println("Iniciando prueba de carga con " + NUM_CLIENTS + " conexiones concurrentes...");
        ExecutorService pool = Executors.newFixedThreadPool(NUM_CLIENTS);
        
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < NUM_CLIENTS; i++) {
            pool.execute(() -> {
                try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    
                    // Simulamos que el cliente tarda en enviar la petición (slow loris)
                    Thread.sleep(50); 
                    out.println("GET / HTTP/1.1");
                    
                    String response = in.readLine();
                    // System.out.println(response); // Opcional
                    
                } catch (Exception e) {
                    // System.err.println("Error cliente: " + e.getMessage());
                }
            });
        }
        
        pool.shutdown();
        try {
            pool.awaitTermination(3, TimeUnit.MINUTES);
            long endTime = System.currentTimeMillis();
            System.out.println("Prueba masiva terminada. Tiempo total: " + (endTime - startTime) + " ms.");
        } catch (InterruptedException e) {
            System.err.println("Prueba interrumpida");
        }
    }
}
