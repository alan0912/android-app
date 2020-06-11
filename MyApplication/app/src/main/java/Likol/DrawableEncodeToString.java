package Likol;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class DrawableEncodeToString extends AsyncTask<BitmapDrawable, Integer, String> {
    private Context context;

    public DrawableEncodeToString(Context context)
    {
        this.context = context;
    }
    @Override
    protected String doInBackground(BitmapDrawable... drawables) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bitmap bm = drawables[0].getBitmap();
        bm.compress(Bitmap.CompressFormat.JPEG, 90, out);

        return Base64.getUrlEncoder().encodeToString(out.toByteArray());
    }

    @Override
    protected void onPostExecute(String s)
    {

    }
}
