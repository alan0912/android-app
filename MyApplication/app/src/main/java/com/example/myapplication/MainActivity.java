package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import Likol.BitmapProcessingAsyncTask;
import Likol.DrawableEncodeToString;
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

    private static final int READ_REQUEST_CODE = 42;
    private static final int WRITE_REQUEST_CODE = 43;
    private final String TAG = "Main";

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

        Thread closeConnection = new Thread(() -> {
            messagePublish("/event:leave");
            MQConnector.getInstance().disconnect();
        });

        closeConnection.start();

        try {
            closeConnection.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    public void messagePublish(String _msg) {
        Thread thread = new Thread(() -> {
            if (_msg.length() < 1) return;
            String msg = pack(_msg);

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
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                new BitmapProcessingAsyncTask(this).execute(uri);
            }
        }

        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                new BitmapProcessingAsyncTask(this).execute(uri);
            }
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    public void onImageViewClick(View view)
    {
        ImageView imageView = (ImageView)view;

        try {
            String base64_string = new DrawableEncodeToString(this).execute((BitmapDrawable)imageView.getDrawable()).get();
            SharedPreferences preferences = getSharedPreferences("image_view", MODE_PRIVATE);
            preferences.edit().putString("image", base64_string).apply();
            startActivity(new Intent(this, SimplePhotoActivity.class));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}

