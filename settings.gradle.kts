pluginManagement {
    repositories {
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven(url = "https://dl.bintray.com/orangy/maven")
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "kotlin2js") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${requested.version}")
            }
        }
    }
}
rootProject.name = "ktor"
enableFeaturePreview("GRADLE_METADATA")

include(
    ":ktor-server:ktor-server-core",
    ":ktor-server:ktor-server-tests",
    ":ktor-server:ktor-server-host-common",
    ":ktor-server:ktor-server-test-host",
    ":ktor-server:ktor-server-jetty",
    ":ktor-server:ktor-server-jetty:ktor-server-jetty-test-http2",
    ":ktor-server:ktor-server-servlet",
    ":ktor-server:ktor-server-tomcat",
    ":ktor-server:ktor-server-netty",
    ":ktor-server:ktor-server-cio",
    ":ktor-server:ktor-server-benchmarks",
    ":binary-compatibility-validator",
    ":ktor-server",
    ":ktor-client",
    ":ktor-client:ktor-client-core",
    ":ktor-client:ktor-client-tests",
    ":ktor-client:ktor-client-apache",
    ":ktor-client:ktor-client-android",
    ":ktor-client:ktor-client-cio",
    ":ktor-client:ktor-client-curl",
    ":ktor-client:ktor-client-ios",
    ":ktor-client:ktor-client-jetty",
    ":ktor-client:ktor-client-js",
    ":ktor-client:ktor-client-mock",
    ":ktor-client:ktor-client-okhttp",
    ":ktor-client:ktor-client-benchmarks",
    ":ktor-client:ktor-client-features",
    ":ktor-client:ktor-client-features:ktor-client-json",
    ":ktor-client:ktor-client-features:ktor-client-json:ktor-client-json-tests",
    ":ktor-client:ktor-client-features:ktor-client-json:ktor-client-gson",
    ":ktor-client:ktor-client-features:ktor-client-json:ktor-client-jackson",
    ":ktor-client:ktor-client-features:ktor-client-json:ktor-client-serialization",
    ":ktor-client:ktor-client-features:ktor-client-auth",
    ":ktor-client:ktor-client-features:ktor-client-auth-basic",
    ":ktor-client:ktor-client-features:ktor-client-logging",
    ":ktor-client:ktor-client-features:ktor-client-encoding",
    ":ktor-client:ktor-client-features:ktor-client-websockets",
    ":ktor-features:ktor-freemarker",
    ":ktor-features:ktor-mustache",
    ":ktor-features:ktor-thymeleaf",
    ":ktor-features:ktor-velocity",
    ":ktor-features:ktor-gson",
    ":ktor-features:ktor-jackson",
    ":ktor-features:ktor-metrics",
    ":ktor-features:ktor-metrics-micrometer",
    ":ktor-features:ktor-server-sessions",
    ":ktor-features:ktor-locations",
    ":ktor-features:ktor-websockets",
    ":ktor-features:ktor-html-builder",
    ":ktor-features:ktor-auth",
    ":ktor-features:ktor-auth-ldap",
    ":ktor-features:ktor-auth-jwt",
    ":ktor-features:ktor-webjars",
    ":ktor-features:ktor-serialization",
    ":ktor-features",
    ":ktor-http",
    ":ktor-http:ktor-http-cio",
    ":ktor-io",
    ":ktor-utils",
    ":ktor-network",
    ":ktor-network:ktor-network-tls",
    ":ktor-network:ktor-network-tls:ktor-network-tls-certificates",
    ":ktor-bom",
    ":ktor-test-dispatcher"
)
