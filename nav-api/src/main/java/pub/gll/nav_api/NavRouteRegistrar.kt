package pub.gll.nav_api

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder

/**
 * ==============================================
 * 🗺️ NavRouteRegistrar
 * ==============================================
 *
 * 导航路由注册器接口，用于将注解处理器生成的页面路由
 * 注册到 Jetpack Compose Navigation 的 NavGraphBuilder 中。
 *
 * 框架内部会通过 ServiceLoader 或其他机制加载实现类，
 * 并调用 `registerRoutes` 将所有页面绑定到导航图中。
 *
 * ---
 * ### 使用方式
 *
 * ```kotlin
 * class GeneratedNavRegistrar : NavRouteRegistrar {
 *     override fun registerRoutes(
 *         builder: NavGraphBuilder,
 *         navController: NavController,
 *         analytics: NavAnalytics?
 *     ) {
 *         builder.composable("home") { backStackEntry ->
 *             HomeScreen(navController)
 *         }
 *         builder.composable("profile/{userId}") { backStackEntry ->
 *             val userId = backStackEntry.arguments?.getString("userId")
 *             ProfileScreen(navController, userId)
 *         }
 *     }
 * }
 * ```
 *
 * 框架会自动生成并调用这些注册器，无需开发者手动注册。
 *
 */
interface NavRouteRegistrar {

    /**
     * 将页面路由注册到 NavGraphBuilder
     *
     * @param builder NavGraphBuilder 用于注册导航目标
     * @param navController 当前导航控制器
     * @param analytics 可选参数，用于页面埋点或统计
     */
    fun registerRoutes(builder: NavGraphBuilder, navController: NavController, analytics: NavAnalytics?)
}
