import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientHandler extends Thread
{
    private final Socket clientSocket;
    private User client;
    private List<User> listClient;
    //variabile booleana per sapere se il client è in esecuzione oppure no
    private volatile boolean running = true;
    private Scanner in;
    private PrintWriter out;

    public ClientHandler(Socket socket, List<User> listClient)
    {
        this.clientSocket = socket;
        this.listClient = listClient;
    }

    @Override
    public void run()
    {
        try
        {
            //configurazione input e output
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            in = new Scanner(inputStream);
            out = new PrintWriter(outputStream, true);

            //verifica se l'utente è già registrato (si è dichiarato publish o subscribe)
            if (client == null)
            {
                //se l'utente non è già registrato, esso si deve registrare per forza prima di proseguire
                while(client == null)
                {
                    try
                    {
                        String inputLine = in.nextLine().toLowerCase();
                        if (inputLine.startsWith("publish "))
                        {
                            String topicName = inputLine.substring(8).trim();
                            if(!topicName.isEmpty())
                            {
                                client = new Publisher(clientSocket,Server.getOrCreateTopic(topicName));
                                client.registerOutputAndInput();
                                client.handleCommand(inputLine);
                                synchronized (listClient)
                                {
                                    listClient.add(client);
                                }
                                //stampa il messaggio sulla console del server
                                System.out.println("Un nuovo client si è connesso come PUBLISHER al topic " + topicName.toUpperCase());
                            }
                            else
                            {
                                out.println("Errore: il topic non è specificato. Riprova");
                            }
                        }
                        else if (inputLine.startsWith("subscribe "))
                        {
                            String topicName = inputLine.substring(10).trim();
                            if(!topicName.isEmpty())
                            {
                                client = new Subscriber(clientSocket,Server.getOrCreateTopic(topicName));
                                client.registerOutputAndInput();
                                client.handleCommand(inputLine);
                                synchronized (listClient)
                                {
                                    listClient.add(client);
                                }
                                //stampa il messaggio sulla console del server
                                System.out.println("Un nuovo client si è connesso come SUBSCRIBER al topic " + topicName.toUpperCase());
                            }
                            else
                            {
                                out.println("Errore: il topic non è specificato. Riprova");
                            }
                        }
                        else if(inputLine.equals("show"))
                        {
                            Server.showTopics(out);
                        }
                        else if(inputLine.equals("quit"))
                        {
                            out.println("Disconnessione in corso...");
                        }
                        else
                        {
                            out.println("Prima di compiere operazioni devi prima registrarti come publisher o subscriber.");
                        }
                    }
                    catch(NoSuchElementException e)
                    {
                        //eccezione gestita per quando un client scrive il comando quit ancora prima di scrivere publish o subscribe
                        return;
                    }
                }
            }
            // Ciclo per gestire ulteriori comandi
            while (running && in.hasNextLine())
            {
                String inputLine = in.nextLine();
                if (!client.getTopic().isInInspection())
                {
                    client.handleCommand(inputLine);
                }
                else
                {
                    client.handleCommand("inspect");
                    client.addInspectMessage(inputLine);
                }
                if (inputLine.equals("quit"))
                {
                    System.out.println("Client disconnesso");
                    break;
                }
            }
        }
        catch (SocketException e)
        {
            System.err.println("SocketException: " + e.getMessage());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            stopClient();
        }
    }

    public void stopClient()
    {
        running = false;
        try
        {
            synchronized (listClient)
            {
                listClient.removeIf(user -> user.getClientSocket().equals(clientSocket));
            }
            if (!clientSocket.isClosed())
            {
                clientSocket.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}