import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;

public class Server {
    private static final List<Topic> topics = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Utilizzo: java Server <porta>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server in ascolto sulla porta " + port);

            // Accetta connessioni dei client in un ciclo infinito
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Crea o recupera un topic esistente
    public static synchronized Topic getOrCreateTopic(String name) {
        for (Topic topic : topics) {
            if (topic.getName().equals(name)) {
                return topic;
            }
        }
        Topic newTopic = new Topic(name);
        topics.add(newTopic);
        return newTopic;
    }

    // Mostra la lista dei topic attualmente disponibili
    public static synchronized void showTopics(PrintWriter out) {
        out.println("Topics:");
        for (Topic topic : topics) {
            out.println("- " + topic.getName());
        }
    }
}