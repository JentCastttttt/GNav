package pub.gll.nav

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import pub.gll.nav_api.NavAnalytics
import pub.gll.nav_api.NavRouteRegistrar
import java.util.ServiceLoader

/**
 * ==============================================
 * 🧭 GNavRouteRegistrarLoad
 * ==============================================
 *
 * 路由注册加载器（框架内部使用）。
 *
 * 该对象负责在运行时通过 [ServiceLoader] 动态加载
 * 实现了 [NavRouteRegistrar] 接口的自动注册器，
 * 并将所有由注解处理器（KSP）生成的导航页面注册到 [NavGraphBuilder] 中。
 *
 * ---
 * ### ⚙️ 工作机制
 *
 * 1. 在编译期，注解处理器会扫描所有使用 `@NavPage` 的页面，
 *    并生成一个实现了 [NavRouteRegistrar] 的类（例如 `GeneratedNavRegistrar`）。
 *
 * 2. 该类会被注册到 `META-INF/services/pub.gll.nav_api.NavRouteRegistrar` 中。
 *
 * 3. 在运行时，[GNavRouteRegistrarLoad] 使用 [ServiceLoader] 自动发现并加载该类。
 *
 * 4. 调用 [registerRoutes] 后，将自动执行所有页面的注册逻辑。
 *
 * ---
 * ### ✅ 示例
 *
 * 在 `GNavCompose` 中自动调用：
 * ```kotlin
 * NavHost(
 *     navController = navController,
 *     startDestination = "home"
 * ) {
 *     GNavRouteRegistrarLoad.registerRoutes(this, navController, navAnalytics)
 * }
 * ```
 *
 * ---
 * ### ⚠️ 注意事项
 * - 必须在应用的 `build.gradle` 中启用注解处理器（KSP）；
 * - 若找不到任何实现类，将抛出 `IllegalStateException`；
 * - 若项目中存在多个模块，应确保每个模块的注册文件都能被正确合并。
 *
 * @author  GLL
 * @since   1.0.0
 * @see     NavRouteRegistrar
 * @see     pub.gll.nav.GNavCompose
 */
object GNavRouteRegistrarLoad {

    /**
     * 注册所有通过 [NavRouteRegistrar] 生成的路由页面。
     *
     * @param builder Compose Navigation 的 [NavGraphBuilder]
     * @param navController 当前的 [NavController]
     * @param analytics 可选的导航统计回调接口 [NavAnalytics]
     *
     * @throws IllegalStateException 如果未找到任何 [NavRouteRegistrar] 实现类
     */
    fun registerRoutes(
        builder: NavGraphBuilder,
        navController: NavController,
        analytics: NavAnalytics? = null
    ) {
        val registrar = ServiceLoader.load(NavRouteRegistrar::class.java)
            .firstOrNull()
            ?: throw IllegalStateException(
                "No NavRouteRegistrar implementation found! " +
                        "Please ensure KSP annotation processing is enabled."
            )

        // 执行注册逻辑
        registrar.registerRoutes(builder, navController, analytics)
    }
}
