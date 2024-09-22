import java.net.Socket;

public class Subscriber extends User
{
    //metodo costruttore che inizializza la socket e il topic
    public Subscriber(Socket socket, Topic topic)
    {
        super(socket, topic);
    }

    //metodo per gestire i comandi ricevuti dal client
    @Override
    public void handleCommand(String inputLine)
    {
        //conversione del comando in minuscolo per uniformità
        inputLine = inputLine.toLowerCase();
        //gestione del comando "subscribe" per registrarsi ad un topic
        if (inputLine.startsWith("subscribe "))
        {
            //controllo se il client non è già registrato al topic
            if(!clientCreate)
            {
                //estrazione del nome del topic dal comando
                String topicName = inputLine.substring(10).trim();
                //recupero il topic se è già esistente altrimenti lo creo
                currentTopic = Server.getOrCreateTopic(topicName);
                //iscrivo il subscriber ala lista dei subscriber
                currentTopic.subscribe(this);
                out.println("Registrazione avvenuta con successo come SUBSCRIBER per il topic " + topicName.toUpperCase()+".");
                clientCreate = true;
            }
            else
            {
                out.println("ERRORE: non puoi effettuare una nuova registrazione a un diverso topic nella stessa esecuzione.");
            }
        }
        //gestione del comando "listall" per mostrare tutti i messaggi pubblicati sul topic da tutti i client
        else if (inputLine.equals("listall"))
        {
            if (currentTopic != null)
            {
                //verifica se ci sono messaggi pubblicati sul topic
                if(!currentTopic.getMessages().isEmpty())
                {
                    //visualizzazione della lista di tutti i messaggi pubblicati sul topic
                    out.println((currentTopic.getMessages().size() == 1 ? "Un messaggio pubblicato": currentTopic.getMessages().size()+" messaggi pubblicati")+" sul topic "+currentTopic.getName().toUpperCase()+":");
                    for (Message message : currentTopic.getMessages())
                    {
                        out.println(message);
                    }
                }
                else
                {
                    out.println("Non è stato ancora pubblicato alcun messaggio sul topic "+currentTopic.getName().toUpperCase());
                }
            }
            else
            {
                out.println("Non sei iscritto o non stai pubblicando su alcun topic.");
            }
        }
        //gestione del comando "show" per mostrare tutti i topic disponibili a cui è possibile collegarsi
        else if (inputLine.equals("show"))
        {
            Server.showTopics(out);
        }
        //gestione del comando "quit" per disconnettersi
        else if (inputLine.equals("quit"))
        {
            out.println("Disconnessione in corso...");
        }
        //gestione del comando "inspect" per avvisare l'utente che il topic è in ispezione
        else if (inputLine.equals("inspect"))
        {
            out.println("Il topic è in ispezione. Il messaggio verrà elaborato dal server una volta terminata la fase di ispezione");
        }
        else
        {
            out.println("Comando sconosciuto.");
        }
    }

    //metodo che invia un messaggio al client con il nome e il contenuto del messaggio
    public void sendMessage(Message message)
    {
        out.println("Nuovo messaggio inviato sul topic " + currentTopic.getName().toUpperCase() + ":\n" + message);
    }
}