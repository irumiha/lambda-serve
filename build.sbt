ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.3"
ThisBuild / organization := "net.lambdaserve"

val commonDeps = Seq("org.scalameta" %% "munit" % "1.0.0" % Test)

lazy val core = (project in file("modules/core"))
  .settings(name := "lambdaserve-core", libraryDependencies ++= commonDeps)

lazy val mapextract = (project in file("modules/mapextract"))
  .settings(
    name := "lambdaserve-mapextract",
    libraryDependencies ++= commonDeps ++ Seq(
      "com.softwaremill.magnolia1_3" %% "magnolia" % "1.3.7"
    )
  )

lazy val jsonJsoniter = (project in file("modules/json-jsoniter"))
  .settings(
    name := "lambdaserve-json-jsoniter",
    libraryDependencies ++= commonDeps ++ Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.30.6",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.30.7"
      )
  )
  .dependsOn(core)

lazy val viewsScalatags = (project in file("modules/views-scalatags"))
  .settings(
    name := "lambdaserve-views-scalatags",
    libraryDependencies ++= commonDeps ++ Seq(
      "com.lihaoyi" %% "scalatags" % "0.13.1"
    )
  ).dependsOn(core)

lazy val viewsTyrian = (project in file("modules/views-tyrian-tags"))
  .settings(
    name := "lambdaserve-views-tyrian-tags",
    libraryDependencies ++= commonDeps ++ Seq(
      "io.indigoengine" %% "tyrian" % "0.10.0"
    )
  ).dependsOn(core)

lazy val requestmapped = (project in file("modules/requestmapped"))
  .settings(
    name := "lambdaserve-requestmapped",
    libraryDependencies ++= commonDeps
  )
  .dependsOn(core, mapextract)

lazy val serverJetty = (project in file("modules/server-jetty"))
  .settings(
    name := "lambdaserve-server-jetty",
    libraryDependencies ++= commonDeps ++ Seq(
      "org.eclipse.jetty" % "jetty-server" % "12.0.11"
    )
  )
  .dependsOn(core)

lazy val all = (project in file("modules/all"))
  .settings(
    name := "lambdaserve-all"
  )
  .dependsOn(
    core,
    serverJetty,
    jsonJsoniter,
    mapextract,
    requestmapped,
    viewsTyrian
  )

lazy val example = (project in file("modules/example"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "lambdaserve-example",
    libraryDependencies ++= Seq("com.outr" %% "scribe-slf4j2" % "3.15.0")
  )
  .dependsOn(all)

lazy val `lambda-serve` = (project in file("."))
  .settings(
    publish / skip := true,
    publishLocal / skip := true
  )
  .aggregate(
    core,
    serverJetty,
    jsonJsoniter,
    mapextract,
    requestmapped,
    viewsScalatags,
    viewsTyrian,
    all
  )
