package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import adapter.MsgAdapter;
import bean.Msg;

public class MainActivity extends AppCompatActivity {
    EditText inputbox;
    ImageView send_bt;
    String tmp;
    Channel channel;
    RecyclerView recyclerView;
    List<Msg> msgList = new ArrayList<>();
    MsgAdapter adapter;
    String tmp_name;

    InputMethodManager mInputMethodManager;
    char separato = 127;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        recyclerView = findViewById(R.id.recycler_view);
        inputbox = findViewById(R.id.inputbox);
        send_bt = findViewById(R.id.send_bt);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new MsgAdapter(msgList);
        recyclerView.setAdapter(adapter);
        GetRandomId();
        MQConnectionHandler();

        recyclerView.setOnTouchListener((v, event) -> {
            mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            return false;
        });
    }

    public void Send(View view) {
        new Thread(() -> {
            try {
                /*send*/
                tmp = inputbox.getText().toString();
                if(tmp.equals(""))return;
                pack();
                inputbox.setText("");
                channel.basicPublish("MyExchange", "", null, tmp.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + tmp + "'");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void MQConnectionHandler() {
        new Thread(() -> {
            try {
                Connection connection = MQConnector.getInstance().connectGenerator();
                channel = connection.createChannel();

                channel.exchangeDeclare("MyExchange", "fanout");

                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, "MyExchange", "");

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    Log.d("recv handler", "start");
                    tmp = new String(delivery.getBody(), "UTF-8");
                    unpack();
                    System.out.println(" [x] Received '" + tmp + "'");
                };

                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                });

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void GetRandomId() {
        int random_num = (int) (Math.random() * 999999999 + 1);
        EnterActivity.time = random_num + EnterActivity.time;
    }

    public void pack() {
        String random_time = String.valueOf(EnterActivity.time);
        tmp = random_time + separato + EnterActivity.your_name + separato + tmp;

    }

    public void unpack() {
        Log.d("unpack handler", "start unpack");
        if (tmp.split(separato + "")[0].matches(String.valueOf(EnterActivity.time))) {
            Log.d("unpack handler", "msg from self");
            tmp_name = (String) tmp.split(separato+"")[1];
            tmp = tmp.split(separato+"")[2];
            Msg msg = new Msg(tmp,Msg.TYPE_SENT,tmp_name);
            msgList.add(msg);
        } else {
            Log.d("unpack handler", "msg from others");
            tmp_name = (String) tmp.split(separato+"")[1];
            tmp = tmp.split(separato+"")[2];
            Msg msg = new Msg(tmp,Msg.TYPE_RECEIVED,tmp_name);
            msgList.add(msg);
        }

        runOnUiThread(() -> {
            adapter.notifyItemInserted(msgList.size()-1);
            recyclerView.scrollToPosition(msgList.size() - 1);
        });
    }
}

