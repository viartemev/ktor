import org.jetbrains.kotlin.konan.target.HostManager

buildscript {
    val dokka_version: String = "0.10.0"
    val kotlin_version: String = "1.3.61"
    val atomicfu_version: String = "0.14.1"
    val jmh_plugin_version: String = "0.4.5"
    val benchmarks_version: String = "0.2.0-dev-5"
    val spring_dependency_management_version: String = "1.0.8.RELEASE"
    /*
     * These property group is used to build ktor against Kotlin compiler snapshot.
     * How does it work:
     * When build_snapshot_train is set to true, kotlin_version property is overridden with kotlin_snapshot_version,
     * atomicfu_version, coroutines_version, serialization_version and kotlinx_io_version are overwritten by TeamCity environment.
     * Additionally, mavenLocal and Sonatype snapshots are added to repository list and stress tests are disabled.
     * DO NOT change the name of these properties without adapting kotlinx.train build chain.
     */
    val prop = rootProject.properties["build_snapshot_train"]
//    ext.build_snapshot_train = prop != null && prop != ""
    val build_snapshot_train = prop != null && prop != ""
    if (build_snapshot_train) {
//        ext.kotlin_version = rootProject.properties["kotlin_snapshot_version"]
        val kotlin_version = rootProject.properties["kotlin_snapshot_version"]
        if (kotlin_version == null) {
            throw IllegalArgumentException("'kotlin_snapshot_version' should be defined when building with snapshot compiler")
        }
        repositories {
            mavenLocal()
            maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        }

        configurations.classpath {
            //            resolutionStrategy.eachDependency { details ->
//                //                DependencyResolveDetails details ->
//                if (details.requested.group == "org.jetbrains.kotlin") {
////                    details.useVersion kotlin_version
//                }
//            }
        }
    }

    repositories {
        mavenLocal()
        jcenter()
        gradlePluginPortal()
        maven(url = "https://dl.bintray.com/jetbrains/kotlin-native-dependencies")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-dev")
        maven(url = "https://dl.bintray.com/orangy/maven")
        maven(url = "https://kotlin.bintray.com/kotlinx")
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlin_version")
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:$dokka_version")
        classpath("org.jetbrains.kotlinx:atomicfu-gradle-plugin:$atomicfu_version")
        classpath("org.jetbrains.kotlin:kotlin-serialization:$kotlin_version")
        classpath("me.champeau.gradle:jmh-gradle-plugin:$jmh_plugin_version")
        classpath("org.jetbrains.kotlinx:kotlinx.benchmark.gradle:$benchmarks_version")
        classpath("io.spring.gradle:dependency-management-plugin:$spring_dependency_management_version")
    }
}


//ext.configuredVersion = project.hasProperty("releaseVersion") ? project.releaseVersion : project.version
//ext.globalM2 = "$buildDir/m2"
//ext.publishLocal = project.hasProperty("publishLocal")

apply(from = "gradle/experimental.gradle")
apply(from = "gradle/verifier.gradle")

/**
 * `darwin` is subset of `posix`.
 * Don"t create `posix` and `darwin` sourceSets in single project.
 */
val platforms = listOf("common", "jvm", "js", "posix", "darwin")
project.ext["skipPublish"] = listOf(
    "binary-compatibility-validator", "ktor-server-benchmarks", "ktor-client-benchmarks"
)
project.ext["nonDefaultProjectStructure"] = listOf("ktor-bom")

fun projectNeedsPlatform(project: Project, platform: String) {
    if (rootProject.ext["skipPublish"].contains(project.name)) return platform == "jvm"

    val files = project.projectDir.listFiles()
    val hasPosix = files.any { it.name == "posix" }
    val hasDarwin = files.any { it.name == "darwin" }

    if (hasPosix && hasDarwin) return false

    if (hasPosix && platform == "darwin") return false
    if (hasDarwin && platform == "posix") return false
    if (!hasPosix && !hasDarwin && platform == "darwin") return false

    return files.any { it.name == "common" || it.name == platform }
}

fun check(version: Any, libVersion: String, libName: String) {
    if (version != libVersion) {
        throw new IllegalStateException ("Current deploy version is $version, but $libName version is not overridden ($libVersion)")
    }
}

allprojects {
    group = "io.ktor"
    version = configuredVersion
    project.ext.hostManager = HostManager()

    if (build_snapshot_train) {
        ext.kotlin_version = rootProject.properties["kotlin_snapshot_version"]
        println("Using Kotlin $kotlin_version for project $it")
        val deployVersion = properties["DeployVersion"]
        if (deployVersion != null) version = deployVersion

        val skipSnapshotChecks = rootProject.properties["skip_snapshot_checks"] != null
        if (!skipSnapshotChecks) {
            check(version, atomicfu_version, "atomicfu")
            check(version, coroutines_version, "coroutines")
            check(version, serialization_version, "serialization")
        }
        kotlin_version = rootProject.properties["kotlin_snapshot_version"]
        repositories {
            mavenLocal()
            maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
        }
    }

    repositories {
        mavenLocal()
        maven {
            url = "https://dl.bintray.com/kotlin/kotlinx/"
            credentials {
                username = if (project.hasProperty("bintrayUser")) {
                    project.property("bintrayUser")
                } else {
                    System.getenv("BINTRAY_USER")
                } ?: ""

                password = if (project.hasProperty("bintrayApiKey")) {
                    project.property("bintrayApiKey")
                } else {
                    System.getenv("BINTRAY_API_KEY")
                } ?: ""
            }
        }
        maven(url = "https://dl.bintray.com/kotlin/kotlin-eap")
        maven(url = "https://dl.bintray.com/kotlin/kotlin-dev")
        maven(url = "https://dl.bintray.com/orangy/maven")
        jcenter()

    }

    if (rootProject.ext.nonDefaultProjectStructure.contains(project.name)) return

    apply(plugin = "kotlin-multiplatform")

    platforms.each { platform ->
        if (projectNeedsPlatform(project, platform)) {
            configure([it]) {
                apply(from = rootProject.file("gradle/utility.gradle"))
                apply(from = rootProject.file("gradle/${platform}.gradle"))
            }
        }
    }

    if (!rootProject.ext.skipPublish.contains(project.name)) {
        apply(from = rootProject.file("gradle/dokka.gradle"))
        apply(from = rootProject.file("gradle/publish.gradle"))
    }

    configurations { testOutput }

    kotlin {
        configure(sourceSets) {
            val srcDir = if (name.endsWith("Main")) "src" else "test"
            val resourcesPrefix = if (name.endsWith("Test")) "test-" else ""
            val platform = name[0..-5]

            kotlin.srcDirs = ["$platform/$srcDir"]
            resources.srcDirs = ["$platform/${resourcesPrefix}resources"]

            languageSettings {
                progressiveMode = true
                experimentalAnnotations.each { useExperimentalAnnotation(it) }

                if (project.path.startsWith(":ktor-server:ktor-server") && project.name != "ktor-server-core") {
                    useExperimentalAnnotation("io.ktor.server.engine.EngineAPI")
                }
            }
        }
    }
}

tasks.register<KotlinTestReport>("rootAllTest") {
    val rootAllTest = it
    destinationDir = File(project.buildDir, "reports/tests/rootAllTest")

    allprojects {
        it.afterEvaluate {
            if (it.tasks.findByName("allTests") != null) {
                val projectTests = it.tasks.named("allTests")
                rootAllTest.addChild(projectTests)
                rootAllTest.dependsOn(projectTests)
            }
        }
    }

    beforeEvaluate {
        project.gradle.taskGraph.whenReady { graph ->
            rootAllTest.maybeOverrideReporting(graph)
        }
    }
}

build.dependsOn(rootAllTest)

println("Using Kotlin compiler version: $org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION")
if (build_snapshot_train) {
    println("Hacking test tasks, removing stress and flaky tests")
    allprojects {
        tasks.withType(Test).all {
            exclude("**/*ServerSocketTest*")
            exclude("**/*NettyStressTest*")
            exclude("**/*CIOMultithreadedTest*")
            exclude("**/*testBlockingConcurrency*")
            exclude("**/*testBigFile*")
            exclude("**/*numberTest*")
            exclude("**/*testWithPause*")
            exclude("**/*WebSocketTest*")
            exclude("**/*PostTest*")
        }
    }

    println("Manifest of kotlin-compiler-embeddable.jar")
    configure(subprojects.findAll { it.name == "ktor-client" }) {
        configurations.matching { it.name == "kotlinCompilerClasspath" }.all {
            resolvedConfiguration.getFiles().findAll { it.name.contains("kotlin-compiler-embeddable") }.each {
                val manifest = zipTree(it).matching {
                    include("META-INF/MANIFEST.MF")
                }.getFiles().first()

                manifest.readLines().each {
                    println(it)
                }
            }
        }
    }
}

afterEvaluate {
    val allCompileKotlinTasks = subprojects.collect {
        if (it.hasProperty("compileKotlinJvm")) [it.compileKotlinJvm] else []
    }.flatten()

    configure(allCompileKotlinTasks) {
        kotlinOptions.freeCompilerArgs += ["-XXLanguage:+InlineClasses"]
    }

    tasks.register<Dokka>("dokkaWebsite") {
        outputFormat = "kotlin-website"
        outputDirectory = "${rootProject.projectDir}/apidoc"

        kotlinTasks { allCompileKotlinTasks }

        reportUndocumented = false
    }
}
