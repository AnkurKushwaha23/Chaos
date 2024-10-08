// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
}
buildscript{
    dependencies {
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.8.1")
//        classpath(libs.plugins.navigation.safe.args.gradle.plugin)
    }
}