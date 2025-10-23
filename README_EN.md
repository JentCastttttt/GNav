[English](https://github.com/a765032380/GNav/blob/master/README_EN.md) | [简体中文](https://github.com/a765032380/GNav/blob/master/README.md)

# GNav - Compose Navigation Framework: Say Goodbye to Boilerplate Code, Automatic Registration, Zero Configuration

## Project Overview

GNav is a modern navigation framework based on Jetpack Compose Navigation. It thoroughly simplifies navigation configuration and management in Compose applications through annotation processing and automated code generation technology. It's not just a utility library; it represents a new paradigm for navigation programming. **Project Address:** [GNav on GitHub](https://github.com/a765032380/GNav)

## Core Value

### 🚀 Enhanced Development Efficiency

-   **Automatic Generation** of navigation-related code, reducing 90% of boilerplate code
-   **Compile-time Type Checking**, completely eliminating runtime routing errors
-   **Intelligent Parameter Parsing**, automatically handling type conversion and safe null value processing

### 🛡️ Code Quality Assurance

-   **Type-safe** navigation functions, avoiding string concatenation errors
-   **Modular Design**, clear separation of responsibilities, easy to maintain and extend
-   **Fully Compatible** with existing Jetpack Compose Navigation, enabling smooth migration

### 💪 Enterprise-Grade Features

-   **Built-in Interceptor Mechanism**, supporting business scenarios like permission verification and login validation
-   **Complete Statistical Analysis** interface, easily integrating user behavior tracking
-   **Rich Animation Support**, providing smooth page transition experiences

## Quick Start

### 1. Add Dependencies

Add to `build.gradle.kts`:
```kotlin
dependencies {
    implementation("pub.gll:nav:1.0.2")
    ksp("pub.gll:nav-processor:1.0.2")
}
```
### 2. Declare Pages

Mark Composable functions with the `@NavPage` annotation:

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
### 3. Configure Navigation Host
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
### 4. Execute Build

After executing the build task, route registration code and extension function code will be automatically generated.

### 5. Perform Navigation Operations
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
## Detailed Core Features

### ✨ Intelligent Route Registration

**Traditional Way vs GNav:**

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
**Route Configuration Options:**

-   **Automatic Route Generation:** Intelligently generated based on function names (e.g., `UserProfilePage` → `user_profile`)
-   **Custom Routes:** Supports explicitly specifying complex route paths
-   **Parameterized Routes:** Automatically handles parameter placeholders and type mapping

### 🛡️ Powerful Interceptor System


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
### 📊 Enterprise-Grade Statistical Analysis
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
## In-depth Technical Architecture Analysis

### Compile-time Code Generation Architecture

GNav uses KSP (Kotlin Symbol Processing) for compile-time code generation, ensuring zero runtime overhead:

```kotlin
@NavPage 注解 → KSP 处理器 → 生成的代码
    ↓               ↓               ↓
Composable函数 → 符号分析 → 路由注册器 + 扩展函数
```
### Core Processing Flow

1.  **Symbol Scanning** ([NavProcessor](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/NavProcessor.kt))
    -   Scans all Composable functions annotated with `@NavPage`
    -   Extracts function signatures, parameter information, and annotation configurations
2.  **Route Building** ([RouteBuilder](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/RouteBuilder.kt))
    -   Intelligent route path generation
    -   Parameter placeholder handling (`{param}` format)
3.  **Parameter Processing** ([ArgReaderGenerator](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/ArgReaderGenerator.kt))
    -   Generates type-safe parameter parsing code
    -   Supports automatic conversion for String, numeric types, Boolean, etc.
4.  **Extension Function Generation** ([NavExtensionBuilder](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/NavExtensionBuilder.kt))
    -   Generates type-safe `goXxx()` navigation functions
    -   Automatic parameter encoding and route concatenation

### Generated Code Example

**Input:**

```kotlin
@NavPage
@Composable
fun UserProfilePage(id: Int, tab: String)
```
**Output:**


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
## Modular Architecture Design

GNav employs a clear modular architecture, ensuring single responsibility for each component:

| Module                      | Responsibility | Core Components                                                                                                                                                                                                                                              |
| --------------------------- | -------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **nav-api**                 | Interface Definition  | `NavRouteRegistrar`, `GNavInterceptor`, `NavAnalytics`                                                                                                                                                                                                      |
| **nav-annotations**         | Annotation Definition | [`@NavPage`](https://github.com/a765032380/GNav/blob/master/nav-annotations/src/main/java/pub/gll/nav_annotations/NavPage.kt)                                                                                                                                |
| **nav-processor**           | Code Generation      | [`NavProcessor`](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/NavProcessor.kt), [`RouteBuilder`](https://github.com/a765032380/GNav/blob/master/nav-processor/src/main/java/pub/gll/nav_processor/RouteBuilder.kt) |
| **nav**                     | Runtime Implementation | [`GNavCompose`](https://github.com/a765032380/GNav/blob/master/nav/src/main/java/pub/gll/nav/GNavCompose.kt), [`GNav`](https://github.com/a765032380/GNav/blob/master/nav/src/main/java/pub/gll/nav/GNav.kt)                                                 |

## Comparison with Traditional Solutions

| Feature             | Jetpack Navigation | GNav               |
| ------------------- | ------------------ | ------------------ |
| **Route Registration** | Manual, easy to miss        | ✅ Automatic, Zero-config |
| **Parameter Safety**   | Runtime string parsing      | ✅ Compile-time type check |
| **Navigation Call**    | Requires passing NavController | ✅ Global Navigation API |
| **Interceptors**       | Requires manual implementation | ✅ Declarative configuration |
| **Type Safety**        | Manually ensured            | ✅ Auto-generated type-safe functions |
| **Maintainability**    | Scattered configuration code | ✅ Centralized annotation management |

## Best Practices

### Route Naming Conventions
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
### Parameter Design Principles
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
### Interceptor Composition

```kotlin
// 业务特定的拦截器组合
@NavPage(interceptors = [
    AuthInterceptor::class,      // 认证检查
    PermissionInterceptor::class, // 权限验证  
    AnalyticsInterceptor::class   // 埋点统计
])
@Composable 
fun PaymentPage(){
    
}
```
## Target Scenarios - Single Activity

### 🏢 Enterprise Applications

-   Financial, medical applications requiring strict permission control
-   E-commerce platforms needing complete user behavior tracking
-   Complex multi-module navigation architectures

### 🚀 Startup Projects

-   Rapid prototyping, focusing on business logic
-   Technology selection requiring high-quality codebase
-   Growing projects expecting complex navigation needs

### 🔧 Migrating Existing Projects

-   Gradual migration, compatible with existing Navigation
-   Pilot testing in specific modules, reducing migration risk
-   Enjoy the development efficiency boost from automation

## Summary

GNav provides an enterprise-grade solution for Jetpack Compose navigation through innovative compile-time code generation technology. It not only significantly improves development efficiency but, more importantly, fundamentally enhances code quality and maintainability through type safety and automation mechanisms. Whether for a new project from scratch or a technical upgrade of an existing project, GNav can provide you with professional and reliable navigation architecture support. **Get Started:** [GNav GitHub Repository](https://github.com/a765032380/GNav)

---

*GNav - Making Navigation Simple and Powerful*


