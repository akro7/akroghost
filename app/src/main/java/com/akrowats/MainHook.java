package com.akrowats;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    public static final String PACKAGE_NAME = "com.whatsapp";
    public static final String MODULE_PKG   = "com.akrowats";
    public static XSharedPreferences prefs;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(PACKAGE_NAME)) return;

        prefs = new XSharedPreferences(MODULE_PKG, "akrowats_prefs");
        prefs.makeWorldReadable();

        // Privacy hooks — أسماء حقيقية من DEX
        new StealthMain().handleLoadPackage(lpparam);

        // Features إضافية
        MediaSaver.init(lpparam);
        GhostMenu.init(lpparam);
        ProfileDownload.init(lpparam);
        StoryDownload.init(lpparam);
    }
}
