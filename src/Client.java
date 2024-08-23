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

            System.out.println("Connesso al server " + serverIp + ":" + serverPort);

            //thread per gestire la ricezione dei messaggi dal server
            Thread listenerThread = new Thread(() ->
            {
                try
                {
                    String response;
                    while (!socket.isClosed() && (response = in.readLine()) != null)
                    {
                        //TODO: codice da completare da campe per chiusura corretta dei client
                        if(response.equals("close")){
                            System.out.println("Il server si è disconnesso. Riprova a connetterti successivamente...");
                        }else{
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
            });
            //avvio il thread
            listenerThread.start();
            String userInput;
            System.out.println("Inserisci publish o subscribe poi il nome del topic su cui vuoi operare");
            while ((userInput = stdIn.readLine()) != null)
            {
                out.println(userInput);
                if ("quit".equalsIgnoreCase(userInput))
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