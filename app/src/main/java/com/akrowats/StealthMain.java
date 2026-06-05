package com.akrowats;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Akrowats - StealthMain
 * WA 2.26.20.72 | AKRO © 2026
 * أسماء الـ classes مستخرجة مباشرة من DEX
 */
public class StealthMain implements IXposedHookLoadPackage {

    private static final String TAG = "Akrowats";
    private static final String WA  = "com.whatsapp";

    // ══ Class Names — WA 2.26.20.72 (من DEX مباشرة) ══
    private static final String CLS_READ_RECEIPT  = "X.C34077bA";  // WatermarkReadReceiptListener
    private static final String CLS_PRESENCE      = "X.C1GE";      // PresenceStateManager
    private static final String CLS_STORY         = "X.C10572Ut";  // StatusReceiptStore
    private static final String CLS_TYPING        = "X.C1wY";      // HandleMeComposing
    private static final String CLS_OTA           = "X.C1zT";      // OTAUpdateVersion

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!WA.equals(lpparam.packageName)) return;
        XposedBridge.log(TAG + ": hooking WA");

        hookReadReceipt(lpparam.classLoader);
        hookOnlineStatus(lpparam.classLoader);
        hookStoryReceipt(lpparam.classLoader);
        hookTyping(lpparam.classLoader);
        hookOtaUpdate(lpparam.classLoader);
    }

    // ── helper: hook كل methods بالاسم ──────────────────────────
    private void hookAllByName(ClassLoader cl, String cls, String method,
                               boolean staticOnly, int paramCount, XC_MethodHook hook) {
        try {
            Class<?> c = XposedHelpers.findClass(cls, cl);
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                if (!m.getName().equals(method)) continue;
                if (staticOnly && !java.lang.reflect.Modifier.isStatic(m.getModifiers())) continue;
                if (paramCount >= 0 && m.getParameterCount() != paramCount) continue;
                XposedBridge.hookAllMethods(c, method, hook);
                XposedBridge.log(TAG + ": hooked " + cls + "#" + method);
                return;
            }
        } catch (Throwable e) {
            XposedBridge.log(TAG + ": FAIL " + cls + "#" + method + " -> " + e);
        }
    }

    // ── 1. Read Receipt ──────────────────────────────────────────
    private void hookReadReceipt(ClassLoader cl) {
        hookAllByName(cl, CLS_READ_RECEIPT, "A9C", false, -1, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam p) {
                MainHook.prefs.reload();
                if (MainHook.prefs.getBoolean("hide_read", false)) {
                    p.setResult(null);
                    XposedBridge.log(TAG + ": read receipt blocked");
                }
            }
        });
    }

    // ── 2. Online Status ─────────────────────────────────────────
    private void hookOnlineStatus(ClassLoader cl) {
        // A00 = static setAvailable(C1GE, boolean) — 2 params
        hookAllByName(cl, CLS_PRESENCE, "A00", true, 2, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam p) {
                MainHook.prefs.reload();
                if (MainHook.prefs.getBoolean("hide_online", false)) {
                    p.setResult(null);
                    XposedBridge.log(TAG + ": setAvailable blocked");
                }
            }
        });
        // A01 = setUnavailable() — 0 params
        hookAllByName(cl, CLS_PRESENCE, "A01", false, 0, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam p) {
                MainHook.prefs.reload();
                if (MainHook.prefs.getBoolean("hide_online", false)) {
                    p.setResult(null);
                    XposedBridge.log(TAG + ": setUnavailable blocked");
                }
            }
        });
    }

    // ── 3. Story Receipt ─────────────────────────────────────────
    private void hookStoryReceipt(ClassLoader cl) {
        XC_MethodHook storyHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam p) {
                MainHook.prefs.reload();
                if (MainHook.prefs.getBoolean("stealth_story", false)) {
                    p.setResult(null);
                    XposedBridge.log(TAG + ": story receipt blocked");
                }
            }
        };
        hookAllByName(cl, CLS_STORY, "A0D", false, -1, storyHook);
        hookAllByName(cl, CLS_STORY, "A0E", false, -1, storyHook);
    }

    // ── 4. Typing Indicator ──────────────────────────────────────
    private void hookTyping(ClassLoader cl) {
        hookAllByName(cl, CLS_TYPING, "A01", true, -1, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam p) {
                MainHook.prefs.reload();
                if (MainHook.prefs.getBoolean("hide_typing", false)) {
                    p.setResult(null);
                    XposedBridge.log(TAG + ": typing blocked");
                }
            }
        });
    }

    // ── 5. OTA Update Blocker ────────────────────────────────────
    private void hookOtaUpdate(ClassLoader cl) {
        XC_MethodHook otaHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam p) {
                p.setResult(null);
                XposedBridge.log(TAG + ": OTA blocked");
            }
        };
        hookAllByName(cl, CLS_OTA, "A00", true, -1, otaHook);
        hookAllByName(cl, CLS_OTA, "A02", false, -1, otaHook);
    }
}
