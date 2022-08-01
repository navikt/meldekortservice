import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

val flywayVersion = "9.0.1"
val h2Version = "2.1.214"
val jacksonVersion = "2.13.3"
val javaxActivationVersion = "1.1.1"
val javaxAnnotationApiVersion = "1.3.2"
val jaxbApiVersion = "2.4.0-b180830.0359"
val jaxbRuntimeVersion = "4.0.0"
val jaxwsApiVersion = "2.3.1"
val jaxwsToolsVersion = "2.3.5" // Senere versjoner har ikke javax.jws.WebService
val kotlinLoggerVersion = "2.1.23"
val ktorVersion = "2.0.3"
val logbackVersion = "1.2.11"
val logstashVersion = "7.2"
val micrometerVersion = "1.9.2"
val mockOauthVersion = "0.5.1"
val mockkVersion = "1.12.4"
val navCommonCacheVersion = "2.2020.03.18_12.19-ac82e907ebc9"
val navCommonVersion = "2.2022.07.01_07.12-6a0864fa6938"
val ojdbc8Version = "19.3.0.0"
val postgresVersion = "42.4.0"
val swaggerVersion = "4.11.1" // Husk å endre versjonen også i SwaggerUi.kt
val tjenestespecVersion = "2589.e85bf84"
val tokenValidationVersion = "2.1.2"
val vaultJdbcVersion = "1.3.9"
val vaultVersion = "5.1.0"
val cxfVersion = "3.5.3"


project.setProperty("mainClassName", "io.ktor.server.netty.EngineMain")

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
    maven("https://jitpack.io")
}

plugins {

    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"

    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.10"

    id("com.github.johnrengelman.shadow") version "7.1.2"

    id("org.flywaydb.flyway") version ("9.0.1")

    id("org.sonarqube") version "3.4.0.2513"

    id("com.github.ben-manes.versions") version "0.42.0"

    jacoco

    application
}

jacoco {
    toolVersion = "0.8.7"
}

application {
    mainClass.set(project.property("mainClassName").toString())
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
    implementation("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggerVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-default-headers:$ktorVersion")
    implementation("io.ktor:ktor-server-locations:$ktorVersion")
    implementation("io.ktor:ktor-server-metrics-micrometer:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerVersion")
    implementation("javax.activation:activation:$javaxActivationVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationApiVersion")
    implementation("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    implementation("javax.xml.ws:jaxws-api:$jaxwsApiVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("no.nav.common:cache:$navCommonCacheVersion")
    implementation("no.nav.common:cxf:$navCommonVersion")
    implementation("no.nav.security:token-validation-ktor-v2:$tokenValidationVersion")
    implementation("com.github.navikt.tjenestespesifikasjoner:arena-sakogaktivitet_v1:$tjenestespecVersion")
    implementation("org.apache.cxf:cxf-core:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-bindings-soap:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-simple:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("org.webjars:swagger-ui:$swaggerVersion")
    implementation("no.nav:vault-jdbc:$vaultJdbcVersion")

    testImplementation(kotlin("test-junit5"))
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauthVersion")

}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    withType<Jar> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE

        manifest.attributes["Main-Class"] = project.property("mainClassName").toString()
        from(configurations.compileClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    withType<ShadowJar> {
        isZip64 = true

        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
        }
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
        mainClass.set(project.property("mainClassName").toString())
        classpath = sourceSets["main"].runtimeClasspath
    }
}
