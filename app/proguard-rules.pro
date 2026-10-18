# Mathra Oubliettes: shrink, rename and flatten every class so the release APK reads as noise.
-repackageclasses ''
-allowaccessmodification
-overloadaggressively
-renamesourcefileattribute ''
-optimizationpasses 5
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}
