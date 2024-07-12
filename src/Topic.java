import java.util.ArrayList;

//qui si svolgeranno le operazioni sul topic (eliminazione)
public class Topic
{
    private String name;
    private ArrayList<Messaggio> messaggi = new ArrayList<Messaggio>();

    public Topic(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }
}
