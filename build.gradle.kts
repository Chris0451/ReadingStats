// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // Plugin Google Services via alias del catalogo
    alias(libs.plugins.google.services) apply false
    // Hilt (se avrai moduli che lo applicano a livello di modulo)
    alias(libs.plugins.hilt.android) apply false
    // Kapt (verrà applicato nel modulo :app)
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.ksp) apply false
}