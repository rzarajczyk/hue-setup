plugins {
    kotlin("jvm") version "1.9.0"
    application
}

group = "pl.zarajczyk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.14.2")
    implementation("org.apache.commons:commons-text:1.10.0")
    implementation("org.hibernate.validator:hibernate-validator:8.0.0.Final")
    implementation("org.jetbrains.kotlinx:kotlinx-cli-jvm:0.3.6")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testImplementation("io.kotest:kotest-assertions-core:5.7.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
    compilerOptions.freeCompilerArgs.add("-Xemit-jvm-type-annotations")
}

application {
    mainClass.set("pl.zarajczyk.huesetup.MainKt")
}