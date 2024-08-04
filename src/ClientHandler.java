import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private User client;
    private List<Socket> listClientSocket;
    private volatile boolean running = true; // Variabile per controllare il ciclo di esecuzione

    public ClientHandler(Socket socket, List<Socket> listClientSocket) {
        this.clientSocket = socket;
        this.listClientSocket = listClientSocket;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String inputLine = in.readLine();
                if (inputLine.startsWith("publish ")) {
                    client = new Publisher(clientSocket);
                    client.registerOutputAndInput();
                    client.handleCommand(inputLine);

                } else if (inputLine.startsWith("subscribe ")) {
                    client = new Subscriber(clientSocket);
                    client.registerOutputAndInput();
                    client.handleCommand(inputLine);

                } else {
                    out.println("Devi prima registrarti come publisher o subscriber.");
                    stopClient(); // Utilizza il metodo per fermare il client
                    return;
                }

                // Ciclo per gestire ulteriori comandi
                while (running && (inputLine = in.readLine()) != null) {
                    client.handleCommand(inputLine);
                    if (inputLine.equals("quit")) {
                        break;
                    }
                }
        } catch (SocketException e) { //quando faccio quit il buffer genera un eccezione in quanto la socket è ststa chiusa
            System.err.println("SocketException: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            stopClient(); // Chiude il socket e rimuove il client dalla lista
        }
    }

    // Metodo per fermare il client
    public void stopClient() {
        running = false; // Interrompe il ciclo di esecuzione
        try {
            synchronized (listClientSocket) {
                listClientSocket.remove(clientSocket);
            }
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
