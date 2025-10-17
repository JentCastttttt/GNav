package gll.pub.gnav.page.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import gll.pub.gnav.R
import gll.pub.gnav.page.main.home.HomePage
import gll.pub.gnav.page.main.message.MessagePage
import gll.pub.gnav.page.main.mine.MinePage
import kotlinx.coroutines.launch
import pub.gll.nav_annotations.NavPage

@Composable
@NavPage
fun MainPage() {
    val state = rememberPagerState {
        3
    }
    Column (modifier = Modifier.fillMaxSize()){
        HorizontalPager(state,modifier = Modifier.weight(1f),) { index ->
            when(index){
                0 -> HomePage()
                1 -> MessagePage()
                2 -> MinePage()
            }
        }
        MainPageBottom(state)
    }
}

@Composable
fun MainPageBottom(state: PagerState) {
    val coroutineScope = rememberCoroutineScope()
    Row(modifier = Modifier
        .fillMaxWidth()
        .height(50.dp)){
        MainPageBottomItem(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.main_home),
            icon = Icons.Default.Home,
            isSelect = state.currentPage == 0
        ){
            coroutineScope.launch {
                state.animateScrollToPage(0)
            }
        }
        MainPageBottomItem(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.main_message),
            icon = Icons.Default.MailOutline,
            isSelect = state.currentPage == 1
        ){
            coroutineScope.launch {
                state.animateScrollToPage(1)
            }
        }
        MainPageBottomItem(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.main_mine),
            icon = Icons.Default.Person,
            isSelect = state.currentPage == 2
        ){
            coroutineScope.launch {
                state.animateScrollToPage(2)
            }
        }
    }
}
@Composable
fun MainPageBottomItem(modifier: Modifier = Modifier,
                       title: String,
                       icon:ImageVector,
                       isSelect: Boolean,onClick: () -> Unit) {
    Column(modifier = modifier
        .fillMaxSize()
        .clickable(onClick = {
            onClick()
        }),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center){
        Icon(imageVector = icon, contentDescription = null, tint = if (isSelect) Color.Red else Color.Black)
        Text(text = title, color = if (isSelect) Color.Red else Color.Black)
    }
}