import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Publisher extends User
{
    private List<Message> messaggiUtente = new ArrayList<>();

    public Publisher(Socket socket, Topic topic)
    {
        super(socket, topic);
    }

    @Override
    public void handleCommand(String inputLine)
    {
        if (inputLine.startsWith("publish "))
        {
            String topicName = inputLine.split(" ")[1];
            currentTopic = Server.getOrCreateTopic(topicName);
            out.println("Registrato come publisher per il topic: " + topicName);
        }
        else if (inputLine.startsWith("send "))
        {
            String messageText = inputLine.substring(5);
            Message message = new Message(messageText);
            if (currentTopic != null)
            {
                currentTopic.addMessage(message);
                //aggiunta del messaggio alla lista dei messaggi dell'utente
                messaggiUtente.add(message);
                out.println("Messaggio inviato.");
            }
            else
            {
                out.println("Devi prima pubblicare su un topic.");
            }
        }
        else if (inputLine.equals("list"))
        {
            if (currentTopic != null)
            {
                out.println("Messaggi inviati dal client:");
                for (Message message : messaggiUtente)
                {
                    out.println(message);
                }
            }
            else
            {
                out.println("Devi prima iscriverti a un topic.");
            }
        }
        else if (inputLine.equals("listall"))
        {
            if (currentTopic != null)
            {
                out.println("Messaggi nel topic:");
                for (Message message : currentTopic.getMessages())
                {
                    out.println(message);
                }
            }
            else
            {
                out.println("Non sei iscritto o non stai pubblicando su alcun topic.");
            }
        }
        else if (inputLine.equals("show"))
        {
            Server.showTopics(out);
        }
        else if (inputLine.equals("quit"))
        {
            out.println("Disconnessione in corso...");
        }
        else if (inputLine.equals("inspect"))
        {
            out.println("Il topic è in ispezione. Il messaggio verrà elaborato dal server una volta terminata la fase di ispezione");
        }
        else
        {
            out.println("Comando sconosciuto.");
        }
    }
}