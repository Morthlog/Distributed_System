
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import static java.lang.Thread.sleep;

public class Worker extends Communication {

    // Hashmap<String, Store> for memory and backup
    /**
     * actionTable for every way the {@link Worker} should respond
     * current actions are not permanent
     */
    private static <T> T actionTable(Message<T> msg) {
        Client client = msg.getClient();
        int code = msg.getRequest();
        T val = msg.getValue();
        return switch (client) {
            case Customer -> switch (code) {
                case 1 -> sendString(val);
                case 2 -> sendNum(val);
                default -> {
                    System.err.println("Unknown customer code: " + code);
                    throw new RuntimeException();
                }
            };
            case Manager -> switch (code) {
                case 1 -> null;
                case 2 -> null;
                default -> {
                    System.err.println("Unknown manager code: " + code);
                    throw new RuntimeException();
                }
            };
        };

    }

    private static <T> T sendString(T val){
        return (T) (val + " changed");
    }

    private static <T> T sendNum(T val){
        return (T) Integer.valueOf((Integer) val + 100);
    }


    /**
     * Take the appropriate action based on the msg's value's type
     * @param msg {@link Message} containing client's request
     */
    public static <T> void ManageRequest(Message<T> msg, Worker client)
    {
        try{
            T value = actionTable(msg);
            msg.setValue(value);

            client.sendMessage(msg);
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