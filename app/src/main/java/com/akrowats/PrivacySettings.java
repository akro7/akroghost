package com.akrowats;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PrivacySettings {

    public static void init(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedBridge.hookAllMethods(
                XposedHelpers.findClass("android.app.Activity", lpparam.classLoader),
                "onResume",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Activity act = (Activity) param.thisObject;
                        String cls = act.getClass().getName().toLowerCase();
                        String pkg = act.getPackageName();

                        if (!pkg.equals("com.whatsapp") && !pkg.equals("com.whatsapp.w4b")) return;
                        if (!cls.contains("privacy") && !cls.contains("setting")) return;

                        ViewGroup root = (ViewGroup) act.getWindow().getDecorView();
                        injectAkrowatsSection(act, root);
                    }
                }
            );
            XposedBridge.log("[Akrowats] PrivacySettings hook ✓");
        } catch (Throwable t) {
            XposedBridge.log("[Akrowats] PrivacySettings hook failed: " + t.getMessage());
        }
    }

    public static void injectAkrowatsSection(Context ctx, ViewGroup parent) {
        if (parent == null) return;
        if (parent.findViewWithTag("akrowats_ghost_section") != null) return;

        LinearLayout section = new LinearLayout(ctx);
        section.setTag("akrowats_ghost_section");
        section.setOrientation(LinearLayout.VERTICAL);
        section.setBackgroundColor(Color.parseColor("#0D1117"));
        section.setPadding(0, 4, 0, 16);

        TextView header = new TextView(ctx);
        header.setText("👻 Ghost - Akrowats");
        header.setTextSize(14f);
        header.setTextColor(Color.parseColor("#25D366"));
        header.setTypeface(null, Typeface.BOLD);
        header.setPadding(48, 24, 48, 8);
        section.addView(header);

        section.addView(buildSwitch(ctx, "إخفاء مشاهدة الاستوري",  "لا أحد يعرف إنك شفت استوريه",        "stealth_story"));
        section.addView(buildSwitch(ctx, "إخفاء مؤشر الكتابة",     "لا يظهر للآخرين أنك تكتب",             "hide_typing"));
        section.addView(buildSwitch(ctx, "إخفاء حالة الاتصال",     "تظهر دائماً غير متصل",                 "hide_online"));
        section.addView(buildSwitch(ctx, "إخفاء علامة القراءة",    "لا ترسل علامة الاستلام الزرقاء",       "hide_read"));
        section.addView(buildSwitch(ctx, "حفظ الاستوري تلقائياً",  "يحفظ الاستوري في الجاليري",            "save_story"));

        parent.addView(section);
    }

    private static LinearLayout buildSwitch(Context ctx, String title, String sub, String key) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(48, 20, 48, 20);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout texts = new LinearLayout(ctx);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
            ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        texts.setLayoutParams(lp);

        TextView t = new TextView(ctx);
        t.setText(title);
        t.setTextSize(15f);
        t.setTextColor(Color.WHITE);

        TextView s = new TextView(ctx);
        s.setText(sub);
        s.setTextSize(12f);
        s.setTextColor(Color.GRAY);

        texts.addView(t);
        texts.addView(s);

        Switch sw = new Switch(ctx);
        MainHook.prefs.reload();
        sw.setChecked(MainHook.prefs.getBoolean(key, false));
        sw.setOnCheckedChangeListener((btn, checked) -> AkrowatsPrefs.save(ctx, key, checked));

        row.addView(texts);
        row.addView(sw);
        return row;
    }
}
