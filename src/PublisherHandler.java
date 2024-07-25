import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class PublisherHandler extends Thread {
    private final Socket clientSocket;
    //inviare messaggi al client
    private PrintWriter out;
    //leggere messaggi dal client
    private BufferedReader in;
    private Topic currentTopic;

    public PublisherHandler(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            //leggo le linee di input dal client finchè non riceve una linea nulla
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("send ")) {
                    String messageText = inputLine.substring(5);
                    Message message = new Message(messageText);
                    //caso in cui manda un messaggio senza specificare il topic
                    if (currentTopic != null) {
                        currentTopic.addMessage(message);
                        out.println("Messaggio inviato.");
                    } else {
                        out.println("Devi prima pubblicare su un topic.");
                    }
                    //TODO: list non va bene perchè stampa tutti i messaggi come listall e non solo quelli mandati da quel publisher
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
                }
                else if (inputLine.equals("show"))
                {
                    Server.showTopics(out);
                }
                else if (inputLine.equals("quit"))
                {
                    out.println("Disconnessione in corso...");
                    break;
                }
                else
                {
                    out.println("Comando sconosciuto!.");
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