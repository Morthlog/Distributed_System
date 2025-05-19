import lib.shared.*;

import java.net.InetAddress;

import static java.lang.Thread.sleep;

public class stubUser extends Communication{
    private String name;


    public stubUser(String name) {
        this.name = name;
    }

    public static void main(String[] args) {
        System.out.println("I am stub user #" + args[0]);
        stubUser stub = new stubUser(args[0]);
        Message<?> msg, response;
        String val;
        for (int i = 0; i < 5; i++) {
            try{
//                stubUser stub = new stubUser(args[0]);
                String ip = InetAddress.getLocalHost().getHostAddress();
                stub.startConnection(ip, TCPServer.basePort);
                if (i%2 == 0)
                {
                    val = "From: "+ stub.name + ". Just add changed on end " + i + " ";
                    msg = new Message<>(val, Client.Customer, RequestCode.STUB_TEST_1);
                }
                else
                    msg = new Message<>(57 + 2*i, Client.Customer, RequestCode.STUB_TEST_2);
                stub.sendMessage(msg);
                response = stub.receiveMessage();
                System.out.println(response.getValue());
                stub.stopConnection();
            } catch (Exception e) {
                System.err.printf("Could not connect to server with ip: %s", e.getMessage());
            }
        }
    }
}
