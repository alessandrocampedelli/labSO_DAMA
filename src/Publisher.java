import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Publisher extends User
{
    private final List<Message> messaggiUtente = new ArrayList<>();

    public Publisher(Socket socket, Topic topic)
    {
        super(socket, topic);
    }

    @Override
    public void handleCommand(String inputLine)
    {
        if (inputLine.startsWith("publish "))
        {
            String topicName = inputLine.substring(8).trim();
            currentTopic = Server.getOrCreateTopic(topicName);
            out.println("Registrato come PUBLISHER per il topic " + topicName.toUpperCase());
        }
        else if (inputLine.startsWith("send "))
        {
            String messageText = inputLine.substring(5);
            Message message = new Message(messageText);
            if (currentTopic != null)
            {
                if(!message.getText().isEmpty()){
                    currentTopic.addMessage(message);
                    //aggiunta del messaggio alla lista dei messaggi dell'utente
                    messaggiUtente.add(message);
                    out.println("Messaggio inviato.");
                }else{
                    out.println("Errore: contenuto del messaggio non presente.");
                }
            }
            else
            {
                out.println("Prima di inviare un messaggio devi prima specificare il topic.");
            }
        }
        else if (inputLine.equals("list"))
        {
            if (currentTopic != null)
            {
                if(!messaggiUtente.isEmpty()){
                    out.println((currentTopic.getMessages().size() == 1 ? "Un messaggio inviato": currentTopic.getMessages().size()+" messaggi inviati")+" da te sul topic "+currentTopic.getName().toUpperCase()+":");
                    for (Message message : messaggiUtente)
                    {
                        out.println(message);
                    }
                }else{
                    out.println("Non hai ancora inviato alcun messaggio sul topic "+currentTopic.getName().toUpperCase());
                }
            }
            else
            {
                out.println("Per poter riceve la lista dei tuoi messaggi inviati devi prima iscriverti a un topic.");
            }
        }
        else if (inputLine.equals("listall"))
        {
            if (currentTopic != null)
            {
                if(!currentTopic.getMessages().isEmpty()){
                    out.println((currentTopic.getMessages().size() == 1 ? "Un messaggio pubblicato": currentTopic.getMessages().size()+" messaggi pubblicati")+" sul topic "+currentTopic.getName().toUpperCase()+":");
                    for (Message message : currentTopic.getMessages())
                    {
                        out.println(message);
                    }
                }else{
                    out.println("Non è stato ancora pubblicato alcun messaggio sul topic "+currentTopic.getName().toUpperCase());
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