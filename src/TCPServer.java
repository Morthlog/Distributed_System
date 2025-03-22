import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Communication {
    public static final int basePort = 8000;
    public ServerSocket serverSocket;
    public int port = basePort;
    public Connection type;

    public TCPServer(Socket connection, Connection type) {
        socket = connection;

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

    @Override
    public void stopConnection() {
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



    public void run() {
        manageRequest();
    }

    public void manageRequest()
    {

    }

}
