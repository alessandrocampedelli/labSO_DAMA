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
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
                    while (!socket.isClosed() && (response = in.readLine()) != null)
                    {
                        //caso in cui il server avvisa che si sta disconnettendo
                        if(response.equals("#close")){
                            System.out.println("Il server si è disconnesso. Riprova a connetterti successivamente...");
                            break;
                            //caso in cui il server avvisa che ha avviato una sessione interattiva
                        }
                        else if (response.equals("#session_start"))
                        {
                            System.out.println("ATTENZIONE: è stata avviata una sessione interattiva sul topic da parte del server\n");
                            //caso in cui il server avvisa che ha terminato la sessione interattiva
                        }
                        else if (response.equals("#session_end"))
                        {
                            System.out.println("ATTENZIONE: il server ha interrotto la sessione interattiva sul topic\n");
                        }
                        else
                        {
                            //caso di default quando il server inoltra un messaggio inviato sul topic ai subscriber di quel topic
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
                    if (!socket.isClosed())
                    {
                        System.out.println("Errore durante la lettura dal server: " + e.getMessage());
                    }
                }
                finally
                {
                    //chiusura delle istanze del client
                    System.exit(0);
                }
            });
            //avvio il thread
            listenerThread.start();
            String userInput;

            while ((userInput = stdIn.readLine()) != null)
            {
                out.println(userInput);
                /*
                TODO:
                    Il problema relativo al comando quit che eseguito durante la fase di inspect si verifica qui. Infatti, come per tutti gli altri comandi, anche il comando quit
                    viene inserito correttamente in coda alla lista dei messsagi "inspectati" di un client ma a causa dell'esecuzione dell'if sottostante l'istanza del client viene arrestata.
                    Per risolvere il problema ci sono due strade:
                    1) Cercare di aggiungere una condizione simile a quella che vedete commentata: l'obiettivo è quello di far eseguire l'if solo quando entrambe le condizioni siano vere ovvero
                        il comando scritto è "quit" e non è attiva la fase di inspect. Tuttavia questa soluzione l'ho già sperimentata e al momento non funziona. Il metodo getIsInIspection della
                        classe ClientHandler, per poter essere visibile nella classe Client, deve essere static; ciò comporta che anche l'attributo client della classe ClientHandler debba esssere
                        static e questo poi comporta dei problemi nell'esecuzione.
                     2) Far eseguire sempre l'if sottostante quando viene scritto "quit", in questo caso però prima di eventualmente interrompere l'esecuzione del client inoltra una richiesta al server per informarlo sullo stato
                        della sessione interattiva. Il client attenderà la risposta del server e a seconda di cosa risponde il client verrà interrotto oppure no.

                */

                if ("quit".equalsIgnoreCase(userInput) /*&& !ClientHandler.getIsInIspection()*/)
                {
                    //il programma esce dal ciclo se l'utente inserisce il comando 'quit'
                    break;
                }
            }
            // Chiudi il socket e interrompi il thread listener
            socket.close();
            listenerThread.join(); // Aspetta che il thread finisca
        }
        catch (IOException | InterruptedException e)
        {
            System.out.println("Impossibile connettersi al server " + serverIp + ":" + serverPort);
        }
    }
}