package com.example.customerapp.network;

import java.util.List;

import lib.shared.Client;
import lib.shared.Communication;
import lib.shared.Filter;
import lib.shared.Message;
import lib.shared.RatingChange;
import lib.shared.RequestCode;
import lib.shared.ShoppingCart;
import lib.shared.Store;
import lib.shared.TCPServer;

public class TcpCustomerService extends Communication implements CustomerServices
{
    private String ip;
    public TcpCustomerService()
    {
        setIp();
    }

    public void setIp()
    {

        new Thread(() ->
        {
            try
            {
                //default ip for android emulator
                ip ="10.0.2.2";
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }).start();
    }

    @Override
    public List<Store> searchStores(Filter filter) throws Exception
    {
        startConnection(ip, TCPServer.basePort);
        Message<Filter> msg = new Message<>(filter, Client.Customer, RequestCode.SEARCH);
        sendMessage(msg);

        Message<List<Store>> responseMsg = receiveMessage();
        List<Store> stores = responseMsg.getValue();
        stopConnection();
        return stores;
    }

    @Override
    public String placeOrder(ShoppingCart cart) throws Exception
    {
        startConnection(ip, TCPServer.basePort);
        Message<ShoppingCart> msg = new Message<>(cart, Client.Customer, RequestCode.BUY);
        sendMessage(msg);

        // Receive purchase confirmation
        Message<String> responseMsg = receiveMessage();
        String verification = responseMsg.getValue();
        stopConnection();
        return verification;
    }

    @Override
    public String rateStore(String storeName, int oldRating, int newRating) throws Exception
    {
        startConnection(ip, TCPServer.basePort);

        RatingChange ratingChange = new RatingChange(storeName, oldRating, newRating);
        Message<RatingChange> msg = new Message<>(ratingChange, Client.Customer, RequestCode.RATE_STORE);
        sendMessage(msg);

        Message<String> responseMsg = receiveMessage();
        String verification = responseMsg.getValue();

        stopConnection();

        return verification;
    }
}
