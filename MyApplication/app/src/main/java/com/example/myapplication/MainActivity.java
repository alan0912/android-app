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

import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import Likol.MQConnector;
import adapter.MsgAdapter;
import bean.Msg;

public class MainActivity extends AppCompatActivity {
    EditText inputbox;
    Button send_bt;
    RecyclerView recyclerView;
    List<Msg> msgList = new ArrayList<>();
    MsgAdapter adapter;

    InputMethodManager mInputMethodManager;
    char separato = 127;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE);

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
        Log.d("CHAT", "on client destroy");

        Thread closeConnection = new Thread(() -> {
            messagePublish("/event:leave");
            Log.d("TEST", "leave event send");
            MQConnector.getInstance().disconnect();
            Log.d("CHAT", "Connection close!");
        });

        closeConnection.start();

        try {
            closeConnection.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d("CHAT", "Start destroy");
        super.onDestroy();
    }

    public void messagePublish(String _msg) {
        Thread thread = new Thread(() -> {
            if (_msg.length() < 1) return;
            String msg = pack(_msg);
            Log.d("CHAT", String.format("Send message: %s", msg));
            try {
                MQConnector.getInstance().getChannel().basicPublish("MyExchange", "", null, msg.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onSendButtonClick(View view) {
        messagePublish(inputbox.getText().toString());

        inputbox.setText("");
    }

    public void MQConnectionHandler() {
        new Thread(() -> {
            try {
                MQConnector.getInstance().getChannel().exchangeDeclare("MyExchange", "fanout");

                String queueName = MQConnector.getInstance().getChannel().queueDeclare().getQueue();
                MQConnector.getInstance().getChannel().queueBind(queueName, "MyExchange", "");

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    runOnUiThread(() -> {
                        unpack(new String(delivery.getBody(), StandardCharsets.UTF_8));
                    });

                };

                MQConnector.getInstance().getChannel().basicConsume(queueName, true, deliverCallback, consumerTag -> {
                });

                messagePublish("/event:join");
                Log.d("THREAD", "MQConnectionHandler done!");

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

        Log.d("CHAT", String.format("Receive message: %s", omg));

        if (message.equals("/event:join"))
        {
            updateRecyclerView(new Msg(String.format("%s %s", name, getResources().getString(R.string.join_chat)), Msg.TYPE_EVENT, null));
            return;
        }

        if (message.equals("/event:leave"))
        {
            updateRecyclerView(new Msg(String.format("%s %s", name, getResources().getString(R.string.leave_chat)), Msg.TYPE_EVENT, null));
            return;
        }

        if (id.equals(String.valueOf(EnterActivity.time)))
        {
            updateRecyclerView(new Msg(message,Msg.TYPE_SENT,name));
            return;
        }

        updateRecyclerView(new Msg(message,Msg.TYPE_RECEIVED,name));
    }

    public void updateRecyclerView(Msg _msg)
    {
        msgList.add(_msg);
        adapter.notifyItemInserted(msgList.size()-1);
        recyclerView.scrollToPosition(msgList.size() - 1);
    }

    public void onImageButtonClick(View v)
    {
        return;
    }
}

