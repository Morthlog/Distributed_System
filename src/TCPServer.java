import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread {
    public static final int basePort = 8000;
    public ServerSocket serverSocket;
    public Socket clientSocket;
    private ObjectOutputStream out = null;
    public ObjectInputStream in = null;
    public int port = basePort;
    public Connection type;

    public TCPServer(Socket connection, Connection type) {
        clientSocket = connection;
        this.type = type;
        if (type == Connection.Broker)
            port = 8001;
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TCPServer(int port, Connection type) {
        this.port = port;
        this.type = type;
        try{
            serverSocket = new ServerSocket(port);
        }catch(IOException e){

        }
    }

    public void stopConnection() {
        try{
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        }
        catch(IOException e){
            System.err.println("Couldn't close server");
        }
    }

    public void startConnection() throws IOException {
        clientSocket = serverSocket.accept();
        out = new ObjectOutputStream(clientSocket.getOutputStream());
        in = new ObjectInputStream(clientSocket.getInputStream());
    }

    public void sendMessage(String msg) {
        try
        {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String receiveMessage() throws IOException {
        System.out.println("About to get message");
        return in.readUTF();
    }


    public void run() {
        manageRequest();
    }

    public void manageRequest()
    {

    }

}
