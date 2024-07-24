import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Topic currentTopic;

    public ClientHandler(Socket socket) {
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Gestione dei comandi del client
                if (inputLine.startsWith("publish ")) {
                    String topicName = inputLine.split(" ")[1];
                    currentTopic = Server.getOrCreateTopic(topicName);
                    out.println("Registrato come publisher per il topic: " + topicName);
                } else if (inputLine.startsWith("subscribe ")) {
                    String topicName = inputLine.split(" ")[1];
                    currentTopic = Server.getOrCreateTopic(topicName);
                    currentTopic.subscribe(this);
                    out.println("Iscritto al topic: " + topicName);
                } else if (inputLine.equals("show")) {
                    Server.showTopics(out);
                } else if (inputLine.startsWith("send ")) {
                    String messageText = inputLine.substring(5);
                    Message message = new Message(messageText);
                    if (currentTopic != null) {
                        currentTopic.addMessage(message);
                        out.println("Messaggio inviato.");
                    } else {
                        out.println("Devi prima pubblicare su un topic.");
                    }
                } else if (inputLine.equals("list")) {
                    if (currentTopic != null) {
                        out.println("Messaggi:");
                        for (Message message : currentTopic.getMessages()) {
                            out.println(message);
                        }
                    } else {
                        out.println("Devi prima pubblicare su un topic.");
                    }
                } else if (inputLine.equals("listall")) {
                    if (currentTopic != null) {
                        out.println("Messaggi:");
                        for (Message message : currentTopic.getMessages()) {
                            out.println(message);
                        }
                    } else {
                        out.println("Non sei iscritto o non stai pubblicando su alcun topic.");
                    }
                } else if (inputLine.equals("quit")) {
                    out.println("Disconnessione in corso...");
                    break;
                } else {
                    out.println("Comando sconosciuto.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Invia un messaggio al client
    public void sendMessage(Message message) {
        out.println("Nuovo messaggio sul topic " + currentTopic.getName() + ": " + message);
    }
}