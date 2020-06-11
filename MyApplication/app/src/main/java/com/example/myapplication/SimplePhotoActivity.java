package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.github.chrisbanes.photoview.PhotoView;


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

        byte[] bytes = getIntent().getByteArrayExtra("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        mPhotoView.setImageBitmap(bitmap);

    }
}
