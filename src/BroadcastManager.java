import java.util.HashMap;
import java.util.Map;

public class BroadcastManager {
    private final Map<Integer, Master> threads;

    public BroadcastManager() {
        threads = new HashMap<Integer, Master>();
    }

    public void addThread(int id, Master master) {
        synchronized (threads) {
            threads.put(id, master);
        }
    }

    public <T> void notifyThread(BackendMessage<T> msg) {
        Master thread;
        int id = msg.getId();
        synchronized (threads) {
            thread = threads.remove(id);
        }
        thread.setReducerReturn(msg.getValue());
        synchronized (thread) {
            thread.notify();
        }
    }
}
