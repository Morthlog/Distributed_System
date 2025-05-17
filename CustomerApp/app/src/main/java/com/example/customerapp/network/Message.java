package com.example.customerapp.network;

import java.io.Serializable;

public class Message <T> implements Serializable {
    private T value;
    private Client client;
    private RequestCode request;


    public Message(){}

    public Message(T value) {
        this.value = value;
    }
    
    public Message(T value, Client client, RequestCode request) {
        this(value);
        this.client = client;
        this.request = request;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    /**
     * @return client's type
     */
    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Identifier that corresponds to the type of request
     * @return request code
     */
    public RequestCode getRequest() {
        return request;
    }

    /**
     * Identifier that corresponds to the type of request
     */
    public void setRequest(RequestCode request) {
        this.request = request;
    }
}
