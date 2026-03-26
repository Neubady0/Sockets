package webserver;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class MiniApache {
    private static Config config;
    
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("        Arrancando MiniApache V1.0       ");
        System.out.println("=========================================");
        
        config = new Config("config/server.config");
        
        // Usamos un Pool de Threads equivalente a la directiva MaxRequestWorkers / ThreadsPerChild de Apache
        ExecutorService pool = Executors.newFixedThreadPool(150);
        
        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            System.out.println("MiniApache en ejecución. Escuchando en el puerto: " + config.getPort());
            System.out.println("Directorio Raíz: " + config.getDocumentRoot());
            System.out.println("Esperando conexiones TCP...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new HttpHandler(clientSocket, config));
            }
        } catch (IOException e) {
            System.err.println("MiniApache ha sufrido un fallo crítico al intentar bindear el puerto " + config.getPort() + ": " + e.getMessage());
        } finally {
            pool.shutdown();
        }
    }
}
