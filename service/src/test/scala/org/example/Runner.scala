package org.example

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

import java.nio.file.{Path, Paths}


object Runner extends App {
  val props = new GatlingPropertiesBuilder()
    .resourcesDirectory(IDEPathHelper.mavenResourcesDirectory.toString)
    .resultsDirectory(IDEPathHelper.resultsDirectory.toString)
    .binariesDirectory(IDEPathHelper.mavenBinariesDirectory.toString)

  Gatling.fromMap(props.build)
}

// https://github.com/gatling/gatling-maven-plugin-demo/blob/master/src/test/scala/IDEPathHelper.scala
object IDEPathHelper {
  private val projectRootDir = Paths.get(getClass.getClassLoader.getResource("gatling.conf").toURI).getParent.getParent.getParent
  private val mavenTargetDirectory = projectRootDir.resolve("target")
  private val mavenSrcTestDirectory = projectRootDir.resolve("src").resolve("test")

  val mavenSourcesDirectory: Path = mavenSrcTestDirectory.resolve("scala")
  val mavenResourcesDirectory: Path = mavenSrcTestDirectory.resolve("resources")
  val mavenBinariesDirectory: Path = mavenTargetDirectory.resolve("test-classes")
  val resultsDirectory: Path = mavenTargetDirectory.resolve("gatling")
  val recorderConfigFile: Path = mavenResourcesDirectory.resolve("recorder.conf")
}