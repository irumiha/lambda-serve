ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.3"

lazy val core = (project in file("modules/core"))
  .settings(
    name := "lambdaserve-core"
  )

lazy val formMapping = (project in file("modules/form-mapping"))
  .settings(
    name := "lambdaserve-form-mapping",
    libraryDependencies ++= Seq(
      "com.softwaremill.magnolia1_3" %% "magnolia" % "1.3.4"
    )
  )

lazy val jsonJsoniter = (project in file("modules/json-jsoniter"))
  .settings(
    name := "lambdaserve-json-jsoniter",
    libraryDependencies ++= Seq(
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "2.28.4",
      "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "2.28.4" 
    )
  )
  .dependsOn(core)

lazy val serverJetty = (project in file("modules/server-jetty"))
  .settings(
    name := "lambdaserve-server-jetty",
    libraryDependencies ++= Seq(
      "org.eclipse.jetty" % "jetty-server" % "12.0.8",
    )
  )
  .dependsOn(core)

lazy val example = (project in file("modules/example"))
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "lambdaserve-example",
    libraryDependencies ++= Seq(
      "com.outr" %% "scribe-slf4j2" % "3.13.2",
    )
  )
  .dependsOn(core, serverJetty, jsonJsoniter)

lazy val `lambda-serve` = (project in file("."))
  .aggregate(core,serverJetty,jsonJsoniter,formMapping)
