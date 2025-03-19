
import java.io.*;
import java.util.Scanner;

// import json-simple in project structure
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;

import java.net.*;

import static java.lang.Thread.sleep;

public class Master implements Runnable {
    TCPServer server = new TCPServer();

    public void startForBroker(int port) {
        try{
            server.serverSocket = new ServerSocket(port);

            int i = 0;
            while (i++< 3){
                // request from Master
                System.out.println("Waiting for connection..." + port);
                server.startConnection();
                server.sendMessage("Give me your name " + port);
                String msg = server.receiveMessage();
                if (!"".equals(msg)) {
                    System.out.println("Got message: " + msg);
                }
                else {
                    System.out.println("unrecognised greeting");
                }
            }


        }
        catch(IOException e){
            System.err.println("Couldn't start server");
        }

    }

    private void startForClient(int port) {
        try
        {
            System.out.println("Wait for request");
            String msg = server.in.readUTF();
            System.out.println("Client asked for: " + msg);
            server.sendMessage("Here it is: " + msg);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        server.stop();
    }
    public void run() {
        if (server.type == Connection.Broker)
            this.startForBroker(server.port);
        else
            this.startForClient(server.port);
    }



    public static void compile(){
        try
        {
            System.out.println("Compile Worker...");
            ProcessBuilder pb = new ProcessBuilder(
                    "cmd.exe", "/c", "del Worker.class && javac Worker.java && javac stubUser.java");
            Process p = pb.start();
            p.waitFor();
        }
        catch(Exception e)
        {
            System.err.println("Could not compile Worker");
        }
    }

    // different port for each Worker to work on local machine
    public static void connectWorkers(Master[] workers){
        for (int i = 0; i < workers.length; i++)
        {
            workers[i] = new Master();
            workers[i].server.type = Connection.Broker;
            workers[i].server.port = workers[i].server.port + i;
            System.out.println(workers[i].server.port);
            Thread t = new Thread(workers[i]);
            t.start();
        }
    }

    public static void connectClients(Master serverClients){
        try {
            while (true)
            {
                serverClients.server.startConnection();
                Thread t = new Thread(serverClients);
                t.start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args){

        // Compile Worker and then continue
        compile();


        final int n_workers = Integer.parseInt(args[0]);
        final String DATA_PATH = "./Data/Stores.json";
        final Scanner on = new Scanner(System.in);
        Process[] workers = new Process[n_workers];

        Master[] serversWorkers = new Master[n_workers];
        // a server for each Worker
        connectWorkers(serversWorkers);

        // Initialize workers
        for (int i = 0; i < n_workers; i++)
        {
            try
            {
                System.out.println("Starting worker #" + i + "...");
                // during use, every worker will be on the same system with the same IP
                // using a different port should stop all connectivity issues
                ProcessBuilder pb = new ProcessBuilder(
                        "cmd", "/c", "start", "cmd", "/k", "java Worker " + i);
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
        Master serverClients = new Master();

        serverClients.server.port = TCPServer.basePort - 1000;
        serverClients.server.type = Connection.Client;

        try
        {
            //===========================================
            // Under normal circumstances, each user will have his own IP address
            // causing no issues when connecting on the same port
            System.out.println("Starting user #" + "...");
            for (int i = 0; i < 15; i++)
            {
                ProcessBuilder pb = new ProcessBuilder(
                        "cmd", "/c", "start", "cmd", "/k", "java stubUser User-" + i);
                pb.start();
            }
            //===========================================
            serverClients.server.serverSocket = new ServerSocket(serverClients.server.port);

            Thread t = new Thread(()    ->  connectClients(serverClients));
            t.start();

            System.out.println("Would you like to exit?");
            String answer = on.nextLine();
            System.out.println(answer);
            if (answer.equals("Yes"))
            {
                // stop all connections on threads
            }
        }
        catch (IOException e)
        {
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
