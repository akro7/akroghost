package com.akrowats;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * زر حفظ الاستوري يظهر جنب زر القلب (❤️) في شاشة عرض الاستوري
 * يدعم أسماء activities متعددة
 */
public class StoryDownload {

    private static final String BTN_TAG = "akrowats_story_btn";

    public static void init(XC_LoadPackage.LoadPackageParam lpparam) {
        hookStoryActivity(lpparam);
    }

    private static void hookStoryActivity(XC_LoadPackage.LoadPackageParam lpparam) {
        String[] activities = {
            "com.whatsapp.status.ui.StatusViewerActivity",
            "com.whatsapp.StatusViewerActivity",
            "com.whatsapp.status.StatusActivity",
            "com.whatsapp.status.viewer.StatusViewerActivity",
        };

        for (String actName : activities) {
            try {
                Class<?> actClass = XposedHelpers.findClass(actName, lpparam.classLoader);

                XposedBridge.hookAllMethods(actClass, "onResume", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Activity act = (Activity) param.thisObject;
                        injectStoryButton(act);
                    }
                });

                XposedBridge.log("[Akrowats] StoryDownload hooked: " + actName);
                break;
            } catch (Throwable t) {
                XposedBridge.log("[Akrowats] StoryDownload skip: " + actName);
            }
        }
    }

    private static void injectStoryButton(Activity act) {
        try {
            ViewGroup root = (ViewGroup) act.getWindow().getDecorView();

            // مش موجود؟ نكمل
            if (root.findViewWithTag(BTN_TAG) != null) return;

            // نبحث عن زر القلب (reaction button) عن طريق الـ layout في أسفل الشاشة
            View reactionBar = findReactionBar(root);

            // ─── زر الحفظ ────────────────────────────────
            TextView saveBtn = new TextView(act);
            saveBtn.setTag(BTN_TAG);
            saveBtn.setText("💾");
            saveBtn.setTextSize(22f);
            saveBtn.setGravity(Gravity.CENTER);
            saveBtn.setPadding(20, 12, 20, 12);

            saveBtn.setOnClickListener(v -> MediaSaver.saveCurrentStory(act));

            if (reactionBar != null && reactionBar.getParent() instanceof ViewGroup) {
                // نضيف جنب زر القلب
                ViewGroup barParent = (ViewGroup) reactionBar.getParent();

                if (barParent instanceof LinearLayout) {
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    lp.setMargins(8, 0, 8, 0);
                    lp.gravity = Gravity.CENTER_VERTICAL;
                    barParent.addView(saveBtn, lp);
                    XposedBridge.log("[Akrowats] Story save btn added next to reaction bar");
                    return;
                }
            }

            // fallback: نضيف في أسفل يمين الشاشة كـ FrameLayout overlay
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.gravity = Gravity.BOTTOM | Gravity.END;
            lp.setMargins(0, 0, 90, 48);

            saveBtn.setBackgroundColor(Color.parseColor("#CC1A1A2E"));
            root.addView(saveBtn, lp);
            XposedBridge.log("[Akrowats] Story save btn fallback added");

        } catch (Throwable t) {
            XposedBridge.log("[Akrowats] injectStoryButton error: " + t.getMessage());
        }
    }

    // ─── نبحث عن شريط الـ reactions في أسفل شاشة الاستوري ───────────────
    private static View findReactionBar(ViewGroup root) {
        // نبحث عن LinearLayout أو ViewGroup في أسفل الشاشة يحتوي على ImageView أو emoji
        int screenH = root.getHeight();
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            int[] loc = new int[2];
            child.getLocationInWindow(loc);
            // الـ reaction bar عادةً في ربع الشاشة السفلي
            if (loc[1] > screenH * 0.65f && child instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) child;
                if (vg.getChildCount() >= 2) return vg;
                View inner = findReactionBar(vg);
                if (inner != null) return inner;
            }
        }
        return null;
    }
}
