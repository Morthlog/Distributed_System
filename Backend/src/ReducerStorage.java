import lib.shared.Client;
import lib.shared.RequestCode;

import java.util.ArrayList;
import java.util.List;

public class ReducerStorage {
    private int counter;
    private final List<Object> data;
    private final Client client;
    private final RequestCode requestCode;

    ReducerStorage(int counter,Client client, RequestCode requestCode) {
        this.counter = counter;
        this.client = client;
        this.requestCode = requestCode;
        data = new ArrayList<Object>();
    }

    public Client getClient() {
        return client;
    }

    public int getCounter() {
        return counter;
    }

    public RequestCode getRequestCode() {
        return requestCode;
    }

    public List<Object> getData() {
        return data;
    }

    public void addData(Object data) {
        this.data.add(data);
    }

    public void reduceCounter() {
        counter--;
    }

    public void reset(int workerCount) {
        counter = workerCount;
        data.clear();
    }
}
