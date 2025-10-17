plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
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
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                groupId = "pub.gll"
                artifactId = "nav-api"
                version = providers.gradleProperty("gllNavPushVersion").get()

                pom {
                    name.set("nav-api")
                    description.set("nav-api")
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