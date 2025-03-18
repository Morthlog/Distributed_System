import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    public static final int basePort = 8000;
    public ServerSocket serverSocket;
    public Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
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
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }
}
