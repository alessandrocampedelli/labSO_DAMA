import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client
{
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 9000;

    /* //per inserire la porta manualmente da terminale
            if (args.length != 2) {
                System.out.println("Utilizzo: java Client <indirizzo_server> <porta_server>");
                return;
            }
    */

    public static void main(String[] args)
    {
        String serverIp = DEFAULT_SERVER_IP;
        int serverPort = DEFAULT_SERVER_PORT;

        try (Socket socket = new Socket(serverIp, serverPort);
             //oggetto di tipo "PrintWriter" per inviare i dati al server
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             //oggetto di tipo "BufferedReader" per ricevere dati dal server
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             //oggetto di tipo "BufferedReader" per leggere input dell'utente da tastiera
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("\nBENVENUTO! Ti sei connesso con successo al server avente indirizzo IP " + serverIp + " e porta " + serverPort);
            System.out.println("\nEcco i comandi da utilizzare in fase di registrazione:\n- show: \t\t\t\t\t\t  comando per vedere i topic già creati\n" +
                    "- publish/subscribe <nome_topic>: comando per registrarti al server come publisher/subscriber\n");

            //thread per gestire la ricezione dei messaggi dal server
            Thread listenerThread = new Thread(() ->
            {
                try
                {
                    String response;
                    //ciclo per continuare a ricevere messaggi finché la socket non è chiusa
                    while (!socket.isClosed() && (response = in.readLine()) != null)
                    {
                        //caso in cui il server avvisa che si sta disconnettendo
                        if(response.equals("#close")){
                            System.out.println("Il server si è disconnesso. Riprova a connetterti successivamente...");
                            break;
                        }
                        //caso in cui il server avvisa che ha avviato una sessione interattiva
                        else if (response.equals("#session_start"))
                        {
                            System.out.println("ATTENZIONE: è stata avviata una sessione interattiva sul topic da parte del server\n");
                        }
                        //caso in cui il server avvisa che ha terminato la sessione interattiva
                        else if (response.equals("#session_end"))
                        {
                            System.out.println("ATTENZIONE: il server ha interrotto la sessione interattiva sul topic\n");
                        }
                        //caso di default quando il server inoltra un messaggio inviato sul topic ai subscriber di quel topic
                        else
                        {
                            System.out.println(response);
                            if(!in.ready())
                            {
                                System.out.println();
                            }
                        }
                    }
                }
                catch (IOException e)
                {
                    //gestione delle eccezioni in caso di disconnessione del server
                    if (!socket.isClosed())
                    {
                        System.out.println("Il server si è disconnesso. Riprova a connetterti successivamente...");
                    }
                }
                finally
                {
                    //uscita dall'applicazione quando il thread termina
                    System.exit(0);
                }
            });
            //avvio del thread
            listenerThread.start();
            String userInput;
            //ciclo per leggere l'input dell'utente da tastiera e inviarlo al server
            while ((userInput = stdIn.readLine()) != null)
            {
                out.println(userInput);
            }
            //aspetta che il thread finisca
            listenerThread.join();
        }
        catch (IOException | InterruptedException e)
        {
            System.out.println("Impossibile connettersi al server " + serverIp + ":" + serverPort);
        }
    }
}