package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LishNilaiViewModel
import com.example.ui.viewmodel.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: LishNilaiViewModel = viewModel()
) {
    val isAuthenticated by viewModel.isAuthenticated.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val remedialList by viewModel.remedialList.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg.text,
                duration = SnackbarDuration.Short
            )
            viewModel.clearUiMessage()
        }
    }

    if (!isAuthenticated) {
        LoginScreen(
            onLoginSuccess = { viewModel.authenticate(true) }
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                    TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                color = SurfaceBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo_checklist_1787747912802),
                                    contentDescription = "Logo",
                                    modifier = Modifier.padding(3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "LishNilai",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = OnSurfaceLight
                                )
                                Text(
                                    text = selectedClass?.name ?: "Pilih Kelas",
                                    fontSize = 11.sp,
                                    color = OnSurfaceVariantLight,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Kunci Aplikasi",
                                tint = OnSurfaceLight
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = OnSurfaceLight,
                        actionIconContentColor = OnSurfaceLight
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 4.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.DASHBOARD,
                        onClick = { viewModel.setTab(MainTab.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dasbor") },
                        label = { Text("Dasbor", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            unselectedIconColor = OnSurfaceVariantLight,
                            unselectedTextColor = OnSurfaceVariantLight,
                            indicatorColor = TealPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.PENILAIAN,
                        onClick = { viewModel.setTab(MainTab.PENILAIAN) },
                        icon = { Icon(Icons.Default.EditNote, contentDescription = "Penilaian") },
                        label = { Text("Penilaian", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.PENILAIAN) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            unselectedIconColor = OnSurfaceVariantLight,
                            unselectedTextColor = OnSurfaceVariantLight,
                            indicatorColor = TealPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tab_penilaian")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.REKAP,
                        onClick = { viewModel.setTab(MainTab.REKAP) },
                        icon = { Icon(Icons.Default.TableChart, contentDescription = "Rekap") },
                        label = { Text("Rekap", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.REKAP) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            unselectedIconColor = OnSurfaceVariantLight,
                            unselectedTextColor = OnSurfaceVariantLight,
                            indicatorColor = TealPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tab_rekap")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.REMEDIAL,
                        onClick = { viewModel.setTab(MainTab.REMEDIAL) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (remedialList.isNotEmpty()) {
                                        Badge(containerColor = RedError, contentColor = Color.White) {
                                            Text("${remedialList.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Warning, contentDescription = "Remedial")
                            }
                        },
                        label = { Text("Di Bawah KKM", fontSize = 10.sp, fontWeight = if (currentTab == MainTab.REMEDIAL) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RedError,
                            selectedTextColor = RedError,
                            unselectedIconColor = OnSurfaceVariantLight,
                            unselectedTextColor = OnSurfaceVariantLight,
                            indicatorColor = RedErrorContainer
                        ),
                        modifier = Modifier.testTag("tab_remedial")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.KELOLA,
                        onClick = { viewModel.setTab(MainTab.KELOLA) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Kelola") },
                        label = { Text("Kelola", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.KELOLA) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            unselectedIconColor = OnSurfaceVariantLight,
                            unselectedTextColor = OnSurfaceVariantLight,
                            indicatorColor = TealPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tab_kelola")
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Crossfade(targetState = currentTab, label = "tab_transition") { tab ->
                    when (tab) {
                        MainTab.DASHBOARD -> DashboardScreen(viewModel)
                        MainTab.PENILAIAN -> GradingScreen(viewModel)
                        MainTab.REKAP -> RecapScreen(viewModel)
                        MainTab.REMEDIAL -> RemedialScreen(viewModel)
                        MainTab.KELOLA -> ManagementScreen(viewModel)
                    }
                }
            }
        }
    }
}
