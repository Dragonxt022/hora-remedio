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
