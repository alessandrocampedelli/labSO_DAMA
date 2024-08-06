import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ServerHandler extends Thread {
    private final ServerSocket serverSocket;
    private final List<Socket> listClientSocket;
    private final Scanner stdIn;

    public ServerHandler(ServerSocket serverSocket, List<Socket> listClientSocket) {
        this.serverSocket = serverSocket;
        this.listClientSocket = listClientSocket;
        this.stdIn = new Scanner(System.in);
    }

    @Override
    public void run() {
        try {
            String userInput;
            while ((userInput = stdIn.nextLine()) != null) {
                if (userInput.startsWith("quit")) {
                    stopServer();
                    break;
                } else if (userInput.startsWith("show")) {
                    showTopics();
                } else if (userInput.startsWith("inspect ")) {
                    String topicName = userInput.split(" ", 2)[1].trim();
                    Topic topic = getTopicByName(topicName);
                    if (topic == null) {
                        System.out.println("Errore: Topic '" + topicName + "' non trovato.");
                    } else {
                        sessioneInterattiva(topic);
                    }
                } else if (userInput.startsWith("num client")) {
                    showClientCount();
                } else {
                    System.out.println("Comando non riconosciuto.");
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante l'esecuzione del server: " + e.getMessage());
        } finally {
            stdIn.close();
        }
    }

    private void stopServer() {
        int numDisconnected = 0;
        synchronized (listClientSocket) {
            for (Socket clientSocket : listClientSocket) {
                try {
                    clientSocket.close();
                    numDisconnected++;
                } catch (IOException e) {
                    System.err.println("Errore durante la chiusura del socket client: " + e.getMessage());
                }
            }
            listClientSocket.clear();
        }

        try {
            if (!serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("ServerSocket chiuso.");
            }
        } catch (IOException e) {
            System.err.println("Errore durante la chiusura del ServerSocket: " + e.getMessage());
        }

        System.out.println("Numero client scollegati: " + numDisconnected);
        System.out.println("Server scollegato...");
    }

    private void showTopics() {
        System.out.println("Topics: ");
        synchronized (Server.topics) {
            for (Topic topic : Server.topics) {
                System.out.println("    - " + topic.getName());
            }
        }
    }

    private Topic getTopicByName(String name) {
        synchronized (Server.topics) {
            for (Topic topic : Server.topics) {
                if (topic.getName().equals(name)) {
                    return topic;
                }
            }
        }
        return null;
    }

    private void sessioneInterattiva(Topic topic) {
        topic.setInInspection(true);
        System.out.println("Sessione per il topic: " + topic.getName() + " iniziata");

        try {
            String userInput;
            while ((userInput = stdIn.nextLine()) != null) {
                if (userInput.startsWith("listall")) {
                    // Implementa la logica per elencare tutti i messaggi nel topic
                } else if (userInput.startsWith("delete ")) {
                    String messageId = userInput.split(" ", 2)[1].trim();
                    // Implementa la logica per eliminare il messaggio con l'ID specificato
                } else if (userInput.startsWith("end")) {
                    topic.setInInspection(false);
                    System.out.println("Sessione topic chiusa");
                    break;
                } else {
                    System.out.println("Comando non riconosciuto nella sessione interattiva.");
                }
            }
        } catch (Exception e) {
            System.err.println("Errore durante la lettura dell'input nella sessione interattiva: " + e.getMessage());
        }
    }

    private void showClientCount() {
        synchronized (listClientSocket) {
            System.out.println("Client connessi: " + listClientSocket.size());
        }
    }
}
