
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// import json-simple in project structure
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.net.*;

import static java.lang.Thread.sleep;

public class Master extends Thread {
    private static Integer id = 0;
    private TCPServer serverClient = null;
    private List<TCPServer> serverWorker = new ArrayList<>();

    public Master(Socket connection, Connection type, List<TCPServer> workers){
        if (type == Connection.Client)
            serverClient = new TCPServer(connection, type);
        this.serverWorker = workers;
    }

    public Master(){}

    public <T> Message<T> startForBroker(Message<T> msg) {
        Message<T> response = null;
        try{
            // request from Master
            System.out.println("Waiting for connection...");
            serverWorker.get(0).startConnection();
            serverWorker.get(0).sendMessage(msg);
            System.out.println("Worker was asked");
            response = serverWorker.get(0).receiveMessage();

            //debug messages
            if (response.getValue().getClass() == String.class) {
                System.out.println("Got message: " + response.getValue());
            }
            else {
                System.out.println("Got number: " + response.getValue());
            }
        }
        catch(IOException e){
            System.err.println("Couldn't start server: " + e.getMessage());
        }
        return response;

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
            response = startForBroker(msg);
            serverClient.sendMessage(response);
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
            serverWorker.add(new TCPServer(TCPServer.basePort + i + 1, Connection.Broker));
            System.out.println(TCPServer.basePort + i + 1);
        }
    }


    public static void main(String[] args){


        final int n_workers = Integer.parseInt(args[0]);
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
                Thread t = new Master(serverSocket, Connection.Client, server.serverWorker);
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
