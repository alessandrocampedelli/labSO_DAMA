import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String serverIp = "127.0.0.1";
    private static final int serverPort = 9000;
    public static void main(String[] args)
    {
        /*
        //l'utente avvia il client indicando l'indirizzo IP e il numero di porta del Server
        if (args.length != 2)
        {
            System.out.println("ERRORE: devi utilizzare la seguente sintassi: --> java Client <indirizzo_server> <porta_server>");
            return;
        }
        */
        Flag flag = new Flag(true);
        //i dati appena inseriti dall'utente
        try {
            /*
            String serverIp = args[0];
            int serverPort = Integer.parseInt(args[1]);
            */
            // Apertura della connessione con il server e gestione delle risorse con try-with-resources
            try (Socket socket = new Socket(serverIp, serverPort);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 Scanner stdIn = new Scanner(System.in)) {

                System.out.println("\nBENVENUTO! Ti sei connesso con successo al server avente indirizzo IP " + serverIp + " e porta " + serverPort);
                System.out.println("\nEcco i comandi da utilizzare in fase di registrazione:\n- show: \t\t\t\t\t\t  comando per vedere i topic già creati\n" +
                        "- publish/subscribe <nome_topic>: comando per registrarti al server come publisher/subscriber\n");

                Thread listenerThread = startListenerThread(socket, in, flag);
                String userInput;

                // Ciclo per leggere l'input dell'utente da tastiera e inviarlo al server
                while ((userInput = stdIn.nextLine()) != null) {
                    userInput = userInput.trim().toLowerCase();
                    out.println(userInput);
                    if (userInput.equals("quit")) {

                        listenerThread.join();

                        if (!flag.getFlag()) {
                            break;
                        }
                        if (!listenerThread.isAlive()) {
                            listenerThread = startListenerThread(socket, in, flag);
                        }
                    }
                }
            }

        } catch (NumberFormatException e) {
            System.out.println("ERRORE: numero di porta non corretto");
        } catch (IOException e) {
            System.out.println("Impossibile connettersi al server " + args[0] + ":" + args[1]);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    public static Thread startListenerThread(Socket socket, BufferedReader in, Flag flag)
    {
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
                    if (response.equals("#closeServer"))
                    {
                        System.out.println("Il server si è disconnesso. Riprova a connetterti successivamente...");
                        flag.setFlag(false);
                        System.exit(0);
                        break;
                    }
                    //caso in cui il client avvisa che si sta disconnettendo
                    else if (response.equals("#closeClient"))
                    {
                        flag.setFlag(false);
                        System.out.println("Disconnesione in corso...");
                    }
                    //caso in cui il è avviata la fase di ispezione da parte del server
                    else if (response.equals("#inspect"))
                    {
                        flag.setFlag(true);
                        break;
                    }
                    //caso in cui viene avvisato l'utente con l'ID del messaggio eliminato dal server
                    else if (response.startsWith("#delete"))
                    {
                        String id = response.substring(7);
                        System.out.println("Il server ha eliminato il messaggio con id: "+id+"\n");
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
            catch (Exception e)
            {
                //gestione delle eccezioni in caso di disconnessione del server
                if (!socket.isClosed())
                {
                    System.out.println("Il server si è disconnesso. Riprova a connetterti successivamente...");
                }
            }
        });
        //avvio del thread
        listenerThread.start();
        return listenerThread;
    }
    private static class Flag{
        private volatile Boolean flag;

        public Flag(Boolean flag){
            this.flag = flag;
        }

        public Boolean getFlag(){
            return this.flag;
        }
        public void setFlag(Boolean val){
            this.flag = val;
        }
    }
}
