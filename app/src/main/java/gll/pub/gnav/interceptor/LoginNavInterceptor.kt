package gll.pub.gnav.interceptor

import android.widget.Toast
import androidx.navigation.NavController
import gll.pub.gnav.R
import gll.pub.gnav.manager.UserInfoManager
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pub.gll.nav_api.GNavInterceptor

/**
 * [LoginNavInterceptor]
 *
 * [zh-CN]
 * 登录拦截器
 * 如果用户已登录，则不允许导航，否则允许导航
 *
 * [en-US]
 * Login interceptor
 * If the user is logged in, disallow navigation; otherwise, allow navigation.
 */
class LoginNavInterceptor: GNavInterceptor {
    var isLogin = false
    init {
        MainScope().launch {
            UserInfoManager.isLogin.collect {
                isLogin = it
            }
        }
    }
    override fun shouldIntercept(navController: NavController, route: String): Boolean {
        if(isLogin){
            Toast.makeText(navController.context, navController.context.getString(R.string.logged_toast), Toast.LENGTH_SHORT).show()
        }
        return isLogin
    }
}