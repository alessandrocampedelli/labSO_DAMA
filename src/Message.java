import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message
{
    private static int counter = 0;
    private final int id;
    private final String text;
    private final String dataOra;

    public Message(String text)
    {
        this.id = ++counter;
        this.text = text.trim();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        this.dataOra = now.format(formatter);
    }

    public int getId()
    {
        return id;
    }

    public String getText()
    {
        return text;
    }

    @Override
    public String toString()
    {
        return "ID: " + id + "\nTesto: " + text + "\nData: " + dataOra;
    }
}