package Likol;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.exifinterface.media.ExifInterface;

import com.example.myapplication.MainActivity;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Base64;

public class BitmapProcessingAsyncTask extends AsyncTask<Uri, Integer, String> {
    private Context context;
    private MainActivity activity;
    private final String TAG = "BitmapProcessingAsyncTask";
    private Uri fileUri;

    public BitmapProcessingAsyncTask(Context context)
    {
        this.context = context;
        this.activity = (MainActivity)context;
    }

    @Override
    protected String doInBackground(Uri... uris) {
        fileUri = uris[0];
        String image_base64 = null;

        try {
            Bitmap image = getBitmapFromUri();
            Log.d(TAG, String.format("file size: %d bytes", image.getByteCount()));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 80, out);
            Log.d(TAG, String.format("JPEG compress size: %d bytes", out.size()));
            image_base64 = Base64.getUrlEncoder().encodeToString(out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image_base64;
    }

    public Bitmap scaleDown(Bitmap realImage, boolean filter) {
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

    private FileDescriptor getFileDescriptor() throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                context.getContentResolver().openFileDescriptor(this.fileUri, "r");
        return parcelFileDescriptor.getFileDescriptor();
    }

    private Bitmap getBitmapFromUri() throws IOException {
        Bitmap image = BitmapFactory.decodeFileDescriptor(getFileDescriptor());

        int MAX_FILE_SIZE = 204800;
        if (image.getByteCount() > MAX_FILE_SIZE)
            image = scaleDown(image, true);

        return rotateImage(image);
    }

    private Bitmap rotateImage(Bitmap bitmap) throws IOException {
        int rotate = 0;
        ExifInterface exif = new ExifInterface(getFileDescriptor());

        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
    }

    @Override
    protected void onPostExecute(String s)
    {
        activity.messagePublish("/event:image " + s);
    }
}
