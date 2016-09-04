androidBuild
useSupportVectors

instrumentTestRunner :=
  "android.support.test.runner.AndroidJUnitRunner"

scalaVersion := "2.11.8"

/* useProguard := false
useProguardInDebug := false
dexMulti := true */

platformTarget := "android-24"

javacOptions in Compile ++= "-source" :: "1.7" :: "-target" :: "1.7" :: Nil

packagingOptions := PackagingOptions(excludes = Seq("META-INF/ASL2.0", "META-INF/LICENSE", "META-INF/NOTICE"))

libraryDependencies ++=
  Seq("com.so" %% "typebase-lite-android" % "0.1-SNAPSHOT",
    "com.couchbase.lite" % "couchbase-lite-android-forestdb" % "1.3.0",
    "com.android.support" % "appcompat-v7" % "24.0.0",
    "com.android.support.test" % "runner" % "0.5" % "androidTest",
    "com.android.support.test.espresso" % "espresso-core" % "2.2.2" % "androidTest"
  )

proguardOptions in Android ++= Seq(
  "-keep class com.couchbase.** { *;}",
  "-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry",
  "-dontwarn okio.**",
  "-dontwarn shapeless.**"
)