import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final String DEFAULT_SERVER_IP = "127.0.0.1"; // IP predefinito
    private static final int DEFAULT_SERVER_PORT = 9000; // Porta predefinita
    public static void main(String[] args) {
        /* //per inserire la porta manualmente da terminale
        if (args.length != 2) {
            System.out.println("Utilizzo: java Client <indirizzo_server> <porta_server>");
            return;
        }
        */
        String serverIp = DEFAULT_SERVER_IP;
        int serverPort = DEFAULT_SERVER_PORT;

        //creo la socket per connettersi al server
        try (Socket socket = new Socket(serverIp, serverPort);
             //oggetto per inviare dati al server
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             //oggetto per leggere i dati dal server
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             //oggetto per leggere l'input dell'utente dalla console
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connesso al server " + serverIp + ":" + serverPort);
            String userInput;
            //TODO: USARE SEMAFORI PER EVITARE CHE NON VENGA COMPLETATA UNA QUALSIASI STAMPA
            System.out.println("Inserisci publish o subscribe poi il nome del topic su cui vuoi operare");
            while ((userInput = stdIn.readLine()) != null) {
                //invio messaggio al server
                out.println(userInput);
                String response;

                //ciclo for per leggere la risposta del server.
                while ((response = in.readLine()) != null) {
                    System.out.println(response);

                    //il ciclo si interrompe quando non ci sono più dati pronti per essere letti
                    if (!in.ready()) {
                        break;
                    }
                }
                //se il comando è 'quit' mi disconnetto
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Impossibile connettersi al server " + serverIp + ":" + serverPort);
        }
    }
}