import java.net.Socket;

public class Subscriber extends User
{
    public Subscriber(Socket socket, Topic topic)
    {
        super(socket, topic);
    }

    @Override
    public void handleCommand(String inputLine)
    {
        inputLine = inputLine.toLowerCase();
        if (inputLine.startsWith("subscribe "))
        {
            //controllo che il client non si sia ancora mai registrato
            if(!clientCreate){
                String topicName = inputLine.substring(10).trim();
                currentTopic = Server.getOrCreateTopic(topicName);
                currentTopic.subscribe(this);
                out.println("Registrazione avvenuta con successo come SUBSCRIBER per il topic " + topicName.toUpperCase()+".");
                clientCreate = true;
            }
            else
            {
                out.println("ERRORE: non puoi effettuare una nuova registrazione a un diverso topic nella stessa esecuzione.");
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
                    }
                    else
                    {
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