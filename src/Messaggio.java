import java.time.LocalDateTime;

public class Messaggio
{
    private static int counter = 0;
    private int id;
    private String text;
    private LocalDateTime timestamp;

    public Messaggio(String text)
    {
        this.id = counter + 1;
        this.text = text;
        this.timestamp = LocalDateTime.now();
    }

    public int getId()
    {
        return id;
    }

    public String getText()
    {
        return text;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public String toString()
    {
        return "ID: " + id + "\nTesto: " + text + "\nData: " + timestamp;
    }
}
