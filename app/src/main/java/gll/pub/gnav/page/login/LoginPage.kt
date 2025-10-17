package gll.pub.gnav.page.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import gll.pub.gnav.interceptor.LoginNavInterceptor
import gll.pub.gnav.manager.UserInfoManager
import pub.gll.nav.GNav
import pub.gll.nav_annotations.NavPage

@Composable
@NavPage(interceptors = [LoginNavInterceptor::class])
fun LoginPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "LoginPage")
        Button(onClick = {
            UserInfoManager.login()
            GNav.popBackStack()
        }) {
            Text(text = "Login")
        }
    }
}
