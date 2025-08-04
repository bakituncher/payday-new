#------------- Google Mobile Ads (AdMob) Kuralları -------------
# Release modda reklamların çalışması için bu kurallar zorunludur.
# Google Mobile Ads SDK'sının sınıflarının ProGuard tarafından kaldırılmasını engeller.
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}

#------------- Gson (JSON Kütüphanesi) Kuralları -------------
# Veri modellerinizin (örn: BackupData) adlarının değiştirilmesini önler.
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keepattributes Signature, InnerClasses

#------------- Google Drive API ve Google Sign-In Kuralları -------------
# Bu API'lerin kullandığı sınıfların ve alanların korunmasını sağlar.
-keep class com.google.api.services.drive.** { *; }
-keepclassmembers class * {
    @com.google.api.client.util.Key <fields>;
}

#------------- Firebase Crashlytics Kuralları -------------
# Hata raporlarının doğru şekilde oluşturulabilmesi için önemlidir.
-keep class com.google.firebase.crashlytics.** { *; }
-keep public class * extends com.google.firebase.crashlytics.CustomKeysAndValues
-keep class * implements com.google.firebase.crashlytics.internal.persistence.CrashlyticsReportPersistence
-keepattributes *Annotation*

#------------- Room (Veritabanı) Kuralları -------------
# Veritabanı tablolarınızla eşleşen @Entity ve @Database ile işaretlenmiş
# sınıfların adlarının değiştirilmesini engeller.
-keep class androidx.room.RoomDatabase
-keepclassmembers class * {
    @androidx.room.Entity *;
    @androidx.room.Dao *;
    @androidx.room.Database *;
    @androidx.room.TypeConverter *;
}
-keep @androidx.room.Entity class *
-keep @androidx.room.Database class *

#------------- MPAndroidChart (Grafik Kütüphanesi) Kuralları -------------
-keep class com.github.mikephil.charting.** { *; }

#------------- Projenizin Kendi Sınıfları İçin Kural -------------
# Bu kural, 'com.codenzi.payday' paketi altındaki tüm sınıfları ve üyelerini korur.
# Bu kural genellikle veri sınıflarınız (Data-Model) ve diğer önemli sınıflarınız
# için ek bir koruma katmanı sağlar.
-keep class com.codenzi.payday.** { *; }