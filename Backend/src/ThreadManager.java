import java.util.HashMap;
import java.util.Map;

public class ThreadManager {
    private final Map<Integer, Master> threads;
    private boolean locked = false;
    private final Object lock = new Object();

    public ThreadManager() {
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
        synchronized (threads)
        {
            thread = threads.remove(id);
        }
        synchronized (thread)
        {
            thread.setReturnValStorage(msg.getValue());
            thread.notify();
        }
    }

    public void notifyAllThreads() {
        setLocked(false);
        synchronized (threads) {
            for (Master thread : threads.values()) {
                synchronized (thread)
                {
                    thread.notify();
                }
            }
        }
    }

    public void setLocked(boolean locked) {
        synchronized (lock) {
            this.locked = locked;
        }
    }

    public boolean isLocked() {
        synchronized (lock) {
            return locked;
        }
    }
}
