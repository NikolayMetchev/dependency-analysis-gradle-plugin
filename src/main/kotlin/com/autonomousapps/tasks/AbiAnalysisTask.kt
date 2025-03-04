// Copyright (c) 2024. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
@file:Suppress("UnstableApiUsage")

package com.autonomousapps.tasks

import com.autonomousapps.TASK_GROUP_DEP
import com.autonomousapps.internal.AbiExclusions
import com.autonomousapps.internal.kotlin.computeAbi
import com.autonomousapps.internal.utils.*
import com.autonomousapps.internal.utils.filterToClassFiles
import com.autonomousapps.internal.utils.getAndDelete
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@CacheableTask
abstract class AbiAnalysisTask @Inject constructor(
  private val workerExecutor: WorkerExecutor
) : DefaultTask() {

  init {
    group = TASK_GROUP_DEP
    description = "Produces a report of the ABI of this project"
  }

  /** Mutually exclusive with [javaClasses] and [kotlinClasses]. */
  @get:Optional
  @get:Classpath
  abstract val jar: RegularFileProperty

  /** Class files generated by any JVM source (Java, Kotlin, Groovy, etc.). May be empty. */
  @get:Classpath
  @get:InputFiles
  abstract val classes: ConfigurableFileCollection

  /** Class files generated by Java source. Mutually exclusive with [jar]. */
  @get:Optional
  @get:Classpath
  @get:InputFiles
  abstract val javaClasses: ConfigurableFileCollection

  /** Class files generated by Kotlin source. Mutually exclusive with [jar]. */
  @get:Optional
  @get:Classpath
  @get:InputFiles
  abstract val kotlinClasses: ConfigurableFileCollection

  @get:Optional
  @get:Input
  abstract val exclusions: Property<String>

  @get:OutputFile
  abstract val output: RegularFileProperty

  @get:OutputFile
  abstract val abiDump: RegularFileProperty

  @TaskAction
  fun action() {
    workerExecutor.noIsolation().submit(AbiAnalysisWorkAction::class.java) {
      jar.set(this@AbiAnalysisTask.jar)
      classFiles.setFrom(
        classes.asFileTree
          .plus(javaClasses.asFileTree)
          .plus(kotlinClasses.asFileTree)
          .filterToClassFiles()
          .files
      )
      exclusions.set(this@AbiAnalysisTask.exclusions)
      output.set(this@AbiAnalysisTask.output)
      abiDump.set(this@AbiAnalysisTask.abiDump)
    }
  }

  interface AbiAnalysisParameters : WorkParameters {
    val jar: RegularFileProperty
    val classFiles: ConfigurableFileCollection
    val exclusions: Property<String>
    val output: RegularFileProperty
    val abiDump: RegularFileProperty
  }

  abstract class AbiAnalysisWorkAction : WorkAction<AbiAnalysisParameters> {

    override fun execute() {
      val output = parameters.output.getAndDelete()
      val outputAbiDump = parameters.abiDump.getAndDelete()

      val jarFile = parameters.jar.orNull?.asFile
      val classFiles = parameters.classFiles.files
      val exclusions = parameters.exclusions.orNull?.fromJson<AbiExclusions>() ?: AbiExclusions.NONE

      val explodingAbi = if (jarFile != null) {
        computeAbi(jarFile, exclusions, outputAbiDump)
      } else {
        computeAbi(classFiles, exclusions, outputAbiDump)
      }

      output.bufferWriteJsonSet(explodingAbi)
    }
  }
}
