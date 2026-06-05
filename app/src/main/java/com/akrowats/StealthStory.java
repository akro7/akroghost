package com.akrowats;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * StealthStory — محذوف المحتوى القديم
 * الـ hooks انتقلت لـ StealthMain بأسماء DEX حقيقية
 * الملف محتاج موجود عشان MainHook بيستدعيه
 */
public class StealthStory {
    public static void init(XC_LoadPackage.LoadPackageParam lpparam) {
        // تم نقل كل hooks لـ StealthMain.java
    }
}
