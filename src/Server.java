import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.io.PrintWriter;

public class Server {
    public static final List<Topic> topics = new ArrayList<>();
    private static final int DEFAULT_PORT = 9000; // Porta predefinita
    public static volatile boolean flag_sessione_interattiva = false;

    public static void main(String[] args) {
        /*//per inserire manualmente la porta
        if (args.length != 1) {
            System.out.println("Utilizzo: java Server <porta>");
            return;
        }
        */
        int port = DEFAULT_PORT; // Usa la porta predefinita
        List<Socket> listClientSocket = new ArrayList<>();

        
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server in ascolto sulla porta " + port);

            //gestisco contemporaneamente la console del server all'attesa di nuovi client
            new ServerHandler(serverSocket,listClientSocket).start();

            // Accetta connessioni dei client in un ciclo infinito
            while (!serverSocket.isClosed()) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    new ClientHandler(clientSocket, listClientSocket).start();

                    synchronized (listClientSocket) {
                        listClientSocket.add(clientSocket);
                    }
                }catch (IOException e) { //quando chiudo il server l'accept non riesce più ad eseguire in quanto la serversocket è ststa chiusa
                    if (!serverSocket.isClosed()) {
                        System.err.println("Errore durante l'accettazione della connessione: " + e.getMessage());
                    }
                    break;
                }

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
