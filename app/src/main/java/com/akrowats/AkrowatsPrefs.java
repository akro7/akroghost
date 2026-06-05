package com.akrowats;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Akrowats - مساعد حفظ الإعدادات
 * يستخدم MODE_PRIVATE (آمن على Android 7+)
 * XSharedPreferences تقرأ نفس الملف من داخل واتساب
 */
public class AkrowatsPrefs {

    public static final String PREF_NAME = "akrowats_prefs";

    public static void save(Context ctx, String key, boolean value) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(key, value).apply();
    }

    public static boolean get(Context ctx, String key, boolean def) {
        SharedPreferences prefs = ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(key, def);
    }
}
