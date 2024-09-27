import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;

public class Server
{
    public static final List<Topic> topics = new ArrayList<>();
    public static final List<ClientHandler> listClient = new ArrayList<>();
    public static final Object lock = new Object();

    public static void main(String[] args)
    {
        //l'utente avvia il Server indicando il numero di porta del Server
        if (args.length != 1)
        {
            System.out.println("Utilizzo: java Server <porta>");
            return;
        }

        //i dati appena inseriti dall'utente
        int port = Integer.parseInt(args[0]);
        try
        {
            //crea una socket del server sulla porta specificata
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server in ascolto sulla porta " + port);
            //gestisco contemporaneamente la console del server all'attesa di nuovi client
            new ServerHandler(serverSocket).start();
            //accetta connessioni dei client in un ciclo infinito
            while (!serverSocket.isClosed())
            {
                try
                {
                    //accetta una nuova connessione client
                    Socket clientSocket = serverSocket.accept();
                    //crea un nuovo handler per il client connesso
                    ClientHandler client = new ClientHandler(clientSocket, serverSocket);
                    //avvia il thread per gestire il client
                    client.start();
                    synchronized (listClient)
                    {
                        //aggiunge il client alla lista in modo sicuro da thread multipli
                        listClient.add(client);
                    }

                }
                catch (IOException e)
                {
                    //quando chiudo il server l'accept non riesce più ad eseguire in quanto la serversocket è stata chiusa
                    if (!serverSocket.isClosed())
                    {
                        //stampa un messaggio di errore se qualcosa va storto nell'accettare la connessione
                        System.err.println("Errore durante l'accettazione della connessione: " + e.getMessage());
                    }
                    break;
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //metodo che crea o recupera un topic esistente
    public static Topic getOrCreateTopic(String name)
    {
        synchronized (topics)
        {
            //cerco se il topic esiste già
            for (Topic topic : topics)
            {
                if (topic.getName().equals(name))
                {
                    //ritorna il topic esistente se presente
                    return topic;
                }
            }
            //crea un nuovo topic e lo aggiunge alla lista se non esiste
            Topic newTopic = new Topic(name);
            topics.add(newTopic);
            return newTopic;
        }
    }

    //metodo che mostra la lista dei topic attualmente disponibili
    public static void showTopics(PrintWriter out)
    {
        synchronized (topics)
        {
            //controlla se ci sono topic creati
            if (!topics.isEmpty())
            {
                out.println(topics.size() == 1 ? ("Un solo topic creato al momento: ") : (topics.size() + " topics creati al momento: ")); //mostra un messaggio diverso se c'è un solo topic o più di uno
                //stampo il nome di ciascun topic
                for (Topic topic : topics)
                {
                    out.println("- " + topic.getName());
                }
            }
            else
            {
                out.println("Non è presente alcun topic creato.");
            }
        }
    }
}