import java.io.*;
import java.net.*;
import java.util.*;

public class MultiClientServer {
    private static final int PORT = 12345; // Server port
    private static Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) {
        System.out.println("Server started...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accept new client
                System.out.println("New client connected: " + clientSocket);
                
                // Create a new thread for each client
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                out.println("Welcome to the server!");

                String message = in.readLine();
                {
                    System.out.println("Client: " + message);
                    
                    // Broadcast message to all clients
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println("Server: " + message);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
            }
        }
    }
}
