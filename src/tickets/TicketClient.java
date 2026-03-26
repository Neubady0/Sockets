package tickets;

import java.io.*;
import java.net.*;

public class TicketClient implements Runnable {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 8081;
    private final int id;

    public TicketClient(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println("Dame ticket");
            String response = in.readLine();
            System.out.println("Cliente " + id + " recibió respuesta: " + response);

        } catch (IOException e) {
            System.err.println("Cliente " + id + " error de conexión: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("Iniciando simulación de múltiples clientes pidiendo tickets...");
        // Creamos una cantidad alta de hilos simultáneos para provocar condición de carrera
        for (int i = 1; i <= 200; i++) {
            new Thread(new TicketClient(i)).start();
        }
    }
}
