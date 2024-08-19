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
        out.println("Nuovo messaggio sul topic " + currentTopic.getName() + ": " + message);
    }
    //inserisco in coda il messaggio da elaborare
    protected void addInspectMessage(String m)
    {
        this.inspectMessages.add(m);
    }
    protected void processInspectMessages()
    {
        //finchè la lista di strighe non è vuota elaboro l'elemento in testa
        while(!inspectMessages.isEmpty())
        {
            this.handleCommand(inspectMessages.getFirst());
            this.inspectMessages.removeFirst();
        }
    }
    protected void registerOutputAndInput() throws IOException
    {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
}