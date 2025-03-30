
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
    private static Map<Integer, List<Object>> mapReduce = new HashMap<>();
    private static int n_workers;

    private TCPServer serverClient = null;
    private static List<TCPServer> serverWorker = new ArrayList<>();

    public Master(Socket connection){
        serverClient = new TCPServer(connection);
    }

    public Master(){}

    private static <T> void broadcast(Message<T> msg){
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
        List<Object> list = new ArrayList<>();
        list.add(Reducer.reduce(msg, mapReduce.get(msg.getId())));
        mapReduce.replace(msg.getId(), list);

    }

    private static <T> void singleWorker(Message<T> msg, int worker){
        try{
            TCPServer server = serverWorker.get(worker);

            TCPServer currentConnection = new TCPServer(server.serverSocket.accept());
            currentConnection.sendMessage(msg);
            Message<T> response = currentConnection.receiveMessage();

            List<Object> list = mapReduce.get(msg.getId());
            synchronized (list)
            {
                list.add(response.getValue());
            }
            currentConnection.stopConnection();
        }
        catch(IOException e){
            System.err.println("Couldn't start server: " + e.getMessage());
        }
    }


    /**
     * Current values are temporary
     */
    private <T> void callAppropriateWorker(Message<T> msg){
        Client client = msg.getClient();
        int code = msg.getRequest();
        int workerId = 0;
        // =========
          // if msg.val is Store, set workerId to the correct worker
        // =========
        switch (client) {
            case Customer:
                switch (code) { // replace with appropriate cases
                    case 1 -> singleWorker(msg, workerId);
                    case 2 -> broadcast(msg);
                    default -> {
                        System.err.println("Unknown customer code: " + code);
                        throw new RuntimeException();
                    }
                }
                break;
            case Manager:
                switch (code) { // replace with appropriate cases
                    case 1, 2 -> singleWorker(msg, workerId);
                    default -> {
                        System.err.println("Unknown manager code: " + code);
                        throw new RuntimeException();
                    }
                }
                break;
        }
    }

    public <T> Message<T> startForBroker(Message<T> msg) {
        callAppropriateWorker(msg);
        msg.setValue((T) mapReduce.get(msg.getId()).get(0));
        return msg;
    }

    private <T> void startForClient() {
        Message<T> response = null;
        try {
            Message<T> msg = serverClient.receiveMessage();
            synchronized (id)
            {
                msg.setId(++id);
            }
            System.out.println("Client asked for: " + msg.getValue());
            synchronized (mapReduce)
            {
                mapReduce.put(msg.getId(), new ArrayList<>());
            }
            response = startForBroker(msg);
            serverClient.sendMessage(response);

            synchronized (mapReduce)
            {
                mapReduce.remove(msg.getId()); // reduce memory overhead
            }
            serverClient.stopConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

//    public void stop() {
//        server.stopConnection();
//    }
    public void run() {
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
                        "cmd", "/c", "start", "cmd", "/k", "cd ./src && java Worker " + i);
                workers[i] = pb.start();
            }
            catch(IOException e)
            {
                System.err.printf("Could not start worker #%d\n", i);
            }
        }

        // Read initial memory
        JSONArray stores;
        try
        {
            Object temp = new JSONParser().parse(new FileReader(DATA_PATH));
            stores = (JSONArray) ((JSONObject)temp).get("Stores");
        }
        catch (Exception e)
        {
            System.out.println("JSON data could not be parsed");
            throw new RuntimeException();
        }

        // Send store data to each Worker
        int currentWorker = 0;
        for (var obj: stores)
        {
            JSONObject store = (JSONObject)obj;

            currentWorker = (currentWorker + 1) % n_workers;
        }

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
