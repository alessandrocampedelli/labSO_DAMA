import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.List;
import java.util.Scanner;

public class ServerHandler extends Thread
{
    private final ServerSocket serverSocket;
    private final List<User> listClient;
    private final Scanner stdIn;
    private PrintWriter out = new PrintWriter(System.out, true);;

    public ServerHandler(ServerSocket serverSocket, List<User> listClient)
    {
        this.serverSocket = serverSocket;
        this.listClient = listClient;
        this.stdIn = new Scanner(System.in);
    }

    @Override
    public void run()
    {
        try
        {
            String userInput;
            while ((userInput = stdIn.nextLine()) != null)
            {
                if (userInput.startsWith("quit"))
                {
                    stopServer();
                    break;
                }
                else if (userInput.startsWith("show"))
                {
                    Server.showTopics(out);
                }
                else if (userInput.startsWith("inspect "))
                {
                    String topicName = userInput.split(" ", 2)[1].trim();
                    Topic topic = getTopicByName(topicName);
                    if (topic == null)
                    {
                        System.out.println("Errore: Topic '" + topicName + "' non trovato.");
                    }
                    else
                    {
                        sessioneInterattiva(topic);
                    }
                }
                else
                {
                    System.out.println("Comando non riconosciuto.");
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Errore durante l'esecuzione del server: " + e.getMessage());
        }
        finally
        {
            stdIn.close();
        }
    }

    private void stopServer()
    {
        synchronized (listClient)
        {
            for (User client : listClient)
            {
                try
                {
                    client.getClientSocket().close();
                }
                catch (IOException e)
                {
                    System.err.println("Errore durante la chiusura del socket client: " + e.getMessage());
                }
            }
            listClient.clear();
        }
        try
        {
            if (!serverSocket.isClosed())
            {
                serverSocket.close();
                System.out.println("ServerSocket chiuso.");
            }
        }
        catch (IOException e)
        {
            System.err.println("Errore durante la chiusura del ServerSocket: " + e.getMessage());
        }
        System.out.println("Server scollegato...");
    }

    private Topic getTopicByName(String name)
    {
        synchronized (Server.topics)
        {
            for (Topic topic : Server.topics)
            {
                if (topic.getName().equals(name))
                {
                    return topic;
                }
            }
        }
        return null;
    }

    private void sessioneInterattiva(Topic topic)
    {
        topic.setInInspection(true);
        System.out.println("Sessione per il topic: " + topic.getName() + " iniziata");
        try
        {
            String userInput;
            while ((userInput = stdIn.nextLine()) != null)
            {
                if (userInput.startsWith(":listall"))
                {
                    if (topic.getMessages().isEmpty())
                    {
                        System.out.println("Nel topic non è presente alcun messaggio");
                    }
                    else
                    {
                        System.out.println("Messaggi presenti all'interno del topic '" + topic.getName() + "':");
                        for (Message message : topic.getMessages())
                        {
                            System.out.println(message.toString()+"\n");
                        }
                    }
                }
                else if (userInput.startsWith(":delete "))
                {
                    boolean trovato=false;
                    String messageIdStr = userInput.split(" ", 2)[1].trim();
                    //Conversione dell'input utente in int
                    int messageId = Integer.parseInt(messageIdStr);
                    //richiamo al metodo di eliminazione del messaggio tramite id nella classe Topic
                    for (Message message : topic.getMessages())
                    {
                        if(message.getId()==messageId)
                        {
                            trovato=true;
                            break;
                        }
                    }
                    if(trovato)
                    {
                        topic.deleteMessage(messageId);
                        System.out.println("Messaggio avente id "+"'"+messageId+"'"+" correttamente eliminato");
                    }
                    else
                    {
                        System.out.println("Messaggio avente id "+"'"+messageId+"'"+" non trovato");
                    }
                }
                else if (userInput.startsWith(":end"))
                {
                    this.processAllInspectMessage();
                    topic.setInInspection(false);
                    System.out.println("Sessione topic chiusa");
                    break;
                }
                else
                {
                    System.out.println("Comando non riconosciuto nella sessione interattiva.");
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Errore durante la lettura dell'input nella sessione interattiva: " + e.getMessage());
        }
    }

    private void processAllInspectMessage()
    {
        for (User client : listClient)
        {
            client.processInspectMessages();
        }
    }
}