import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import no.nils.wsdl2java.Wsdl2JavaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val prometheusVersion = "0.6.0"
val ktorVersion = "1.2.2"
val junitVersion = "5.4.1"
val confluentVersion = "5.2.0"
val logstashVersion = "5.2"
val logbackVersion = "1.2.3"
val vaultJdbcVersion = "1.3.1"
val assertJVersion = "3.12.2"
val jacksonVersion = "2.9.8"
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

val mainClass = "no.nav.meldeplikt.meldekortservice.AppKt"

plugins {
    java

    id("no.nils.wsdl2java") version "0.10"

    kotlin("jvm") version "1.3.50"
    kotlin("plugin.allopen") version "1.3.41"

    id("com.github.johnrengelman.shadow") version "4.0.4"

    application
}

buildscript {
    repositories {
        // maven("https://repo.adeo.no/repository/maven-central/")
        jcenter()
    }
    dependencies {
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
    /*maven("https://repo.adeo.no/repository/maven-central/")
    maven("https://plugins.gradle.org/m2/")
    maven("https://dl.bintray.com/kotlin/ktor/")
    maven("http://repo.spring.io/plugins-release/")*/
    jcenter()
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
    api("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    api("io.prometheus:simpleclient_common:$prometheusVersion")
    api("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    api("io.ktor:ktor-server-netty:$ktorVersion")
    api("io.ktor:ktor-auth:$ktorVersion")
    api("io.ktor:ktor-auth-jwt:$ktorVersion")
    api("io.ktor:ktor-client-apache:$ktorVersion")
    api("io.ktor:ktor-client-json:$ktorVersion")
    api("io.ktor:ktor-client-serialization-jvm:$ktorVersion")
    api("io.ktor:ktor-client-gson:$ktorVersion")
    api("io.ktor:ktor-locations:$ktorVersion")
    api("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    api("io.ktor:ktor-jackson:$ktorVersion")
    api("no.nav.common:cxf:$navCommonVersion")
    api("io.github.microutils:kotlin-logging:$kotlinLoggerVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
    testImplementation(kotlin("test-junit5"))

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
    mainClassName = mainClass
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = application.mainClassName
        from(configurations.runtime.get().map { if (it.isDirectory) it else zipTree(it) })
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