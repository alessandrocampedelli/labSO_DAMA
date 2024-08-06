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
    private List<Socket> listClientSocket;
    private volatile boolean running = true; // Variabile per controllare il ciclo di esecuzione
    private Scanner in;
    private PrintStream out;

    public ClientHandler(Socket socket, List<Socket> listClientSocket) {
        this.clientSocket = socket;
        this.listClientSocket = listClientSocket;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            in = new Scanner(inputStream);
            out = new PrintStream(outputStream, true);

            String inputLine = in.nextLine();

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
            while (running && in.hasNextLine()) {
                inputLine = in.nextLine();
                if (!client.getTopic().isInInspection()) {
                    client.handleCommand(inputLine);
                }else {
                    client.handleCommand("inspect");    //quando il topic è in fase di ispezione dal server
                }
                if (inputLine.equals("quit")) {
                    System.out.println("Client disconnesso");
                    break;
                }
            }

        } catch (SocketException e) { //quando faccio quit il buffer genera un eccezione in quanto la socket è stata chiusa
            System.err.println("SocketException: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
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
