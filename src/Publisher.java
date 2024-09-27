import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Publisher extends User
{
    //lista per memorizzare i messaggi inviati dal publisher
    private final List<Message> messaggiUtente = new ArrayList<>();

    //metodo costruttore che inizializza la socket e il topic
    public Publisher(Socket socket, Topic topic)
    {
        super(socket, topic);
    }

    //metodo per gestire i comandi ricevuti dal client
    @Override
    public void handleCommand(String inputLine)
    {
        //conversione del comando in minuscolo per uniformità
        inputLine = inputLine.toLowerCase();
        //gestione del comando "publish" per registrarsi ad un topic
        if (inputLine.startsWith("publish "))
        {
            //controllo se il client non è già registrato al topic
            if(!clientCreate)
            {
                //estrazione del nome del topic dal comando
                String topicName = inputLine.substring(8).trim();
                //recupero il topic se è già esistente altrimenti lo creo
                currentTopic = Server.getOrCreateTopic(topicName);
                out.println("Registrazione avvenuta con successo come PUBLISHER per il topic " + topicName.toUpperCase()+".");
                clientCreate = true;
            }
            else
            {
                out.println("ERRORE: non puoi effettuare una nuova registrazione a un diverso topic nella stessa esecuzione.");
            }
        }
        //gestione del comando "send" per inviare un messaggio
        else if (inputLine.startsWith("send "))
        {
            //estrazione del testo del messaggio dal comando
            String messageText = inputLine.substring(5);
            Message message;
            synchronized (Message.counter) {
                message = new Message(messageText);
            }
            if (currentTopic != null)
            {
                //verifica che il messaggio non sia vuoto
                if(!message.getText().isEmpty())
                {
                    //aggiungo il messaggio appena scritto al topic
                    currentTopic.addMessage(message);
                    //aggiungi il messaggio appena scritto alla lista dei messaggi dell'utente
                    messaggiUtente.add(message);
                    out.println("Messaggio inviato con successo.");
                }
                else
                {
                    out.println("Errore: contenuto del messaggio non presente.");
                }
            }
            else
            {
                out.println("Prima di inviare un messaggio devi prima specificare il topic.");
            }
        }
        //gestione del comando "list" per mostrare i messaggi inviati da questo client
        else if (inputLine.equals("list"))
        {
            //controllo se il publisher è registrato ad un topic
            if (currentTopic != null)
            {
                //verifica se l'utente ha inviato almeno un messaggio
                if(!messaggiUtente.isEmpty())
                {
                    //visualizzazione della lista dei messaggi inviati dall'utente sul topic corrente
                    out.println((this.messaggiUtente.size() == 1 ? "Un messaggio inviato": this.messaggiUtente.size()+" messaggi inviati")+" da te sul topic "+currentTopic.getName().toUpperCase()+":");
                    for (Message message : messaggiUtente)
                    {
                        out.println(message);
                    }
                }
                else
                {
                    out.println("Non hai ancora inviato alcun messaggio sul topic "+currentTopic.getName().toUpperCase());
                }
            }
            else
            {
                out.println("Per poter riceve la lista dei tuoi messaggi inviati devi prima iscriverti a un topic.");
            }
        }
        //gestione del comando "listall" per mostrare tutti i messaggi pubblicati sul topic da tutti i client
        else if (inputLine.equals("listall"))
        {
            //controllo se il publisher è registrato a un topic
            if (currentTopic != null)
            {
                synchronized (currentTopic.getMessages()) {
                    //verifica se ci sono messaggi pubblicati sul topic
                    if (!currentTopic.getMessages().isEmpty()) {
                        //visualizzazione della lista di tutti i messaggi pubblicati sul topic
                        out.println((currentTopic.getMessages().size() == 1 ? "Un messaggio pubblicato" : currentTopic.getMessages().size() + " messaggi pubblicati") + " sul topic " + currentTopic.getName().toUpperCase() + ":");
                        for (Message message : currentTopic.getMessages()) {
                            out.println(message);
                        }
                    } else {
                        out.println("Non è stato ancora pubblicato alcun messaggio sul topic " + currentTopic.getName().toUpperCase());
                    }
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
        //gestione del comando "inspect" per avvisare l'utente che il topic è in ispezione
        else if (inputLine.equals("inspect"))
        {
            out.println("Il topic è in ispezione. Il messaggio verrà elaborato dal server una volta terminata la fase di ispezione");
        }
        //caso in cui l'utente scrive un comando inesistente scrive "quit" (la stampa corretta avviene nella classe Client.java)
        else if(!inputLine.equals("quit"))
        {
            out.println("Comando sconosciuto.");
        }
    }

    //metodo che restituisce la lista dei messaggi inviati da parte dell'utente
    public List<Message> getMessaggiUtente()
    {
        return messaggiUtente;
    }
}