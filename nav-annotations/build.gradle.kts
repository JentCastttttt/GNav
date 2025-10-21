plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("maven-publish")
    alias(libs.plugins.maven.publish.plugin)
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
    val isLocalMaven = providers.gradleProperty("isLocalMaven")
    if (isLocalMaven.get().toBoolean()) {
        implementation(project(":nav-api"))
    }else{
        implementation(libs.gll.nav.api)
    }
}

mavenPublishing {
    coordinates("pub.gll", "nav-annotations", providers.gradleProperty("gllNavPushVersion").get())
    publishToMavenCentral()
    signAllPublications()

    pom {
        name.set("nav-annotations")
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