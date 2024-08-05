import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class ServerHandler extends Thread {
    private final ServerSocket serverSocket;
    private final List<Socket> listClientSocket;

    public ServerHandler(ServerSocket serverSocket, List<Socket> listClientSocket) {
        this.serverSocket = serverSocket;
        this.listClientSocket = listClientSocket;
    }

    @Override
    public void run() {
        try (BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.startsWith("quit")) {
                    stopServer();
                    break;
                } else if (userInput.startsWith("show")) {
                    System.out.println("Topics: ");
                    synchronized (Server.topics) {
                        for (Topic topic : Server.topics) {
                            System.out.println("    - " + topic.getName());
                        }
                    }
                } else if (userInput.startsWith("inspect")) {
                    sessioneInterattiva();

                } else if (userInput.startsWith("num client")) {
                    synchronized (listClientSocket) {
                        System.out.println("Client connessi: " + listClientSocket.size());
                    }
                } else {
                    System.out.println("Comando non riconosciuto.");
                }
            }
        } catch (IOException e) {
            System.err.println("Errore durante la lettura dell'input dalla console: " + e.getMessage());
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
    private void sessioneInterattiva() throws IOException {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        Server.flag_sessione_interattiva = true;
        System.out.println("Flag sessione interattiva: " + Server.flag_sessione_interattiva);
        while (Server.flag_sessione_interattiva) {
            String s = stdIn.readLine();
            if (s.startsWith("listall")) {

            } else if (s.startsWith("delete")) {

            } else if (s.startsWith("end")) {
                Server.flag_sessione_interattiva = false;
                System.out.println("Flag sessione interattiva: " + Server.flag_sessione_interattiva);
            }
        }
    }

}
