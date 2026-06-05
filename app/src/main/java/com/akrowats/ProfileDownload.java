package com.akrowats;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * زر حفظ صورة البروفايل
 * يتعلق على android.app.Activity ويفلتر على شاشات البروفايل
 */
public class ProfileDownload {

    private static final String BTN_TAG = "akrowats_dl_btn";

    // كلمات دلالية في اسم الـ Activity تعني إنها شاشة بروفايل أو صورة
    private static final String[] PROFILE_HINTS = {
        "profile", "picture", "avatar", "photo", "contact", "info"
    };

    public static void init(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> actClass = XposedHelpers.findClass(
                    "android.app.Activity", lpparam.classLoader);

            XposedBridge.hookAllMethods(actClass, "onWindowFocusChanged", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    boolean hasFocus = (Boolean) param.args[0];
                    if (!hasFocus) return;

                    Activity act = (Activity) param.thisObject;
                    if (!act.getPackageName().contains("whatsapp")) return;

                    String clsName = act.getClass().getName().toLowerCase();
                    boolean isProfile = false;
                    for (String hint : PROFILE_HINTS) {
                        if (clsName.contains(hint)) { isProfile = true; break; }
                    }
                    if (!isProfile) return;

                    injectDownloadButton(act);
                }
            });

            XposedBridge.log("[Akrowats] ProfileDownload hooked on Activity base");
        } catch (Throwable t) {
            XposedBridge.log("[Akrowats] ProfileDownload FAIL: " + t);
        }
    }

    private static void injectDownloadButton(Activity act) {
        try {
            ViewGroup root = (ViewGroup) act.getWindow().getDecorView();
            if (root.findViewWithTag(BTN_TAG) != null) return;

            ImageView profileImg = findLargeImageView(root);
            if (profileImg == null) return;

            ViewGroup imgParent = (ViewGroup) profileImg.getParent();
            if (imgParent == null) return;

            TextView btn = new TextView(act);
            btn.setTag(BTN_TAG);
            btn.setText("⬇ حفظ");
            btn.setTextSize(13f);
            btn.setTextColor(Color.WHITE);
            btn.setBackgroundColor(Color.parseColor("#CC25D366"));
            btn.setPadding(24, 12, 24, 12);

            if (imgParent instanceof FrameLayout) {
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                lp.gravity = Gravity.TOP | Gravity.START;
                lp.setMargins(16, 16, 0, 0);
                imgParent.addView(btn, lp);
            } else {
                // نلف الصورة في FrameLayout
                FrameLayout wrapper = new FrameLayout(act);
                ViewGroup.LayoutParams profileLp = profileImg.getLayoutParams();
                int idx = getChildIndex(imgParent, profileImg);
                imgParent.removeView(profileImg);

                wrapper.addView(profileImg, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                ));
                FrameLayout.LayoutParams btnLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                );
                btnLp.gravity = Gravity.TOP | Gravity.START;
                btnLp.setMargins(16, 16, 0, 0);
                wrapper.addView(btn, btnLp);

                if (idx >= 0) imgParent.addView(wrapper, idx, profileLp);
                else imgParent.addView(wrapper, profileLp);
            }

            btn.setOnClickListener(v -> {
                Bitmap bmp = extractBitmap(profileImg);
                if (bmp != null) MediaSaver.saveBitmapToGallery(act, bmp, "profile_");
                else MediaSaver.saveProfileFromCache(act);
            });

            XposedBridge.log("[Akrowats] ProfileDownload button injected in: "
                    + act.getClass().getName());

        } catch (Throwable t) {
            XposedBridge.log("[Akrowats] injectDownloadButton error: " + t.getMessage());
        }
    }

    private static ImageView findLargeImageView(ViewGroup root) {
        ImageView best = null;
        int bestArea = 0;
        for (int i = 0; i < root.getChildCount(); i++) {
            View child = root.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView iv = (ImageView) child;
                int area = iv.getWidth() * iv.getHeight();
                if (area > bestArea && iv.getDrawable() != null) {
                    bestArea = area;
                    best = iv;
                }
            } else if (child instanceof ViewGroup) {
                ImageView found = findLargeImageView((ViewGroup) child);
                if (found != null) {
                    int area = found.getWidth() * found.getHeight();
                    if (area > bestArea) { bestArea = area; best = found; }
                }
            }
        }
        return best;
    }

    private static Bitmap extractBitmap(ImageView iv) {
        try {
            Drawable d = iv.getDrawable();
            if (d instanceof BitmapDrawable) return ((BitmapDrawable) d).getBitmap();
        } catch (Throwable t) { /* ignore */ }
        return null;
    }

    private static int getChildIndex(ViewGroup parent, View child) {
        for (int i = 0; i < parent.getChildCount(); i++)
            if (parent.getChildAt(i) == child) return i;
        return -1;
    }
}
