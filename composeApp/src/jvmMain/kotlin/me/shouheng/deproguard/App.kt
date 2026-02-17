package me.shouheng.deproguard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import deproguard.composeapp.generated.resources.Res
import deproguard.composeapp.generated.resources.app_name
import deproguard.composeapp.generated.resources.ic_logo
import deproguard.composeapp.generated.resources.tab_home_name
import deproguard.composeapp.generated.resources.tab_proguard_name
import me.shouheng.deproguard.data.Event.Companion.EVENT_NAME_TO_PAGE
import me.shouheng.deproguard.data.HomeTabItem
import me.shouheng.deproguard.data.ROUTE_HOME
import me.shouheng.deproguard.data.ROUTE_PROGUARD
import me.shouheng.deproguard.manager.MessageManager
import me.shouheng.deproguard.manager.ThemeManger
import me.shouheng.deproguard.ui.HomePage
import me.shouheng.deproguard.ui.ProguardPage
import me.shouheng.deproguard.ui.widget.CustomSnackbarHost
import me.shouheng.deproguard.ui.widget.showMessage
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
@Preview
fun App() {
    val snackBarHostState = remember { SnackbarHostState() }
    val navController = rememberNavController()
    val stateHolder = rememberSaveableStateHolder()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route ?: ROUTE_HOME

    val tabs = listOf(
        HomeTabItem(Res.string.tab_home_name, Icons.Default.Home, ROUTE_HOME, { HomePage() }),
        HomeTabItem(Res.string.tab_proguard_name, Icons.Default.EnhancedEncryption, ROUTE_PROGUARD, { ProguardPage() }),
    )

    // 初始化
    LaunchedEffect(Unit) {
        MessageManager.message.collect { snackBarHostState.showMessage(it) }
    }
    // 监听事件
    LaunchedEffect(Unit) {
        MessageManager.event.collect {
            if (it?.name == EVENT_NAME_TO_PAGE) {
                val path = it.data as? String ?: return@collect
                navController.navigate(path) {
                    launchSingleTop = true
                    restoreState = true
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                }
            }
        }
    }

    val colorSchema = ThemeManger.getTheme()
    MaterialTheme(colorScheme = colorSchema) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { CustomSnackbarHost(snackBarHostState) },
            containerColor = colorSchema.background
        ) { paddingValues ->
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(colorSchema.surface)
                        .verticalScroll(rememberScrollState())
                ) {
                    Image(
                        painter = painterResource(Res.drawable.ic_logo),
                        modifier = Modifier.size(60.dp).padding(top = 30.dp),
                        contentDescription = null
                    )
                    Text(stringResource(Res.string.app_name), fontSize = 13.sp)

                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(top = 30.dp),
                        contentColor = colorSchema.primary
                    ) {
                        tabs.forEach { item ->
                            NavigationRailItem(
                                icon = { Icon(item.icon, contentDescription = null) },
                                label = { Text(stringResource(item.title)) },
                                selected = currentRoute == item.route,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            launchSingleTop = true
                                            restoreState = true
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.padding(bottom = 15.dp),
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = colorSchema.primary,
                                    unselectedIconColor = colorSchema.onSurface,
                                    selectedTextColor = colorSchema.primary,
                                    unselectedTextColor = colorSchema.onSurface
                                )
                            )
                        }
                    }
                }
                Divider(modifier = Modifier.width(0.5.dp).fillMaxHeight())
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(paddingValues)
                ) {
                    NavHost(navController, startDestination = ROUTE_HOME) {
                        tabs.forEach { tab ->
                            composable(tab.route) {
                                stateHolder.SaveableStateProvider(tab.route, tab.content)
                            }
                        }
                    }
                }
            }
        }
    }
}