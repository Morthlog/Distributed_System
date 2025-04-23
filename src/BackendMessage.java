public class BackendMessage <T> extends Message<T> {
    private int id;
    private SaveState saveState;

    public BackendMessage(T value){
        super(value);
    }

    public BackendMessage(Message<T> message){
        setValue(message.getValue());
        setClient(message.getClient());
        setRequest(message.getRequest());
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

    public SaveState getSaveState() {
        return saveState;
    }

    public void setSaveState(SaveState saveState) {
        this.saveState = saveState;
    }
}
