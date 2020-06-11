package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.concurrent.ExecutionException;

import Likol.ImageMessageDecoder;


public class SimplePhotoActivity extends AppCompatActivity {

    private PhotoView mPhotoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_photo);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.photo_view_title);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mPhotoView = findViewById(R.id.iv_photo);

        SharedPreferences preferences = getSharedPreferences("image_view", MODE_PRIVATE);
        String base64_string = preferences.getString("image", null);

        Bitmap bitmap = null;
        try {
            bitmap = new ImageMessageDecoder().execute(base64_string).get();
            mPhotoView.setImageBitmap(bitmap);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
