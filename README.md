---
highlight: a11y-dark
---
[English](https://github.com/a765032380/GNav/blob/master/README_EN.md) | [简体中文](https://github.com/a765032380/GNav/blob/master/README.md)

# GNav - Compose 导航框架：告别样板代码，自动注册，零配置
# GNav Multiplatform 基于 KMP 的跨平台方案，第一版已开发完成。  [GNav Multiplatform on GitHub](https://github.com/a765032380/GNavMultiplatform)

## 项目概述

GNav 是一个基于 Jetpack Compose Navigation 的现代化导航框架，通过注解处理和自动化代码生成技术，彻底简化了 Compose 应用中的导航配置和管理。它不仅仅是一个工具库，更代表了一种全新的导航编程范式。  ** 项目地址： **  [GNav on GitHub](https://github.com/a765032380/GNav)

## 核心价值

### 🚀 开发效率提升

-    ** 自动生成 ** 导航相关代码，减少90%的样板代码
-    ** 编译时类型检查 ** ，彻底告别运行时路由错误
-    ** 智能参数解析 ** ，自动处理类型转换和安全空值处理

### 🛡️ 代码质量保障

-    ** 类型安全 ** 的导航函数，避免字符串拼接错误
-    ** 模块化设计 ** ，清晰的职责分离，便于维护和扩展
-    ** 与现有 Jetpack Compose Navigation 完全兼容 ** ，平滑迁移

### 💪 企业级功能

-    ** 内置拦截器机制 ** ，支持权限校验、登录验证等业务场景
-    ** 完整的统计分析 ** 接口，轻松集成用户行为追踪
-    ** 丰富的动画支持 ** ，提供流畅的页面切换体验

## 快速开始

### 1. 添加依赖

在 `build.gradle.kts`中添加：

```kotlin
dependencies {
    implementation("pub.gll:nav:1.0.2")
    ksp("pub.gll:nav-processor:1.0.2")
}
```

### 2. 声明页面

使用 `@NavPage`注解标记 Composable 函数：

```kotlin
@NavPage(interceptors = [AuthInterceptor::class])
@Composable
fun HomePage(userId: String) {
    // 页面内容 - 参数自动解析，类型安全
}

//框架会自动解析方法中userId和from，自动生成带参数的跳转方法
@NavPage
@Composable  
fun ProfilePage(userId: Int, from: String) {
    // 支持可选参数和自定义路由
}
```

### 3. 配置导航宿主

```kotlin
@Composable
fun App() {
    GNavCompose(
        startDestination = getHomeRoute(),
        //navAnalytics 可选参数，如果不需要统计，可以不写
        navAnalytics = object : NavAnalytics {
            override fun onPageEnter(currentRoute: String, previousRoute: String?, params: Map<String, Any?>) {
                // 页面进入统计
            }
            override fun onPageExit(currentRoute: String) {
                // 页面退出统计
            }
        }
    )
}
```
### 4. 执行 Build 

执行build 任务后，会自动生成路由注册代码和扩展函数代码。

### 5. 执行导航操作

```kotlin
// 方式1：使用生成的类型安全扩展函数
navController.goHome("user123")

// 方式2：使用全局导航API  
GNav.navigate(getProfileRoute(123))

// 方式3：协程环境安全导航
viewModelScope.launch {
    GNav.navigateSuspend(getDetailRoute(itemId))
}
```

## 核心特性详解

### ✨ 智能路由注册

 ** 传统方式 vs GNav： ** 

```kotlin
// ❌ 传统方式：手动注册，容易出错
NavHost(navController, "home") {
    composable("home") { HomePage() }
    composable("detail/{id}") { backStackEntry ->
        val id = backStackEntry.arguments?.getString("id") ?: ""
        DetailPage(id)
    }
}
// ❌ 传统方式跳转 ：需要写魔法值，容易出错
navController.navigate("home")

// ✅ GNav：自动注册，类型安全
@NavPage 
@Composable 
fun HomePage(){
    //todo 页面内容
}

@NavPage 
@Composable 
fun DetailPage(id: String){
    //todo 页面内容
}
//✅ GNav：跳转 使用框架自动生成的拓展函数进行跳转
//无需魔法值，无需考虑传参错误，无需考虑拦截器，拦截器由目标页面实现，解耦更彻底

val navController = LocalNavController.current
navController.goHome()
navController.goDetail(id)

```

 ** 路由配置选项： ** 

-    ** 自动路由生成 ** ：基于函数名智能生成（如 `UserProfilePage`→ `user_profile`）
-    ** 自定义路由 ** ：支持显式指定复杂路由路径
-    ** 参数路由 ** ：自动处理参数占位符和类型映射

### 🛡️ 强大的拦截器系统

```kotlin
class AuthInterceptor : GNavInterceptor {
    override fun shouldIntercept(navController: NavController, route: String): Boolean {
        if (requiresAuth(route) && !UserSession.isLoggedIn) {
            showToast("请先登录")
            return true // 拦截原导航
        }
        return false // 放行
    }
}

// 应用拦截器
@NavPage(interceptors = [AuthInterceptor::class, LoggingInterceptor::class])
@Composable 
fun UserProfilePage(){

}
```

### 📊 企业级统计分析

```kotlin
class BusinessAnalytics : NavAnalytics {
    override fun onPageEnter(currentRoute: String, previousRoute: String?, params: Map<String, Any?>) {
        analytics.track("page_view", mapOf(
            "page" to currentRoute,
            "referrer" to previousRoute,
            "params" to params
        ))
    }
    
    override fun onPageExit(currentRoute: String) {
        // 停留时长统计等
    }
}
```

## 技术架构深度解析

### 编译时代码生成架构

GNav 基于 KSP（Kotlin Symbol Processing）实现编译时代码生成，确保运行时零开销：

```kotlin
@NavPage 注解 → KSP 处理器 → 生成的代码
    ↓               ↓               ↓
Composable函数 → 符号分析 → 路由注册器 + 扩展函数
```

### 核心处理流程

1.   ** 符号扫描 ** （[NavProcessor](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/NavProcessor.kt)）

    -   扫描所有 `@NavPage`注解的 Composable 函数
    -   提取函数签名、参数信息、注解配置

1.   ** 路由构建 ** （[RouteBuilder](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/RouteBuilder.kt)）

    -   智能路由路径生成
    -   参数占位符处理（`{param}`格式）

1.   ** 参数处理 ** （[ArgReaderGenerator](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/ArgReaderGenerator.kt)）

    -   类型安全的参数解析代码生成
    -   支持 String、数值类型、Boolean 等自动转换

1.   ** 扩展函数生成 ** （[NavExtensionBuilder](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/NavExtensionBuilder.kt)）

    -   生成 `goXxx()`类型安全导航函数
    -   自动参数编码和路由拼接

### 生成的代码示例

 ** 输入： ** 

```kotlin
@NavPage
@Composable
fun UserProfilePage(id: Int, tab: String)
```

 ** 输出： ** 

```kotlin
// 自动生成的导航函数
fun NavController.goUserProfile(id: Int, tab: String) {
    navigate("user/${Uri.encode(id.toString())}/profile?tab=${Uri.encode(tab)}")
}

// 自动生成的路由获取函数  
fun getUserProfileRoute(id: Int, tab: String) = 
    "user/${Uri.encode(id.toString())}/profile?tab=${Uri.encode(tab)}"

// 自动注册的路由处理
builder.composable("user/{id}/profile") { backStackEntry ->
    val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
    val tab = backStackEntry.arguments?.getString("tab") ?: "info"
    UserProfilePage(id, tab)
}
```

## 模块化架构设计

GNav 采用清晰的模块化架构，确保各组件职责单一：

| 模块                      | 职责    | 核心组件                                                                                                                                                                                                                                                                                                                                                                                  |
| ----------------------- | ----- |---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|  ** nav-api **          | 接口定义  | [`NavRouteRegistrar`](https://github.com/a765032380/GNav/blob/master/nav-api/src/main/java/pub/gll/nav_api/NavRouteRegistrar.kt), [`GNavInterceptor`](https://github.com/a765032380/GNav/blob/master/nav-api/src/main/java/pub/gll/nav_api/GNavInterceptor.kt), [`NavAnalytics`](https://github.com/a765032380/GNav/blob/master/nav-api/src/main/java/pub/gll/nav_api/NavAnalytics.kt) |
|  ** nav-annotations **  | 注解定义  | [`@NavPage`](https://github.com/a765032380/GNav/blob/master/nav-annotations/src/main/java/pub/gll/nav_annotations/NavPage.kt)                                                                                                                                                                                                                                                         |
|  ** nav-processor **    | 代码生成  | [`NavProcessor`](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/NavProcessor.kt), [`RouteBuilder`](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/RouteBuilder.kt)                                                                                                                |
|  ** nav **              | 运行时实现 | [`GNavCompose`](https://github.com/a765032380/GNav/blob/master/nav/src/main/java/pub/gll/nav/GNavCompose.kt), [`GNav`](https://github.com/a765032380/GNav/blob/master/nav/src/main/java/pub/gll/nav/GNav.kt)                                                                                                                                                                          |

## 与传统方案对比

| 特性           | Jetpack Navigation | GNav         |
| ------------ | ------------------ | ------------ |
|  ** 路由注册 **  | 手动编写，容易遗漏          | ✅ 自动注册，零配置   |
|  ** 参数安全 **  | 运行时字符串解析           | ✅ 编译时类型检查    |
|  ** 导航调用 **  | 需要传递 NavController | ✅ 全局导航 API   |
|  ** 拦截器 **   | 需要手动实现             | ✅ 声明式配置      |
|  ** 类型安全 **  | 手动保证               | ✅ 自动生成类型安全函数 |
|  ** 可维护性 **  | 分散的配置代码            | ✅ 集中式注解管理    |

## 最佳实践

### 路由命名规范

```kotlin
// ✅ 推荐：清晰的命名空间
@NavPage
@Composable fun HomePage()

@NavPage
@Composable fun UserProfilePage()

// ❌ 避免：模糊的路由命名
@NavPage
@Composable fun SomePage123()
```

### 参数设计原则

```kotlin
// ✅ 推荐：必要参数 + 可选参数
@NavPage
@Composable
fun OrderDetailPage(
    orderId: String,          // 必要参数
    from: String? = null      // 可选参数
)

// ❌ 暂不支持对象传递，如果需要对象传递，推荐使用 Gson
@NavPage
@Composable fun ComplexPage(
    userInfo:UserInfo
)
```

### 拦截器组合使用

```kotlin
// 业务特定的拦截器组合
@NavPage(interceptors = [
    AuthInterceptor::class,      // 认证检查
    PermissionInterceptor::class, // 权限验证  
    AnalyticsInterceptor::class   // 埋点统计
])
@Composable 
fun PaymentPage()
```

## 适用场景-单 Activity

### 🏢 企业级应用

-   需要严格权限控制的金融、医疗应用
-   需要完整用户行为追踪的电商平台
-   复杂的多模块导航架构

### 🚀 初创项目

-   快速原型开发，专注业务逻辑
-   需要高质量代码基础的技术选型
-   预期会有复杂导航需求的成长型项目

### 🔧 现有项目迁移

-   渐进式迁移，与现有 Navigation 兼容
-   局部模块试点，降低迁移风险
-   享受自动化带来的开发效率提升

## 总结

GNav 通过创新的编译时代码生成技术，为 Jetpack Compose 导航提供了企业级的解决方案。它不仅大幅提升了开发效率，更重要的是通过类型安全和自动化机制，从根本上提升了代码质量和可维护性。 无论是从零开始的新项目，还是现有项目的技术升级，GNav 都能为您提供专业、可靠的导航架构支持。  ** 开始体验： **  [GNav GitHub Repository](https://github.com/a765032380/GNav)

* * *

*GNav - 让导航变得简单而强大*
