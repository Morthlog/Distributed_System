import java.io.IOException;
import java.net.InetAddress;

public interface User {
    void sendMessage(String msg);

    String receiveMessage();

    void startConnection(String ip, int port);

    void stopConnection();
}
