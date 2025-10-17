# GNav - Compose 导航框架：告别样板代码，自动注册，零配置

GNav 是一个基于 Jetpack Compose Navigation 的导航框架，它通过注解处理和自动化代码生成，大大简化了 Compose 应用中的导航配置和管理。它不仅仅是一个工具库，更是一种新的导航编程范式。

## 项目结构

该项目由四个主要模块组成：

1. **nav-api** - 核心接口定义模块
2. **nav-annotations** - 注解定义模块
3. **nav-processor** - 注解处理器模块
4. **nav** - 主要实现模块

## 自动生成 goXXX 拓展函数，自动生成 getXXXRoute 函数，支持拦截器

### 1. 自动路由注册：从手动到声明式

通过 [@NavPage](./nav-annotations/src/main/java/pub/gll/nav_annotations/NavPage.kt#L78-L91) 注解标记 Composable 函数，框架会自动注册路由：

```kotlin
@NavPage(interceptors = [LoginInterceptor::class])
@Composable
fun HomePage(page:Int) {
    // 页面内容
}
```
### ⚙️ 参数说明

@property route
页面对应的导航路由（可选）。
- 如果不指定，默认使用函数名作为路由。如果函数名使用 Page 结尾，会自动去掉 Page 后缀。
- 支持路径式写法（如 `"user/profile"`）
- 支持参数写法（如 `
@Composable
fun HomeScreen(page:Int) {
    // 页面内容
}
`）

@property interceptors
页面拦截器数组（可选）。
用于在导航前执行拦截逻辑，比如登录校验、埋点统计、权限检测等。
每个拦截器需实现 [GNavInterceptor] 接口。


相比传统 Jetpack Compose Navigation 的手动注册方式：

```kotlin
// 传统方式需要手动注册每个页面
NavHost(navController = navController, startDestination = "home") {
    composable("home") { HomePage() }
    composable("profile/{userId}") { backStackEntry ->
        val userId = backStackEntry.arguments?.getString("userId")
        ProfilePage(userId)
    }
}
```


GNav 自动处理这些注册工作，开发者只需关注页面实现。

### 2. 全局导航管理：随时随地的导航自由

使用 [GNav](./nav/src/main/java/pub/gll/nav/GNav.kt) 对象进行导航操作：

```kotlin
//Compose中导航，框架生成的扩展函数（类型安全）
navController.goHome()

// 普通导航
GNav.navigate(getHomeRoute())//getHomeRoute()是框架自动生成的获取路由地址的函数

// 返回上一级
GNav.popBackStack()

// 协程中导航
GNav.navigateSuspend(getHomeRoute())
```


传统方式需要直接操作 `NavController`：

```kotlin
// 传统方式需要获取并传递 NavController 实例
navController.navigate("profile")
navController.popBackStack()
```


### 3. 导航拦截器：构建你的导航守卫

支持为特定路由添加拦截器，用于权限检查、登录验证等：

```kotlin
class LoginInterceptor : GNavInterceptor {
    override fun shouldIntercept(navController: NavController, route: String): Boolean {
        val isLoggedIn = checkLoginStatus()
        if (!isLoggedIn) {
            navController.goLogin()
            return true // 阻止原页面跳转
        }
        return false // 允许跳转
    }
}

@NavPage(interceptors = [LoginInterceptor::class])
@Composable
fun HomePage() { ... }
```


### 4. 导航动画支持：让页面切换更生动

`GNavCompose` 函数支持自定义页面切换动画：

```kotlin
GNavCompose(
    startDestination = getHomeRoute(),
    animationDuration = 300,
    enterTransition = { fadeIn(animationSpec = tween(300)) },
    exitTransition = { fadeOut(animationSpec = tween(300)) }
)
```


### 5. 导航统计分析：洞察用户行为

支持通过 [NavAnalytics](./nav-api/src/main/java/pub/gll/nav_api/NavAnalytics.kt#L11-L31) 接口进行页面访问统计：

```kotlin
interface NavAnalytics {
    fun onPageEnter(currentRoute: String, previousRoute: String?, params: Map<String, Any?>)
    fun onPageExit(currentRoute: String)
}
```


### 6. 类型安全的导航扩展函数：告别字符串拼接错误

框架会为每个带参数的页面自动生成类型安全的扩展函数：

```kotlin
// 定义带参数的页面
@NavPage(route = "user")
@Composable
fun UserPage(userId: Int) { ... }

// 自动生成的类型安全扩展函数
fun NavController.goUser(userId: Int) {
    // 自动处理参数编码和路由拼接
}
```


传统方式需要手动处理参数：

```kotlin
// 传统方式需要手动处理参数编码
navController.navigate("user/${Uri.encode(userId.toString())}")
```


## 技术内幕：GNav 如何工作？

### 1. 基于 KSP 的编译时代码生成

GNav 使用 Kotlin Symbol Processing (KSP) 在编译时分析 `@NavPage` 注解并生成代码：

- 自动生成 `GeneratedNavRegistrar` 实现 `NavRouteRegistrar` 接口
- 为每个页面生成类型安全的导航扩展函数
- 自动处理参数解析和路由构建

### 2. GFlowBus 事件总线：导航的中枢神经

框架内部使用 `GFlowBus` 实现全局导航事件传递：

- 基于 Kotlin Flow 实现
- 支持生命周期感知的自动订阅/取消订阅
- 支持粘性事件（Sticky Events）

```kotlin
// 导航事件通过 GFlowBus 传递
GFlowBus.with<String>(GNavEventKey.KEY_G_NAV_EVENT)
    .post(scope, route)
```


### 3. 参数自动解析：智能类型转换

框架自动处理路由参数的解析：

```kotlin
// 支持多种参数类型
@NavPage(route = "detail")
@Composable
fun DetailPage(id: Int, name: String, isActive: Boolean?) { ... }
```


生成的代码会自动从 `NavBackStackEntry` 中解析参数并转换为正确的类型。

### 4. 模块化解耦设计：清晰的职责分离

框架采用模块化设计，各模块职责清晰：

- `nav-api`: 定义核心接口
- `nav-annotations`: 定义注解
- `nav-processor`: 实现注解处理逻辑
- `nav`: 提供运行时实现

## nav-processor 模块详解

`nav-processor` 模块是 GNav 框架的核心编译时处理模块，它负责分析注解并生成代码。该模块基于 KSP (Kotlin Symbol Processing) 实现，具有高性能和类型安全的特点。

### 核心组件

#### 1. [NavProcessor](./nav-processor/src/main/java/pub/gll/nav_processor/NavProcessor.kt#L42-L267) - 主处理类

[NavProcessor](./nav-processor/src/main/java/pub/gll/nav_processor/NavProcessor.kt#L42-L267) 是整个注解处理器的核心，它实现了 `SymbolProcessor` 接口，负责扫描所有使用 [@NavPage](./nav-annotations/src/main/java/pub/gll/nav_annotations/NavPage.kt#L78-L91) 注解的函数并生成相应的代码。

```kotlin
class NavProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor
```


该处理器的主要工作流程包括：
1. 扫描所有使用 [@NavPage](./nav-annotations/src/main/java/pub/gll/nav_annotations/NavPage.kt#L78-L91) 注解的函数
2. 解析函数参数和注解信息
3. 生成路由注册代码
4. 生成导航扩展函数
5. 写入生成的代码文件

#### 2. [NavProcessorProvider](./nav-processor/src/main/java/pub/gll/nav_processor/NavProcessorProvider.kt#L11-L16) - 处理器提供者

[NavProcessorProvider](./nav-processor/src/main/java/pub/gll/nav_processor/NavProcessorProvider.kt#L11-L16) 是 KSP 框架要求的入口点，用于创建 [NavProcessor](./nav-processor/src/main/java/pub/gll/nav_processor/NavProcessor.kt#L42-L267) 实例：

```kotlin
class NavProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return NavProcessor(environment.codeGenerator, environment.logger)
    }
}
```


#### 3. [ParamInfo](./nav-processor/src/main/java/pub/gll/nav_processor/ParamInfo.kt#L46-L52) - 参数信息数据类

[ParamInfo](./nav-processor/src/main/java/pub/gll/nav_processor/ParamInfo.kt#L46-L52) 用于封装函数参数的详细信息，包括参数名、类型、是否可空等：

```kotlin
data class ParamInfo(
    val name: String,              // 参数名
    val qname: String,             // 完整限定类型名
    val isNullable: Boolean,       // 是否可空
    val isNavController: Boolean,  // 是否是 NavController
    val typeName: TypeName         // KotlinPoet 类型对象
)
```


#### 4. [RouteBuilder](./nav-processor/src/main/java/pub/gll/nav_processor/RouteBuilder.kt#L30-L79) - 路由构建器

[RouteBuilder](./nav-processor/src/main/java/pub/gll/nav_processor/RouteBuilder.kt#L30-L79) 负责根据函数名和参数自动生成路由字符串：

```kotlin
object RouteBuilder {
    fun buildRoute(
        func: KSFunctionDeclaration,
        routeArg: String?,
        params: List<ParamInfo>
    ): String
}
```


它支持多种路由生成方式：
- 显式指定路由（优先级最高）
- 自动从函数名生成路由（如 `UserProfilePage` → `user_profile`）
- 自动添加参数占位符（如 `{userId}`）

#### 5. [ArgReaderGenerator](./nav-processor/src/main/java/pub/gll/nav_processor/ArgReaderGenerator.kt#L16-L104) - 参数读取代码生成器

[ArgReaderGenerator](./nav-processor/src/main/java/pub/gll/nav_processor/ArgReaderGenerator.kt#L16-L104) 负责生成从 `NavBackStackEntry` 中读取参数的代码：

```kotlin
object ArgReaderGenerator {
    fun generate(p: ParamInfo): CodeBlock
}
```


它支持多种数据类型的自动转换：
- `String` 类型直接读取
- 数值类型（`Int`、`Long`、`Float`、`Double`）自动转换
- `Boolean` 类型使用 `toBooleanStrictOrNull` 进行安全转换

#### 6. [NavExtensionBuilder](./nav-processor/src/main/java/pub/gll/nav_processor/NavExtensionBuilder.kt#L33-L169) - 导航扩展函数构建器

[NavExtensionBuilder](./nav-processor/src/main/java/pub/gll/nav_processor/NavExtensionBuilder.kt#L33-L169) 负责为每个带注解的页面生成类型安全的导航扩展函数：

```kotlin
object NavExtensionBuilder {
    fun build(
        func: KSFunctionDeclaration,
        route: String,
        routeArg: String?,
        params: List<ParamInfo>,
        navControllerClass: ClassName
    ): FunSpec
}
```


生成的扩展函数具有以下特点：
- 函数名遵循 `goXxx` 命名规范
- 参数类型与原函数保持一致
- 自动处理参数编码（使用 `Uri.encode`）
- 支持可空参数的安全处理

### 代码生成流程

1. **符号扫描**：[NavProcessor](./nav-processor/src/main/java/pub/gll/nav_processor/NavProcessor.kt#L42-L267) 扫描所有使用 [@NavPage](./nav-annotations/src/main/java/pub/gll/nav_annotations/NavPage.kt#L78-L91) 注解的函数
2. **信息提取**：提取函数参数、注解参数等信息并封装为 [ParamInfo](./nav-processor/src/main/java/pub/gll/nav_processor/ParamInfo.kt#L46-L52)
3. **路由构建**：使用 [RouteBuilder](./nav-processor/src/main/java/pub/gll/nav_processor/RouteBuilder.kt#L30-L79) 构建路由字符串
4. **参数读取代码生成**：使用 [ArgReaderGenerator](./nav-processor/src/main/java/pub/gll/nav_processor/ArgReaderGenerator.kt#L16-L104) 生成参数读取代码
5. **扩展函数生成**：使用 [NavExtensionBuilder](./nav-processor/src/main/java/pub/gll/nav_processor/NavExtensionBuilder.kt#L33-L169) 生成导航扩展函数
6. **路由注册代码生成**：生成 [GeneratedNavRegistrar](./app/build/generated/ksp/debug/kotlin/pub/gll/generated/GeneratedNavRegistrar.kt#L23-L125) 类实现路由注册
7. **文件写入**：使用 KotlinPoet 将生成的代码写入文件

### 生成代码示例

对于以下页面定义：

```kotlin
@NavPage(route = "user")
@Composable
fun UserPage(userId: Int) { ... }
```


`nav-processor` 会生成类似以下的代码：

```kotlin
// 导航扩展函数
fun NavController.goUser(userId: Int) {
    pub.gll.nav.GNav.navigate(this, "user/" + Uri.encode(userId.toString()))
}

// 路由注册代码
class GeneratedNavRegistrar : NavRouteRegistrar {
    override fun registerRoutes(
        builder: NavGraphBuilder,
        navController: NavController,
        analytics: NavAnalytics?
    ) {
        builder.composable("user/{userId}") { backStackEntry ->
            val userIdStr = backStackEntry.arguments?.getString("userId")
            val userId = userIdStr?.toIntOrNull() ?: 0
            UserPage(userId)
        }
    }
}
```


## 使用方法

### 1. 添加依赖

在 `build.gradle` 文件中添加依赖：

```kotlin
dependencies {
    implementation("pub.gll:nav:1.0.2")
    ksp("pub.gll:nav-processor:1.0.2")
}
```


### 2. 创建页面

使用 [@NavPage](./nav-annotations/src/main/java/pub/gll/nav_annotations/NavPage.kt#L78-L91) 注解创建页面：

```kotlin
@NavPage(route = "home")
@Composable
fun HomePage() {
    // 页面内容
}

@NavPage(route = "profile/{userId}")
@Composable
fun ProfilePage(userId: String) {
    // 页面内容
}
```


### 3. 设置导航宿主

在应用中使用 [GNavCompose](./nav/src/main/java/pub/gll/nav/GNavCompose.kt#L59-L123) 设置导航宿主：

```kotlin
GNavCompose(
    startDestination = "home"
)
```


### 4. 进行导航

使用生成的扩展函数或 [GNav](./nav/src/main/java/pub/gll/nav/GNav.kt#L8-L64) 进行导航：

```kotlin
// 使用生成的扩展函数（类型安全）
navController.goProfile("123")

// 或使用 GNav（全局导航）
GNav.navigate(getProfileRoute("123"))
```


## 优势对比

| 特性 | 传统 Jetpack Navigation | GNav |
|------|------------------------|------|
| 路由注册 | 手动编写每个路由 | 自动注册 |
| 参数处理 | 手动解析和类型转换 | 自动解析和转换 |
| 导航调用 | 需要 NavController 实例 | 支持全局导航 |
| 类型安全 | 需要手动保证 | 自动生成类型安全函数 |
| 拦截器支持 | 需要手动实现 | 内置拦截器机制 |
| 统计分析 | 需要手动添加 | 内置分析接口 |

## 开源价值

1. **提升开发效率** - 减少样板代码，自动生成导航相关代码
2. **降低出错率** - 类型安全的导航函数避免字符串拼接错误
3. **增强可维护性** - 模块化设计，清晰的职责分离
4. **功能丰富** - 内置拦截器、统计分析等高级功能
5. **易于集成** - 与现有 Jetpack Compose Navigation 兼容

GNav 通过编译时代码生成和运行时框架的结合，为 Jetpack Compose 应用提供了一套完整的导航解决方案，显著提升了导航相关的开发体验。它不仅仅是一个工具，更是一种新的开发理念——让开发者专注于业务逻辑，而不是样板代码。