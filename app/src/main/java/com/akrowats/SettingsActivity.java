package com.akrowats;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0D1117"));

        // ─── Header ───────────────────────────────────────
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setGravity(Gravity.CENTER);
        header.setPadding(0, 72, 0, 40);
        header.setBackgroundColor(Color.parseColor("#161B22"));

        TextView emoji = new TextView(this);
        emoji.setText("👻");
        emoji.setTextSize(48f);
        emoji.setGravity(Gravity.CENTER);

        TextView appName = new TextView(this);
        appName.setText("Ghost · Akrowats");
        appName.setTextSize(26f);
        appName.setTextColor(Color.parseColor("#25D366"));
        appName.setTypeface(null, Typeface.BOLD);
        appName.setGravity(Gravity.CENTER);
        appName.setPadding(0, 8, 0, 0);

        TextView appSub = new TextView(this);
        appSub.setText("LSPosed Module · WhatsApp Privacy");
        appSub.setTextSize(12f);
        appSub.setTextColor(Color.GRAY);
        appSub.setGravity(Gravity.CENTER);
        appSub.setPadding(0, 6, 0, 0);

        header.addView(emoji);
        header.addView(appName);
        header.addView(appSub);
        root.addView(header);

        // ─── السويتشات (تُقرأ وتُكتب مباشرة في الـ prefs) ──────────────
        addSectionHeader(root, "⚙️ الإعدادات");

        addSwitch(root, "إخفاء مشاهدة الاستوري",  "لا أحد يعرف إنك شفت استوريه",         "stealth_story");
        addSwitch(root, "إخفاء مؤشر الكتابة",     "لا يظهر للآخرين أنك تكتب",             "hide_typing");
        addSwitch(root, "إخفاء حالة الاتصال",     "تظهر دائماً غير متصل",                  "hide_online");
        addSwitch(root, "إخفاء علامة القراءة",    "لا ترسل الصح الزرقاء للرسايل",          "hide_read");

        addDivider(root);
        addSectionHeader(root, "📌 تعليمات الاستخدام");
        addInfo(root, "Ghost 👻 في النقاط الثلاث ⋮",
            "افتح واتساب → النقاط الثلاث → Ghost 👻\n" +
            "هتلاقي كل الميزات تقدر تفعّلها أو تعطّلها.");
        addInfo(root, "📸 حفظ صورة البروفايل",
            "افتح صورة البروفايل → هيظهر زر ⬇ حفظ في أعلى يسار الصورة.");
        addInfo(root, "💾 حفظ الاستوري",
            "افتح أي استوري → هيظهر زر 💾 جنب القلب ❤️ في الأسفل.");
        addInfo(root, "⚠️ ملاحظة",
            "بعض الميزات تحتاج إعادة تشغيل واتساب بعد التفعيل.");

        scroll.addView(root);
        setContentView(scroll);
    }

    private void addSectionHeader(LinearLayout parent, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(14f);
        tv.setTextColor(Color.parseColor("#25D366"));
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(48, 28, 48, 8);
        parent.addView(tv);
    }

    private void addSwitch(LinearLayout parent, String title, String sub, String key) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(48, 18, 48, 18);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        texts.setLayoutParams(lp);

        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(15f);
        t.setTextColor(Color.WHITE);

        TextView s = new TextView(this);
        s.setText(sub);
        s.setTextSize(12f);
        s.setTextColor(Color.GRAY);

        texts.addView(t);
        texts.addView(s);

        Switch sw = new Switch(this);
        sw.setChecked(AkrowatsPrefs.get(this, key, false));
        sw.setOnCheckedChangeListener((btn, checked) -> AkrowatsPrefs.save(this, key, checked));

        row.addView(texts);
        row.addView(sw);
        parent.addView(row);
    }

    private void addDivider(LinearLayout parent) {
        TextView div = new TextView(this);
        div.setBackgroundColor(Color.parseColor("#21262D"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 1);
        lp.setMargins(0, 12, 0, 12);
        div.setLayoutParams(lp);
        parent.addView(div);
    }

    private void addInfo(LinearLayout parent, String title, String body) {
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(13f);
        t.setTextColor(Color.parseColor("#25D366"));
        t.setTypeface(null, Typeface.BOLD);
        t.setPadding(48, 20, 48, 4);
        parent.addView(t);

        TextView b = new TextView(this);
        b.setText(body);
        b.setTextSize(12f);
        b.setTextColor(Color.parseColor("#AAAAAA"));
        b.setPadding(48, 0, 48, 16);
        parent.addView(b);
    }
}
