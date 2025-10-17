package pub.gll.nav

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import pub.gll.nav.GNavEventKey.KEY_G_NAV_EVENT
import pub.gll.nav_api.NavAnalytics

/**
 * ==============================================
 * 🧭 GNavCompose
 * ==============================================
 *
 * Compose 导航框架核心入口。
 *
 * 该函数封装了：
 * - Jetpack Compose Navigation 的基础配置；
 * - GFlowBus 全局事件监听机制；
 * - 动画切换支持；
 * - 可选的导航统计 (NavAnalytics)；
 *
 * 用于在 Compose 应用中快速构建支持全局导航与动画的 NavHost 容器。
 *
 * 示例用法：
 * ```
 * GNavCompose(
 *     startDestination = "home",
 *     navAnalytics = MyNavAnalyticsImpl()
 * )
 * ```
 *
 * @param modifier [Modifier] 修饰符，可控制布局大小、背景等。
 * @param navController [NavHostController] 导航控制器，可传入外部实例进行复用。
 * @param startDestination 导航图的起始目的地。
 * @param animationDuration 页面切换动画时长（毫秒）。
 * @param enterTransition 页面进入动画（默认淡入）。
 * @param exitTransition 页面退出动画（默认淡出）。
 * @param popEnterTransition 返回栈页面进入动画（默认同 enterTransition）。
 * @param popExitTransition 返回栈页面退出动画（默认同 exitTransition）。
 * @param navAnalytics 可选的导航统计回调接口，用于埋点分析。
 *
 * @see GNav
 * @see GFlowBus
 * @see GNavRouteRegistrarLoad
 */
@Composable
fun GNavCompose(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String,
    animationDuration: Int = 300,
    enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        fadeIn(animationSpec = tween(animationDuration))
    },
    exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        fadeOut(animationSpec = tween(animationDuration))
    },
    popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = enterTransition,
    popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = exitTransition,
    navAnalytics: NavAnalytics? = null,
) {
    // 提供全局 NavController 上下文
    CompositionLocalProvider(LocalNavController provides navController) {

        val lifecycleOwner = LocalLifecycleOwner.current

        /**
         * 监听 GFlowBus 全局导航事件。
         *
         * 当外部调用：
         * ```
         * GNav.navigate("profile")
         * ```
         * 时，将自动触发 navController 导航。
         */
        LaunchedEffect(Unit) {
            GFlowBus.with<String>(KEY_G_NAV_EVENT).register(lifecycleOwner) { route ->
                if (route.isEmpty()) {
                    // 空字符串代表执行返回操作
                    navController.popBackStack()
                } else {
                    // 执行普通导航
                    GNav.navigate(navController, route)
                }
            }
        }

        /**
         * 构建导航容器。
         *
         * GNavRouteRegistrarLoad.registerRoutes() 用于自动注册
         * 通过 @NavPage 注解生成的路由页面。
         */
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier,
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition,
        ) {
            GNavRouteRegistrarLoad.registerRoutes(
                this,
                navController,
                navAnalytics
            )
        }
    }
}

/**
 * 提供全局 [NavController]。
 *
 * 可在任意 Composable 中通过：
 * ```
 * val navController = LocalNavController.current
 * ```
 * 访问当前导航控制器。
 *
 * ⚠️ 若未在 [GNavCompose] 环境中使用，将抛出异常。
 */
val LocalNavController = staticCompositionLocalOf<NavController> {
    error("NavController not provided — please wrap your content with GNavCompose()")
}
