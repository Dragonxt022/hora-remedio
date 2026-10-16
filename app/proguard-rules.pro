# Preserve Kotlin metadata and annotations used by Room/reflection
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions, RuntimeVisibleAnnotations

# Room
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-dontwarn androidx.room.paging.**

# WorkManager workers (instantiated via reflection)
-keep class * extends androidx.work.ListenableWorker { public <init>(...); }

# Glance widgets
-keep class androidx.glance.** { *; }
-dontwarn androidx.glance.**

# Play Billing
-keep class com.android.billingclient.** { *; }
-dontwarn com.android.billingclient.**

# App entities/models (accessed via reflection by Room and backup)
-keep class com.example.aprobile.data.** { *; }

# SQLCipher (JNI + reflexão)
-keep class net.zetetic.database.** { *; }
-keep class net.sqlcipher.** { *; }
-keep class net.zetetic.database.sqlcipher.** { *; }
-dontwarn net.zetetic.database.**

# Google Code Scanner / ML Kit
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_barcode** { *; }
-dontwarn com.google.mlkit.**

# kotlinx.serialization (backup em JSON)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * { kotlinx.serialization.KSerializer serializer(...); }

# Coil
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn coil.**
