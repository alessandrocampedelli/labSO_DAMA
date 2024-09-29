import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message
{
    //contatore statico per generare ID univoci per ogni messaggio
    public static Integer counter = 0;
    //l'ID univoco del messaggio
    private final int id;
    //il testo del messaggio
    private final String text;
    //la data e l'ora di creazione del messaggio
    private final String dataOra;
    public static final Object lockcounter = new Object();

    //metodo costruttore per inizializzare un nuovo messaggio
    public Message(String text)
    {
        this.id = ++counter;
        this.text = text.trim();
        LocalDateTime now = LocalDateTime.now();
        //formattazione della data e ora nel formato "dd/MM/yyyy HH:mm:ss"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        //conversione della data e ora formattate in una stringa
        this.dataOra = now.format(formatter);
    }

    //metodo per ottenere l'ID del messaggio
    public int getId()
    {
        return id;
    }

    //metodo per ottenere il testo del messaggio
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