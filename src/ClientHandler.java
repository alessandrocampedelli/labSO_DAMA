import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.Scanner;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private User client;
    private List<User> listClient;
    private volatile boolean running = true;
    private Scanner in;
    private PrintStream out;

    public ClientHandler(Socket socket, List<User> listClient) {
        this.clientSocket = socket;
        this.listClient = listClient;
    }

    @Override
    public void run() {
        try {
            // Configurazione input/output
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            in = new Scanner(inputStream);
            out = new PrintStream(outputStream, true);

            // Verifica se l'utente è già registrato
            if (client == null) {
                String inputLine = in.nextLine();
                if (inputLine.startsWith("publish ")) {
                    client = new Publisher(clientSocket);
                } else if (inputLine.startsWith("subscribe ")) {
                    client = new Subscriber(clientSocket);
                } else {
                    out.println("Devi prima registrarti come publisher o subscriber.");
                    stopClient();
                    return;
                }
                client.registerOutputAndInput();
                client.handleCommand(inputLine);
                System.out.println("nuovo user: "+System.identityHashCode(client));
            }

            // Ciclo per gestire ulteriori comandi
            while (running && in.hasNextLine()) {
                String inputLine = in.nextLine();
                if (!client.getTopic().isInInspection()) {
                    client.handleCommand(inputLine);
                } else {
                    client.handleCommand("inspect");
                    client.addInspectMessage(inputLine);
                }
                if (inputLine.equals("quit")) {
                    System.out.println("Client disconnesso");
                    break;
                }
            }

        } catch (SocketException e) {
            System.err.println("SocketException: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            stopClient();
        }
    }

    public void stopClient() {
        running = false;
        try {
            synchronized (listClient) {
                listClient.remove(clientSocket);
            }
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
