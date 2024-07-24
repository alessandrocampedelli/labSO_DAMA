import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Utilizzo: java Client <indirizzo_server> <porta_server>");
            return;
        }

        String serverIp = args[0];
        int serverPort = Integer.parseInt(args[1]);

        try (Socket socket = new Socket(serverIp, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Connesso al server " + serverIp + ":" + serverPort);
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                String response;
                while ((response = in.readLine()) != null) {
                    System.out.println(response);
                    if (!in.ready()) {
                        break;
                    }
                }
                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Impossibile connettersi al server " + serverIp + ":" + serverPort);
        }
    }
}