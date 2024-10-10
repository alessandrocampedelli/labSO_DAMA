import java.util.ArrayList;
import java.util.List;

public class Topic
{
    //il nome del topic
    private final String name;
    //la lista dei messaggi pubblicati sul topic
    private final List<Message> messages = new ArrayList<>();
    //la lista dei subscriber iscritti al topic
    private final List<Subscriber> subscribers = new ArrayList<>();
    //flag per indicare se il topic è in ispezione
    private boolean inInspection = false;

    //metodo costruttore che inizializza il nome del topic
    public Topic(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    //metodo che restituisce lo stato del flag di ispezione
    public boolean isInInspection()
    {
        return inInspection;
    }

    //metodo che imposta lo stato del flag di ispezione
    public void setInInspection(boolean inInspection)
    {
        this.inInspection = inInspection;
    }

    //metodo che aggiunge un messaggio al topic e notifica tutti i subscriber
    public void addMessage(Message message)
    {
        synchronized (messages)
        {
            //aggiunge il messaggio alla lista dei messaggi
            messages.add(message);
            //notifica tutti i subscriber connessi del nuovo messaggio
            notifySubscribers(message);
        }
    }

    //metodo che restituisce tutti i messaggi del topic
    public List<Message> getMessages()
    {
        synchronized (messages)
        {
            return new ArrayList<>(messages);
        }
    }

    //metodo che aggiunge un subscriber alla lista dei subscriber
    public void subscribe(Subscriber client)
    {
        synchronized (subscribers)
        {
            subscribers.add(client);
        }
    }

    //metodo che notifica tutti i subscriber con un nuovo messaggio
    private void notifySubscribers(Message message)
    {
        synchronized (subscribers)
        {
            for (Subscriber client : subscribers)
            {
                //invia il messaggio a ciascun subscriber connesso
                client.sendMessage(message);
            }
        }
    }

    //metodo che rimuove un messaggio dal topic dato il suo ID
    public void deleteMessage(int id)
    {
        synchronized (messages) {
            messages.removeIf(message -> message.getId() == id);
        }
    }
}