package chat;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClient {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner sc = new Scanner(System.in)) {

            System.out.println("Conectado al servidor de Chat.");

            Thread readerThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                        if ("¡Hasta luego!".equals(serverMessage)) {
                            System.exit(0);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Desconectado del servidor.");
                }
            });
            readerThread.start();

            while (true) {
                String toSend = sc.nextLine();
                if (toSend != null && !toSend.trim().isEmpty()) {
                    out.println(toSend);
                    if ("salir".equalsIgnoreCase(toSend)) {
                        break;
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Error de conexión con el servidor: " + e.getMessage());
        }
    }
}
