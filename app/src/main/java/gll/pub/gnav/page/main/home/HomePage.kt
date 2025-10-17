package gll.pub.gnav.page.main.home

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
import pub.gll.generated.goDetail
import pub.gll.generated.goSetting
import pub.gll.nav.LocalNavController

@Composable
fun HomePage() {
    val navController = LocalNavController.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "HomePage")
        Button(onClick = {
            navController.goDetail(100, "DetailTitle", "DetailInfo")
        }) {
            Text(text = stringResource(R.string.go_detail_page))
        }

        Button(onClick = {
            navController.goSetting()
        }) {
            Text(text = stringResource(R.string.go_setting_page))
        }

    }
}
