package com.akrowats;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import de.robv.android.xposed.XposedBridge;

public class MediaSaver {

    private static final String SAVE_DIR_IMG = "Pictures/Akrowats";
    private static final String SAVE_DIR_VID = "Movies/Akrowats";

    public static void init(de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam lpparam) {
        XposedBridge.log("[Akrowats] MediaSaver ready");
    }

    // ─── حفظ الاستوري الحالي (أحدث ملف في .Statuses) ────────────────────
    public static void saveCurrentStory(Activity act) {
        try {
            File statusDir = getStatusDir();
            if (statusDir == null || !statusDir.exists()) {
                toast(act, "⚠️ مش لاقي مجلد .Statuses");
                return;
            }

            File[] files = statusDir.listFiles(f -> {
                String n = f.getName().toLowerCase();
                return n.endsWith(".jpg") || n.endsWith(".jpeg") ||
                       n.endsWith(".png") || n.endsWith(".mp4");
            });

            if (files == null || files.length == 0) {
                toast(act, "⚠️ مفيش استوري محفوظ في .Statuses");
                return;
            }

            // أحدث ملف
            Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
            File src = files[0];

            boolean isVideo = src.getName().toLowerCase().endsWith(".mp4");
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            saveFileToGallery(act, src, "story_" + ts + (isVideo ? ".mp4" : ".jpg"), isVideo);

        } catch (Throwable t) {
            toast(act, "❌ فشل حفظ الاستوري: " + t.getMessage());
            XposedBridge.log("[Akrowats] saveCurrentStory error: " + t);
        }
    }

    // ─── حفظ صورة البروفايل من كاش واتساب ───────────────────────────────
    public static void saveProfileFromCache(Activity act) {
        try {
            // مسارات كاش البروفايل في واتساب
            File[] cacheDirs = {
                new File(Environment.getExternalStorageDirectory(),
                    "Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Profile Photos"),
                new File(act.getFilesDir().getParent(), "cache/Profile Pictures"),
                new File(act.getFilesDir().getParent(), "databases/ProfilePictures"),
            };

            File src = null;
            for (File dir : cacheDirs) {
                if (dir.exists() && dir.isDirectory()) {
                    File[] imgs = dir.listFiles(f ->
                        f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg"));
                    if (imgs != null && imgs.length > 0) {
                        Arrays.sort(imgs, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
                        src = imgs[0];
                        break;
                    }
                }
            }

            if (src == null) {
                toast(act, "⚠️ مش لاقي صورة البروفايل في الكاش");
                return;
            }

            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            saveFileToGallery(act, src, "profile_" + ts + ".jpg", false);

        } catch (Throwable t) {
            toast(act, "❌ فشل: " + t.getMessage());
            XposedBridge.log("[Akrowats] saveProfileFromCache error: " + t);
        }
    }

    // ─── حفظ Bitmap (صورة مستخرجة من ImageView) ──────────────────────────
    public static void saveBitmapToGallery(Context ctx, Bitmap bmp, String prefix) {
        try {
            String ts = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = prefix + ts + ".jpg";
            OutputStream out = openOutputStream(ctx, fileName, false);
            if (out == null) {
                toast(ctx, "❌ مش قادر يفتح ملف للحفظ");
                return;
            }
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            toast(ctx, "✅ تم الحفظ: " + fileName);
        } catch (Throwable t) {
            toast(ctx, "❌ فشل الحفظ: " + t.getMessage());
        }
    }

    // ─── حفظ ملف في الجاليري ──────────────────────────────────────────────
    public static void saveFileToGallery(Context ctx, File src, String fileName, boolean isVideo) {
        try {
            OutputStream out = openOutputStream(ctx, fileName, isVideo);
            if (out == null) {
                toast(ctx, "❌ مش قادر يفتح ملف للحفظ");
                return;
            }
            FileInputStream in = new FileInputStream(src);
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            in.close();
            out.flush();
            out.close();
            toast(ctx, "✅ تم الحفظ: " + fileName);
        } catch (Throwable t) {
            toast(ctx, "❌ فشل الحفظ: " + t.getMessage());
            XposedBridge.log("[Akrowats] saveFileToGallery error: " + t);
        }
    }

    // ─── يفتح OutputStream حسب إصدار Android ─────────────────────────────
    private static OutputStream openOutputStream(Context ctx, String fileName, boolean isVideo)
            throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues cv = new ContentValues();
            cv.put(isVideo ? MediaStore.Video.Media.DISPLAY_NAME
                           : MediaStore.Images.Media.DISPLAY_NAME, fileName);
            cv.put(isVideo ? MediaStore.Video.Media.MIME_TYPE
                           : MediaStore.Images.Media.MIME_TYPE,
                   isVideo ? "video/mp4" : "image/jpeg");
            cv.put(isVideo ? MediaStore.Video.Media.RELATIVE_PATH
                           : MediaStore.Images.Media.RELATIVE_PATH,
                   isVideo ? SAVE_DIR_VID : SAVE_DIR_IMG);
            Uri uri = ctx.getContentResolver().insert(
                isVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        : MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
            return uri != null ? ctx.getContentResolver().openOutputStream(uri) : null;
        } else {
            String folder = isVideo ? SAVE_DIR_VID : SAVE_DIR_IMG;
            File dir = new File(Environment.getExternalStorageDirectory(), folder);
            if (!dir.exists()) dir.mkdirs();
            return new FileOutputStream(new File(dir, fileName));
        }
    }

    // ─── مسار مجلد .Statuses ──────────────────────────────────────────────
    private static File getStatusDir() {
        File[] candidates = {
            new File(Environment.getExternalStorageDirectory(),
                "Android/media/com.whatsapp/WhatsApp/Media/.Statuses"),
            new File(Environment.getExternalStorageDirectory(),
                "WhatsApp/Media/.Statuses"),
        };
        for (File f : candidates) if (f.exists()) return f;
        return null;
    }

    public static void toast(Context ctx, String msg) {
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }
}
