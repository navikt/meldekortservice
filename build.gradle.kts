import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import no.nils.wsdl2java.Wsdl2JavaTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val flywayVersion = "6.5.7"
val h2Version = "1.4.200"
val jacksonVersion = "2.12.4"
val javaxActivationVersion = "1.1.1"
val javaxAnnotationApiVersion = "1.3.2"
val javaxJaxwsApiVersion = "2.3.1"
val jaxbApiVersion = "2.4.0-b180830.0359"
val jaxbRuntimeVersion = "2.4.0-b180830.0438"
val jaxwsApiVersion = "2.3.1"
val jaxwsToolsVersion = "2.3.5"
val junitVersion = "5.7.2"
val kluentVersion = "1.68"
val kotestVersion = "4.6.2"
val kotlinLoggerVersion = "1.12.5"
val ktorVersion = "1.6.2"
val logbackVersion = "1.2.5"
val logstashVersion = "5.3"
val mockOauthVersion = "0.3.4"
val mockkVersion = "1.12.0"
val navCommonVersion = "1.2021.07.07_10.18-72bd65c546f6"
val ojdbc8Version = "19.3.0.0"
val postgresVersion = "42.2.23"
val prometheusVersion = "0.11.0"
val slf4jVersion = "1.7.32"
val swaggerVersion = "3.23.8"
val tjenestespecVersion = "1.2019.09.25-00.21-49b69f0625e0"
val tokenValidationVersion = "1.1.5"
val vaultJdbcVersion = "1.3.1"
val vaultVersion = "3.1.0"

project.setProperty("mainClassName", "io.ktor.server.netty.EngineMain")

plugins {

    id("com.github.ManifestClasspath") version "0.1.0-RELEASE"

    id("no.nils.wsdl2java") version "0.10"

    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.5.21"

    id("com.github.johnrengelman.shadow") version "6.1.0"

    id("org.flywaydb.flyway") version ("6.5.7")

    id("org.sonarqube") version "2.8"

    jacoco

    application
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:6.1.0")
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
        classpath("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
        classpath("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
        classpath("com.sun.activation:javax.activation:1.2.0")
        classpath("com.sun.xml.ws:jaxws-tools:2.3.5") {
            exclude(group = "com.sun.xml.ws", module = "policy")
        }
    }
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://plugins.gradle.org/m2/")
    maven("https://jitpack.io")
}


dependencies {
    wsdl2java("javax.annotation:javax.annotation-api:$javaxAnnotationApiVersion")
    wsdl2java("javax.activation:activation:$javaxActivationVersion")
    wsdl2java("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")
    wsdl2java("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    wsdl2java("javax.xml.ws:jaxws-api:$javaxJaxwsApiVersion")
    wsdl2java("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }

    implementation(kotlin("stdlib"))
    implementation("no.nav:vault-jdbc:$vaultJdbcVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("ch.qos.logback:logback-core:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-json:$ktorVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("io.ktor:ktor-jackson:$ktorVersion")
    implementation("no.nav.common:cxf:$navCommonVersion")
    implementation("no.nav.common:cache:$navCommonVersion")
    implementation("no.nav.common:log:$navCommonVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("no.nav.common:types:$navCommonVersion")
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
    testImplementation("org.mockito:mockito-core:2.+")
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
    implementation("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass.set(project.property("mainClassName").toString())
}

jacoco {
    toolVersion = "0.8.7"
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = project.property("mainClassName").toString()
        from(configurations.runtime.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }

    withType<KotlinCompile> {
        dependsOn("wsdl2java")
        kotlinOptions.jvmTarget = "11"
    }

    withType<Wsdl2JavaTask> {
        wsdlDir = file("$projectDir/src/main/resources/wsdl")
        wsdlsToGenerate = listOf(
            mutableListOf(
                "-xjc",
                "-b",
                "$projectDir/src/main/resources/xjb/bindings.xml",
                "$projectDir/src/main/resources/wsdl/amelding_EksternKontrolEmeldingService.wsdl"
            )
        )
    }

    withType<ShadowJar> {
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
        }
    }

    register("runServer", JavaExec::class) {
        main = project.property("mainClassName").toString()
        classpath = sourceSets["main"].runtimeClasspath
    }

    sonarqube {
        properties {
            property("sonar.projectKey", System.getenv("SONAR_PROJECT_KEY_MELDEKORTSERVICE"))
            property("sonar.organization", "navit")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.login", System.getenv("SONAR_TOKEN_MELDEKORTSERVICE"))
            property("sonar.java.coveragePlugin", "jacoco")
        }
    }

    jacocoTestReport {
        reports {
            xml.isEnabled = true
        }
    }
}

tasks.named("sonarqube") {
    dependsOn("jacocoTestReport")
}