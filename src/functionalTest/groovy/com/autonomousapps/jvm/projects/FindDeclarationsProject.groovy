// Copyright (c) 2024. Tony Robalik.
// SPDX-License-Identifier: Apache-2.0
package com.autonomousapps.jvm.projects

import com.autonomousapps.AbstractProject
import com.autonomousapps.kit.GradleProject
import com.autonomousapps.kit.gradle.Plugin

import java.nio.file.Files

import static com.autonomousapps.kit.gradle.dependencies.Dependencies.okHttp
import static com.autonomousapps.kit.gradle.dependencies.Dependencies.okio

final class FindDeclarationsProject extends AbstractProject {

  final name = 'proj'
  final GradleProject gradleProject

  FindDeclarationsProject() {
    this.gradleProject = build()
  }

  private GradleProject build() {
    def builder = newGradleProjectBuilder()
    builder.withSubproject(name) { s ->
      s.withBuildScript { bs ->
        bs.plugins = [Plugin.javaLibrary]
        bs.dependencies = [
          okHttp('implementation'),
          okio('implementation'),
        ]
      }
    }

    def project = builder.build()
    project.writer().write()
    return project
  }

  void mutateBuildScript() {
    def f = gradleProject.rootDir.toPath().resolve("$name/build.gradle")
    assert Files.exists(f)

    def lines = f.readLines()
    lines.removeIf {
      it.contains('okio')
    }

    f.write(lines.join('\n'))
  }
}
