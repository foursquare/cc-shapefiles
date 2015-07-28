import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype._
import SonatypeKeys._
import com.typesafe.sbt.SbtGit.{GitKeys => git}
import com.typesafe.sbt.SbtGit._
import com.typesafe.sbt.SbtSite.site
import sbtunidoc.Plugin.UnidocKeys._
import sbtunidoc.Plugin._
import net.virtualvoid.sbt.graph.Plugin.graphSettings

object QuadtreeBuild extends Build {
  lazy val buildSettings = Seq(
    organization := "com.foursquare",
    name := "quadtree",
    version      := "0.1a",
    scalaVersion := "2.11.4",
    crossScalaVersions := Seq("2.11.4", "2.10.2"),
    javacOptions ++= Seq("-source", "1.6", "-target", "1.6"),
    javacOptions in doc := Seq("-source", "1.6"),

    publishMavenStyle := true,

    publishTo <<= (version) { v =>
      val nexus = "https://oss.sonatype.org/"
      if (v.endsWith("-SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },

    pomIncludeRepository := { _ => false },

    // firt try to load from .ivy2 which includes Realm
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),

    // if not, load from .m2
    credentials ++= {
      val sonatype = ("Sonatype Nexus Repository Manager", "oss.sonatype.org")
      def loadMavenCredentials(file: java.io.File) : Seq[Credentials] = {
        xml.XML.loadFile(file) \ "servers" \ "server" map (s => {
          val host = (s \ "id").text
          val realm = if (host == sonatype._2) sonatype._1 else "Unknown"
          Credentials(realm, host, (s \ "username").text, (s \ "password").text)
        })
      }
      val ivyCredentials   = Path.userHome / ".ivy2" / ".credentials"
      val mavenCredentials = Path.userHome / ".m2"   / "settings.xml"
      (ivyCredentials.asFile, mavenCredentials.asFile) match {
        case (ivy, _) if ivy.canRead => Credentials(ivy) :: Nil
        case (_, mvn) if mvn.canRead => loadMavenCredentials(mvn)
        case _ => Nil
      }
    },

    pomExtra := (
      <url>http://github.com/foursquare/cc-shapefiles</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://www.opensource.org/licenses/bsd-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:foursquare/cc-shapefiles.git</url>
        <connection>scm:git:git@github.com:foursquare/cc-shapefiles.git</connection>
      </scm>
      <developers>
        <developer>
          <id>slackhappy</id>
          <name>John Gallagher</name>
          <url>https://github.com/slackhappy</url>
        </developer>
        <developer>
          <id>blackmad</id>
          <name>David Blackman</name>
          <url>http://blackmad.com</url>
        </developer>
      </developers>)

  )

  lazy val scoptSettings = Seq(
    libraryDependencies += "com.github.scopt" %% "scopt" % "3.1.0"
  )

  lazy val specsSettings = Seq(
      libraryDependencies <<= (scalaVersion, libraryDependencies) {(version, dependencies) =>
        val specs2 =
          if (version.startsWith("2.10"))
          "org.specs2" %% "specs2" % "1.14" % "test"
          else if (version == "2.9.3")
            "org.specs2" %% "specs2" % "1.12.4.1" % "test"
          else if (version.startsWith("2.11"))
            "org.specs2" %% "specs2-core" % "2.4.17" % "test"
          else
            "org.specs2" %% "specs2" % "1.12.3" % "test"
        dependencies :+ specs2
      }
  )

  com.typesafe.sbt.SbtSite.site.includeScaladoc()

  lazy val defaultSettings = super.settings ++ buildSettings ++ Defaults.defaultSettings ++ sonatypeSettings ++ graphSettings ++
   com.typesafe.sbt.SbtGhPages.ghpages.settings ++
   unidocSettings ++
    com.typesafe.sbt.SbtSite.site.settings ++
      Seq(
    site.addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), "latest/api"),
    git.gitRemoteRepo := "git@github.com:foursquare/cc-shapefiles.git",
    resolvers += "geomajas" at "http://maven.geomajas.org",
    resolvers += "osgeo" at "http://download.osgeo.org/webdav/geotools/",
    resolvers += "twitter" at "http://maven.twttr.com",
    resolvers += "repo.novus rels" at "http://repo.novus.com/releases/",
    resolvers += "repo.novus snaps" at "http://repo.novus.com/snapshots/",
    resolvers += "Java.net Maven 2 Repo" at "http://download.java.net/maven/2",
    resolvers += "apache" at "http://repo2.maven.org/maven2/org/apache/hbase/hbase/",
    resolvers += "cloudera" at "https://repository.cloudera.com/artifactory/cloudera-repos/",
    resolvers += "codahale" at "http://repo.codahale.com",
    resolvers += "springsource" at "http://repo.springsource.org/libs-release-remote",

    fork in run := true,
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },

    pomExtra := (
      <url>http://github.com/foursquare/twofishes</url>
      <licenses>
        <license>
          <name>Apache</name>
          <url>http://www.opensource.org/licenses/Apache-2.0</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:foursquare/twofishes.git</url>
        <connection>scm:git:git@github.com:foursquare/twofishes.git</connection>
      </scm>
      <developers>
        <developer>
          <id>blackmad</id>
          <name>David Blackman</name>
          <url>http://github.com/blackmad</url>
        </developer>
      </developers>
    )
  )

  lazy val all = Project(id = "all",
    settings = defaultSettings ++ Seq(
      publishArtifact := false
    ),
    base = file(".")) aggregate(quadtree, country_revgeo, timezone_revgeo, timezone_lookup)

  val geoToolsVersion = "9.2"

  lazy val quadtree = Project(id = "quadtree",
      base = file("quadtree"),
      settings = defaultSettings ++ specsSettings ++ Seq(
        publishArtifact := true,
        libraryDependencies ++= Seq(
          "org.geotools" % "gt-shapefile" % geoToolsVersion,
          "org.geotools" % "gt-epsg-hsql" % geoToolsVersion,
          "org.geotools" % "gt-epsg-extension" % geoToolsVersion,
          "org.geotools" % "gt-referencing" % geoToolsVersion,
          "org.scalaj" %% "scalaj-collection" % "1.6"
        )
      )
    )

   lazy val country_revgeo = Project(id = "country_revgeo",
      base = file("country_revgeo"),
      settings = defaultSettings ++ specsSettings ++ Seq(
        publishArtifact := true
      )
    ) dependsOn(quadtree)


   lazy val timezone_revgeo = Project(id = "timezone_revgeo",
      base = file("timezone_revgeo"),
      settings = defaultSettings ++ specsSettings ++ Seq(
        publishArtifact := true
      )
    ) dependsOn(quadtree)

    lazy val timezone_lookup = Project(id = "timezone_lookup",
      base = file("timezone_lookup"),
      settings = defaultSettings ++ specsSettings ++ Seq(
        publishArtifact := true
      )
    ) dependsOn(quadtree)
}
