package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AssessmentDialog
import com.example.ui.components.QuickScoreRangeDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.LishNilaiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradingScreen(
    viewModel: LishNilaiViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val students by viewModel.classStudents.collectAsState()
    val assessments by viewModel.classAssessments.collectAsState()
    val groups by viewModel.classGroups.collectAsState()
    val selectedAssessment by viewModel.selectedAssessment.collectAsState()
    val currentScores by viewModel.currentScores.collectAsState()

    var showEditAssessmentDialog by remember { mutableStateOf(false) }
    var showNewAssessmentDialog by remember { mutableStateOf(false) }
    var showScoreRangeDialog by remember { mutableStateOf(false) }

    // State for Individual Grading Modal Sheet
    var gradingStudent by remember { mutableStateOf<Student?>(null) }

    // State for Group Grading Modal Sheet
    var gradingGroup by remember { mutableStateOf<StudentGroup?>(null) }
    var groupMembersMap by remember { mutableStateOf<Map<Long, List<Student>>>(emptyMap()) }

    var searchQuery by remember { mutableStateOf("") }

    // Map scores by studentId
    val scoresByStudent = remember(currentScores) {
        currentScores.associateBy { it.studentId }
    }

    // Load group members
    LaunchedEffect(groups, students) {
        val map = mutableMapOf<Long, List<Student>>()
        val studentMap = students.associateBy { it.id }
        groups.forEach { g ->
            val members = viewModel.getGroupMembers(g.id)
            map[g.id] = members.mapNotNull { studentMap[it.studentId] }
        }
        groupMembersMap = map
    }

    val activeAssessment = selectedAssessment ?: assessments.firstOrNull()

    Scaffold(
        floatingActionButton = {
            if (selectedClass != null) {
                ExtendedFloatingActionButton(
                    onClick = { showNewAssessmentDialog = true },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Penilaian Baru", fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("create_assessment_fab")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // === TOP: Assessment Selector Tabs ===
            Surface(
                color = SurfaceElevated,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Text(
                        text = "PILIH PENILAIAN / MATERI:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )

                    if (assessments.isEmpty()) {
                        Text(
                            text = "Belum ada penilaian untuk kelas ini.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(assessments) { a ->
                                val isSelected = a.id == activeAssessment?.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectAssessment(a.id) },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(a.title, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                            if (a.isGroupAssessment) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Surface(
                                                    shape = CircleShape,
                                                    color = if (isSelected) IndigoSecondary else IndigoSecondaryContainer,
                                                    modifier = Modifier.size(8.dp)
                                                ) {}
                                            }
                                        }
                                    },
                                    leadingIcon = if (isSelected) {
                                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            if (activeAssessment != null) {
                // Assessment Meta Info Card with Edit & Settings button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(16.dp),
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
                                    text = activeAssessment.title,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnSurfaceLight
                                )
                                if (activeAssessment.topic.isNotBlank()) {
                                    Text(
                                        text = "Topik: ${activeAssessment.topic}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showEditAssessmentDialog = true },
                                modifier = Modifier.testTag("edit_assessment_button")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Penilaian", tint = TealPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (activeAssessment.isGroupAssessment) IndigoSecondaryContainer else TealPrimaryContainer
                            ) {
                                Text(
                                    text = if (activeAssessment.isGroupAssessment) "👥 Mode Kelompok" else "👤 Mode Individu",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeAssessment.isGroupAssessment) OnIndigoSecondaryContainer else OnTealPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SurfaceElevated
                            ) {
                                Text(
                                    text = "${activeAssessment.aspectCount} Aspek • Bobot ${activeAssessment.weightType}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // Score Range Badge (Clickable to change range quickly)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = TealPrimaryContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showScoreRangeDialog = true }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "🎯 Rentang: ${activeAssessment.targetMinScore.toInt()} - ${activeAssessment.targetMaxScore.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnTealPrimaryContainer
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Ubah Rentang",
                                        tint = TealPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AmberTertiaryContainer
                            ) {
                                Text(
                                    text = "KKM: ${activeAssessment.kkm}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnAmberTertiaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama siswa...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        { IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Clear, contentDescription = null) } }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Mode Rendering: Group vs Individual
                if (activeAssessment.isGroupAssessment) {
                    // === GROUP SCORING VIEW ===
                    val filteredGroups = remember(groups, searchQuery) {
                        if (searchQuery.isBlank()) groups
                        else groups.filter { g ->
                            g.name.contains(searchQuery, ignoreCase = true) ||
                                (groupMembersMap[g.id]?.any { it.name.contains(searchQuery, ignoreCase = true) } == true)
                        }
                    }

                    if (groups.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Belum ada kelompok dibuat. Buka tab 'Kelola' untuk membuat kelompok.",
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
                        ) {
                            items(filteredGroups) { group ->
                                val members = groupMembersMap[group.id] ?: emptyList()
                                GroupScoringCard(
                                    group = group,
                                    members = members,
                                    assessment = activeAssessment,
                                    scoresByStudent = scoresByStudent,
                                    onGradeGroupClick = { gradingGroup = group },
                                    onBonusScoreChange = { student, bonus ->
                                        viewModel.updateStudentBonus(activeAssessment, student.id, bonus)
                                    }
                                )
                            }
                        }
                    }
                } else {
                    // === INDIVIDUAL SCORING VIEW ===
                    val filteredStudents = remember(students, searchQuery) {
                        if (searchQuery.isBlank()) students
                        else students.filter { it.name.contains(searchQuery, ignoreCase = true) || it.nis.contains(searchQuery) }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
                    ) {
                        items(filteredStudents) { student ->
                            val score = scoresByStudent[student.id]
                            StudentScoringCard(
                                student = student,
                                assessment = activeAssessment,
                                score = score,
                                onClick = { gradingStudent = student }
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PostAdd, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Belum Ada Penilaian", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "Silakan buat penilaian baru terlebih dahulu untuk mulai menilai.",
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showNewAssessmentDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Text("Buat Penilaian Sekarang")
                        }
                    }
                }
            }
        }
    }

    // === MODAL DIALOG: INDIVIDUAL FAST CHIPS SCORING ===
    if (gradingStudent != null && activeAssessment != null) {
        val student = gradingStudent!!
        val existingScore = scoresByStudent[student.id]

        IndividualScoreDialog(
            student = student,
            assessment = activeAssessment,
            initialScore = existingScore,
            onDismiss = { gradingStudent = null },
            onSave = { a1, a2, a3, a4, a5, a6, a7, bonus, notes ->
                viewModel.saveStudentScore(
                    assessment = activeAssessment,
                    studentId = student.id,
                    aspect1 = a1,
                    aspect2 = a2,
                    aspect3 = a3,
                    aspect4 = a4,
                    aspect5 = a5,
                    aspect6 = a6,
                    aspect7 = a7,
                    bonusScore = bonus,
                    notes = notes
                )
                gradingStudent = null
            }
        )
    }

    // === MODAL DIALOG: GROUP UNIFIED SCORING ===
    if (gradingGroup != null && activeAssessment != null) {
        val group = gradingGroup!!
        val members = groupMembersMap[group.id] ?: emptyList()

        GroupScoreDialog(
            group = group,
            members = members,
            assessment = activeAssessment,
            onDismiss = { gradingGroup = null },
            onSaveGroup = { a1, a2, a3, a4, a5, a6, a7, notes ->
                viewModel.saveGroupScore(
                    assessment = activeAssessment,
                    groupId = group.id,
                    groupName = group.name,
                    aspect1 = a1,
                    aspect2 = a2,
                    aspect3 = a3,
                    aspect4 = a4,
                    aspect5 = a5,
                    aspect6 = a6,
                    aspect7 = a7,
                    groupNotes = notes
                )
                gradingGroup = null
            }
        )
    }

    // Edit Assessment Dialog
    if (showEditAssessmentDialog && activeAssessment != null && selectedClass != null) {
        AssessmentDialog(
            classId = selectedClass!!.id,
            initialAssessment = activeAssessment,
            onDismiss = { showEditAssessmentDialog = false },
            onConfirm = { updated ->
                viewModel.saveAssessment(updated, isNew = false)
                showEditAssessmentDialog = false
            }
        )
    }

    // New Assessment Dialog
    if (showNewAssessmentDialog && selectedClass != null) {
        AssessmentDialog(
            classId = selectedClass!!.id,
            onDismiss = { showNewAssessmentDialog = false },
            onConfirm = { newAssessment ->
                viewModel.saveAssessment(newAssessment, isNew = true)
                showNewAssessmentDialog = false
            }
        )
    }

    // Quick Score Range Dialog
    if (showScoreRangeDialog && activeAssessment != null) {
        QuickScoreRangeDialog(
            assessment = activeAssessment,
            onDismiss = { showScoreRangeDialog = false },
            onConfirm = { minVal, maxVal ->
                viewModel.updateAssessmentScoreRange(activeAssessment, minVal, maxVal)
                showScoreRangeDialog = false
            }
        )
    }
}

// === COMPONENT: INDIVIDUAL STUDENT SCORING ROW CARD ===
@Composable
private fun StudentScoringCard(
    student: Student,
    assessment: Assessment,
    score: StudentScore?,
    onClick: () -> Unit
) {
    val isGraded = score != null
    val finalScore = score?.finalScore ?: 0.0
    val isUnderKkm = isGraded && finalScore < assessment.kkm

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("student_scoring_card_${student.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnderKkm) RedErrorContainer else SurfaceCardLight
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isUnderKkm) RedErrorBorder else OutlineLight.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (student.gender == "P") Color(0xFFFCE7F3) else IndigoSecondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = if (student.gender == "P") Color(0xFF9D174D) else OnIndigoSecondaryContainer,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = OnSurfaceLight
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (student.nis.isNotBlank()) {
                            Text("NIS: ${student.nis}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("• (${student.gender})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Score Badge
            if (isGraded) {
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isUnderKkm) RedError else GreenSuccess
                    ) {
                        Text(
                            text = "${finalScore.toInt()}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    if (isUnderKkm) {
                        Text(
                            text = "Di Bawah KKM (${assessment.kkm})",
                            fontSize = 10.sp,
                            color = RedError,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text(
                            text = "Tuntas",
                            fontSize = 10.sp,
                            color = GreenSuccess,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Beri Nilai", fontSize = 12.sp)
                }
            }
        }
    }
}

// === COMPONENT: GROUP SCORING CARD WITH BONUS ADJUSTMENTS ===
@Composable
private fun GroupScoringCard(
    group: StudentGroup,
    members: List<Student>,
    assessment: Assessment,
    scoresByStudent: Map<Long, StudentScore>,
    onGradeGroupClick: () -> Unit,
    onBonusScoreChange: (Student, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Group Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = IndigoSecondary
                    )
                    Text(
                        text = "${members.size} Anggota Tim",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onGradeGroupClick,
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("grade_group_button_${group.id}")
                ) {
                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nilai 1 Kelompok", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Anggota & Nilai Tambahan Individu:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceLight
            )

            Spacer(modifier = Modifier.height(6.dp))

            members.forEach { student ->
                val score = scoresByStudent[student.id]
                val currentBonus = score?.bonusScore ?: 0
                val finalScore = score?.finalScore ?: 0.0
                val isUnderKkm = score != null && finalScore < assessment.kkm

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(
                            color = if (isUnderKkm) RedErrorContainer.copy(alpha = 0.4f) else SurfaceElevated,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = student.name,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = OnSurfaceLight
                        )
                        if (score != null) {
                            Text(
                                text = "Nilai Akhir: ${finalScore.toInt()} (Bonus: +$currentBonus)",
                                fontSize = 11.sp,
                                color = if (isUnderKkm) RedError else GreenSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text("Belum dinilai", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Bonus Score Adjustment Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Bonus:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        listOf(0, 5, 10, 15).forEach { bonusVal ->
                            val isSelected = currentBonus == bonusVal
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) AmberTertiary else SurfaceCardLight,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onBonusScoreChange(student, bonusVal) }
                                    .border(0.5.dp, if (isSelected) AmberTertiary else OutlineLight, RoundedCornerShape(6.dp))
                            ) {
                                Text(
                                    text = if (bonusVal == 0) "0" else "+$bonusVal",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else OnSurfaceLight,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// === MODAL: INDIVIDUAL SCORING WITH 1-7 ASPECTS VIA CHIP SELECTOR (NO TYPING NEEDED) ===
@Composable
private fun IndividualScoreDialog(
    student: Student,
    assessment: Assessment,
    initialScore: StudentScore?,
    onDismiss: () -> Unit,
    onSave: (a1: Int, a2: Int, a3: Int, a4: Int, a5: Int, a6: Int, a7: Int, bonus: Int, notes: String) -> Unit
) {
    val defaultVal = assessment.minScore
    var a1 by remember { mutableStateOf(initialScore?.aspect1Score?.takeIf { it > 0 } ?: defaultVal) }
    var a2 by remember { mutableStateOf(initialScore?.aspect2Score?.takeIf { it > 0 } ?: defaultVal) }
    var a3 by remember { mutableStateOf(initialScore?.aspect3Score?.takeIf { it > 0 } ?: defaultVal) }
    var a4 by remember { mutableStateOf(initialScore?.aspect4Score?.takeIf { it > 0 } ?: defaultVal) }
    var a5 by remember { mutableStateOf(initialScore?.aspect5Score?.takeIf { it > 0 } ?: defaultVal) }
    var a6 by remember { mutableStateOf(initialScore?.aspect6Score?.takeIf { it > 0 } ?: defaultVal) }
    var a7 by remember { mutableStateOf(initialScore?.aspect7Score?.takeIf { it > 0 } ?: defaultVal) }
    var bonus by remember { mutableStateOf(initialScore?.bonusScore ?: 0) }
    var notes by remember { mutableStateOf(initialScore?.notes ?: "") }

    val aspectNames = listOf(
        assessment.aspect1Name,
        assessment.aspect2Name,
        assessment.aspect3Name,
        assessment.aspect4Name,
        assessment.aspect5Name,
        assessment.aspect6Name,
        assessment.aspect7Name
    )

    // Calculate real-time preview of 0-100 score based on targetMinScore and targetMaxScore
    val aspectScores = listOf(a1, a2, a3, a4, a5, a6, a7).take(assessment.aspectCount)
    val rawSum = aspectScores.sum()
    val minPossible = assessment.aspectCount * assessment.minScore
    val maxPossible = assessment.aspectCount * assessment.maxScore
    val baseScore = if (maxPossible == minPossible) {
        assessment.targetMaxScore
    } else {
        val ratio = ((rawSum - minPossible).toDouble() / (maxPossible - minPossible).toDouble()).coerceIn(0.0, 1.0)
        assessment.targetMinScore + ratio * (assessment.targetMaxScore - assessment.targetMinScore)
    }
    val previewScore = (baseScore + bonus).coerceIn(0.0, 100.0)
    val isUnderKkm = previewScore < assessment.kkm

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Beri Nilai Siswa", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "${student.name} • ${assessment.title}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Real-Time Score Gauge Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnderKkm) RedErrorContainer else GreenSuccessContainer
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Nilai Hasil Otomatis (0 - 100):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isUnderKkm) OnRedErrorContainer else OnGreenSuccessContainer
                            )
                            Text(
                                text = "${"%.1f".format(previewScore)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isUnderKkm) RedError else GreenSuccess
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isUnderKkm) RedError else GreenSuccess
                        ) {
                            Text(
                                text = if (isUnderKkm) "Di Bawah KKM (${assessment.kkm})" else "TUNTAS KKM",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Text(
                    "Pilih Bobot Tiap Aspek (${assessment.minScore} - ${assessment.maxScore}) • Rentang Nilai: ${assessment.targetMinScore.toInt()} - ${assessment.targetMaxScore.toInt()}:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                // Aspect 1
                if (assessment.aspectCount >= 1) {
                    AspectChipSelector(
                        name = "1. ${aspectNames[0]}",
                        selectedValue = a1,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a1 = it }
                    )
                }
                // Aspect 2
                if (assessment.aspectCount >= 2) {
                    AspectChipSelector(
                        name = "2. ${aspectNames[1]}",
                        selectedValue = a2,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a2 = it }
                    )
                }
                // Aspect 3
                if (assessment.aspectCount >= 3) {
                    AspectChipSelector(
                        name = "3. ${aspectNames[2]}",
                        selectedValue = a3,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a3 = it }
                    )
                }
                // Aspect 4
                if (assessment.aspectCount >= 4) {
                    AspectChipSelector(
                        name = "4. ${aspectNames[3]}",
                        selectedValue = a4,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a4 = it }
                    )
                }
                // Aspect 5
                if (assessment.aspectCount >= 5) {
                    AspectChipSelector(
                        name = "5. ${aspectNames[4]}",
                        selectedValue = a5,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a5 = it }
                    )
                }
                // Aspect 6
                if (assessment.aspectCount >= 6) {
                    AspectChipSelector(
                        name = "6. ${aspectNames[5]}",
                        selectedValue = a6,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a6 = it }
                    )
                }
                // Aspect 7
                if (assessment.aspectCount >= 7) {
                    AspectChipSelector(
                        name = "7. ${aspectNames[6]}",
                        selectedValue = a7,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a7 = it }
                    )
                }

                // Nilai Tambahan / Bonus
                Column {
                    Text("Nilai Tambahan / Bonus (Poin):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0, 2, 5, 10, 15).forEach { b ->
                            FilterChip(
                                selected = bonus == b,
                                onClick = { bonus = b },
                                label = { Text("+$b", fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Guru (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(a1, a2, a3, a4, a5, a6, a7, bonus, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                modifier = Modifier.testTag("confirm_individual_score_button")
            ) {
                Text("Simpan Nilai")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// === MODAL: GROUP UNIFIED SCORING DIALOG ===
@Composable
private fun GroupScoreDialog(
    group: StudentGroup,
    members: List<Student>,
    assessment: Assessment,
    onDismiss: () -> Unit,
    onSaveGroup: (a1: Int, a2: Int, a3: Int, a4: Int, a5: Int, a6: Int, a7: Int, notes: String) -> Unit
) {
    val defaultVal = assessment.minScore
    var a1 by remember { mutableStateOf(defaultVal) }
    var a2 by remember { mutableStateOf(defaultVal) }
    var a3 by remember { mutableStateOf(defaultVal) }
    var a4 by remember { mutableStateOf(defaultVal) }
    var a5 by remember { mutableStateOf(defaultVal) }
    var a6 by remember { mutableStateOf(defaultVal) }
    var a7 by remember { mutableStateOf(defaultVal) }
    var notes by remember { mutableStateOf("") }

    val aspectNames = listOf(
        assessment.aspect1Name,
        assessment.aspect2Name,
        assessment.aspect3Name,
        assessment.aspect4Name,
        assessment.aspect5Name,
        assessment.aspect6Name,
        assessment.aspect7Name
    )

    // Calculate score preview with target score range
    val aspectScores = listOf(a1, a2, a3, a4, a5, a6, a7).take(assessment.aspectCount)
    val rawSum = aspectScores.sum()
    val minPossible = assessment.aspectCount * assessment.minScore
    val maxPossible = assessment.aspectCount * assessment.maxScore
    val previewScore = if (maxPossible == minPossible) {
        assessment.targetMaxScore
    } else {
        val ratio = ((rawSum - minPossible).toDouble() / (maxPossible - minPossible).toDouble()).coerceIn(0.0, 1.0)
        (assessment.targetMinScore + ratio * (assessment.targetMaxScore - assessment.targetMinScore)).coerceIn(0.0, 100.0)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Nilai Kelompok Bersama", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "${group.name} (${members.size} Siswa)",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = IndigoSecondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Nilai ini akan otomatis diterapkan ke seluruh ${members.size} anggota tim:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnIndigoSecondaryContainer
                        )
                        Text(
                            members.joinToString(", ") { it.name },
                            fontSize = 11.sp,
                            color = OnIndigoSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Skor Terhitung: ${"%.1f".format(previewScore)} / 100",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = IndigoSecondary
                        )
                    }
                }

                Text(
                    "Pilih Bobot Tiap Aspek (${assessment.minScore} - ${assessment.maxScore}) • Rentang Nilai: ${assessment.targetMinScore.toInt()} - ${assessment.targetMaxScore.toInt()}:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                if (assessment.aspectCount >= 1) {
                    AspectChipSelector(
                        name = "1. ${aspectNames[0]}",
                        selectedValue = a1,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a1 = it }
                    )
                }
                if (assessment.aspectCount >= 2) {
                    AspectChipSelector(
                        name = "2. ${aspectNames[1]}",
                        selectedValue = a2,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a2 = it }
                    )
                }
                if (assessment.aspectCount >= 3) {
                    AspectChipSelector(
                        name = "3. ${aspectNames[2]}",
                        selectedValue = a3,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a3 = it }
                    )
                }
                if (assessment.aspectCount >= 4) {
                    AspectChipSelector(
                        name = "4. ${aspectNames[3]}",
                        selectedValue = a4,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a4 = it }
                    )
                }
                if (assessment.aspectCount >= 5) {
                    AspectChipSelector(
                        name = "5. ${aspectNames[4]}",
                        selectedValue = a5,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a5 = it }
                    )
                }
                if (assessment.aspectCount >= 6) {
                    AspectChipSelector(
                        name = "6. ${aspectNames[5]}",
                        selectedValue = a6,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a6 = it }
                    )
                }
                if (assessment.aspectCount >= 7) {
                    AspectChipSelector(
                        name = "7. ${aspectNames[6]}",
                        selectedValue = a7,
                        minVal = assessment.minScore,
                        maxVal = assessment.maxScore,
                        targetMin = assessment.targetMinScore,
                        targetMax = assessment.targetMaxScore,
                        onSelect = { a7 = it }
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Projek Kelompok") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveGroup(a1, a2, a3, a4, a5, a6, a7, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                modifier = Modifier.testTag("confirm_group_score_button")
            ) {
                Text("Terapkan ke Semua Anggota")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// === COMPONENT: ASPECT CHIP SELECTOR (NO TYPING!) ===
@Composable
private fun AspectChipSelector(
    name: String,
    selectedValue: Int,
    minVal: Int,
    maxVal: Int,
    targetMin: Double = 67.0,
    targetMax: Double = 90.0,
    onSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevated, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnSurfaceLight,
                modifier = Modifier.weight(1f)
            )

            // Current selection badge with converted score
            val currentConverted = if (maxVal == minVal) targetMax.toInt() else {
                (targetMin + ((selectedValue - minVal).toDouble() / (maxVal - minVal).toDouble()) * (targetMax - targetMin)).toInt()
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TealPrimary,
                modifier = Modifier.padding(start = 6.dp)
            ) {
                Text(
                    text = "Bobot $selectedValue → Nilai $currentConverted",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Chip row for options with auto-calculated score mapping
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            (minVal..maxVal).forEach { value ->
                val isSelected = selectedValue == value
                val valueConverted = if (maxVal == minVal) targetMax.toInt() else {
                    (targetMin + ((value - minVal).toDouble() / (maxVal - minVal).toDouble()) * (targetMax - targetMin)).toInt()
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) TealPrimary else SurfaceCardLight,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(value) }
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) TealPrimary else OutlineLight,
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(vertical = 5.dp, horizontal = 2.dp)
                    ) {
                        Text(
                            text = "$value",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else OnSurfaceLight
                        )
                        Text(
                            text = "($valueConverted)",
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
