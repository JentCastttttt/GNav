package gll.pub.gnav.page.vip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import gll.pub.gnav.interceptor.NoLoginNavInterceptor
import pub.gll.nav_annotations.NavPage

/**
 * [VipPage]
 *
 */
@Composable
@NavPage(interceptors =[NoLoginNavInterceptor::class] )
fun VipPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "VipPage")
    }
}