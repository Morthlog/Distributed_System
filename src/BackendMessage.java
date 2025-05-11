public class BackendMessage <T> extends Message<T> {
    private int id;
    private SaveState saveState;
    private boolean callReducer = false;

    public BackendMessage(T value){
        super(value);
        setSaveState(SaveState.MEMORY);
    }

    public BackendMessage(Message<T> message){
        this();
        setValue(message.getValue());
        setClient(message.getClient());
        setRequest(message.getRequest());
    }

    public BackendMessage() {
        setSaveState(SaveState.MEMORY);
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

    public boolean isCallReducer() {
        return callReducer;
    }

    public void setCallReducer(boolean callReducer) {
        this.callReducer = callReducer;
    }
}
