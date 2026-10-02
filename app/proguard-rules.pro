# --- Firebase ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# --- Hilt ---
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# --- Kotlin Coroutines ---
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# --- Compose ---
-keep class androidx.compose.runtime.** { *; }

# --- Room ---
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# --- Keep app models (data classes used by Firestore) ---
-keep class com.school.manager.domain.model.** { *; }
-keep class com.school.manager.ui.admin.AdminStats { *; }
-keep class com.school.manager.ui.teacher.StudentRow { *; }
-keep class com.school.manager.ui.student.* { *; }
-keep class com.school.manager.ui.parent.* { *; }
-keep class com.school.manager.ui.notice.Notice { *; }
-keep class com.school.manager.ui.fees.FeeRecord { *; }
