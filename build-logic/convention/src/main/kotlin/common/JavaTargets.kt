package common

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

/**
 * Single source of truth for JVM/Kotlin compilation targets.
 *
 * All Android modules align on the same Java/Kotlin level so bytecode from
 * the Compose compiler, Kotlin compiler, and AGP stay interoperable.
 */
internal object JavaTargets {

    /** Java source/target compatibility for `compileOptions { ... }` blocks. */
    val JAVA_VERSION: JavaVersion = JavaVersion.VERSION_18

    /** `jvmTarget` for the Kotlin compiler in JVM-bound compilations. */
    val KOTLIN_JVM_TARGET: JvmTarget = JvmTarget.JVM_18
}
