package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;

public class EnterActivity extends AppCompatActivity {

    public static long time;
    public static String your_name;
    TextInputEditText name;
    Button enter_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);
        //設定隱藏標題
        getSupportActionBar().hide();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        //name = (TextInputEditText)findViewById(R.id.name);
        enter_bt = (Button)findViewById(R.id.enter_bt);

    }

    public void Enter(View view){

        if(name.getText().toString().matches("")){
            Toast toast =Toast.makeText(this, "Please type your name", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
        }else{
            time = System.currentTimeMillis();
            your_name = name.getText().toString();
            Intent intent = new Intent();
            intent.setClass(EnterActivity.this , MainActivity.class);
            startActivity(intent);
        }

    }

}
