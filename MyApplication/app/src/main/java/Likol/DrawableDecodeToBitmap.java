package Likol;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;

public class DrawableDecodeToBitmap extends AsyncTask<BitmapDrawable, Integer, byte[]> {

    @Override
    protected byte[] doInBackground(BitmapDrawable... drawables) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bitmap bm = drawables[0].getBitmap();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, out);

        return out.toByteArray();
    }
}
