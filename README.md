# ⚡ Akrowats
**LSPosed Module for WhatsApp 2.26.19.73**

---

## الميزات
| الميزة | الوصف |
|--------|--------|
| 🙈 إخفاء مشاهدة الاستوري | يمنع إرسال إشعار المشاهدة لأصحاب الاستوري |
| 💾 حفظ صور البروفايل | زر حفظ عند فتح أي بروفايل |
| 💾 حفظ الاستوري | زر حفظ عند مشاهدة الاستوري (صورة وفيديو) |

---

## المتطلبات
- ✅ روت (Magisk أو KernelSU)
- ✅ LSPosed Framework مثبت
- ✅ واتساب إصدار 2.26.19.73

---

## طريقة التثبيت

### 1. بناء المشروع
```bash
# افتح المشروع في Android Studio
# Build > Generate Signed APK
# أو عبر command line:
./gradlew assembleRelease
```

### 2. تثبيت الـ APK
```bash
adb install app/build/outputs/apk/release/app-release.apk
```

### 3. تفعيل في LSPosed
1. افتح **LSPosed Manager**
2. روح **Modules**
3. فعّل **Akrowats**
4. اختار **WhatsApp** من قائمة التطبيقات المستهدفة
5. أعد تشغيل واتساب

### 4. الإعدادات
- افتح تطبيق **Akrowats** مباشرة لضبط الخيارات
- **أو** روح واتساب > Settings > Privacy وهتلاقي قسم Akrowats في الأسفل

---

## الملفات المحفوظة
```
/storage/emulated/0/Pictures/Akrowats/
├── profile_20240101_120000.jpg
└── story_20240101_120500.mp4
```

---

## ملاحظات تقنية
- الـ hooks مبنية على إصدار **2.26.19.73** تحديداً
- لو اتحدّث واتساب، ممكن تحتاج تحدّث أسماء الكلاسات
- راجع **LSPosed logs** لو في مشكلة: `adb logcat | grep Akrowats`

---

## Class Names (2.26.20.72)
```
StealthStory:   com.whatsapp.status.StatusPlaybackManager
ProfilePhoto:   com.whatsapp.profilepicture.ProfilePictureActivity
StatusViewer:   com.whatsapp.status.ui.StatusViewerActivity
PrivacyScreen:  com.whatsapp.privacy.PrivacySettingsActivity
```

---

*Akrowats v1.0 — مصنوع بـ ❤️*
