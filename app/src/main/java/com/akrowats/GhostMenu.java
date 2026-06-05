package com.akrowats;

import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Ghost Menu — يتعلق على android.app.Activity مباشرة
 * ويفلتر على أي Activity داخل com.whatsapp
 */
public class GhostMenu {

    private static final int GHOST_MENU_ID     = 0xAC00;
    private static final int ITEM_STEALTH      = 0xAC01;
    private static final int ITEM_SAVE_STORY   = 0xAC02;
    private static final int ITEM_SAVE_PROFILE = 0xAC03;
    private static final int ITEM_HIDE_TYPING  = 0xAC04;
    private static final int ITEM_HIDE_ONLINE  = 0xAC05;
    private static final int ITEM_HIDE_READ    = 0xAC06;

    public static void init(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // hook على Activity الأم مباشرة — يشمل كل Activities في WA
            Class<?> actClass = XposedHelpers.findClass(
                    "android.app.Activity", lpparam.classLoader);

            XposedBridge.hookAllMethods(actClass, "onCreateOptionsMenu", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Activity act = (Activity) param.thisObject;
                    // فلتر: فقط Activities داخل واتساب
                    if (!act.getPackageName().contains("whatsapp")) return;
                    Menu menu = (Menu) param.args[0];
                    addGhostMenu(menu, act);
                }
            });

            XposedBridge.hookAllMethods(actClass, "onOptionsItemSelected", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Activity act = (Activity) param.thisObject;
                    if (!act.getPackageName().contains("whatsapp")) return;
                    if (handleGhostItem((MenuItem) param.args[0], act)) {
                        param.setResult(true);
                    }
                }
            });

            XposedBridge.log("[Akrowats] GhostMenu hooked on Activity base class");
        } catch (Throwable t) {
            XposedBridge.log("[Akrowats] GhostMenu FAIL: " + t);
        }
    }

    private static void addGhostMenu(Menu menu, Activity act) {
        // تجنب الإضافة المتكررة
        if (menu.findItem(GHOST_MENU_ID) != null) return;

        MainHook.prefs.reload();

        SubMenu ghost = menu.addSubMenu(0, GHOST_MENU_ID, 0, "Ghost 👻");

        boolean stealth    = MainHook.prefs.getBoolean("stealth_story", false);
        boolean hideTyping = MainHook.prefs.getBoolean("hide_typing",   false);
        boolean hideOnline = MainHook.prefs.getBoolean("hide_online",   false);
        boolean hideRead   = MainHook.prefs.getBoolean("hide_read",     false);

        ghost.add(0, ITEM_STEALTH,      0, (stealth    ? "✅ " : "⬜ ") + "إخفاء مشاهدة الاستوري");
        ghost.add(0, ITEM_HIDE_TYPING,  0, (hideTyping ? "✅ " : "⬜ ") + "إخفاء مؤشر الكتابة");
        ghost.add(0, ITEM_HIDE_ONLINE,  0, (hideOnline ? "✅ " : "⬜ ") + "إخفاء حالة الاتصال");
        ghost.add(0, ITEM_HIDE_READ,    0, (hideRead   ? "✅ " : "⬜ ") + "إخفاء علامة القراءة");
        ghost.add(0, ITEM_SAVE_STORY,   0, "💾 حفظ الاستوري");
        ghost.add(0, ITEM_SAVE_PROFILE, 0, "📸 حفظ صورة البروفايل");
    }

    public static boolean handleGhostItem(MenuItem item, Activity act) {
        int id = item.getItemId();
        Context ctx = act.getApplicationContext();

        if (id == ITEM_STEALTH) {
            boolean v = !MainHook.prefs.getBoolean("stealth_story", false);
            AkrowatsPrefs.save(ctx, "stealth_story", v);
            toast(act, v ? "👻 إخفاء الاستوري: مفعّل" : "👁️ إخفاء الاستوري: معطّل");
            return true;
        }
        if (id == ITEM_HIDE_TYPING) {
            boolean v = !MainHook.prefs.getBoolean("hide_typing", false);
            AkrowatsPrefs.save(ctx, "hide_typing", v);
            toast(act, v ? "✅ إخفاء الكتابة: مفعّل" : "❌ إخفاء الكتابة: معطّل");
            return true;
        }
        if (id == ITEM_HIDE_ONLINE) {
            boolean v = !MainHook.prefs.getBoolean("hide_online", false);
            AkrowatsPrefs.save(ctx, "hide_online", v);
            toast(act, v ? "✅ إخفاء الاتصال: مفعّل" : "❌ إخفاء الاتصال: معطّل");
            return true;
        }
        if (id == ITEM_HIDE_READ) {
            boolean v = !MainHook.prefs.getBoolean("hide_read", false);
            AkrowatsPrefs.save(ctx, "hide_read", v);
            toast(act, v ? "✅ إخفاء القراءة: مفعّل" : "❌ إخفاء القراءة: معطّل");
            return true;
        }
        if (id == ITEM_SAVE_STORY) {
            MediaSaver.saveCurrentStory(act);
            return true;
        }
        if (id == ITEM_SAVE_PROFILE) {
            MediaSaver.saveProfileFromCache(act);
            return true;
        }
        return false;
    }

    private static void toast(Context ctx, String msg) {
        android.widget.Toast.makeText(ctx, msg, android.widget.Toast.LENGTH_SHORT).show();
    }
}
