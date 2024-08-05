import java.util.ArrayList;
import java.util.List;

public class Topic {
    private final String name;
    private final List<Message> messages = new ArrayList<>();
    private final List<Subscriber> subscribers = new ArrayList<>();

    public Topic(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    // Aggiunge un messaggio al topic e notifica tutti i subscriber
    public synchronized void addMessage(Message message) {
        messages.add(message);
        notifySubscribers(message);
    }


    // Restituisce tutti i messaggi del topic
    public synchronized List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    // Aggiunge un subscriber alla lista dei subscriber
    public synchronized void subscribe(Subscriber client) {
        subscribers.add(client);
    }

    // Notifica tutti i subscriber con un nuovo messaggio
    private void notifySubscribers(Message message) {
        for (Subscriber client : subscribers) {
            client.sendMessage(message);
        }
    }

    // Elimina un messaggio dal topic dato il suo ID
    public synchronized void deleteMessage(int id) {
        messages.removeIf(message -> message.getId() == id);
    }
}