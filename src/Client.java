import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client
{
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 9000;
    private static volatile Boolean flag = true;
    private static final Object lock = new Object(); // Oggetto di lock per sincronizzazione



    public static void main(String[] args)
    {
        String serverIp = DEFAULT_SERVER_IP;
        int serverPort = DEFAULT_SERVER_PORT;

        try (Socket socket = new Socket(serverIp, serverPort);
             // oggetto di tipo "PrintWriter" per inviare i dati al server
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             // oggetto di tipo "Scanner" per ricevere dati dal server
             Scanner in = new Scanner(new InputStreamReader(socket.getInputStream()));
             // oggetto di tipo "Scanner" per leggere input dell'utente da tastiera
             Scanner stdIn = new Scanner(System.in)) {

            System.out.println("\nBENVENUTO! Ti sei connesso con successo al server avente indirizzo IP " + serverIp + " e porta " + serverPort);
            System.out.println("\nEcco i comandi da utilizzare in fase di registrazione:\n- show: \t\t\t\t\t\t  comando per vedere i topic già creati\n" +
                    "- publish/subscribe <nome_topic>: comando per registrarti al server come publisher/subscriber\n");

            Thread listenerThread = startListenerThread(socket,in);
            String userInput;

            // ciclo per leggere l'input dell'utente da tastiera e inviarlo al server
            while ((userInput = stdIn.nextLine()) != null)
            {
                out.println(userInput);

                if (userInput.equals("quit")) {
                    listenerThread.join();
                    if (flag == false) {
                        break;
                    }
                    if(!listenerThread.isAlive()){
                        listenerThread = startListenerThread(socket,in);
                    }
                }
            }
        }
        catch (IOException e)
        {
            System.out.println("Impossibile connettersi al server " + serverIp + ":" + serverPort);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static Thread startListenerThread(Socket socket, Scanner in){
        // thread per gestire la ricezione dei messaggi dal server
        Thread listenerThread = new Thread(() ->
        {
            try
            {
                // ciclo per continuare a ricevere messaggi finché la socket non è chiusa
                while (!socket.isClosed() && in.hasNextLine())
                {
                    String response = in.nextLine();
                    // caso in cui il server avvisa che si sta disconnettendo
                    if (response.equals("#close")) {
                        System.out.println("Il server si è disconnesso. Riprova a connetterti successivamente...");
                        flag = false;
                        System.out.println("digita quit per terminare il programma");
                        break;
                    } else if (response.equals("#inspect")) {
                        flag = true;
                        break;
                    }
                    // caso in cui il server avvisa che ha avviato una sessione interattiva
                    else if (response.equals("#session_start")) {
                        System.out.println("ATTENZIONE: è stata avviata una sessione interattiva sul topic da parte del server\n");
                    }
                    // caso in cui il server avvisa che ha terminato la sessione interattiva
                    else if (response.equals("#session_end")) {
                        System.out.println("ATTENZIONE: il server ha interrotto la sessione interattiva sul topic\n");
                    }
                    // caso di default quando il server inoltra un messaggio inviato sul topic ai subscriber di quel topic
                    else {
                        System.out.println(response);
                    }
                }
            }
            catch (Exception e)
            {
                // gestione delle eccezioni in caso di disconnessione del server
                if (!socket.isClosed())
                {
                    System.out.println("Il server si è disconnesso. Riprova a connetterti successivamente...");
                }
            }
        });
        // avvio del thread
        listenerThread.start();
        return listenerThread;
    }
}
