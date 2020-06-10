package Likol;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Base64;

public class ImageMessageDecoder extends AsyncTask<String, Integer, Bitmap> {

    private final String TAG = "ImageMessageDecoder";

    @Override
    protected Bitmap doInBackground(String... strings) {
        byte[] imgInBytes = Base64.getUrlDecoder().decode(strings[0]);
        Bitmap image = BitmapFactory.decodeByteArray(imgInBytes, 0, imgInBytes.length);
        Log.d(TAG, String.format("decoder image size: %d bytes", image.getByteCount()));
        return image;
    }

}
