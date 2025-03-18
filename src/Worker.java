
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import static java.lang.Thread.sleep;

public class Worker {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) {
        try {
            clientSocket = new Socket(ip, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
        catch (IOException e) {
            System.err.printf("Could not connect to server with ip: %s and port: %d", ip, port);
        }

    }

    public void sendMessage(String msg) {
        try{
            in.readLine();
        }
        catch (IOException e) {
            System.err.printf("Could not send message (%s): %s", msg, e.getMessage());
        }
        out.println(msg);
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

    public static void connect(String msg, Worker client, int port)
    {
        try{
            String ip = InetAddress.getLocalHost().getHostAddress();
            client.startConnection(ip, TCPServer.basePort + port);
            client.sendMessage(msg);
            client.stopConnection();
        } catch (Exception e) {
            System.err.printf("Could not connect to server with ip: %s", e.getMessage());
        }
    }
    public static void main(String[] args){
        System.out.printf("Worker %s has started\n", args[0]);
        System.out.println("Update 1 worked");
        Worker client = new Worker();

//        while(true){
//            System.out.println("Waiting for request...");
//
//        }

        // each Worker should be listening for Master requests
        Thread t = new Thread(() -> connect("hello server from worker " + args[0], client, Integer.parseInt(args[0])));
        t.start();
        try{
            sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Second Thread");
        Thread t2 = new Thread(() -> connect("hello server from worker " + args[0] +" round 2", client, Integer.parseInt(args[0])));
        t2.start();
        try{
            sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Third Thread");
        Thread t3 = new Thread(() -> connect("hello server from worker " + args[0] +" round 3", client, Integer.parseInt(args[0])));
        t3.start();

    }

}