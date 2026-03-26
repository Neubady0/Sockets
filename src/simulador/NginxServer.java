package simulador;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class NginxServer {
    private static final int PORT = 8082;
    private static int activeConnections = 0;

    public static void main(String[] args) {
        System.out.println("Simulador de Nginx (Modelo No-Bloqueante Event-Driven) iniciado en puerto " + PORT);

        try (Selector selector = Selector.open();
             ServerSocketChannel serverSocket = ServerSocketChannel.open()) {

            serverSocket.bind(new InetSocketAddress(PORT));
            serverSocket.configureBlocking(false);
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                // EVENT LOOP: Equivalente a epoll_wait(...)
                // El único hilo del servidor duerme sólo aquí hasta que OCURRA algún evento (ej: llegan datos a algún canal)
                selector.select();
                
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();

                while (iter.hasNext()) {
                    SelectionKey key = iter.next();

                    if (key.isAcceptable()) {
                        // Evento: Alguien se ha conectado
                        SocketChannel client = serverSocket.accept();
                        client.configureBlocking(false); 
                        // Configuramos el canal como NO bloqueante. 
                        // Registramos este canal para que el SO nos avise si hay datos listos para OP_READ
                        client.register(selector, SelectionKey.OP_READ);
                        
                        activeConnections++;
                        if (activeConnections % 100 == 0 || activeConnections == 500) {
                            System.out.println("[Nginx] Total sockets multiplexados (un solo hilo): " + activeConnections);
                        }
                    }

                    if (key.isReadable()) {
                        // Evento: Hay datos para leer en el socket sin quedarse atascado "esperando"
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(256);
                        
                        try {
                            int bytesRead = client.read(buffer);
                            
                            if (bytesRead > 0) {
                                // Se processan los datos simulando respuesta HTTP
                                ByteBuffer outBuffer = ByteBuffer.wrap("HTTP/1.1 200 OK\r\n\r\nHola desde Nginx Simulado\r\n".getBytes());
                                client.write(outBuffer);
                                client.close();
                                activeConnections--;
                            } else if (bytesRead == -1) {
                                // Cliente cerró la conexión
                                client.close();
                                activeConnections--;
                            }
                        } catch (IOException e) {
                            client.close();
                            activeConnections--;
                        }
                    }
                    iter.remove();
                }
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor Nginx (NIO): " + e.getMessage());
        }
    }
}
