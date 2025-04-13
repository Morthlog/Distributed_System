import java.io.*;
import java.util.*;

// import json-simple in project structure
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import java.net.*;
import static java.lang.Thread.sleep;

public class Master extends Thread {
    private static Integer id = 0;

    private static int n_workers;

    private TCPServer serverClient = null;
    private static List<TCPServer> serverWorker = new ArrayList<>();

    public Master(Socket connection){
        serverClient = new TCPServer(connection);
    }

    public Master(){}

    private static <T,T1> T1 broadcast(Message<T> msg){
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < n_workers; i++){
            final int workerId = i;
            Thread thread = new Thread(() -> singleWorker(msg, workerId));
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

        return Reducer.reduce(msg);
    }

    private static <T,T1> T1 singleWorker(Message<T> msg, int worker){
        Message<T1> response;
        try{
            TCPServer server = serverWorker.get(worker);
            TCPServer currentConnection;
            synchronized (server){
                currentConnection = new TCPServer(server.serverSocket.accept());
            }

            currentConnection.sendMessage(msg);
            response = currentConnection.receiveMessage();

            List<Object> list = Reducer.map.get(msg.getId());
            synchronized (list)
            {
                list.add(response.getValue());
            }
            currentConnection.stopConnection();
            return response.getValue(); // only used on non-broadcast calls
        }
        catch(IOException e){
            System.err.println("Couldn't start server: " + e.getMessage());
        }
        return null;
    }


    /**
     * Current values are temporary
     */
    private <T,T1> T1 callAppropriateWorker(Message<T> msg){
        Client client = msg.getClient();
        RequestCode code = msg.getRequest();
        int workerId = 0;
        // =========
          // if msg.val is Store, set workerId to the correct worker
        // =========
        return switch (client) {
            case Customer -> switch (code) { // replace with appropriate cases
                case STUB_TEST_1 -> singleWorker(msg, workerId);
                case STUB_TEST_2 -> broadcast(msg);
                default -> {
                    System.err.println("Unknown customer code: " + code);
                    throw new RuntimeException();
                }
            };
            case Manager -> switch (code) { // replace with appropriate cases
                case ADD_STORE, REMOVE_PRODUCT -> singleWorker(msg, workerId);
                default -> {
                    System.err.println("Unknown manager code: " + code);
                    throw new RuntimeException();
                }
            };
        };
    }

    public <T,T1> Message<T1> startForBroker(Message<T> msg) {
        T1 reduced = callAppropriateWorker(msg);
        Message<T1> response = new Message<>(reduced);

        response.setId(msg.getId());
        response.setClient(msg.getClient());
        response.setRequest(msg.getRequest());

        return response;
    }

    private void initMapReduce(int id){
        synchronized (Reducer.map)
        {
            Reducer.map.put(id, new ArrayList<>());
        }
    }

    private void clearMapReduce(int id){
        synchronized (Reducer.map)
        {
            Reducer.map.remove(id);
        }
    }

    private <T> void startForClient() {
        Message<T> response = null;
        try {
            Message<T> msg = serverClient.receiveMessage();
            setIdToRequest(msg);
            System.out.println("Client asked for: " + msg.getValue());
            initMapReduce(msg.getId());

            response = startForBroker(msg);
            serverClient.sendMessage(response);

            clearMapReduce(msg.getId());

            serverClient.stopConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private synchronized <T> void setIdToRequest(Message<T> msg){
        msg.setId(++id);
    }

	public void run()
	{
		this.startForClient();
	}


    // different port for each Worker
    public void connectWorkers(int size){
        for (int i = 0; i < size; i++)
        {
            serverWorker.add(new TCPServer(TCPServer.basePort + i + 1));
            System.out.println(TCPServer.basePort + i + 1);
        }
    }

    private void initWorkerMemory(String DATA_PATH){
        try {
            Object temp = new JSONParser().parse(new FileReader(DATA_PATH));
            JSONArray stores = (JSONArray) ((JSONObject)temp).get("Stores");
            Message<Store> msg; // should be extended store
            for (int i = 0; i < stores.size(); i++){
                int workerID = i % n_workers;
                TCPServer server = serverWorker.get(workerID);
                server.startConnection();
                Store store = new Store((JSONObject)stores.get(i));
                System.out.println(store);
                msg = new Message<>(store);
                msg.setRequest(RequestCode.INIT_MEMORY);
                System.out.println("Sending Store: " + i);
                server.sendMessage(msg);
            }
            for (int i = 0; i < n_workers; i++){
                serverWorker.get(i).startConnection();
                msg = new Message<>();
                msg.setRequest(RequestCode.END_INIT_MEMORY);
                serverWorker.get(i).sendMessage(msg);
            }
        }catch (Exception e){
            e.printStackTrace();
            System.err.println("Couldn't initialize Worker memory: " + e.getMessage());
            throw new RuntimeException();
        }


    }


    public static void main(String[] args){

        n_workers = Integer.parseInt(args[0]);
        final String DATA_PATH = "./src/Data/Stores.json";
        final Scanner on = new Scanner(System.in);
        Process[] workers = new Process[n_workers];

        // a server for each Worker
        Master server = new Master();
        server.connectWorkers(n_workers);

        // Initialize workers
        for (int i = 0; i < n_workers; i++)
        {
            try
            {
                System.out.println("Starting worker #" + i + "...");
                // during use, every worker will be on the same system with the same IP
                // using a different port should stop all connectivity issues
                ProcessBuilder pb = new ProcessBuilder(
                        "cmd", "/c", "start", "cmd", "/k", "cd ./src && java  -cp .;jar/json-simple-1.1.1.jar  Worker " + i);
                workers[i] = pb.start();
            }
            catch(IOException e)
            {
                System.err.printf("Could not start worker #%d\n", i);
            }
        }

        server.initWorkerMemory(DATA_PATH);

        // Loop and wait for TCP call

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
                Thread t = new Master(serverSocket);
                t.start();

                if (false)
                    break;
            }

            System.out.println("Would you like to exit?");
            String answer = on.nextLine();
            System.out.println(answer);
            if (answer.equals("Yes"))
            {
                // stop all connections on threads
            }
        }
        catch (Exception e)
        {
            System.err.println("Couldn't start server: " + e.getMessage() );
            throw new RuntimeException(e);
        }

        try
        {
            sleep(15000);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }

//        for (Master server: serversWorkers)
//            server.stop();

    }
}
