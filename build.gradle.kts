val ktorVersion = "2.2.2"
val logbackVersion = "1.4.5"
val logstashEncoderVersion = "7.2"
val junitJupiterVersion = "5.9.2"
val bigQueryClientVersion = "2.20.1"

val mainClassName = "no.nav.security.MainKt"

plugins {
   kotlin("jvm") version "1.8.0"
   kotlin("plugin.serialization") version "1.8.0"
   id("com.github.johnrengelman.shadow") version "7.1.2"
}

repositories {
   mavenCentral()
}

java {
   sourceCompatibility = JavaVersion.VERSION_17
   targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
   implementation(kotlin("stdlib"))
   implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
   implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
   implementation("ch.qos.logback:logback-classic:$logbackVersion")
   implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")
   implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")

   implementation("com.google.cloud:google-cloud-bigquery:$bigQueryClientVersion")

   testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
   testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
   testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
}

tasks {
   withType<Jar> {
      archiveBaseName.set("app")

      manifest {
         attributes["Main-Class"] = mainClassName
         attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(separator = " ") {
            it.name
         }
      }

      doLast {
         configurations.runtimeClasspath.get().forEach {
            val file = File("$buildDir/libs/${it.name}")
            if (!file.exists())
               it.copyTo(file)
         }
      }
   }

   withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
      kotlinOptions {
         jvmTarget = "17"
         freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
      }
   }

   withType<Test> {
      useJUnitPlatform()
      testLogging {
         showExceptions = true
      }
   }

   withType<Wrapper> {
      gradleVersion = "7.6"
   }

}

