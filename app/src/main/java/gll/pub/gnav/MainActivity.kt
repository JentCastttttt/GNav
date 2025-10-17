package gll.pub.gnav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import gll.pub.gnav.ui.theme.GNavTheme
import pub.gll.generated.getMainRoute
import pub.gll.nav.GNavCompose
import pub.gll.nav_api.NavAnalytics

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GNavTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    /**
                     * TODO
                     * [zh-CN]
                     * 1.首先添加 [GNavCompose]
                     * 2.添加 ComposePage 示例
                     * [gll.pub.gnav.page.login.LoginPage] 带拦截器
                     * [gll.pub.gnav.page.vip.VipPage] 带拦截器
                     * [gll.pub.gnav.page.detail.DetailPage] 带参数
                     * [gll.pub.gnav.page.setting.SettingPage] 普通
                     * 3.执行 build
                     * 4.使用
                     * val navController = LocalNavController.current 获取navController对象
                     * navController.goLogin(),
                     * navController.goVip(),
                     * navController.goDetail(100, "DetailTitle", "DetailInfo"),
                     * navController.goSetting()
                     * 或者
                     * GNav.navigate(getLoginRoute())
                     * GNav.navigate(getVipRoute())
                     * GNav.navigate(getDetailRoute(100, "DetailTitle", "DetailInfo"))
                     * GNav.navigate(getSettingRoute())
                     * 进行页面跳转
                     *
                     * 5.请执行 GNav.popBackStack()或者 navController.popBackStack() 返回上一页
                     *
                     *
                     * [en-US]
                     * 1.first add [GNavCompose]
                     * 2.add ComposePage example
                     * [gll.pub.gnav.page.login.LoginPage] Equipped with interceptors
                     * [gll.pub.gnav.page.vip.VipPage] Equipped with interceptors
                     * [gll.pub.gnav.page.detail.DetailPage] with parameters
                     * [gll.pub.gnav.page.setting.SettingPage] ordinary
                     * 3.execute build
                     * 4.use
                     * val navController = LocalNavController.current to get navController object
                     * navController.goLogin(),
                     * navController.goVip(),
                     * navController.goDetail(),
                     * navController.goSetting()
                     * or
                     * GNav.navigate(getLoginRoute())
                     * GNav.navigate(getVipRoute())
                     * GNav.navigate(getDetailRoute(100, "DetailTitle", "DetailInfo"))
                     * GNav.navigate(getSettingRoute())
                     *
                     * Please execute GNav.popBackStack() to return to the previous page
                     *
                     */
                    GNavCompose(
                        modifier = Modifier.padding(innerPadding),//[zh-CN]可选 [en-US]Optional
                        startDestination = getMainRoute(),
                        animationDuration = 300,// [zh-CN]可选 [en-US]Optional
                        navAnalytics = object :NavAnalytics{// [zh-CN]可选 [en-US]Optional
                            override fun onPageEnter(
                                currentRoute: String,
                                previousRoute: String?,
                                params: Map<String, Any?>
                            ) {
                                println("onPageEnter: $currentRoute, previousRoute: $previousRoute, params: $params")
                            }
                            override fun onPageExit(currentRoute: String) {
                                println("onPageExit: $currentRoute")

                            }
                        })
                }
            }
        }
    }
}