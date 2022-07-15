package com.example.metronom.OtherClasses;

import android.content.Context;
import android.content.SharedPreferences;

public class Functions {
    public static SharedPreferences getMySharedPreferences(Context context) {
        return context.getSharedPreferences("metronomeData", 0);
    }
}
