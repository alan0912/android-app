package Likol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.example.myapplication.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Base64;

public class BitmapProcessingAsyncTask extends AsyncTask<Uri, Integer, String> {
    private Context context;
    private MainActivity activity;
    private final String TAG = "BitmapProcessingAsyncTask";

    public BitmapProcessingAsyncTask(Context context)
    {
        this.context = context;
        this.activity = (MainActivity)context;
    }

    @Override
    protected String doInBackground(Uri... uris) {
        String image_base64 = null;
        for(Uri uri: uris) {
            try {
                Bitmap image = getBitmapFromUri(uri);
                Log.d(TAG, String.format("file size: %d bytes", image.getByteCount()));

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.JPEG, 80, out);
                Log.d(TAG, String.format("JPEG compress size: %d bytes", out.size()));
                image_base64 = Base64.getUrlEncoder().encodeToString(out.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return image_base64;
    }

    public Bitmap scaleDown(Bitmap realImage, boolean filter) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        int MAX_SIDE_SIZE = 1024;
        float ratio = Math.min(
                (float) MAX_SIDE_SIZE / realImage.getWidth(),
                (float) MAX_SIDE_SIZE / realImage.getHeight());
        int width = Math.round((float) ratio * realImage.getWidth());
        int height = Math.round((float) ratio * realImage.getHeight());

        Bitmap newBitmap = Bitmap.createScaledBitmap(realImage, width,
                height, filter);

        return newBitmap;
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        int MAX_FILE_SIZE = 204800;
        if (image.getByteCount() > MAX_FILE_SIZE)
            image = scaleDown(image, true);
        return image;
    }

    @Override
    protected void onPostExecute(String s)
    {

        activity.messagePublish("/event:image " + s);
        Log.d(TAG, String.format("image base64 string: /image:%s", s));
    }
}
