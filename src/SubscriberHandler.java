import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SubscriberHandler extends Thread {
    private final Socket clientSocket;
    //inviare messaggi al client
    private PrintWriter out;
    //leggere messaggi dal client
    private BufferedReader in;
    private Topic currentTopic;

    public SubscriberHandler(Socket socket) {
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
                if (inputLine.equals("listall")) {
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
}