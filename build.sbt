ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.3"

lazy val core = (project in file("modules/core"))
  .settings(
    name := "lambdaserve-core"
  )

lazy val mapextract = (project in file("modules/mapextract"))
  .settings(
    name := "lambdaserve-mapextract",
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

lazy val requestmapped = (project in file("modules/requestmapped"))
  .settings(
    name := "lambdaserve-requestmapped",
  )
  .dependsOn(core, mapextract)

lazy val requestmappedJson = (project in file("modules/requestmapped-json"))
  .settings(
    name := "lambdaserve-requestmapped-json",
  )
  .dependsOn(core, mapextract, jsonJsoniter)

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
  .dependsOn(core, serverJetty, jsonJsoniter, mapextract, requestmappedJson)

lazy val `lambda-serve` = (project in file("."))
  .aggregate(core,serverJetty,jsonJsoniter,mapextract,requestmapped, requestmappedJson)
