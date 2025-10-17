package gll.pub.gnav.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [UserInfoManager]
 *
 * [zh-CN]
 * 用户信息管理类
 * 用于管理用户登录状态
 *
 * [en-US]
 * User information manager
 * Used to manage user login status
 */
object UserInfoManager {
    private val _isLogin = MutableStateFlow(false)
    val isLogin: StateFlow<Boolean> = _isLogin.asStateFlow()

    fun login() {
        _isLogin.value = true
    }

    fun logout() {
        _isLogin.value = false
    }
}