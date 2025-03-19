import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    public static final int basePort = 8000;
    public ServerSocket serverSocket;
    public Socket clientSocket;
    private ObjectOutputStream out = null;
    public ObjectInputStream in = null;
    public int port = basePort;
    public Connection type;


    public void stop() {
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
        return in.readUTF();
    }
}
