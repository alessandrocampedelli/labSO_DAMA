import java.net.Socket;

public class Subscriber extends User {
    public Subscriber(Socket socket) {
        super(socket);
    }

    @Override
    public void handleCommand(String inputLine) {
        if (inputLine.startsWith("subscribe ")) {
            String topicName = inputLine.split(" ")[1];
            currentTopic = Server.getOrCreateTopic(topicName);
            currentTopic.subscribe(this);
            out.println("Iscritto al topic: " + topicName);
        } else if (inputLine.equals("listall")) {
                if (currentTopic != null) {
                    out.println("Messaggi:");
                    for (Message message : currentTopic.getMessages()) {
                        out.println(message);
                    }
                } else {
                    out.println("Non sei iscritto o non stai pubblicando su alcun topic.");
                }
        } else if (inputLine.equals("show")) {
            Server.showTopics(out);
        } else if (inputLine.equals("quit")) {
            out.println("Disconnessione in corso...");
        }else if (inputLine.equals("inspect")) {
            out.println("Il topic è in ispezione. Il messaggio verrà elaborato dal server una volta terminata la fase di ispezione");
        }  else {
            out.println("Comando sconosciuto.");
        }
    }
}