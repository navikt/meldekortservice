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
val jacksonVersion = "2.9.9"
val jaxwsApiVersion = "2.3.1"
val javaxAnnotationApiVersion = "1.3.2"
val jaxbRuntimeVersion = "2.4.0-b180830.0438"
val jaxbApiVersion = "2.4.0-b180830.0359"
val javaxActivationVersion = "1.1.1"
val jaxwsToolsVersion = "2.3.1"
val javaxJaxwsApiVersion = "2.2.1"

val mainClass = "no.nav.meldeplikt.meldekortservice.AppKt"

plugins {
    java
    kotlin("jvm") version "1.3.50"
    kotlin("plugin.allopen") version "1.3.41"

    id("no.nils.wsdl2java") version "0.10"

    application
}

buildscript {
    repositories {
        maven("https://repo.adeo.no/repository/maven-central")
        jcenter()
    }
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.2.0")
    }
}

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compile("no.nav:vault-jdbc:$vaultJdbcVersion")
    compile("ch.qos.logback:logback-classic:$logbackVersion")
    compile("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    compile("io.prometheus:simpleclient_common:$prometheusVersion")
    compile("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-auth:$ktorVersion")
    compile("io.ktor:ktor-auth-jwt:$ktorVersion")
    compile("io.ktor:ktor-client-apache:$ktorVersion")
    compile("io.ktor:ktor-client-json:$ktorVersion")
    compile("io.ktor:ktor-client-serialization-jvm:$ktorVersion")
    compile("io.ktor:ktor-client-gson:$ktorVersion")
    compile("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    compile("io.ktor:ktor-jackson:$ktorVersion")

    testCompile("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testCompile("org.assertj:assertj-core:$assertJVersion")
    testCompile(kotlin("test-junit5"))

    wsdl2java("javax.annotation:javax.annotation-api:$javaxAnnotationApiVersion")
    wsdl2java("javax.activation:activation:$javaxActivationVersion")
    wsdl2java("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")
    wsdl2java("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    wsdl2java("javax.xml.ws:jaxws-api:$javaxJaxwsApiVersion")
    wsdl2java("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }

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
}

tasks.withType<KotlinCompile> {
    dependsOn("wsdl2java")
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClassName = mainClass
}

tasks {
    withType<Jar> {
        manifest {
            attributes["Main-Class"] = application.mainClassName
        }
        from(configurations.runtime.get().map { if (it.isDirectory) it else zipTree(it) })
    }

    register("runServer", JavaExec::class) {
        main = application.mainClassName
        classpath = sourceSets["main"].runtimeClasspath
    }

    withType<Wsdl2JavaTask> {
        wsdlDir = file("$projectDir/src/main/resources/wsdl")
        wsdlsToGenerate = listOf(
            mutableListOf("-xjc", "-b", "$projectDir/src/main/resources/xjb/bindings.xml", "$projectDir/src/main/resources/wsdl/amelding_EksternKontrolEmeldingService.wsdl")
        )
    }
}