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
        if (inputLine.startsWith("subscribe "))
        {
            String topicName = inputLine.substring(10).trim();
            System.out.println("'"+topicName+"'");
            currentTopic = Server.getOrCreateTopic(topicName);
            currentTopic.subscribe(this);
            out.println("Registrato come SUBSCRIBER per il topic " + topicName.toUpperCase());
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