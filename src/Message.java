import java.time.LocalDateTime;

public class Message {
    private static int counter = 0;
    private final int id;
    private final String text;
    private final LocalDateTime timestamp;

    public Message(String text) {
        this.id = ++counter;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ID: " + id + "\nTesto: " + text + "\nData: " + timestamp;
    }
}