ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.6"
ThisBuild / organization := "net.lambdaserve"

// Coverage settings
ThisBuild / coverageMinimumStmtTotal := 60
ThisBuild / coverageMinimumBranchTotal := 50
ThisBuild / coverageFailOnMinimum := false
ThisBuild / coverageHighlighting := true

val commonDeps =
  Seq(
    "org.slf4j"      % "slf4j-api" % "2.0.17",
    "org.scalameta" %% "munit"     % "1.2.1" % Test
  )

lazy val core = (project in file("modules/core"))
  .settings(
    name := "lambdaserve-core",
    libraryDependencies ++= commonDeps ++ Seq(
      "com.lihaoyi" %% "pprint" % "0.9.4"
    )
  )

lazy val mapextract = (project in file("modules/mapextract"))
  .settings(
    name := "lambdaserve-mapextract",
    libraryDependencies ++= commonDeps ++ Seq(
      "com.softwaremill.magnolia1_3" %% "magnolia" % "1.3.18"
    )
  ).dependsOn(core)

lazy val jsonJsoniter = (project in file("modules/json-jsoniter"))
  .settings(
    name := "lambdaserve-json-jsoniter",
    libraryDependencies ++= commonDeps ++ Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.38.3",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.38.3" % "provided"
      )
  )
  .dependsOn(core)

lazy val viewsScalatags = (project in file("modules/views-scalatags"))
  .settings(
    name := "lambdaserve-views-scalatags",
    libraryDependencies ++= commonDeps ++ Seq(
      "com.lihaoyi" %% "scalatags" % "0.13.1"
    )
  )
  .dependsOn(core)

lazy val viewsTyrian = (project in file("modules/views-tyrian-tags"))
  .settings(
    name := "lambdaserve-views-tyrian-tags",
    libraryDependencies ++= commonDeps ++ Seq(
      "io.indigoengine" %% "tyrian" % "0.10.0"
    )
  )
  .dependsOn(core)

lazy val viewsJte = (project in file("modules/views-jte"))
  .settings(
    name := "lambdaserve-views-jte",
    libraryDependencies ++= commonDeps ++ Seq("gg.jte" % "jte" % "3.2.1")
  )
  .dependsOn(core)

lazy val requestmapped = (project in file("modules/requestmapped"))
  .settings(
    name := "lambdaserve-requestmapped",
    libraryDependencies ++= commonDeps
  )
  .dependsOn(core, mapextract)

lazy val filters = (project in file("modules/filters"))
  .settings(name := "lambdaserve-filters", libraryDependencies ++= commonDeps)
  .dependsOn(core)

lazy val jwt = (project in file("modules/jwt"))
  .settings(
    name := "lambdaserve-jwt",
    libraryDependencies ++= commonDeps ++ Seq(
      "org.bitbucket.b_c" % "jose4j" % "0.9.6"
    )
  )
  .dependsOn(core, filters)

lazy val serverJetty = (project in file("modules/server-jetty"))
  .settings(
    name := "lambdaserve-server-jetty",
    libraryDependencies ++= commonDeps ++ Seq(
      "org.eclipse.jetty" % "jetty-server" % "12.1.3",
      "org.eclipse.jetty.compression" % "jetty-compression-server" % "12.1.3",
      "org.eclipse.jetty.compression" % "jetty-compression-gzip" % "12.1.3",
    )
  )
  .dependsOn(core)

lazy val serverJdk = (project in file("modules/server-jdk"))
  .settings(
    name := "lambdaserve-server-jdk",
    libraryDependencies ++= commonDeps
  )
  .dependsOn(core)

lazy val all = (project in file("modules/all"))
  .settings(name := "lambdaserve-all")
  .dependsOn(
    core,
    serverJetty,
    jwt,
    jsonJsoniter,
    mapextract,
    requestmapped,
    viewsTyrian,
    viewsScalatags,
    viewsJte,
    filters
  )

lazy val `lambda-serve` = (project in file("."))
  .settings(publish / skip := true, publishLocal / skip := true)
  .aggregate(
    core,
    jwt,
    serverJetty,
    jsonJsoniter,
    mapextract,
    requestmapped,
    viewsScalatags,
    viewsTyrian,
    viewsJte,
    filters,
    all
  )
