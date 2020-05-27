package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    private final static String QUEUE_NAME = "Main_Queue";
    ConnectionFactory factory;
    TextView chatbox;
    EditText inputbox;
    Button send_bt;
    String tmp;
    Channel channel ;
    InputMethodManager mInputMethodManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //設定隱藏標題
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        chatbox = (TextView) findViewById(R.id.chatbox);
        inputbox = (EditText)findViewById(R.id.inputbox);
        send_bt = (Button)findViewById(R.id.send_bt);

        factory = new ConnectionFactory();
        factory.setHost("10.0.2.2");
        Connect();
        Recv();
    }

    public void Send(View view) {
        new Thread(()->{
            try  {
                /*send*/
                tmp = inputbox.getText().toString();
                inputbox.setText("");
                channel.basicPublish("", QUEUE_NAME, null, tmp.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + tmp + "'");
                mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void Recv(){
        new Thread(()->{
            try  {
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
                /*recv*/
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    tmp = new String(delivery.getBody(), "UTF-8");
                    chatbox.append(tmp + "\n");
                    System.out.println(" [x] Received '" + tmp + "'");
                };
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });

            }catch (IOException  | TimeoutException e){
                e.printStackTrace();
            }
        }).start();
    }

    public void Connect(){
        new Thread(()->{
            try  {
                Connection connection = factory.newConnection();
                channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
