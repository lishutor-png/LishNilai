package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Assessment
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentScore
import com.example.ui.theme.*
import com.example.ui.viewmodel.LishNilaiViewModel

@Composable
fun RecapScreen(
    viewModel: LishNilaiViewModel
) {
    val context = LocalContext.current
    val selectedClass by viewModel.selectedClass.collectAsState()
    val students by viewModel.classStudents.collectAsState()
    val assessments by viewModel.classAssessments.collectAsState()
    val currentScores by viewModel.currentScores.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var customKkm by remember { mutableStateOf(selectedClass?.defaultKkm ?: 75) }
    var showKkmDialog by remember { mutableStateOf(false) }

    // Update customKkm when selectedClass changes
    LaunchedEffect(selectedClass) {
        selectedClass?.defaultKkm?.let { customKkm = it }
    }

    // Load all scores across all assessments for this class
    // We can collect all scores
    val allClassScoresMap = remember(students, assessments, currentScores) {
        // Collect mapping of Pair(assessmentId, studentId) -> StudentScore
        val map = mutableMapOf<Pair<Long, Long>, StudentScore>()
        currentScores.forEach { sc ->
            map[Pair(sc.assessmentId, sc.studentId)] = sc
        }
        map
    }

    // Filter students
    val filteredStudents = remember(students, searchQuery) {
        if (searchQuery.isBlank()) students
        else students.filter { it.name.contains(searchQuery, ignoreCase = true) || it.nis.contains(searchQuery) }
    }

    // Compute Class Stats
    val studentAverages = remember(students, assessments, currentScores) {
        students.map { student ->
            val scores = assessments.mapNotNull { a ->
                allClassScoresMap[Pair(a.id, student.id)]?.finalScore
            }
            if (scores.isNotEmpty()) scores.average() else 0.0
        }
    }

    val highestScore = remember(studentAverages) {
        if (studentAverages.isNotEmpty()) "%.1f".format(studentAverages.maxOrNull() ?: 0.0) else "-"
    }
    val lowestScore = remember(studentAverages) {
        if (studentAverages.isNotEmpty()) "%.1f".format(studentAverages.minOrNull() ?: 0.0) else "-"
    }
    val classAvg = remember(studentAverages) {
        if (studentAverages.isNotEmpty()) "%.1f".format(studentAverages.average()) else "-"
    }
    val passRate = remember(studentAverages, customKkm) {
        if (studentAverages.isNotEmpty()) {
            val passed = studentAverages.count { it >= customKkm }
            val pct = (passed.toDouble() / studentAverages.size.toDouble()) * 100.0
            "${pct.toInt()}%"
        } else "0%"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // === HEADER & ACTION BAR ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCardLight),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Rekapitulasi Semester",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TealPrimaryDark
                        )
                        Text(
                            text = "${selectedClass?.name ?: "Kelas"} • ${selectedClass?.subject ?: "Mata Pelajaran"}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // KKM Badge & Edit Trigger
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = AmberTertiaryContainer,
                        modifier = Modifier
                            .clickable { showKkmDialog = true }
                            .border(1.dp, AmberTertiary, RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = OnAmberTertiaryContainer, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "KKM: $customKkm",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = OnAmberTertiaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Export Buttons Row (Excel & PDF)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.exportPdf(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = RedError),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_pdf_button"),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ekspor PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    Button(
                        onClick = { viewModel.exportExcel(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("export_excel_button"),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.TableView, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ekspor Excel", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // === STATS SUMMARY BAR ===
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryChip(label = "Rerata", value = classAvg, color = TealPrimary, modifier = Modifier.weight(1f))
            SummaryChip(label = "Tertinggi", value = highestScore, color = GreenSuccess, modifier = Modifier.weight(1f))
            SummaryChip(label = "Terendah", value = lowestScore, color = RedError, modifier = Modifier.weight(1f))
            SummaryChip(label = "% Tuntas", value = passRate, color = IndigoSecondary, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari siswa...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // === STUDENTS RECAP LIST ===
        if (students.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Belum ada siswa di kelas ini.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredStudents) { student ->
                    StudentRecapCard(
                        student = student,
                        assessments = assessments,
                        scoresMap = allClassScoresMap,
                        kkmThreshold = customKkm
                    )
                }
            }
        }
    }

    // === KKM ADJUSTMENT DIALOG ===
    if (showKkmDialog) {
        var kkmInput by remember { mutableStateOf(customKkm.toString()) }
        AlertDialog(
            onDismissRequest = { showKkmDialog = false },
            title = { Text("Atur Nilai Standar KKM", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Nilai siswa yang berada di bawah angka KKM ini akan secara otomatis ditandai MERAH pada seluruh tabel dan laporan.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = kkmInput,
                        onValueChange = { if (it.all { c -> c.isDigit() }) kkmInput = it },
                        label = { Text("Nilai KKM (Misal: 75)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("kkm_dialog_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = kkmInput.toIntOrNull() ?: 75
                        customKkm = parsed
                        selectedClass?.let { cls ->
                            viewModel.updateClass(cls.copy(defaultKkm = parsed))
                        }
                        showKkmDialog = false
                    }
                ) {
                    Text("Terapkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKkmDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = SurfaceElevated
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

// === DETAILED STUDENT RECAP CARD ===
@Composable
private fun StudentRecapCard(
    student: Student,
    assessments: List<Assessment>,
    scoresMap: Map<Pair<Long, Long>, StudentScore>,
    kkmThreshold: Int
) {
    // Calculate student average and below KKM count
    val scoresList = assessments.map { a ->
        Pair(a, scoresMap[Pair(a.id, student.id)]?.finalScore)
    }

    val validScores = scoresList.mapNotNull { it.second }
    val average = if (validScores.isNotEmpty()) validScores.average() else 0.0

    val belowKkmItems = scoresList.filter { pair ->
        val sc = pair.second ?: 0.0
        sc < (pair.first.kkm) || sc < kkmThreshold
    }
    val isUnderKkm = average < kkmThreshold || belowKkmItems.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnderKkm) RedErrorContainer.copy(alpha = 0.35f) else SurfaceCardLight
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Student Name, NIS, Average Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (student.gender == "P") Color(0xFFFCE7F3) else IndigoSecondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.take(1).uppercase(),
                            fontWeight = FontWeight.Bold,
                            color = if (student.gender == "P") Color(0xFF9D174D) else OnIndigoSecondaryContainer,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = OnSurfaceLight
                        )
                        if (student.nis.isNotBlank()) {
                            Text(
                                text = "NIS: ${student.nis} (${student.gender})",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Average Score Pill
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isUnderKkm) RedError else GreenSuccess
                    ) {
                        Text(
                            text = "Rerata: ${"%.1f".format(average)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Assessment Columns Horizontal Carousel
            if (assessments.isEmpty()) {
                Text(
                    text = "Belum ada penilaian dibuat",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    scoresList.forEach { (assessment, score) ->
                        val scVal = score ?: 0.0
                        val isScored = score != null
                        val isScoreUnder = isScored && scVal < assessment.kkm

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                !isScored -> SurfaceElevated
                                isScoreUnder -> RedError
                                else -> GreenSuccessContainer
                            },
                            border = if (isScoreUnder) null else androidx.compose.foundation.BorderStroke(0.5.dp, OutlineLight)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (assessment.title.length > 10) assessment.title.take(9) + ".." else assessment.title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isScoreUnder) Color.White else OnSurfaceLight
                                )
                                Text(
                                    text = if (isScored) "${scVal.toInt()}" else "-",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when {
                                        !isScored -> MaterialTheme.colorScheme.onSurfaceVariant
                                        isScoreUnder -> Color.White
                                        else -> GreenSuccess
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (belowKkmItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = RedError, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${belowKkmItems.size} Penilaian di bawah KKM (Perlu Remedial)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RedError
                    )
                }
            }
        }
    }
}
