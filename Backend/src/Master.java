import java.io.*;
import java.util.*;

// import json-simple in project structure
import lib.shared.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.net.*;

public class Master extends Communication
{
    private static Integer id = 0;
    private static Integer nextWorkerId;
    private static int[] hashingVariance;

    private static int n_workers;
    private static final HashMap<String, Integer> storeToWorkerMemory = new HashMap<>();
    private static final HashMap<String, Integer> storeToWorkerBackup = new HashMap<>();
    private static Holder<Boolean>[] activeWorkers;
    private static final Holder<Integer> activeRequests = new Holder<>(0);

    private TCPServer serverClient = null;
    private static final List<TCPServer> serverWorker = new ArrayList<>();
    private static TCPServer serverReducer = null;

    private static final ThreadManager broadcastManager = new ThreadManager();
    private static final ThreadManager reestablishWorkerManager = new ThreadManager();
    private final Holder<Object> returnValStorage = new Holder<>();

    public Master(Socket connection){
        try {
            serverClient = new TCPServer(connection);
        }catch (IOException e){
            throw new RuntimeException(e);
        }

    }

    public Master(){}

    private <T,T1> T1 broadcast(BackendMessage<T> msg) throws SocketTimeoutException{
        List<Thread> threads = new ArrayList<>();
        Holder<Boolean> gotException = new Holder<>(false);
        msg.setCallReducer(true);
        broadcastManager.addThread(msg.getId(), this);
        for (int i = 0; i < n_workers; i++){
            synchronized (activeWorkers[i]){
                if (!activeWorkers[i].get())
                    continue;
            }

            final int workerId = i;
            Thread thread = new Thread(() -> {
                try {
                    singleWorker(msg, workerId);
                } catch (Exception e) {
                    gotException.set(true);
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads){
            try
            {
                thread.join();
            } catch (InterruptedException e)
            {
                System.err.println(e.getMessage());
            }
        }

        try {
            synchronized (this)
            {
                if (getReturnValStorage() == null)
                    this.wait(); // prevent sleep when notification has been sent already
            }
        }catch (InterruptedException e){
            System.err.println(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        if (gotException.get())
        {
            messageReducer(RequestCode.RESET, msg.getId());
            setReturnValStorage(null);
            throw new SocketTimeoutException();
        }

        return (T1) getReturnValStorage();
    }

    private void messageReducer(RequestCode code) {
        messageReducer(code, -1);
    }

    private void messageReducer(RequestCode code, int requestId) {
        BackendMessage<Integer> msg = new BackendMessage<>();
        msg.setId(requestId);
        msg.setRequest(code);
        msg.setClient(Client.MASTER);
        String ipReducer;
        try
        {
            ipReducer = InetAddress.getLocalHost().getHostAddress(); // Reducer ip
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
        startConnection(ipReducer, Reducer.serverPort);
        sendMessage(msg);
        receiveMessage();
        stopConnection();
    }

    private <T,T1> T1 singleWorker(BackendMessage<T> msg, int worker) throws SocketTimeoutException{
        Message<T1> response;
        TCPServer server;
        TCPServer currentConnection;
        int workerId = -1;
        if (msg.getSaveState() == SaveState.MEMORY)
        {
            try{
                server = serverWorker.get(worker);
                synchronized (server){
                    currentConnection = new TCPServer(server.serverSocket.accept());
                }

                currentConnection.sendMessage(msg);
                if (msg.isCallReducer()) // only broadcast receives it
                    return null;

                response = currentConnection.receiveMessage();
                setReturnValStorage(response);
                msg.setSaveState(((BackendMessage<T1>)response).getSaveState());
                currentConnection.stopConnection();
            }
            catch(SocketException e){
                disableWorker(worker);
                throw new SocketTimeoutException();
            }
            catch(IOException e){
                System.err.println("Couldn't start server: " + e.getMessage());
                return null;
            }
        }

        if (msg.getSaveState() == SaveState.BACKUP)
        {
            try {
                workerId = storeToWorkerBackup.get(((StoreNameProvider) msg.getValue()).getStoreName());
                boolean canBackup;
                synchronized (activeWorkers[workerId])
                {
                    canBackup = activeWorkers[workerId].get() && workerId != worker;
                }
                if (canBackup) { // no secondary backup location exists
                    server = serverWorker.get(workerId);
                    synchronized (server) {
                        currentConnection = new TCPServer(server.serverSocket.accept());
                    }
                    currentConnection.sendMessage(msg);
                }
            } catch (SocketException e) {
                disableWorker(workerId);
                throw new SocketTimeoutException();
            } catch (IOException e) {
                System.err.println("Couldn't start server: " + e.getMessage());
            }
        }
        return ((Message<T1>) getReturnValStorage()).getValue(); // only used on non-broadcast calls
    }

    private synchronized void disableWorker(int workerId){
        System.out.println("Disabling worker " + workerId);
        synchronized (activeWorkers[workerId]){
            if (!activeWorkers[workerId].get())
                return;
        }

        try{
            Vector<Vector<String>> stores = new Vector<>();
            for (int i = 0; i < n_workers; i++){
                stores.add(new Vector<>());
            }

            for (var set : storeToWorkerMemory.entrySet()){
                if (!set.getValue().equals(workerId))
                    continue;
                int backupId = storeToWorkerBackup.get(set.getKey());
                stores.get(backupId).add(set.getKey());
            }

            for (int i = 0; i < n_workers; i++){
                if (i == workerId)
                    continue;
                TCPServer server = serverWorker.get(i);
                TCPServer currentConnection;
                synchronized (server){
                    currentConnection = new TCPServer(server.serverSocket.accept());
                }
                Message<Vector<String>> msg = new BackendMessage<>(stores.get(i));
                msg.setClient(Client.MASTER);
                msg.setRequest(RequestCode.TRANSFER_TO_MEMORY);
                currentConnection.sendMessage(msg);
                currentConnection.receiveMessage();
            }
            
            messageReducer(RequestCode.REMOVE_WORKER);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        synchronized (activeWorkers[workerId]){
            activeWorkers[workerId].set(false); // Master is free to redirect to other workers
        }
        Thread t = new Thread(() -> reestablishWorker(workerId));
        t.start();
    }

    private void reestablishWorker(int workerId){
        TCPServer server = serverWorker.get(workerId);
        Vector<Store> dataMemory = new Vector<>();
        Vector<Store> dataBackup = new Vector<>();
        while(true)
        {
            try {
                server.startConnection(); // simple ping
                break;
            } catch (Exception e) {} // times out after a while
        }

        reestablishWorkerManager.setLocked(true);
        while(activeRequests.get() != 0)
        {
            onSpinWait();
        }

        Vector<Vector<String>> storesMemory = new Vector<>();
        Vector<Vector<String>> storesBackup = new Vector<>();
        for (int i = 0; i < n_workers; i++){
            storesMemory.add(new Vector<>());
            storesBackup.add(new Vector<>());
        }
        for (var set : storeToWorkerMemory.entrySet()){
            if (!set.getValue().equals(workerId))
                continue;
            int backupId = storeToWorkerBackup.get(set.getKey());
            storesMemory.get(backupId).add(set.getKey());
        }
        for (var set : storeToWorkerBackup.entrySet()){
            if (!set.getValue().equals(workerId))
                continue;
            int memoryId = storeToWorkerMemory.get(set.getKey());
            storesBackup.get(memoryId).add(set.getKey());
        }

        for (int i = 0; i < n_workers; i++){
            if (i == workerId)
                continue;
            TCPServer serverB = serverWorker.get(i);

            try {
                serverB.startConnection();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            Message<Vector<String>> msg = new BackendMessage<>(storesMemory.get(i));
            msg.setClient(Client.MASTER);
            msg.setRequest(RequestCode.TRANSFER_TO_BACKUP);
            serverB.sendMessage(msg);
            dataMemory.addAll((Vector<Store>)serverB.receiveMessage().getValue());

            try {
                serverB.startConnection();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            msg = new BackendMessage<>(storesBackup.get(i));
            msg.setClient(Client.MASTER);
            msg.setRequest(RequestCode.GET_STORES);
            serverB.sendMessage(msg);
            dataBackup.addAll((Vector<Store>)serverB.receiveMessage().getValue());

        }
        try
        {
            for (Store store : dataMemory){
                server.startConnection();
                Message<Store> msg = new BackendMessage<>(store);
                msg.setRequest(RequestCode.INIT_MEMORY);
                server.sendMessage(msg);
            }

            for (Store store : dataBackup){
                server.startConnection();
                Message<Store> msg = new BackendMessage<>(store);
                msg.setRequest(RequestCode.INIT_BACKUP);
                server.sendMessage(msg);
            }

            Message<Store> msg = new Message<>();
            msg.setRequest(RequestCode.END_INIT_MEMORY);
            server.startConnection();
            server.sendMessage(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        for (var set : storeToWorkerBackup.entrySet()){
            if (!set.getValue().equals(workerId))
                continue;
            storeToWorkerBackup.replace(set.getKey(), workerId);
        }

        synchronized (activeWorkers[workerId]){
            activeWorkers[workerId].set(true);
        }
        messageReducer(RequestCode.ADD_WORKER);
        reestablishWorkerManager.notifyAllThreads();
    }

    /**
     * Remove first "if" when stub is no longer needed
     *
     */
    private <T,T1> T1 findCorrectWorker(BackendMessage<T> msg) throws SocketTimeoutException{
        Integer workerId;
        if (msg.getRequest() == RequestCode.STUB_TEST_1 || msg.getRequest() == RequestCode.STUB_TEST_2)
            return singleWorker(msg, 0); // only for stub, should be removed with stub

        String storeName = ((StoreNameProvider) msg.getValue()).getStoreName();
        workerId = storeToWorkerMemory.get(storeName);

        if (workerId == null) // case of new store
        {
            workerId = getNextWorkerId();
            synchronized (storeToWorkerMemory){
                storeToWorkerMemory.put(storeName, workerId);
            }
            int backupWorkerId = createBackupId(workerId);

            synchronized (storeToWorkerBackup){
                storeToWorkerBackup.put(storeName, backupWorkerId);
            }
        }
        synchronized (activeWorkers[workerId]){
            if (!activeWorkers[workerId].get())
                workerId = storeToWorkerBackup.get(storeName);
        }


        return singleWorker(msg, workerId);
    }

    /**
     * Current values are temporary
     */
    private <T,T1> T1 callAppropriateWorker(BackendMessage<T> msg){
        Client client = msg.getClient();
        RequestCode code = msg.getRequest();
        try{
            return switch (client) {
                case Customer -> switch (code) { // replace with appropriate cases
                    case STUB_TEST_1,BUY, RATE_STORE -> findCorrectWorker(msg);
                    case STUB_TEST_2, SEARCH -> broadcast(msg);
                    default -> {
                        System.err.println("Unknown customer code: " + code);
                        throw new RuntimeException();
                    }
                };
                case Manager -> switch (code) { // replace with appropriate cases
                    case ADD_STORE, ADD_PRODUCT, REMOVE_PRODUCT, MANAGE_STOCK, GET_SALES_BY_STORE -> findCorrectWorker(msg);
                    case GET_SALES_BY_STORE_TYPE, GET_SALES_BY_PRODUCT_TYPE, GET_STORES -> broadcast(msg);
                    default -> {
                        System.err.println("Unknown manager code: " + code);
                        throw new RuntimeException();
                    }
                };
                case MASTER -> null; // should never address himself
            };
        }catch (SocketTimeoutException e){
            return callAppropriateWorker(msg); // retry method after transferring Worker memory
        }

    }

    public <T,T1> Message<T1> startForBroker(BackendMessage<T> msg) {
        T1 reduced = callAppropriateWorker(msg);
        Message<T1> response = new Message<>(reduced);

        response.setClient(msg.getClient());
        response.setRequest(msg.getRequest());

        return response;
    }


    private <T> void startForClient() {
        Message<T> response;
        try {
            BackendMessage<T> msg = new BackendMessage<>(serverClient.receiveMessage());
            setIdToRequest(msg);
            if (reestablishWorkerManager.isLocked())
            {
                reestablishWorkerManager.addThread(msg.getId(), this);
                synchronized (this)
                {
                    wait();
                }
            }
            synchronized (activeRequests)
            {
                activeRequests.set(activeRequests.get() + 1);
            }

            response = startForBroker(msg);
            serverClient.sendMessage(response);

            synchronized (activeRequests)
            {
                activeRequests.set(activeRequests.get() - 1);
            }

            serverClient.stopConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private synchronized <T> void setIdToRequest(BackendMessage<T> msg){
        msg.setId(++id);
    }

    private synchronized int getNextWorkerId(){
        return nextWorkerId++;
    }

    private synchronized int createBackupId(int workerId){
        int backupWorkerId = (workerId + (++hashingVariance[workerId])) % n_workers;
        if (backupWorkerId == workerId)
            backupWorkerId = (workerId + 1) % n_workers;
        return backupWorkerId;
    }

	public void run()
	{
		this.startForClient();
	}


    // different port for each Worker
    public void connectWorkers(int size){
        for (int i = 0; i < size; i++)
            serverWorker.add(new TCPServer(TCPServer.basePort + i + 1));
        for (int i = 0; i < size; i++){
            try{
                System.out.println("Pinging " + i);
                serverWorker.get(i).startConnection(); // simple ping
                System.out.println("Ping successful");
                serverWorker.get(i).setSocketTOState(true);
            }catch (IOException e){
                e.printStackTrace();
            }
            System.out.println(TCPServer.basePort + i + 1);
        }

    }

    private void initWorkerMemory(String DATA_PATH){
        try {
            Map<String, ExtendedStore> nameToStore = new HashMap<>(); // should be extended store
            Object temp = new JSONParser().parse(new FileReader(DATA_PATH));
            JSONArray stores = (JSONArray) ((JSONObject)temp).get("Stores");
            Message<ExtendedStore> msg; // should be extended store
            int workerId = 0;
            for (int i = 0; i < stores.size(); i++){
                workerId = i % n_workers;
                TCPServer server = serverWorker.get(workerId);
                server.startConnection();

                ExtendedStore store = new ExtendedStore((JSONObject)stores.get(i));
                msg = new Message<>(store);
                msg.setRequest(RequestCode.INIT_MEMORY);
                server.sendMessage(msg);

                storeToWorkerMemory.put(store.getStoreName(), workerId);
                nameToStore.put(store.getStoreName(), store);
            }
            nextWorkerId = (workerId + 1) % n_workers; // initialize value
            hashingVariance = new int[n_workers];

            for (var set: storeToWorkerMemory.entrySet()){
                workerId = createBackupId(set.getValue());

                TCPServer server = serverWorker.get(workerId);
                server.startConnection();

                ExtendedStore store = nameToStore.get(set.getKey());
                msg = new Message<>(store);
                msg.setRequest(RequestCode.INIT_BACKUP);
                server.sendMessage(msg);

                storeToWorkerBackup.put(store.getStoreName(), workerId);
            }

            for (int i = 0; i < n_workers; i++){ // end Initialization
                serverWorker.get(i).startConnection();
                msg = new Message<>();
                msg.setRequest(RequestCode.END_INIT_MEMORY);
                serverWorker.get(i).sendMessage(msg);
                activeWorkers[i].set(true);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("Couldn't initialize Worker memory: " + e.getMessage());
            throw new RuntimeException();
        }
    }

    private static <T> void manageBroadcast(){
        while (true)
        {
            try
            {
                System.out.println("Waiting for reducer...");
                serverReducer.startConnection();
                Message<T> msg = serverReducer.receiveMessage();
                broadcastManager.notifyThread((BackendMessage<T>)msg);
            }catch (IOException e)
            {
                System.err.println("Couldn't connect to server reducer: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
    }

    public static void main(String[] args){

        n_workers = Integer.parseInt(args[0]);
        final String DATA_PATH = "./src/Data/Stores.json";
        activeWorkers = new Holder[n_workers];
        for (int i = 0; i < n_workers; i++)
            activeWorkers[i] = new Holder<>();
        serverReducer = new TCPServer(TCPServer.basePort - 1);
        // a server for each Worker
        Master server = new Master();
        server.connectWorkers(n_workers);


        server.initWorkerMemory(DATA_PATH);

        System.out.println("Initializing Reducer");
        try {
            serverReducer.startConnection();
            serverReducer.sendMessage(new BackendMessage<>(n_workers));
            Thread t = new Thread(Master::manageBroadcast);
            t.start();
        }catch (IOException e){
            e.printStackTrace();
            System.err.println("Couldn't initialize Reducer: " + e.getMessage());
        }


        ServerSocket serverClient;

        try{
            serverClient = new ServerSocket(TCPServer.basePort);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try
        {
            while (true)
            {
                System.out.println("Waiting for request...");
                Socket serverSocket = serverClient.accept();
                System.out.println("Request received");
                Thread t = new Master(serverSocket);
                t.start();
            }
        }
        catch (Exception e)
        {
            System.err.println("Couldn't start server: " + e.getMessage() );
            throw new RuntimeException(e);
        }
    }

    public Object getReturnValStorage() {
        synchronized (returnValStorage) {
            return returnValStorage.get();
        }
    }

    public void setReturnValStorage(Object val) {
        synchronized (returnValStorage) {
            returnValStorage.set(val);
        }
    }
}
