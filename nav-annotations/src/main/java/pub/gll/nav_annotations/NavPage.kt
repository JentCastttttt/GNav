package pub.gll.nav_annotations

import pub.gll.nav_api.GNavInterceptor
import kotlin.reflect.KClass

/**
 * ==============================================
 * 🚀 NavPage 注解
 * ==============================================
 *
 * 用于标记 **Composable 函数** 作为导航系统中的页面（Destination）。
 * 被标记的函数会在 **KSP 注解处理器 (NavProcessor)** 编译时自动解析，
 * 并生成相应的导航注册代码，从而简化导航配置。
 *
 * ---
 * ### 🧭 功能概述
 * 1. 自动注册页面路由（无需手动写 NavGraph）
 * 2. 支持为指定路由绑定多个拦截器（如登录校验、埋点拦截等）
 * 3. 与 Compose Navigation 深度集成
 *
 * ---
 * ### 💡 使用示例
 * ```kotlin
 * @NavPage(
 *     route = "home",
 *     interceptors = [LoginInterceptor::class]
 * )
 * @Composable
 * fun HomeScreen() {
 *     // 页面内容
 * }
 * ```
 *
 * 该注解会在编译时自动生成：
 * ```kotlin
 * builder.composable("home") { backStackEntry ->
 *     HomeScreen()
 * }
 * ```
 *
 * 并在导航前通过 `LoginInterceptor` 进行拦截判断。
 *
 * ---
 * ### ⚙️ 参数说明
 *
 * @property route
 * 页面对应的导航路由（可选）。
 * - 支持路径式写法（如 `"user/profile"`）
 * - 支持参数写法（如 `
 *  @Composable
 *  * fun HomeScreen(page:Int) {
 *  *     // 页面内容
 *  * }
 * `）
 *
 * @property interceptors
 * 页面拦截器数组（可选）。
 * 用于在导航前执行拦截逻辑，比如登录校验、埋点统计、权限检测等。
 * 每个拦截器需实现 [GNavInterceptor] 接口。
 *
 * ---
 * ### 🧩 编译期行为
 * NavProcessor 将扫描所有使用了 `@NavPage` 的函数，
 * 并自动生成 `NavRouteRegistrar` 实现类，用于统一注册路由。
 *
 * 例如：
 * ```
 * object GeneratedNavRegistrar : NavRouteRegistrar {
 *     override fun registerRoutes(...) {
 *         builder.composable("home") { HomeScreen() }
 *     }
 * }
 * ```
 *
 * ---
 * @see pub.gll.nav_api.GNavInterceptor 导航拦截器接口
 * @see pub.gll.nav_api.NavRouteRegistrar 路由注册接口
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NavPage(
    /**
     * 页面路由路径（全局唯一）
     */
    val route: String = "",

    /**
     * 该页面的拦截器集合
     * - 在导航前会依次调用每个拦截器的 shouldIntercept() 方法
     */
    val interceptors: Array<KClass<out GNavInterceptor>> = []
)
