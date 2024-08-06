import java.io.IOException;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public abstract class User {
    protected Socket clientSocket;
    protected PrintWriter out;
    protected BufferedReader in;
    protected Topic currentTopic;

    public User(Socket socket) {
        this.clientSocket = socket;
    }

    public abstract void handleCommand(String inputLine);

    // Invia un messaggio al client
    public void sendMessage(Message message) {
        out.println("Nuovo messaggio sul topic " + currentTopic.getName() + ": " + message);
    }
    public Topic getTopic(){
        return this.currentTopic;
    }

    protected void registerOutputAndInput() throws IOException {
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }
}