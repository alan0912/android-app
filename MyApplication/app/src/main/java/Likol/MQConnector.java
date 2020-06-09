package Likol;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MQConnector {

    public static MQConnector instance;
    private String host = "likol.idv.tw";
    private String username = "506170101";
    private String password = "506170101";
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;

    private MQConnector() {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);

        connection = getConnection();
        try {
            channel = connection.createChannel();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static MQConnector getInstance()
    {
        if (instance == null)
            instance = new MQConnector();

        return instance;
    }

    private Connection connectGenerator() {
        try {
            return factory.newConnection();
        } catch (TimeoutException | IOException e) {
            return null;
        }
    }

    private Channel channelGenerator() {
        try {
            return connection.createChannel();
        } catch (NullPointerException | IOException e) {
            return null;
        }
    }

    public Connection getConnection() {
        if (connection == null || !connection.isOpen())
            connection = connectGenerator();

        return connection;
    }

    public Channel getChannel() {
        if (!channel.isOpen())
                channel = channelGenerator();
        return channel;
    }

    public void disconnect() {
        Thread thread = new Thread(() -> {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
