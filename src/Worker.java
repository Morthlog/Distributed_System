
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.lang.Thread.sleep;

public class Worker {
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        }
        catch (IOException e) {
            System.err.printf("Could not connect to server with ip: %s and port: %d", ip, port);
        }

    }

    public void sendMessage(String msg) {
        try{
            out.writeUTF(msg);
            out.flush();
        }
        catch (IOException e) {
            System.err.printf("Could not send message (%s): %s", msg, e.getMessage());
        }

    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        }
        catch (IOException e) {
            System.err.printf("Could not close socket: %s", e.getMessage());
        }
    }

    public static void ManageRequest(String msg, Worker client)
    {
        try{
            msg += " changed";
            client.sendMessage(msg);
            client.stopConnection();
        } catch (Exception e) {
            System.err.printf("Could not connect to server with ip: %s", e.getMessage());
        }
    }
    public static void main(String[] args){
        System.out.printf("Worker %s has started\n", args[0]);
        System.out.println("Update 1 worked");
        Worker client;
        String ip;
        try
        {
            ip = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }

        while(true){
            client = new Worker();
            System.out.println("Waiting for request...");
//            String msg = "hello server from worker #" + args[0] + " round " + i;
            String request;
            client.startConnection(ip, TCPServer.basePort + 1);
            try{
                request = client.in.readUTF();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
            //ManageRequest(msg, client);
            Worker finalClient = client;
            Thread t = new Thread(() -> ManageRequest(request, finalClient));
            t.start();
            try{
                sleep(0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

}