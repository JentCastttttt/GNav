package pub.gll.nav

import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import pub.gll.nav_api.GNavInterceptor
import java.util.concurrent.ConcurrentHashMap

object GNav {

    private val scope: CoroutineScope = MainScope()

    // 使用线程安全 Map
    private val interceptors = ConcurrentHashMap<String, GNavInterceptor>()

    /**
     * 添加路由拦截器
     */
    fun addInterceptor(route: String, interceptor: GNavInterceptor) {
        interceptors[route] = interceptor
    }

    /**
     * 通过 FlowBus 异步导航
     */
    fun navigate(route: String) {
        GFlowBus.with<String>(GNavEventKey.KEY_G_NAV_EVENT)
            .post(scope, route)
    }

    /**
     * 异步返回上一级
     */
    fun popBackStack() {
        GFlowBus.with<String>(GNavEventKey.KEY_G_NAV_EVENT)
            .post(scope, "")
    }

    /**
     * 挂起版本（可在协程内使用）
     */
    suspend fun navigateSuspend(route: String) {
        GFlowBus.with<String>(GNavEventKey.KEY_G_NAV_EVENT)
            .post(route)
    }
 /**
     * 挂起版本（可在协程内使用）
     */
    suspend fun popBackStackSuspend() {
        GFlowBus.with<String>(GNavEventKey.KEY_G_NAV_EVENT)
            .post("")
    }

    /**
     * 真正执行 NavController 导航
     * 自动执行拦截器检查
     */
    fun navigate(navController: NavController, route: String) {
        val interceptor = interceptors[route]
        if (interceptor?.shouldIntercept(navController, route) == true) {
            return
        }
        navController.navigate(route)
    }
}
