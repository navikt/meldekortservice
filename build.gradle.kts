import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

val flywayVersion = "11.15.0"
val h2Version = "2.4.240"
val jacksonVersion = "2.20.1"
val kotlinLoggerVersion = "3.0.5"
val ktorVersion = "3.3.1"
val logbackVersion = "1.5.20"
val logstashVersion = "9.0"
val micrometerVersion = "1.16.0"
val mockOauthVersion = "3.0.0"
val mockkVersion = "1.14.6"
val navCommonVersion = "3.2025.10.10_08.21-bb7c7830d93c"
val ojdbc8Version = "19.3.0.0"
val postgresVersion = "42.7.8"
val swaggerVersion = "5.30.1" // Husk å endre versjonen også i resource i SwaggerUi.kt
val tjenestespecVersion = "2643.2f3e8e9"
val tokenValidationVersion = "5.0.39"
val vaultJdbcVersion = "1.3.10"
val vaultVersion = "5.1.0"

project.ext.set("mainClassName", "io.ktor.server.netty.EngineMain")

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven("https://jitpack.io")
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven("https://build.shibboleth.net/maven/releases/")
}

plugins {

    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"

    kotlin("jvm") version "2.2.21"
    kotlin("plugin.allopen") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"

    id("com.gradleup.shadow") version "9.2.2"

    id("org.flywaydb.flyway") version ("11.11.0")

    id("org.sonarqube") version "7.0.1.6134"

    id("com.github.ben-manes.versions") version "0.53.0"

    jacoco

    application
}

jacoco {
    toolVersion = "0.8.11"
}

application {
    mainClass.set(project.ext.get("mainClassName").toString())
}

dependencies {

    implementation(kotlin("stdlib"))
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("com.bettercloud:vault-java-driver:$vaultVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    implementation("com.oracle.ojdbc:ojdbc8:$ojdbc8Version")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggerVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-double-receive:$ktorVersion")
    implementation("io.ktor:ktor-server-resources:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("no.nav.common:util:$navCommonVersion")
    implementation("no.nav.security:token-validation-ktor-v3:$tokenValidationVersion")
    implementation("com.github.navikt.tjenestespesifikasjoner:arena-sakogaktivitet_v1:$tjenestespecVersion")
    implementation("org.flywaydb:flyway-database-oracle:$flywayVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("org.webjars:swagger-ui:$swaggerVersion")
    implementation("no.nav:vault-jdbc:$vaultJdbcVersion")

    // Lokal kjøring
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    testImplementation(kotlin("test-junit5"))
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauthVersion")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks {
    withType<KotlinJvmCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest.attributes["Main-Class"] = project.ext.get("mainClassName").toString()
        from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    withType<ShadowJar> {
        isZip64 = true
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        mergeServiceFiles()
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }

    jacocoTestReport {
        reports {
            xml.required.set(true)
        }
    }

    named("sonarqube") {
        dependsOn("jacocoTestReport")
    }

    register("runServer", JavaExec::class) {
        mainClass.set(project.ext.get("mainClassName").toString())
        classpath = sourceSets["main"].runtimeClasspath
    }

    register("runServerTest", JavaExec::class) {
        systemProperties["TOKEN_X_WELL_KNOWN_URL"] = "tokenx.dev.nav.no"
        systemProperties["TOKEN_X_CLIENT_ID"] = "test:meldekort:meldekortservice"
        systemProperties["AZURE_APP_WELL_KNOWN_URL"] = "azure.dev.nav.no"
        systemProperties["AZURE_APP_CLIENT_ID"] = "test:meldekort:meldekortservice"

        mainClass.set(project.ext.get("mainClassName").toString())
        classpath = sourceSets["main"].runtimeClasspath
    }
}
