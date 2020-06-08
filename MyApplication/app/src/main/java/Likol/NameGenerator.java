package Likol;

import android.content.Context;

import com.example.myapplication.R;

public class NameGenerator {
    private String prefix;
    private String suffix;

    public NameGenerator(Context context) {
        prefix = context.getResources().getString(R.string.guest);
        suffix = generatorSuffix();
    }

    private String generatorSuffix()
    {
        int max = 999999;
        int min = 100000;
        return "" + (int)(Math.random() * ((max-min)+1) + min);
    }

    public String getName()
    {
        return String.format("%s %s", prefix, suffix);
    }
}
