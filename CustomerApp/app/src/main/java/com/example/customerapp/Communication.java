package com.example.customerapp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public abstract class Communication extends Thread {
    protected ObjectOutputStream out;
    protected ObjectInputStream in;
    protected Socket socket;

    public <T> void sendMessage(Message<T> msg){
        try
        {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Message<T> receiveMessage(){
        try {
            return (Message<T>) in.readObject();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /** Make a new socket to connect with the server
     * @param ip address of the server
     * @param port port number used for communication
     */
    public void startConnection(String ip, int port){
        try {
            socket = new Socket(ip, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        }
        catch (IOException e) {
            System.err.printf("Could not connect to server with ip: %s and port: %d", ip, port);
            System.out.println(e.getMessage());
            throw new RuntimeException();
        }
    }

    public void stopConnection(){
        try {
            in.close();
            out.close();
            socket.close();
        }
        catch (IOException e) {
            System.err.printf("Could not close socket: %s", e.getMessage());
        }
    }
}
