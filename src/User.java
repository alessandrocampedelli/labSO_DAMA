import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;

public class User
{
    protected Socket clientSocket;
    protected PrintWriter out;
    protected BufferedReader in;
    protected Topic currentTopic;
    protected LinkedList<String> inspectMessages;
    protected boolean clientCreate;

    public User(Socket socket, Topic topic)
    {
        this.clientSocket = socket;
        this.inspectMessages = new LinkedList<>();
        this.currentTopic = topic;
    }

    public Topic getTopic()
    {
        return this.currentTopic;
    }

    protected Socket getClientSocket()
    {
        return this.clientSocket;
    }

    public void handleCommand(String inputLine) {};

    // Invia un messaggio al client
    public void sendMessage(Message message)
    {
        out.println("Nuovo messaggio inviato sul topic " + currentTopic.getName().toUpperCase() + ":\n" + message);
    }

    //inserisco in coda il messaggio da elaborare
    protected void addInspectMessage(String m)
    {
            this.inspectMessages.add(m);
    }

    protected void processInspectMessages()
    {
        if(!inspectMessages.isEmpty())
        {
            out.println("Ecco le stampe relative "+(inspectMessages.size() == 1 ? " al comando inviato " : "ai "+inspectMessages.size()+" comandi inviati ")+"da te durante la fase di inspect");
        }
        //finchè la lista di strighe non è vuota elaboro l'elemento in testa
        while(!inspectMessages.isEmpty())
        {
            this.handleCommand(inspectMessages.removeFirst());
        }
    }

    protected void registerOutputAndInput() throws IOException
    {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
}