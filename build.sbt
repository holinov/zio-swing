import Versions._

resolvers ++= Seq(
  Resolver.mavenLocal,
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots"),
  Resolver.jcenterRepo
)

def module(moduleName: String) = Seq(
// Refine scalac params from tpolecat
  addCompilerPlugin(scalafixSemanticdb),
  scalacOptions ++= Seq("-Ywarn-unused", "-Yrangepos"),
  scalacOptions in console -= "-Xfatal-warnings",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayVcsUrl := Some("git@github.com:holinov/zio-swing.git"),
  organization := "FruTTecH",
  name := moduleName,
  version := ProjectVersion,
  scalaVersion := "2.13.1",
  maxErrors := 3,
  testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
)

lazy val zioDeps = libraryDependencies ++= Seq(
  "dev.zio" %% "zio"          % ZioVersion,
  "dev.zio" %% "zio-streams"  % ZioVersion,
  "dev.zio" %% "zio-test"     % ZioVersion % "test",
  "dev.zio" %% "zio-test-sbt" % ZioVersion % "test"
)

lazy val core = (project in file("core"))
  .settings(
    module("zio-swing-core"),
    zioDeps
  )

lazy val controls = (project in file("controls"))
  .settings(
    module("zio-swing-controls")
  )
  .dependsOn(core)

lazy val root = (project in file("."))
  .aggregate(core, controls)

// Aliases
addCommandAlias("rel", "reload")
addCommandAlias("com", "all compile test:compile it:compile")
addCommandAlias("fix", "all compile:scalafix test:scalafix")
addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")
