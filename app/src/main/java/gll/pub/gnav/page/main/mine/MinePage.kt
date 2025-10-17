package gll.pub.gnav.page.main.mine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import gll.pub.gnav.R
import gll.pub.gnav.manager.UserInfoManager
import pub.gll.generated.goLogin
import pub.gll.generated.goVip
import pub.gll.nav.LocalNavController

@Composable
fun MinePage() {
    val navController = LocalNavController.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "MinePage")
        Button(onClick = {
            navController.goLogin()
        }) {
            Text(text = stringResource(R.string.go_login_page))
        }
        Button(onClick = {
            UserInfoManager.logout()
        }) {
            Text(text = stringResource(R.string.logout))
        }
        Button(onClick = {
            navController.goVip()
        }) {
            Text(text = stringResource(R.string.go_vip_page))
        }
    }
}