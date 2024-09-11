import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.List;
import java.util.Scanner;

//DA CONTROLLARE COMMENTI GENERATI!!

// Classe che gestisce le operazioni del server in esecuzione.
// Estende Thread per permettere l'esecuzione concorrente del server.
public class ServerHandler extends Thread
{
    private final ServerSocket serverSocket; // Socket del server per accettare connessioni dai client
    private final List<ClientHandler> listClient; // Lista dei client connessi al server
    private final Scanner stdIn; // Scanner per leggere l'input dell'utente dal terminale
    private final PrintWriter out = new PrintWriter(System.out, true); // Writer per stampare messaggi nel terminale

    // Costruttore della classe. Inizializza il ServerSocket e la lista dei ClientHandler.
    public ServerHandler(ServerSocket serverSocket, List<ClientHandler> listClient)
    {
        this.serverSocket = serverSocket;
        this.listClient = listClient;
        this.stdIn = new Scanner(System.in); // Inizializza lo Scanner per leggere l'input dal terminale
    }

    @Override
    public void run()
    {
        try
        {
            String userInput;
            // Ciclo principale per leggere l'input dell'utente e gestire i comandi
            while ((userInput = stdIn.nextLine()) != null)
            {
                userInput = userInput.toLowerCase(); // Converti l'input in minuscolo per una gestione uniforme dei comandi
                if (userInput.startsWith("quit"))
                {
                    // Comando per terminare il server. Notifica tutti i client e chiude il server.
                    notifyUsers("#close", null);
                    stopServer();
                    break; // Esci dal ciclo e termina il thread
                }
                else if (userInput.startsWith("show"))
                {
                    // Comando per mostrare i topic disponibili
                    Server.showTopics(out);
                }
                else if (userInput.startsWith("inspect "))
                {
                    // Comando per iniziare una sessione interattiva su un topic specificato
                    String topicName = userInput.split(" ", 2)[1].trim(); // Estrai il nome del topic
                    Topic topic = getTopicByName(topicName); // Trova il topic per nome
                    if (topic == null)
                    {
                        System.out.println("Errore: Topic '" + topicName + "' non trovato.");
                    }
                    else
                    {
                        // Notifica i client che la sessione di ispezione sta per iniziare
                        notifyUsers("#session_start", topic);
                        sessioneInterattiva(topic); // Avvia la sessione interattiva
                    }
                }
                else
                {
                    System.out.println("Comando non riconosciuto.");
                }
            }
        }
        catch (Exception e)
        {
            // Gestione delle eccezioni durante l'esecuzione del server
            System.err.println("Errore durante l'esecuzione del server: " + e.getMessage());
            e.printStackTrace(); // Stampa lo stack trace per il debug
        }
        finally
        {
            stdIn.close(); // Chiudi lo Scanner
        }
    }

    // Metodo per notificare tutti i client o solo quelli iscritti a un topic specifico
    private void notifyUsers(String message, Topic topic)
    {
        synchronized (listClient) // Sincronizza l'accesso alla lista dei client
        {
            for (ClientHandler client : listClient)
            {
                // Salta i client che non sono ancora completamente registrati
                if(client.getClient() == null)
                {
                    continue;
                }
                // Notifica solo i client iscritti al topic specificato (se esiste)
                if (topic == null || client.getClient().getTopic().equals(topic))
                {
                    try
                    {
                        // Crea un PrintWriter per inviare il messaggio al client
                        PrintWriter out = new PrintWriter(client.getClient().getClientSocket().getOutputStream(), true);
                        out.println(message);
                    }
                    catch (IOException e)
                    {
                        // Gestione delle eccezioni durante l'invio del messaggio
                        System.err.println("Errore durante l'invio della notifica al client: " + e.getMessage());
                    }
                }
            }
        }
    }

    // Metodo per fermare il server e chiudere tutte le connessioni
    private void stopServer() throws IOException
    {
        synchronized (listClient) // Sincronizza l'accesso alla lista dei client
        {
            for (ClientHandler client : listClient)
            {
                // Salta i client non completamente registrati
                if(client.getClient() == null)
                {
                    continue;
                }
                try
                {
                    // Chiudi il socket del client
                    client.getClient().getClientSocket().close();
                }
                catch (IOException e)
                {
                    // Gestione delle eccezioni durante la chiusura del socket
                    System.err.println("Errore durante la chiusura del socket client: " + e.getMessage());
                }
            }
            listClient.clear(); // Pulisci la lista dei client
        }
        try
        {
            if (!serverSocket.isClosed())
            {
                // Chiudi il ServerSocket se non è già chiuso
                serverSocket.close();
                System.out.println("ServerSocket chiuso.");
            }
        }
        catch (IOException e)
        {
            // Gestione delle eccezioni durante la chiusura del ServerSocket
            System.err.println("Errore durante la chiusura del ServerSocket: " + e.getMessage());
        }
        System.out.println("Server scollegato...");
    }

    // Metodo per trovare un topic per nome
    private Topic getTopicByName(String name)
    {
        synchronized (Server.topics) // Sincronizza l'accesso alla lista dei topic
        {
            for (Topic topic : Server.topics)
            {
                if (topic.getName().equals(name))
                {
                    return topic; // Ritorna il topic se trovato
                }
            }
        }
        return null; // Ritorna null se il topic non è stato trovato
    }

    // Metodo per gestire una sessione interattiva su un topic
    private void sessioneInterattiva(Topic topic)
    {
        topic.setInInspection(true); // Imposta il topic come in ispezione
        System.out.println("Sessione interattiva per il topic " + topic.getName().toUpperCase() + " iniziata");
        try
        {
            String userInput;
            while ((userInput = stdIn.nextLine()) != null)
            {
                userInput = userInput.toLowerCase(); // Converti l'input in minuscolo
                if (userInput.startsWith(":listall"))
                {
                    // Comando per elencare tutti i messaggi del topic
                    if (topic.getMessages().isEmpty())
                    {
                        System.out.println("Nel topic "+topic.getName().toUpperCase()+" non è stato inviato alcun messaggio");
                    }
                    else
                    {
                        System.out.println((topic.getMessages().size() == 1 ? "Un messaggio inviato ": topic.getMessages().size()+" messaggi inviati ")+"sul topic " + topic.getName().toUpperCase() + ":");
                        for (Message message : topic.getMessages())
                        {
                            System.out.println(message.toString()+"\n"); // Mostra ogni messaggio
                        }
                    }
                }
                else if (userInput.startsWith(":delete "))
                {
                    // Comando per eliminare un messaggio specificato
                    boolean trovato = false;
                    String messageIdStr = userInput.split(" ", 2)[1].trim(); // Estrai l'ID del messaggio
                    int messageId = Integer.parseInt(messageIdStr); // Converte l'ID in int
                    // Verifica se il messaggio con l'ID specificato esiste e lo elimina
                    for (Message message : topic.getMessages())
                    {
                        if(message.getId() == messageId)
                        {
                            trovato = true;
                            break;
                        }
                    }
                    if(trovato)
                    {
                        topic.deleteMessage(messageId);
                        System.out.println("Eliminazione messaggio avente id " + "'" + messageId + "'" + " avvenuta con successo.");
                    }
                    else
                    {
                        System.out.println("ERRORE: messaggio avente id " + "'" + messageId + "'" + " non trovato. Riprova.");
                    }
                }
                else if (userInput.startsWith(":end"))
                {
                    // Comando per terminare la sessione interattiva
                    notifyUsers("#session_end", topic);
                    this.processAllInspectMessage(); // Elenco dei messaggi di ispezione a tutti i client
                    topic.setInInspection(false); // Imposta il topic come non in ispezione
                    System.out.println("Sessione interattiva per il topic " + topic.getName().toUpperCase() + " chiusa");
                    break; // Esci dal ciclo e termina la sessione interattiva
                }
                else
                {
                    System.out.println("Comando non riconosciuto nella sessione interattiva.");
                }
            }
        }
        catch (Exception e)
        {
            // Gestione delle eccezioni durante la lettura dell'input nella sessione interattiva
            System.err.println("Errore durante la lettura dell'input nella sessione interattiva: " + e.getMessage());
        }
    }

    // Metodo per elaborare tutti i messaggi di ispezione per tutti i client
    private void processAllInspectMessage()
    {
        for (ClientHandler client : listClient)
        {
            client.getClient().processInspectMessages(); // Elenco dei messaggi di ispezione per ogni client
        }
    }
}