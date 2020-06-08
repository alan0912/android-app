package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import Likol.NameGenerator;

public class EnterActivity extends AppCompatActivity {

    public static long time;
    public static String your_name;
    TextInputEditText name;
    Button enter_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);

        name = findViewById(R.id.name);
        enter_bt = findViewById(R.id.enter_bt);
    }

    public void Enter(View view){
        if(name.getText().toString().matches("")){
            new MaterialAlertDialogBuilder(view.getContext())
                    .setIcon(R.drawable.ic_warning_black_24dp)
                    .setTitle(R.string.no_nickname_box_title)
                    .setMessage(R.string.no_nickname_box_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.accept, (dialog, which) -> {
                        NameGenerator ng = new NameGenerator(this);
                        name.setText(ng.getName());
                    })
                    .show();
        }else{
            time = System.currentTimeMillis();
            your_name = name.getText().toString();
            Intent intent = new Intent();
            intent.setClass(EnterActivity.this , MainActivity.class);
            startActivity(intent);
        }

    }

}
