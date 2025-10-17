plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.ksp)
    id("maven-publish")
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {
    implementation(libs.androidx.navigation.compose)
    val isLocalMaven = providers.gradleProperty("isLocalMaven")
    if (isLocalMaven.get().toBoolean()) {
        implementation(project(":nav-annotations"))
    }else{
        implementation(libs.gll.nav.annotations)
    }
    implementation("com.google.devtools.ksp:symbol-processing-api:${libs.versions.ksp.get()}")
    implementation("com.squareup:kotlinpoet:2.2.0")
    implementation("com.squareup:kotlinpoet-ksp:2.2.0")
}


afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                groupId = "pub.gll"
                artifactId = "nav-processor"
                version = providers.gradleProperty("gllNavPushVersion").get()

                pom {
                    name.set("nav-processor")
                    description.set("nav-processor")
                    url.set("http://gll.pub")

                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }

                    developers {
                        developer {
                            id.set("gongleilei")
                            name.set("gongleilei")
                            email.set("gll_android@163.com")
                        }
                    }
                }
            }
        }

        repositories {
            maven {
                name = "NexusPublic"
                url = uri("http://gll.pub:8081/repository/maven-releases/")
                isAllowInsecureProtocol = true
                credentials {
                    username = "admin"
                    password = "GLL_android"
                }
            }
        }
    }
}