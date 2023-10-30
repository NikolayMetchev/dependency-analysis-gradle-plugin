package com.autonomousapps.kit.gradle.dependencies

import com.autonomousapps.kit.gradle.Plugin

object Plugins {
  @JvmStatic val KOTLIN_VERSION: String = "1.9.0"

  @JvmStatic val dagpId: String = "com.autonomousapps.dependency-analysis"
  @JvmStatic val dependencyAnalysis: Plugin = Plugin(dagpId, System.getProperty("com.autonomousapps.plugin-under-test.version"))

  @JvmStatic val androidApp: Plugin = Plugin("com.android.application")
  @JvmStatic val androidLib: Plugin = Plugin("com.android.library")
  @JvmStatic val gradleEnterprise: Plugin = Plugin("com.gradle.enterprise", "3.11.4")
  @JvmStatic val kapt: Plugin = Plugin("org.jetbrains.kotlin.kapt")
  @JvmStatic val kotlinAndroid: Plugin = Plugin("org.jetbrains.kotlin.android")
  @JvmStatic val kotlinNoVersion: Plugin = Plugin("org.jetbrains.kotlin.jvm", null, true)
  @JvmStatic val kotlinNoApply: Plugin = Plugin("org.jetbrains.kotlin.jvm", KOTLIN_VERSION, false)
  @JvmStatic val springBoot: Plugin = Plugin("org.springframework.boot", "2.7.14")
}
