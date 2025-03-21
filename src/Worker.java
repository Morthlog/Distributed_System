import java.io.*;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

public class Worker
{
    private Socket clientSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void startConnection(String ip, int port)
    {
        try
        {
            clientSocket = new Socket(ip, port);
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        }
        catch (IOException e)
        {
            System.err.printf("Could not connect to server with ip: %s and port: %d", ip, port);
        }
    }

    public void sendMessage(String msg)
    {
        try
        {
            out.writeUTF(msg);
            out.flush();
        }
        catch (IOException e)
        {
            System.err.printf("Could not send message (%s): %s", msg, e.getMessage());
        }
    }

    public void stopConnection()
    {
        try
        {
            in.close();
            out.close();
            clientSocket.close();
        }
        catch (IOException e)
        {
            System.err.printf("Could not close socket: %s", e.getMessage());
        }
    }

    public static void ManageRequest(Object request, Worker client)
    {
        try
        {
            if (request instanceof Filter)
            {
                Filter filter = (Filter) request;
                java.util.List<Store> stores = processFilter(filter);
                client.out.writeObject(stores);
            }
            else if (request instanceof java.util.Set)
            {
                client.out.writeObject("Purchase successful");
            }
            client.out.flush();
            client.stopConnection();
        }
        catch (Exception e)
        {
            System.err.printf("Could not process request: %s", e.getMessage());
        }
    }

    private static List<Store> processFilter(Filter filter)
    {
        List<Store> stores = new ArrayList<>();
        Store dummyStore = new Store("Dummy Store", FoodCategory.PIZZA, 5);
        dummyStore.setProducts(Arrays.asList(
                new Product("Margherita", new BigDecimal("8.5")),
                new Product("Pepperoni", new BigDecimal("9.5"))
        ));
        stores.add(dummyStore);
        return stores;
    }

    public static void main(String[] args)
    {
        System.out.printf("Worker %s has started\n", args[0]);
        String ip;
        try
        {
            ip = InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
        Worker client;
        while (true)
        {
            client = new Worker();
            System.out.println("Waiting for request...");
            Object request;
            client.startConnection(ip, TCPServer.basePort + 1);
            try
            {
                request = client.in.readObject();
            }
            catch (IOException | ClassNotFoundException e)
            {
                throw new RuntimeException(e);
            }
            Worker finalClient = client;
            Thread t = new Thread(() -> ManageRequest(request, finalClient));
            t.start();
            try
            {
                sleep(0);
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
