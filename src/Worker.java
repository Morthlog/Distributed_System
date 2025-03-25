
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.lang.Thread.sleep;

public class Worker extends Communication {


    /**
     * Take the appropriate action based on the msg's value's type
     * @param msg {@link Message} containing client's request
     */
    public static <T> void ManageRequest(Message<T> msg, Worker client)
    {
        try{
            Message<?> response = null;
            if (msg.getValue() instanceof String)
                response = new Message<>(msg.getValue() + " changed", msg.getId());
            else if (msg.getValue() instanceof Integer)
                response = new Message<>((Integer)msg.getValue() + 100, msg.getId());
            client.sendMessage(response);
            client.stopConnection();
        } catch (Exception e) {
            System.err.printf("Could not connect to server with ip: %s", e.getMessage());
        }
    }
    public static <T> void main(String[] args){
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
            Message<T> request;
            client.startConnection(ip, TCPServer.basePort + 1 + Integer.parseInt(args[0]));
            try{
                request = client.receiveMessage();
            }
            catch (Exception e)
            {
//                continue;
                System.err.printf("Could not receive request from %s.\n", e.getMessage());
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