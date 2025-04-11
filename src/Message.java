import java.io.Serializable;

public class Message <T> implements Serializable {
    private T value;
    private int id;
    private Client client;
    private int request;

    public Message(){}

    public Message(T value) {
        this.value = value;
    }
    
    public Message(T value, Client client, int request) {
        this(value);
        this.client = client;
        this.request = request;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public int getId() {
        return id;
    }

    /** id is only set from Master for MapReduce
     * @param id message id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return client's type
     */
    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Identifier that corresponds to the type of request
     * @return request code
     */
    public int getRequest() {
        return request;
    }

    /**
     * Identifier that corresponds to the type of request
     */
    public void setRequest(int request) {
        this.request = request;
    }
}
