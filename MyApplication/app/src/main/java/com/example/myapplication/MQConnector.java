package com.example.myapplication;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MQConnector {

    public static MQConnector instance;
    private String host = "likol.idv.tw";
    private String username = "506170101";
    private String password = "506170101";
    public ConnectionFactory factory;

    MQConnector() {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);
    }

    public static MQConnector getInstance()
    {
        if (instance == null)
        {
            instance = new MQConnector();
        }

        return instance;
    }

    public Connection connectGenerator()
    {
        try {
            return getInstance().factory.newConnection();
        } catch (TimeoutException | IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
