package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

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
    Button send_bt;
    Channel channel;
    RecyclerView recyclerView;
    List<Msg> msgList = new ArrayList<>();
    MsgAdapter adapter;
    Connection connection;

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
            if (mInputMethodManager.isActive())
            {
                mInputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

            return false;
        });
    }

    @Override
    protected void onDestroy() {
        Log.d("CHAT", "client destroy");
        messagePublish("/event:leave");
        new Thread(() -> {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        super.onDestroy();
    }

    public void messagePublish(String _msg)
    {
        new Thread(() -> {
            if (_msg.length() < 1) return;
            String msg = pack(_msg);
            Log.d("CHAT", String.format("Send message: %s", msg));
            try {
                channel.basicPublish("MyExchange", "", null, msg.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void onSendButtonClick(View view) {
        messagePublish(inputbox.getText().toString());

        inputbox.setText("");
    }

    public void MQConnectionHandler() {
        new Thread(() -> {
            try {
                connection = MQConnector.getInstance().connectGenerator();
                channel = connection.createChannel();

                channel.exchangeDeclare("MyExchange", "fanout");

                String queueName = channel.queueDeclare().getQueue();
                channel.queueBind(queueName, "MyExchange", "");

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    unpack(new String(delivery.getBody(), "UTF-8"));

                    runOnUiThread(() -> {
                        adapter.notifyItemInserted(msgList.size()-1);
                        recyclerView.scrollToPosition(msgList.size() - 1);
                    });
                };

                channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                });

                this.messagePublish("/event:join");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();
    }

    public void GetRandomId() {
        int random_num = (int) (Math.random() * 999999999 + 1);
        EnterActivity.time = random_num + EnterActivity.time;
    }

    public String pack(String omg) {
        String random_time = String.valueOf(EnterActivity.time);
        return random_time + separato + EnterActivity.your_name + separato + omg;
    }

    public void unpack(String omg) {
        String id = omg.split(separato+"")[0];
        String name = omg.split(separato+"")[1];
        String message = omg.split(separato+"")[2];
        Msg _msg;

        Log.d("CHAT", String.format("Receive message: %s", omg));

        if (message.equals("/event:join"))
        {
            _msg = new Msg(String.format("%s %s", name, getResources().getString(R.string.join_chat)), Msg.TYPE_EVENT, null);
            msgList.add(_msg);
            return;
        }

        if (message.equals("/event:leave"))
        {
            _msg = new Msg(String.format("%s %s", name, getResources().getString(R.string.leave_chat)), Msg.TYPE_EVENT, null);
            msgList.add(_msg);
            return;
        }

        if (id.equals(String.valueOf(EnterActivity.time)))
        {
            _msg = new Msg(message,Msg.TYPE_SENT,name);
            msgList.add(_msg);
            return;
        }

        _msg = new Msg(message,Msg.TYPE_RECEIVED,name);
        msgList.add(_msg);
    }
}

