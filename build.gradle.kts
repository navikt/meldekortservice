import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

val flywayVersion = "8.4.0"
val h2Version = "2.1.214"
val jacksonVersion = "2.13.3"
val javaxActivationVersion = "1.1.1"
val javaxAnnotationApiVersion = "1.3.2"
val javaxJaxwsApiVersion = "2.3.1"
val jaxbApiVersion = "2.4.0-b180830.0359"
val jaxbRuntimeVersion = "3.0.2"
val jaxwsApiVersion = "2.3.1"
val jaxwsToolsVersion = "3.0.2"
val junitVersion = "5.8.2"
val kluentVersion = "1.68"
val kotestVersion = "5.3.2"
val kotlinLoggerVersion = "2.1.23"
val ktorVersion = "1.6.8"
val logbackVersion = "1.2.11"
val logstashVersion = "7.2"
val mockOauthVersion = "0.5.1"
val mockitoVersion = "4.6.1"
val mockkVersion = "1.12.4"
val navCommonCacheVersion = "2.2020.03.18_12.19-ac82e907ebc9"
val navCommonVersion = "1.2021.07.07_10.18-72bd65c546f6"
val ojdbc8Version = "19.3.0.0"
val postgresVersion = "42.4.0"
val slf4jVersion = "1.7.36"
val swaggerVersion = "4.11.1"
val tjenestespecVersion = "1.2019.09.25-00.21-49b69f0625e0"
val tokenValidationVersion = "2.1.2"
val vaultJdbcVersion = "1.3.9"
val vaultVersion = "5.1.0"
val cxfVersion = "3.5.3"


project.setProperty("mainClassName", "io.ktor.server.netty.EngineMain")

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
}

plugins {

    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"

    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.10"

    id("com.github.johnrengelman.shadow") version "7.1.2"

    id("org.flywaydb.flyway") version ("8.4.0")

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

    /*
    implementation("org.apache.cxf:cxf-core:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-bindings-soap:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-simple:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    */

    implementation(kotlin("stdlib"))
    implementation("no.nav:vault-jdbc:$vaultJdbcVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")

    implementation("no.nav.common:cache:$navCommonCacheVersion")
    implementation("no.nav.common:cxf:$navCommonVersion")
    implementation("no.nav.common:log:$navCommonVersion")
    implementation("no.nav.common:types:$navCommonVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggerVersion")
    implementation("com.bettercloud:vault-java-driver:$vaultVersion")
    implementation("no.nav.tjenestespesifikasjoner:arena-sakOgAktivitet_v1:$tjenestespecVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("no.nav.security:token-validation-ktor:$tokenValidationVersion")
    implementation("no.nav.security:token-client-spring:$tokenValidationVersion")
    implementation("com.oracle.ojdbc:ojdbc8:$ojdbc8Version")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")

    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauthVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

    implementation("org.webjars:swagger-ui:$swaggerVersion")
    implementation("javax.xml.ws:jaxws-api:$jaxwsApiVersion")
    implementation("javax.annotation:javax.annotation-api:$javaxAnnotationApiVersion")
    implementation("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")
    implementation("javax.activation:activation:$javaxActivationVersion")
    /*
    implementation("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }
    */
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
