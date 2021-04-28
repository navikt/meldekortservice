import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import no.nils.wsdl2java.Wsdl2JavaTask
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val prometheusVersion = "0.6.0"
val ktorVersion = "1.2.4"
val ktorTestVersion = "1.5.1"
val kotestVersion = "4.4.1"
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
val flywayVersion = "6.5.7"
val postgresVersion = "42.2.5"
val h2Version = "1.4.199"
val kluentVersion = "1.52"
val tokenValidationVersion = "1.1.5"
val ojdbc8Version = "19.3.0.0"
val mockOauthVersion = "0.3.1"

plugins {

    id("no.nils.wsdl2java") version "0.10"

    kotlin("jvm") version "1.3.50"
    kotlin("plugin.allopen") version "1.3.41"

    id("com.github.johnrengelman.shadow") version "4.0.4"

    id("org.flywaydb.flyway") version("5.2.4")

    id("org.sonarqube") version "2.8"

    id("jacoco")

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
    jcenter()
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

    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testCompile(kotlin("test-junit5"))
    testCompile("org.mockito:mockito-core:2.+")
    testImplementation("io.ktor:ktor-server-test-host:${ktorVersion}")
    testImplementation("io.ktor:ktor-client-mock:${ktorVersion}")
    testImplementation("io.ktor:ktor-client-mock-jvm:${ktorVersion}")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${kotestVersion}")


    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauthVersion")
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

jacoco {
    toolVersion = "0.8.4"
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

    sonarqube {
        properties {
            property("sonar.projectKey", System.getenv("SONAR_PROJECT_KEY_MELDEKORTSERVICE"))
            property("sonar.organization", "navit")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.login", System.getenv("SONAR_TOKEN_MELDEKORTSERVICE") )
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