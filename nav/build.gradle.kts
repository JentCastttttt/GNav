plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

android {
    namespace = "pub.gll.nav"
    compileSdk = 36

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.navigation.compose)
    val isLocalMaven = providers.gradleProperty("isLocalMaven")
    if (isLocalMaven.get().toBoolean()) {
        api(project(":nav-api"))
        api(project(":nav-annotations"))
    }else{
        api(libs.gll.nav.api)
        api(libs.gll.nav.annotations)
    }
}



afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["release"])

                groupId = "pub.gll"
                artifactId = "nav"
                version = providers.gradleProperty("gllNavPushVersion").get()

                pom {
                    name.set("nav")
                    description.set("nav")
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