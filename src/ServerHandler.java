import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.List;
import java.util.Scanner;

public class ServerHandler extends Thread
{
    private final ServerSocket serverSocket;
    private final List<ClientHandler> listClient;
    private final Scanner stdIn;
    private final PrintWriter out = new PrintWriter(System.out, true);;

    public ServerHandler(ServerSocket serverSocket, List<ClientHandler> listClient)
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
                userInput = userInput.toLowerCase();
                if (userInput.startsWith("quit"))
                {

                    notifyUsers("#close", null);
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
                        notifyUsers("#session_start", topic);
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
            e.printStackTrace();
        }
        finally
        {
            stdIn.close();
        }
    }

    private void notifyUsers(String message, Topic topic)
    {
        synchronized (listClient)
        {
            for (ClientHandler client : listClient)
            {
                //caso in cui il server si sia attivo ma non si sia ancora registrato come publisher o subscriber
                //in questo caso salto tutte le operazioni di chiusura
                if(client.getClient() == null){
                    continue;
                }
                // Se il topic è null, notificare tutti i client
                // Altrimenti, notificare solo i client iscritti al topic specificato
                if (topic == null || client.getClient().getTopic().equals(topic))
                {
                    try
                    {
                        PrintWriter out = new PrintWriter(client.getClient().getClientSocket().getOutputStream(), true);
                        out.println(message);
                    }
                    catch (IOException e)
                    {
                        System.err.println("Errore durante l'invio della notifica al client: " + e.getMessage());
                    }
                }
            }
        }
    }

    private void stopServer()
    {
        synchronized (listClient)
        {

            for (ClientHandler client : listClient)
            {
                //caso in cui il server si sia attivo ma non si sia ancora registrato come publisher o subscriber
                //in questo caso salto tutte le operazioni di chiusura
                if(client.getClient() == null){
                    continue;
                }
                try
                {
                    client.getClient().getClientSocket().close();
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
        System.out.println("Sessione interattiva per il topic " + topic.getName().toUpperCase() + " iniziata");
        try
        {
            String userInput;
            while ((userInput = stdIn.nextLine()) != null)
            {
                userInput = userInput.toLowerCase();
                if (userInput.startsWith(":listall"))
                {
                    if (topic.getMessages().isEmpty())
                    {
                        System.out.println("Nel topic "+topic.getName().toUpperCase()+" non è stato inviato alcun messaggio");
                    }
                    else
                    {
                        System.out.println((topic.getMessages().size() == 1 ? "Un messaggio inviato ": topic.getMessages().size()+" messaggi inviati ")+"sul topic " + topic.getName().toUpperCase() + ":");
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
                        System.out.println("Eliminazione messaggio avente id "+"'"+messageId+"'"+" avvenuta con successo.");
                    }
                    else
                    {
                        System.out.println("ERRORE: messaggio avente id "+"'"+messageId+"'"+" non trovato. Riprova.");
                    }
                }
                else if (userInput.startsWith(":end"))
                {
                    notifyUsers("#session_end", topic);
                    this.processAllInspectMessage();
                    topic.setInInspection(false);
                    System.out.println("Sessione interattiva per il topic "+topic.getName().toUpperCase()+" chiusa");
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
        for (ClientHandler client : listClient)
        {
            client.getClient().processInspectMessages();
        }
    }
}