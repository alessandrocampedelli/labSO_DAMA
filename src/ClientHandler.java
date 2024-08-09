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
    private volatile boolean running = true; // Variabile per controllare il ciclo di esecuzione
    private Scanner in;
    private PrintStream out;

    public ClientHandler(Socket socket, List<User> listClient) {
        this.clientSocket = socket;
        this.listClient = listClient;
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
                    /*
                     Il comando handleCommand dentro questo else è importantissimo, senza il metodo
                     compare un "errore" con la console che non legge più le stringhe inviate, generando un loop infinito che ci permette soltanto di
                     scrivere all'infinito senza risultati. Per evitare l'errore non bisogna per forza gestire inputLine ma basta che gestiamo una stringa.
                     Comunque il lavoro che dovete fare è quello di passare un inputline o un vettore per salvare tutte le stringhe inviate,
                     durante l'ispezione, al client così per poi eseguirle in un secondo momento quando l'ispezione del server sarà terminata.
                     Ovviamente bignerà trovare un modo nelle sottoclassi di user per distinguere i due tipi di handleCommand, uno per salvare tutte le stringhe
                     in attesa in un vettore per poi eseguirle in un secondo momento e l'altro per eseguire normalmente l'handleCommand delle stringhe non in attesa.
                     */
                    client.handleCommand("inspect");
                    //aggiungo il messaggio inviato alla lista
                    client.addInspectMessage(inputLine);
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
