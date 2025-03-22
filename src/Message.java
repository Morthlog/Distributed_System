import java.io.Serializable;

public class Message <T> implements Serializable {
    private T value;
    private int id;

    public Message(){}

    public Message(T value) {
        this.value = value;
    }

    public Message(T value, int id) {
        this(value);
        this.id = id;
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


}
