package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import adapter.MsgAdapter;
import bean.Msg;

public class MainActivity extends AppCompatActivity {

    ConnectionFactory factory;
    TextView chatbox;
    EditText inputbox;
    Button send_bt;
    String tmp;
    Channel channel ;
    RecyclerView recyclerView;
    List<Msg> msgList = new ArrayList<>();
    MsgAdapter adapter;
    InputMethodManager mInputMethodManager;
    int type;
    char separato = 127;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //設定隱藏標題
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        //recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        chatbox = (TextView) findViewById(R.id.chatbox);
        inputbox = (EditText)findViewById(R.id.inputbox);
        send_bt = (Button)findViewById(R.id.send_bt);

        /*
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        recyclerView.setAdapter(adapter);
        type = Msg.TYPE_SENT;
        */

        factory = new ConnectionFactory();
        factory.setHost("10.0.2.2");
        Connect();
        Recv();
        GetRandomId();

    }

    public void Send(View view) {
        new Thread(()->{
            try  {
                /*send*/
                tmp = inputbox.getText().toString();
                pack();
                inputbox.setText("");
                channel.basicPublish("MyExchange", "", null, tmp.getBytes(StandardCharsets.UTF_8));
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
                channel.exchangeDeclare("MyExchange", "fanout");
                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, "MyExchange", "");

                /*recv*/
                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    tmp = new String(delivery.getBody(), "UTF-8");
                    unpack();
                    chatbox.append(tmp + "\n");
                    System.out.println(" [x] Received '" + tmp + "'");
                };
                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
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
                channel.exchangeDeclare("MyExchange", "fanout");

            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void GetRandomId(){
        int random_num =(int)(Math.random()* 999999999 + 1);
        EnterActivity.time = random_num + EnterActivity.time;
    }

    public void pack(){
        String random_time = String.valueOf(EnterActivity.time);
        tmp = random_time + separato + EnterActivity.your_name + separato + tmp;
    }

    public void unpack(){
        if(tmp.split(separato+"")[0].matches(String.valueOf(EnterActivity.time))){
            tmp = EnterActivity.your_name + " : " + tmp.split(separato+"")[2];
            /*
            tmp = tmp.split(separato+"")[2];
            Msg msg = new Msg(tmp,Msg.TYPE_SENT);
            msgList.add(msg);
            //当有新消息时，刷新RecyclerView中的显示
            adapter.notifyItemInserted(msgList.size()-1);
            //将RecyclerView定位到最后一行
            recyclerView.scrollToPosition(msgList.size() - 1);
             */
        }else{
            tmp = tmp.split(separato+"")[1] + " : " + tmp.split(separato+"")[2];
            /*
            tmp = tmp.split(separato+"")[2];
            Msg msg = new Msg(tmp,Msg.TYPE_RECEIVED);
            msgList.add(msg);
            //当有新消息时，刷新RecyclerView中的显示
            adapter.notifyItemInserted(msgList.size()-1);
            //将RecyclerView定位到最后一行
            recyclerView.scrollToPosition(msgList.size() - 1);
             */
        }
    }
}
