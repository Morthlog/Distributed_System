import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class stubUser implements User {
    int counter = 0;
    String name;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket clientSocket;

    public stubUser(String name) {
        this.name = name;
    }

    @Override
    public void sendMessage(String msg) {
        try
        {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String receiveMessage() {
        counter++;
        return name + ": " + counter;
    }

    @Override
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
    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
            System.out.println("Connection closed");
        }
        catch (IOException e) {
            System.err.printf("Could not close socket: %s", e.getMessage());
        }
    }
    public static void main(String[] args) {
        System.out.println("I am stub user #" + args[0]);
        stubUser stub = new stubUser(args[0]);
        for (int i = 0; i < 5; i++) {
            try{
                sleep(1000);
                String ip = InetAddress.getLocalHost().getHostAddress();
                stub.startConnection(ip, TCPServer.basePort - 1000);
                stub.sendMessage(stub.receiveMessage());
                System.out.println(stub.in.readUTF());
                stub.stopConnection();
            } catch (Exception e) {
                System.err.printf("Could not connect to server with ip: %s", e.getMessage());
            }
        }
    }
}
