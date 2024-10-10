import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientHandler extends Thread
{
    //la socket associata al client
    private final Socket clientSocket;
    private User client;
    //variabile booleana per sapere se il server è in ispezione oppure no
    private volatile boolean running = true;
    private Scanner in;
    private PrintWriter out;
    //metodo costruttore che permette l'inizializzazione della socket e della lista dei client connessi
    public ClientHandler(Socket socket)
    {
        this.clientSocket = socket;
    }
    @Override
    public void run()
    {
        try
        {
            InputStream inputStream = clientSocket.getInputStream();
            OutputStream outputStream = clientSocket.getOutputStream();
            in = new Scanner(inputStream);
            out = new PrintWriter(outputStream, true);

            //verifica se l'utente è già registrato (si è dichiarato publisher o subscriber)
            if (client == null)
            {
                //se l'utente non è già registrato, esso si deve registrare prima di proseguire
                while(client == null && !clientSocket.isClosed())
                {
                    try
                    {
                        String inputLine = in.nextLine().toLowerCase();
                        //il client si registra come "publisher"
                        if (inputLine.startsWith("publish "))
                        {
                            String topicName = inputLine.substring(8).trim();
                            if (!topicName.isEmpty())
                            {
                                //crea un nuovo publisher associato al topic specificato
                                client = new Publisher(clientSocket, Server.getOrCreateTopic(topicName));
                                client.registerOutputAndInput();
                                client.handleCommand(inputLine);
                                System.out.println("Un nuovo client si è connesso come PUBLISHER al topic " + topicName.toUpperCase());
                            }
                            else
                            {
                                out.println("ERRORE: il topic non è specificato. Riprova");
                            }
                        }
                        //il client si registra come "subscriber"
                        else if (inputLine.startsWith("subscribe "))
                        {
                            String topicName = inputLine.substring(10).trim();
                            if (!topicName.isEmpty())
                            {
                                //crea un nuovo subscriber associato al topic specificato
                                client = new Subscriber(clientSocket, Server.getOrCreateTopic(topicName));
                                client.registerOutputAndInput();
                                client.handleCommand(inputLine);
                                System.out.println("Un nuovo client si è connesso come SUBSCRIBER al topic " + topicName.toUpperCase());
                            }
                            else
                            {
                                out.println("ERRORE: il topic non è specificato. Riprova");
                            }
                        }
                        //comando "show" per visualizzare i topic disponibili
                        else if (inputLine.equals("show"))
                        {
                            Server.showTopics(out);
                        }
                        //comando "quit" per disconnettersi dal topic e dal programma
                        else if (inputLine.equals("quit"))
                        {
                            notifyQuit("#closeClient");
                            break;
                        }
                        else if(inputLine.equals("publish") || inputLine.equals("subscribe"))
                        {
                            out.println("ERRORE: il topic non è specificato. Riprova");
                        }
                        else
                        {
                            out.println("ERRORE: prima di compiere operazioni devi prima registrarti come publisher o subscriber.");
                        }
                    }
                    catch(NoSuchElementException e)
                    {
                        //eccezione per quando un client scrive il comando quit prima di scrivere publish o subscribe
                        return;
                    }
                }
            }
            if (this.getClient() != null)
            {
                //ciclo per gestire ulteriori comandi dopo la registrazione
                while (running && in.hasNextLine())
                {
                    //utilizzo il lock per evitare problemi durante un possibile context-switch nel momento della modifica
                    //del valore della variabile booleana "InInspection" da true a false e viceversa
                    synchronized (Server.lock)
                    {
                        String inputLine = in.nextLine();
                        if (!client.getTopic().isInInspection())
                        {
                            client.handleCommand(inputLine);
                        }
                        else
                        {
                            //gestione dei comandi durante un'ispezione
                            if (!inputLine.equals("quit"))
                            {
                                client.handleCommand("inspect");
                                client.addInspectMessage(inputLine);
                            }
                            else
                            {
                                notifyQuit("#inspect");
                                out.println("ERRORE: il comando quit non si può utilizzare durante la fase di ispezione attiva.");
                            }
                        }
                        //disconnessione del client se invia il comando "quit" e non è in ispezione
                        if (inputLine.equals("quit") && !client.getTopic().isInInspection())
                        {
                            //mi chiedo se l'oggetto "client" è istanza di Publisher per avvisare l'utente se si è disconnesso un publisher o un subscriber
                            System.out.println((client instanceof Publisher ? "Un PUBLISHER" : "Un SUBSCRIBER")
                                    + " registrato al topic " + client.currentTopic.getName().toUpperCase() + " si è disconnesso");
                            notifyQuit("#closeClient");
                            break;
                        }
                    }
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

    //metodo per notificare la disconnessione del client al server
    public void notifyQuit(String m)
    {
        try
        {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(m);
        } catch (SocketException e){
            System.err.println("Errore durante l'invio della notifica al client: la socket è già stata chiusa");

        } catch (IOException e)
        {
            System.err.println("Errore durante l'invio della notifica al client: " + e.getMessage());
        }
    }

    //metodo per stoppare il client e rimuoverlo dalla lista dei client connessi
    public void stopClient()
    {
        running = false;
        try
        {
            //rimuovo il client connesso alla lista di client
            synchronized (Server.listClient) {
                Server.listClient.removeIf(ClientHandler -> ClientHandler.getSocket().equals(clientSocket));

                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    //metodo per ottenere il client associato a questo handler
    public User getClient()
    {
        return this.client;
    }

    public Socket getSocket()
    {
        return this.clientSocket;
    }
}