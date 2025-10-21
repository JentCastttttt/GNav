plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
    alias(libs.plugins.maven.publish.plugin)
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


mavenPublishing {
    coordinates("pub.gll", "nav", providers.gradleProperty("gllNavPushVersion").get())
    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("nav")
        description.set("[zh]GNav 路由框架，自动注册，自动生成 goXXX 拓展函数，自动生成 getXXXRoute 函数，支持拦截器 [en]GNav routing framework, automatic registration, automatic generation of goXXX extension functions, automatic generation of getXXXRoute functions, support for interceptors")
        inceptionYear.set("2025")
        url.set("https://github.com/a765032380/GNav")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("gll")
                name.set("gll")
                email.set("gll_android@163.com")
            }
        }
        scm {
            url.set("https://github.com/a765032380/GNav")
            connection.set("scm:git:https://github.com/a765032380/GNav.git")
            developerConnection.set("scm:git:ssh://git@github.com:a765032380/GNav.git")
        }
    }
}