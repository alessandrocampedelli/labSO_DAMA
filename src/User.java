import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;

public abstract class User
{
    //la  socket del client
    protected Socket clientSocket;
    //il writer e il reader per inviare messaggi al client
    protected PrintWriter out;
    protected BufferedReader in;
    protected Topic currentTopic;
    //la lista di messaggi in ispezione che verranno analizzati quando il topic non sarà più in ispezione
    protected LinkedList<String> inspectMessages;
    //il flag che indica se il client è registrato
    protected boolean clientCreate;

    //metodo che costruttore della classe User che inizializza la socket e la linkedList dei messaggi in ispezione
    public User(Socket socket, Topic topic)
    {
        this.clientSocket = socket;
        this.inspectMessages = new LinkedList<>();
        this.currentTopic = topic;
    }

    //metodo che restituisce il topic corrente
    public Topic getTopic()
    {
        return this.currentTopic;
    }

    //metodo che restituisce la socket del client
    protected Socket getClientSocket()
    {
        return this.clientSocket;
    }

    // metodo da sovrascrivere nelle classi derivate per gestire i comandi
    public void handleCommand(String inputLine) {};

    //metodo che inserisce un messaggio in coda per l'elaborazione perchè il server è in ispezione
    protected void addInspectMessage(String m)
    {
        this.inspectMessages.add(m);
    }

    //metodo che elabora i messaggi in ispezione
    protected void processInspectMessages()
    {
        if(!inspectMessages.isEmpty())
        {
            //notifica il client del numero di messaggi in fase di ispezione
            out.println("Ecco le stampe relative "+(inspectMessages.size() == 1 ? " al comando inviato " : "ai "+inspectMessages.size()+" comandi inviati ")+"da te durante la fase di inspect");
        }
        //finché la lista di messaggi non è vuota, elabora il messaggio e lo rimuove
        while(!inspectMessages.isEmpty())
        {
            this.handleCommand(inspectMessages.removeFirst());
        }
    }

    //metodo che registra i flussi di input e output per la comunicazione con il client
    protected void registerOutputAndInput() throws IOException
    {
        //inizializza il writer per l'output del client
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        //inizializza il reader per l'input del client
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
}