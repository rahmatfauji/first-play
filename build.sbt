name := """main-program"""
organization := "com.main-program"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

//scalaVersion := "2.13.2"
scalaVersion := "2.12.4"


EclipseKeys.preTasks := Seq(compile in Compile, compile in Test)

// Java project. Don't expect Scala IDE
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

// Use .class files instead of generated .scala files for views and routes
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)

lazy val myProject = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
libraryDependencies += guice
libraryDependencies += javaWs
//libraryDependencies += ehcache
libraryDependencies += javaJdbc
libraryDependencies += jdbc
libraryDependencies += evolutions
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.41"
libraryDependencies += "javax.xml.bind" % "jaxb-api" % "2.1"
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.3m"
libraryDependencies += "com.auth0" % "java-jwt" % "3.3.0"