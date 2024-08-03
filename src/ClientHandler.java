import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private User client;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

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
                clientSocket.close();
                return;
            }

            // Ciclo per gestire ulteriori comandi
            while ((inputLine = client.in.readLine()) != null) {
                client.handleCommand(inputLine);
                if (inputLine.equals("quit")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}