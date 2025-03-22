import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import static java.lang.Thread.sleep;

public class stubUser extends Communication{
    private String name;


    public stubUser(String name) {
        this.name = name;
    }

    public static void main(String[] args) {
        System.out.println("I am stub user #" + args[0]);
        stubUser stub = new stubUser(args[0]);
        Message<?> msg, response;
        for (int i = 0; i < 5; i++) {
            try{
//                stubUser stub = new stubUser(args[0]);
                String ip = InetAddress.getLocalHost().getHostAddress();
                stub.startConnection(ip, TCPServer.basePort);
                if (i%2 == 0)
                    msg = new Message<>("From: "+ stub.name + ". Just add changed on end " + i + " ");
                else
                    msg = new Message<>(57 + 2*i);
                stub.sendMessage(msg);
                response = stub.receiveMessage();
                System.out.println(response.getValue());
                System.out.println("Response's class: " + response.getValue().getClass());
                stub.stopConnection();
            } catch (Exception e) {
                System.err.printf("Could not connect to server with ip: %s", e.getMessage());
            }
        }
    }
}
