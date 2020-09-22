import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import no.nils.wsdl2java.Wsdl2JavaTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val prometheusVersion = "0.6.0"
val ktorVersion = "1.2.4"
val junitVersion = "5.4.1"
val confluentVersion = "5.2.0"
val logstashVersion = "5.2"
val logbackVersion = "1.2.3"
val vaultJdbcVersion = "1.3.1"
val assertJVersion = "3.12.2"
val jacksonVersion = "2.9.9"
val jaxwsApiVersion = "2.3.1"
val javaxAnnotationApiVersion = "1.3.2"
val jaxbRuntimeVersion = "2.4.0-b180830.0438"
val jaxbApiVersion = "2.4.0-b180830.0359"
val javaxActivationVersion = "1.1.1"
val jaxwsToolsVersion = "2.3.1"
val javaxJaxwsApiVersion = "2.2.1"
val navCommonVersion = "1.2019.05.08-08.52-482a48e1a056"
val kotlinLoggerVersion = "1.5.4"
val ktorSwagger = "0.4.0"
val swaggerVersion = "3.23.8"
val vaultVersion = "3.1.0"
val tjenestespecVersion = "1.2019.08.16-13.46-35cbdfd492d4"
val slf4jVersion = "1.7.26"
val flywayVersion = "5.2.4"
val postgresVersion = "42.2.5"
val h2Version = "1.4.199"
val kluentVersion = "1.52"
val tokenValidationVersion = "1.1.5"

plugins {
    java

    id("no.nils.wsdl2java") version "0.10"

    kotlin("jvm") version "1.3.50"
    kotlin("plugin.allopen") version "1.3.41"

    id("com.github.johnrengelman.shadow") version "4.0.4"

    id("org.flywaydb.flyway") version("5.2.4")

    application
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.github.jengelman.gradle.plugins:shadow:4.0.4")
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
        classpath("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
        classpath("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
        classpath("com.sun.activation:javax.activation:1.2.0")
        classpath("com.sun.xml.ws:jaxws-tools:2.3.1") {
            exclude(group = "com.sun.xml.ws", module = "policy")
        }
    }
}

repositories {
    mavenCentral()
    maven("https://plugins.gradle.org/m2/")
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

    implementation(kotlin("stdlib-jdk8"))
    api("no.nav:vault-jdbc:$vaultJdbcVersion")
    api("ch.qos.logback:logback-classic:$logbackVersion")
    api("ch.qos.logback:logback-core:$logbackVersion")
    api("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    api("io.prometheus:simpleclient_common:$prometheusVersion")
    api("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    api("io.ktor:ktor-server-netty:$ktorVersion")
    api("io.ktor:ktor-auth:$ktorVersion")
    api("io.ktor:ktor-auth-jwt:$ktorVersion")
    api("io.ktor:ktor-client-apache:$ktorVersion")
    api("io.ktor:ktor-client-json:$ktorVersion")
    api("io.ktor:ktor-client-jackson:$ktorVersion")
    api("io.ktor:ktor-locations:$ktorVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    api("com.fasterxml.jackson.module:jackson-module-parameter-names:$jacksonVersion")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    api("io.ktor:ktor-jackson:$ktorVersion")
    api("no.nav.common:cxf:$navCommonVersion")
    api("no.nav.common:cache:$navCommonVersion")
    api("no.nav.common:log:$navCommonVersion")
    api("org.slf4j:slf4j-api:$slf4jVersion")
    api("no.nav.common:types:$navCommonVersion")
    api("io.github.microutils:kotlin-logging:$kotlinLoggerVersion")
    api("com.bettercloud:vault-java-driver:$vaultVersion")
    api("no.nav.tjenestespesifikasjoner:arena-sakOgAktivitet_v1:$tjenestespecVersion")
    api("org.flywaydb:flyway-core:$flywayVersion")
    api("org.postgresql:postgresql:$postgresVersion")
    api("no.nav.security:token-validation-ktor:$tokenValidationVersion")

    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testCompile(kotlin("test-junit5"))
    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testRuntime("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

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
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = application.mainClassName
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
        kotlinOptions.jvmTarget = "1.8"
    }

    withType<Wsdl2JavaTask> {
        wsdlDir = file("$projectDir/src/main/resources/wsdl")
        wsdlsToGenerate = listOf(
            mutableListOf("-xjc", "-b", "$projectDir/src/main/resources/xjb/bindings.xml", "$projectDir/src/main/resources/wsdl/amelding_EksternKontrolEmeldingService.wsdl")
        )
    }

    withType<ShadowJar> {
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
        }
    }

    register("runServer", JavaExec::class) {
        main = application.mainClassName
        classpath = sourceSets["main"].runtimeClasspath
    }
}