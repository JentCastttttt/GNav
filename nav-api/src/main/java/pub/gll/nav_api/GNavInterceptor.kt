package pub.gll.nav_api

import androidx.navigation.NavController

/**
 * ==============================================
 * 🚦 GNavInterceptor
 * ==============================================
 *
 * 导航拦截器接口，用于在导航操作前执行自定义逻辑，
 * 决定是否允许跳转到目标页面。
 *
 * 典型用途：
 * 1. 登录拦截：未登录用户阻止进入特定页面
 * 2. 权限校验：根据角色或权限控制页面访问
 * 3. 埋点统计：在跳转前记录事件或分析数据
 *
 * ---
 * ### 使用方式
 *
 * ```kotlin
 * class LoginInterceptor : GNavInterceptor {
 *     override fun shouldIntercept(navController: NavController, route: String): Boolean {
 *         val isLoggedIn = checkLoginStatus()
 *         if (!isLoggedIn) {
 *             navController.navigate("login")
 *             return true // 阻止原页面跳转
 *         }
 *         return false // 允许跳转
 *     }
 * }
 * ```
 *
 * 在页面注解中注册拦截器：
 * ```kotlin
 * @NavPage(route = "home", interceptors = [LoginInterceptor::class])
 * @Composable
 * fun HomeScreen() { ... }
 * ```
 *
 * 通过在 `GNav` 内部统一调用拦截器，可实现全局页面访问控制和导航逻辑统一管理。
 */
interface GNavInterceptor {

    /**
     * 是否拦截当前导航请求
     *
     * @param navController 当前导航控制器，可用于执行跳转
     * @param route 目标路由路径
     * @return true 表示拦截（阻止跳转），false 表示允许跳转
     */
    fun shouldIntercept(navController: NavController, route: String): Boolean
}
