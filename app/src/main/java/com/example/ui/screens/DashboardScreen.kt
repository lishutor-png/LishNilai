package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Assessment
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentScore
import com.example.ui.components.AssessmentDialog
import com.example.ui.components.ClassDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.LishNilaiViewModel
import com.example.ui.viewmodel.MainTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: LishNilaiViewModel
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val allClasses by viewModel.allClasses.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val students by viewModel.classStudents.collectAsState()
    val assessments by viewModel.classAssessments.collectAsState()
    val currentScores by viewModel.currentScores.collectAsState()
    val remedialList by viewModel.remedialList.collectAsState()

    var showAddClassDialog by remember { mutableStateOf(false) }
    var showAddAssessmentDialog by remember { mutableStateOf(false) }
    var classDropdownExpanded by remember { mutableStateOf(false) }

    val activeClass = selectedClass ?: allClasses.firstOrNull()

    // Calculate class average from scored items
    val classAverage = remember(currentScores) {
        if (currentScores.isNotEmpty()) {
            val avg = currentScores.map { it.finalScore }.average()
            "%.1f".format(avg)
        } else {
            "0.0"
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (activeClass != null) {
                        showAddAssessmentDialog = true
                    } else {
                        showAddClassDialog = true
                    }
                },
                containerColor = TealPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("dashboard_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Penilaian")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
        ) {
            // === HEADER: Class Switcher & Info Banner ===
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardLight),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "KELAS AKTIF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary,
                                letterSpacing = 1.sp
                            )

                            // Switch Class Button
                            Box {
                                OutlinedButton(
                                    onClick = { classDropdownExpanded = true },
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = SurfaceBackground,
                                        contentColor = OnSurfaceLight
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("switch_class_dropdown_button")
                                ) {
                                    Text(
                                        activeClass?.name ?: "Pilih Kelas",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = OnSurfaceLight
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = OnSurfaceLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = classDropdownExpanded,
                                    onDismissRequest = { classDropdownExpanded = false }
                                ) {
                                    allClasses.forEach { c ->
                                        DropdownMenuItem(
                                            text = { Text("${c.name} - ${c.subject}", color = OnSurfaceLight, fontWeight = FontWeight.Medium) },
                                            onClick = {
                                                viewModel.selectClass(c.id)
                                                classDropdownExpanded = false
                                            }
                                        )
                                    }
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Add, contentDescription = null, tint = TealPrimary)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("Tambah Kelas Baru", color = TealPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        onClick = {
                                            classDropdownExpanded = false
                                            showAddClassDialog = true
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = activeClass?.name ?: "Belum ada kelas",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurfaceLight
                        )

                        Text(
                            text = "${activeClass?.subject ?: "Mata Pelajaran"} • TA ${activeClass?.academicYear ?: "2025/2026"} (${activeClass?.semester ?: "Ganjil"})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurfaceVariantLight
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = TealPrimaryContainer,
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, OutlineLight)
                            ) {
                                Text(
                                    text = "🎯 KKM Standar: ${activeClass?.defaultKkm ?: 75}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnTealPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // === NATURAL TONES SPOTLIGHT CARD: PENILAIAN BERJALAN ===
            if (assessments.isNotEmpty()) {
                val currentAssessment = assessments.first()
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCardMuted),
                        border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text(
                                        text = "PENILAIAN BERJALAN",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = OnSurfaceVariantLight
                                    )
                                    Text(
                                        text = currentAssessment.title,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurfaceLight
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight)
                                ) {
                                    Text(
                                        text = activeClass?.name ?: "Kelas",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = OnSurfaceLight,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.8f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight.copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("ASPEK PENILAIAN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariantLight)
                                        Text("${currentAssessment.aspectCount} dari 7 Terpilih", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
                                    }
                                }

                                Surface(
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.8f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight.copy(alpha = 0.6f))
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("RENTANG BOBOT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = OnSurfaceVariantLight)
                                        Text(if (currentAssessment.weightType.contains("10")) "1 - 10" else "1 - 5", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = OnSurfaceLight)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // === REMEDIAL WHATSAPP BANNER (NATURAL TONES PURPLE) ===
            if (remedialList.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = PurpleRemedialContainer),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PurpleRemedialBorder),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.People,
                                        contentDescription = null,
                                        tint = PurpleRemedialText,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "DAFTAR REMEDIAL (KKM: ${activeClass?.defaultKkm ?: 75})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PurpleRemedialText,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PurpleRemedialBorder),
                                    modifier = Modifier.clickable {
                                        activeClass?.let { cls ->
                                            viewModel.copyRemedialWhatsApp(context, cls, "Semua Penilaian")
                                        }
                                    }
                                ) {
                                    Text(
                                        text = "SALIN WA",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PurpleRemedialText,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Siswa di bawah KKM ${activeClass?.name ?: ""}:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PurpleRemedialText
                            )

                            remedialList.take(2).forEachIndexed { idx, item ->
                                Text(
                                    text = "${idx + 1}. ${item.studentName} (${item.score.toInt()}) - ${item.assessmentTitle}",
                                    fontSize = 11.sp,
                                    color = PurpleRemedialText.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
            }

            // === STATS GRID ===
            item {
                Text(
                    text = "Ringkasan Akademik",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Total Siswa",
                        value = "${students.size}",
                        unit = "Anak",
                        icon = Icons.Default.People,
                        color = TealPrimary,
                        containerColor = TealPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Penilaian",
                        value = "${assessments.size}",
                        unit = "Materi",
                        icon = Icons.Default.Assignment,
                        color = IndigoSecondary,
                        containerColor = IndigoSecondaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Rerata Nilai",
                        value = classAverage,
                        unit = "/ 100",
                        icon = Icons.Default.TrendingUp,
                        color = AmberTertiary,
                        containerColor = AmberTertiaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Di Bawah KKM",
                        value = "${remedialList.size}",
                        unit = "Kasus",
                        icon = Icons.Default.Warning,
                        color = RedError,
                        containerColor = RedErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // === QUICK ACTION SHORTCUTS ===
            item {
                Text(
                    text = "Aksi Cepat",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnSurfaceLight
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCardLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickActionRow(
                            icon = Icons.Default.EditNote,
                            title = "Mulai Menilai Siswa / Kelompok",
                            subtitle = "Penilaian 1-7 aspek dengan sistem pilih bobot cepat",
                            badgeColor = TealPrimaryContainer,
                            iconColor = TealPrimary,
                            onClick = { viewModel.setTab(MainTab.PENILAIAN) }
                        )
                        Divider()
                        QuickActionRow(
                            icon = Icons.Default.TableChart,
                            title = "Rekapitulasi Semester & Ekspor",
                            subtitle = "Lihat tabel nilai, cetak PDF & unduh Excel",
                            badgeColor = IndigoSecondaryContainer,
                            iconColor = IndigoSecondary,
                            onClick = { viewModel.setTab(MainTab.REKAP) }
                        )
                        Divider()
                        QuickActionRow(
                            icon = Icons.Default.Share,
                            title = "Daftar Siswa Remedial (WhatsApp)",
                            subtitle = "Salin format pesan WA untuk siswa di bawah KKM",
                            badgeColor = RedErrorContainer,
                            iconColor = RedError,
                            onClick = { viewModel.setTab(MainTab.REMEDIAL) }
                        )
                        Divider()
                        QuickActionRow(
                            icon = Icons.Default.GroupAdd,
                            title = "Kelola Siswa & Impor Data",
                            subtitle = "Impor CSV/Excel atau ketik cepat via format koma",
                            badgeColor = AmberTertiaryContainer,
                            iconColor = AmberTertiary,
                            onClick = { viewModel.setTab(MainTab.KELOLA) }
                        )
                    }
                }
            }

            // === RECENT ASSESSMENTS LIST ===
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daftar Penilaian (${assessments.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceLight
                    )
                    TextButton(
                        onClick = { viewModel.setTab(MainTab.PENILAIAN) }
                    ) {
                        Text("Lihat Semua", color = TealPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (assessments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.AssignmentLate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Belum Ada Penilaian",
                                fontWeight = FontWeight.Bold,
                                color = OnSurfaceLight
                            )
                            Text(
                                "Buat penilaian pertama untuk mulai menilai siswa atau kelompok.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showAddAssessmentDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buat Penilaian Baru")
                            }
                        }
                    }
                }
            } else {
                items(assessments) { assessment ->
                    AssessmentCardItem(
                        assessment = assessment,
                        onGradeClick = {
                            viewModel.selectAssessment(assessment.id)
                            viewModel.setTab(MainTab.PENILAIAN)
                        }
                    )
                }
            }
        }
    }

    // Dialogs
    if (showAddClassDialog) {
        ClassDialog(
            onDismiss = { showAddClassDialog = false },
            onConfirm = { name, subject, year, semester, kkm ->
                viewModel.addClass(name, subject, year, semester, kkm)
                showAddClassDialog = false
            }
        )
    }

    if (showAddAssessmentDialog && activeClass != null) {
        AssessmentDialog(
            classId = activeClass.id,
            onDismiss = { showAddAssessmentDialog = false },
            onConfirm = { newAssessment ->
                viewModel.saveAssessment(newAssessment, isNew = true)
                showAddAssessmentDialog = false
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurfaceLight
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurfaceLight)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = OutlineLight)
    }
}

@Composable
private fun AssessmentCardItem(
    assessment: Assessment,
    onGradeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (assessment.isGroupAssessment) IndigoSecondaryContainer else TealPrimaryContainer
                        ) {
                            Text(
                                text = if (assessment.isGroupAssessment) "Kelompok" else "Individu",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (assessment.isGroupAssessment) OnIndigoSecondaryContainer else OnTealPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = assessment.category,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = assessment.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = OnSurfaceLight
                    )

                    if (assessment.topic.isNotBlank()) {
                        Text(
                            text = "Materi: ${assessment.topic}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onGradeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Buka", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "• ${assessment.aspectCount} Aspek Penilaian",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• Bobot: ${assessment.weightType}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• KKM: ${assessment.kkm}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary
                )
            }
        }
    }
}
