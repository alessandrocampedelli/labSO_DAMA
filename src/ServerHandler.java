import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.List;
import java.util.Scanner;

public class ServerHandler extends Thread
{
    //socket del server per accettare connessioni dai client
    private final ServerSocket serverSocket;
    private final Scanner stdIn;
    private final PrintWriter out = new PrintWriter(System.out, true);

    //metodo costruttore che inizializza la socket del sever, la lista dei ClientHandler e lo scanner
    public ServerHandler(ServerSocket serverSocket)
    {
        this.serverSocket = serverSocket;
        this.stdIn = new Scanner(System.in);
    }

    @Override
    public void run()
    {
        try
        {
            String userInput;
            //ciclo principale per leggere l'input dell'utente
            while ((userInput = stdIn.nextLine()) != null && !serverSocket.isClosed())
            {
                //conversione del comando in minuscolo per uniformità
                userInput = userInput.toLowerCase();
                if (userInput.startsWith("quit"))
                {
                    //metodo per far terminare l'esecuzione del server e notifichiamo tutti i client dell'accaduto
                    notifyUsers("#closeServer", null);
                    stopServer();
                    break;
                }
                //gestione del comando "show" per mostrare tutti i topic disponibili a cui è possibile collegarsi
                else if (userInput.startsWith("show"))
                {
                    Server.showTopics(out);
                }
                //gestione del comando "inspect" per avviare una sessione interattiva su un topic specificato
                else if (userInput.startsWith("inspect "))
                {
                    //estrai il nome del topic e trovo il topic per nome
                    String topicName = userInput.split(" ", 2)[1].trim();
                    Topic topic = getTopicByName(topicName);
                    //cerco se il topic inserito esiste oppure no
                    if (topic == null) {
                        System.out.println("Errore: Topic '" + topicName + "' non trovato.");
                    } else {
                        synchronized (Server.lock) {        //per evitare problemi nel context switch quando il client legge la condizione dell'if isInInspection
                            //imposto il topic come in ispezione (le richieste dei client verranno eseguite quando la variabile torna false)
                            topic.setInInspection(true);
                        }
                        //notifica i client connessi al topicnche la sessione di ispezione sta per iniziare
                        notifyUsers("#session_start", topic);
                        //metodo che permette di avviare la sessione interattiva
                        sessioneInterattiva(topic);
                    }
                } else
                {
                    System.out.println("Comando non riconosciuto.");
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Errore durante l'esecuzione del server: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            stdIn.close();
        }
    }

    //metodo per notificare con un messaggio i client iscritti ad un topic specifico
    private void notifyUsers(String message, Topic topic)
    {
        //sincronizzo l'accesso alla lista dei client
        synchronized (Server.listClient)
        {
            for (ClientHandler client : Server.listClient)
            {
                if(client.getClient() == null)
                {
                    if(message.equals("#closeServer")) {
                        try {
                            //crea un PrintWriter per inviare il messaggio al client specifico
                            PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
                            //invio il messaggio
                            out.println(message);
                        } catch (IOException e) {
                            System.err.println("Errore durante l'invio della notifica al client: " + e.getMessage());
                        }
                    }
                    continue;
                }
                //notifica solo i client iscritti al topic specificato (se esiste)
                if (topic == null || client.getClient().getTopic().equals(topic))
                {
                    try
                    {
                        //crea un PrintWriter per inviare il messaggio al client specifico
                        PrintWriter out = new PrintWriter(client.getClient().getClientSocket().getOutputStream(), true);
                        //invio il messaggio
                        out.println(message);
                    }
                    catch (IOException e)
                    {
                        System.err.println("Errore durante l'invio della notifica al client: " + e.getMessage());
                    }
                }
            }
        }
    }

    //metodo per stoppare il server e chiudere tutte le connessioni ai client
    private void stopServer() throws IOException
    {
        //sincronizzo l'accesso alla lista dei client
        synchronized (Server.listClient)
        {
            for (ClientHandler client : Server.listClient)
            {
                try
                {
                    //chiudo la socket del client specifico
                    client.getSocket().close();
                }
                catch (IOException e)
                {
                    System.err.println("Errore durante la chiusura del socket client: " + e.getMessage());
                }
            }
            //pulisce la lista dei client connessi al server
            Server.listClient.clear();
        }
        try
        {
            if (!serverSocket.isClosed())
            {
                //chiudo la socket del server e lo notifico al cliente con una stampa
                serverSocket.close();
                System.out.println("ServerSocket chiuso.");
            }
        }
        catch (IOException e)
        {
            System.err.println("Errore durante la chiusura del ServerSocket: " + e.getMessage());
        }

        //stampo in console che il server si è scollegato
        System.out.println("Server scollegato...");
    }

    //metodo per trovare un topic dal suo nome
    private Topic getTopicByName(String name)
    {
        //sincronizzo l'accesso alla lista dei topic
        synchronized (Server.topics)
        {
            for (Topic topic : Server.topics)
            {
                //ricerbo il nome del topic dal suo nome
                if (topic.getName().equals(name))
                {
                    //ritorna il topic se presente
                    return topic;
                }
            }
        }
        //ritorna null se il topic non è stato trovato (il topic non esisteva)
        return null;
    }

    //metodo per gestire una sessione interattiva su un topic
    private void sessioneInterattiva(Topic topic)
    {
        System.out.println("Sessione interattiva per il topic " + topic.getName().toUpperCase() + " iniziata");
        try
        {
            String userInput;
            while ((userInput = stdIn.nextLine()) != null)
            {
                //conversione del comando in minuscolo per uniformità
                userInput = userInput.toLowerCase();
                //gestione del comando ":listall" per mostrare tutti i messaggi del topic
                if (userInput.startsWith(":listall"))
                {
                    if (topic.getMessages().isEmpty())
                    {
                        System.out.println("Nel topic "+topic.getName().toUpperCase()+" non è stato inviato alcun messaggio");
                    }
                    else
                    {
                        System.out.println((topic.getMessages().size() == 1 ? "Un messaggio inviato ": topic.getMessages().size()+" messaggi inviati ")+"sul topic " + topic.getName().toUpperCase() + ":");
                        for (Message message : topic.getMessages())
                        {
                            //stampo in Console al sever ogni messaggio inviato nel topic
                            System.out.println(message.toString()+"\n");
                        }
                    }
                }
                //gestione del comando ":delete" per eliminare un messaggio specificato dal suo ID
                else if (userInput.startsWith(":delete "))
                {
                    //variabile booleana per ricercare un ID specifico
                    boolean trovato = false;
                    //estraggo l'ID del messaggio e converto l'ID in intero
                    String messageIdStr = userInput.split(" ", 2)[1].trim();
                    int messageId = Integer.parseInt(messageIdStr);
                    //verifica se il messaggio con l'ID specificato esiste e lo elimina
                    for (Message message : topic.getMessages())
                    {
                        if(message.getId() == messageId)
                        {
                            //l'ID viene trovato e il messaggio di conseguenza viene eliminato
                            trovato = true;
                            break;
                        }
                    }
                    //se ho trovato l'ID elimino il messaggio altrimenti avviso l'utente che l'ID inserito non esisteva
                    if(trovato){
                        //una volta trovato il messaggio lo elimino sia dalla lista di messaggi del topic, sia dalla lista dei messaggi
                        //del publisher che aveva inviato quel messaggio
                        topic.deleteMessage(messageId);
                        synchronized (Server.listClient) {
                            for (ClientHandler clientHandler : Server.listClient) {
                                if (clientHandler.getClient() instanceof Publisher) {
                                    Publisher p = (Publisher) clientHandler.getClient();
                                    //vado ad eliminare nella lista del publisher il messaggio con id corrispondente
                                    p.getMessaggiUtente().removeIf(m -> m.getId() == messageId);
                                    notifyUsers("#delete" + messageId, topic);
                                }
                            }
                        }
                        System.out.println("Eliminazione messaggio avente id " + "'" + messageId + "'" + " avvenuta con successo.");
                    }
                    else
                    {
                        System.out.println("ERRORE: messaggio avente id " + "'" + messageId + "'" + " non trovato. Riprova.");
                    }
                }
                //gestione del comando ":end" per uscire dalla fase di ispezione del topic
                else if (userInput.startsWith(":end"))
                {
                    //comando per terminare la sessione interattiva
                    notifyUsers("#session_end", topic);
                    //l'elenco dei messaggi di ispezione di tutti i client mentre il server era in fase di ispezione
                    this.processAllInspectMessage();
                    //imposto il topic come il server non è più in ispezione
                    topic.setInInspection(false);
                    System.out.println("Sessione interattiva per il topic " + topic.getName().toUpperCase() + " chiusa");
                    //esci dal ciclo e termina la sessione di ispezione al server
                    break;
                }
                else
                {
                    System.out.println("Comando non riconosciuto nella sessione interattiva.");
                }
            }
        }
        catch (Exception e)
        {
            System.err.println("Errore durante la lettura dell'input nella sessione interattiva: " + e.getMessage());
        }
    }

    //metodo per elaborare tutti i messaggi di ispezione per tutti i client
    private void processAllInspectMessage()
    {
        synchronized (Server.listClient) {
            for (ClientHandler client : Server.listClient) {
                //elenco dei messaggi di ispezione per ogni client
                client.getClient().processInspectMessages();
            }
        }
    }
}