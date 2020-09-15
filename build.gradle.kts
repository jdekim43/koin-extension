import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Date

plugins {
    kotlin("jvm") version "1.4.10"
    `maven-publish`
    id("com.jfrog.bintray") version "1.8.4"
}

val artifactName = "koin-extension"
val artifactGroup = "kr.jadekim"
val artifactVersion = "1.0.8"
group = artifactGroup
version = artifactVersion

repositories {
    jcenter()
    mavenCentral()
    maven("https://dl.bintray.com/jdekim43/maven")
}

dependencies {
    val koinVersion: String by project
    val hikaricpVersion: String by project
    val exposedExtensionVersion: String by project
    val lettuceExtensionVersion: String by project

    implementation(kotlin("stdlib-jdk8"))

    api("org.koin:koin-core:$koinVersion")
    api("org.koin:koin-core-ext:$koinVersion")

    compileOnly("com.zaxxer:HikariCP:$hikaricpVersion") {
        exclude("org.slf4j", "slf4j-api")
    }
    compileOnly("kr.jadekim:exposed-extension:$exposedExtensionVersion")
    compileOnly("kr.jadekim:lettuce-extension:$lettuceExtensionVersion")
}

tasks.withType<KotlinCompile> {
    val jvmTarget: String by project

    kotlinOptions.jvmTarget = jvmTarget
}

val sourcesJar by tasks.creating(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.getByName("main").allSource)
}

publishing {
    publications {
        create<MavenPublication>("lib") {
            groupId = artifactGroup
            artifactId = artifactName
            version = artifactVersion
            from(components["java"])
            artifact(sourcesJar)
        }
    }
}

bintray {
    user = System.getenv("BINTRAY_USER")
    key = System.getenv("BINTRAY_KEY")

    publish = true

    setPublications("lib")

    pkg.apply {
        repo = "maven"
        name = rootProject.name
        setLicenses("MIT")
        setLabels("kotlin", "koin")
        vcsUrl = "https://github.com/jdekim43/koin-extension.git"
        version.apply {
            name = artifactVersion
            released = Date().toString()
        }
    }
}