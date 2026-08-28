package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import com.example.ui.components.ClassDialog
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
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isCurrentDark = isDarkMode ?: systemDark

    val allClasses by viewModel.allClasses.collectAsState()
    var topBarClassDropdownExpanded by remember { mutableStateOf(false) }
    var showTopBarAddClassDialog by remember { mutableStateOf(false) }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo_checklist_1787747912802),
                                    contentDescription = "Logo",
                                    modifier = Modifier.padding(3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "LishNilai",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                // Clickable Class Switcher Badge
                                Box {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { topBarClassDropdownExpanded = true }
                                            .padding(vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = selectedClass?.name ?: "Pilih Kelas",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Ganti Kelas",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = topBarClassDropdownExpanded,
                                        onDismissRequest = { topBarClassDropdownExpanded = false }
                                    ) {
                                        Text(
                                            "Pilih Kelas Aktif:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                        )
                                        allClasses.forEach { c ->
                                             val isCurrent = c.id == selectedClass?.id
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Column {
                                                            Text(
                                                                c.name,
                                                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                c.subject,
                                                                fontSize = 11.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                        if (isCurrent) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = MaterialTheme.colorScheme.primary,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.selectClass(c.id)
                                                    topBarClassDropdownExpanded = false
                                                }
                                            )
                                        }
                                        HorizontalDivider()
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Tambah Kelas Baru", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                }
                                            },
                                            onClick = {
                                                topBarClassDropdownExpanded = false
                                                showTopBarAddClassDialog = true
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    actions = {
                        // Dark / Light Theme Toggle Button
                        IconButton(
                            onClick = { viewModel.toggleDarkMode(systemDark) },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isCurrentDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = if (isCurrentDark) "Mode Terang" else "Mode Gelap",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Kunci Aplikasi",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == MainTab.DASHBOARD,
                        onClick = { viewModel.setTab(MainTab.DASHBOARD) },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dasbor") },
                        label = { Text("Dasbor", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.PENILAIAN,
                        onClick = { viewModel.setTab(MainTab.PENILAIAN) },
                        icon = { Icon(Icons.Default.EditNote, contentDescription = "Penilaian") },
                        label = { Text("Penilaian", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.PENILAIAN) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("tab_penilaian")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.REKAP,
                        onClick = { viewModel.setTab(MainTab.REKAP) },
                        icon = { Icon(Icons.Default.TableChart, contentDescription = "Rekap") },
                        label = { Text("Rekap", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.REKAP) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
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
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ) {
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
                            selectedIconColor = MaterialTheme.colorScheme.error,
                            selectedTextColor = MaterialTheme.colorScheme.error,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.testTag("tab_remedial")
                    )

                    NavigationBarItem(
                        selected = currentTab == MainTab.KELOLA,
                        onClick = { viewModel.setTab(MainTab.KELOLA) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Kelola") },
                        label = { Text("Kelola", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.KELOLA) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
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

        if (showTopBarAddClassDialog) {
            ClassDialog(
                onDismiss = { showTopBarAddClassDialog = false },
                onConfirm = { name, subject, year, semester, kkm ->
                    viewModel.addClass(name, subject, year, semester, kkm)
                    showTopBarAddClassDialog = false
                }
            )
        }
    }
}
