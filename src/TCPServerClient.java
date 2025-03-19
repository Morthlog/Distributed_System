import java.io.IOException;
import java.net.Socket;

public class TCPServerClient extends TCPServer {

    public TCPServerClient(Socket connection, Connection type) {
        super(connection, type);
    }

}
