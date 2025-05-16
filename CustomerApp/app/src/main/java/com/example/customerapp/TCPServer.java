package com.example.customerapp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPServer extends Communication {
    public static final int basePort = 8000;
    public ServerSocket serverSocket;
    public int port = basePort;

    public TCPServer(Socket connection) throws IOException {
        socket = connection;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw new SocketException();
        }
    }

    public TCPServer(int port) {
        this.port = port;
        try{
            serverSocket = new ServerSocket(port);
        }catch(IOException e){

        }
    }


    public void stopServer() {
        try{
            in.close();
            out.close();
            socket.close();
            serverSocket.close();
        }
        catch(IOException e){
            System.err.println("Couldn't close server");
        }
    }
    

    /** Accept a connection request coming to the server
     *
     */
    public void startConnection() throws IOException {
        socket = serverSocket.accept();
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void setSocketTOState(boolean state) {
        try{
            if (state)
                serverSocket.setSoTimeout(10000);
            else
                serverSocket.setSoTimeout(0);
        }catch(IOException e){
            System.err.println("Couldn't set socket timeout");
            e.printStackTrace();
            throw new RuntimeException();
        }

    }


}
