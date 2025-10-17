package gll.pub.gnav.interceptor

import androidx.navigation.NavController
import gll.pub.gnav.manager.UserInfoManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pub.gll.generated.goLogin
import pub.gll.nav.GNav
import pub.gll.nav_api.GNavInterceptor

/**
 * [NoLoginNavInterceptor]
 *
 * [zh-CN]
 * 未登录拦截器
 * 如果用户未登录，则不允许导航，否则允许导航
 *
 * [en-US]
 * Unlogged-in interceptor
 * If the user is not logged in, navigation is not allowed; otherwise, navigation is allowed.
 */
class NoLoginNavInterceptor: GNavInterceptor {
    var isLogin = false
    //登录成功后需要跳转的路由
    //如果为null，则默认不跳转
    var nextRoute: String? = null

    init {
        MainScope().launch {
            UserInfoManager.isLogin.collect {
                println("isLogin: $it")
                isLogin = it
                if (isLogin && nextRoute != null){
                    GNav.navigate(nextRoute!!)
                    nextRoute = null
                }
            }
        }
    }
    override fun shouldIntercept(navController: NavController, route: String): Boolean {
        if (!isLogin){
            nextRoute = route
            navController.goLogin()
            return true
        }
        return false
    }
}