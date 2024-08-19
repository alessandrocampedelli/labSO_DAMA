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
                //se l'utente non è già registrato, esso si deve registrare per forza prima di proseguire
                while(client == null){
                    String inputLine = in.nextLine();
                    if (inputLine.startsWith("publish ")) {
                        String topicName = inputLine.substring(8).trim();
                        if(!topicName.isEmpty()){
                            client = new Publisher(clientSocket,Server.getOrCreateTopic(topicName));
                            client.registerOutputAndInput();
                            client.handleCommand(inputLine);
                            synchronized (listClient) {
                                listClient.add(client);
                            }
                            // Stampa il messaggio sulla console del server
                            System.out.println("Un nuovo client si è connesso come PUBLISHER al topic " + topicName.toUpperCase());
                        }else{
                            out.println("Errore: il topic non è specificato. Riprova");
                        }
                    } else if (inputLine.startsWith("subscribe ")) {
                        String topicName = inputLine.substring(10).trim();
                        if(!topicName.isEmpty()){
                            client = new Subscriber(clientSocket,Server.getOrCreateTopic(topicName));
                            client.registerOutputAndInput();
                            client.handleCommand(inputLine);
                            synchronized (listClient) {
                                listClient.add(client);
                            }
                            // Stampa il messaggio sulla console del server
                            System.out.println("Un nuovo client si è connesso come SUBSCRIBER al topic " + topicName.toUpperCase());
                        }else{
                            out.println("Errore: il topic non è specificato. Riprova");
                        }
                    } else {
                        out.println("Devi prima registrarti come publisher o subscriber.");
                    }
                }
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
                listClient.removeIf(user -> user.getClientSocket().equals(clientSocket));
            }
            if (!clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
