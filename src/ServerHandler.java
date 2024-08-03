import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServerHandler extends Thread{
    private final ServerSocket serverSocket;
    private List<Socket> listClientSocket = new ArrayList<>();
    public ServerHandler(ServerSocket serverSocket, List<Socket> listClientSocket) {
        this.serverSocket = serverSocket;
        this.listClientSocket = listClientSocket;
    }

    public void run() {
        try{
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if(userInput.startsWith("quit")){
                    break;
                } else if (userInput.startsWith("show")) {

                } else if (userInput.startsWith("inspect")) {

                }else if(userInput.startsWith("num client")){   //momentaneo per debug
                    System.out.println(listClientSocket.size());
                }
            }
        }catch (IOException e) {
            System.out.println("errore");
        } finally {

        }
    }

}
