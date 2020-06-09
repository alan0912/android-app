package Likol;

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

    private MQConnector() {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);

        connection = connectGenerator();
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

    public Connection getConnection() {
        if (connection == null || !connection.isOpen())
            connection = connectGenerator();

        return connection;
    }

}
