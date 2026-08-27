package com.example.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
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
import com.example.data.model.Assessment
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.ui.theme.*

// === DIALOG ADD / EDIT CLASS ===
@Composable
fun ClassDialog(
    initialClass: SchoolClass? = null,
    onDismiss: () -> Unit,
    onConfirm: (name: String, subject: String, year: String, semester: String, kkm: Int) -> Unit
) {
    var name by remember { mutableStateOf(initialClass?.name ?: "") }
    var subject by remember { mutableStateOf(initialClass?.subject ?: "Pemrograman Web") }
    var academicYear by remember { mutableStateOf(initialClass?.academicYear ?: "2025/2026") }
    var semester by remember { mutableStateOf(initialClass?.semester ?: "Ganjil") }
    var kkmText by remember { mutableStateOf(initialClass?.defaultKkm?.toString() ?: "75") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialClass == null) "Tambah Kelas Baru" else "Edit Data Kelas",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Kelas (Contoh: X RPL 1)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Mata Pelajaran") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_subject_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("Tahun Ajaran") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = semester,
                        onValueChange = { semester = it },
                        label = { Text("Semester") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = kkmText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) kkmText = it },
                    label = { Text("Nilai Standar KKM (Contoh: 75)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("class_kkm_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val kkm = kkmText.toIntOrNull() ?: 75
                        onConfirm(name, subject, academicYear, semester, kkm)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_class_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// === DIALOG ADD / EDIT ASSESSMENT (1 - 7 ASPECTS, WEIGHTS 1-5 OR 1-10) ===
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentDialog(
    classId: Long,
    initialAssessment: Assessment? = null,
    onDismiss: () -> Unit,
    onConfirm: (Assessment) -> Unit
) {
    var title by remember { mutableStateOf(initialAssessment?.title ?: "") }
    var topic by remember { mutableStateOf(initialAssessment?.topic ?: "") }
    var category by remember { mutableStateOf(initialAssessment?.category ?: "Tugas") }
    var isGroup by remember { mutableStateOf(initialAssessment?.isGroupAssessment ?: false) }
    var kkmText by remember { mutableStateOf(initialAssessment?.kkm?.toString() ?: "75") }

    var targetMinScoreText by remember { mutableStateOf(initialAssessment?.targetMinScore?.toInt()?.toString() ?: "67") }
    var targetMaxScoreText by remember { mutableStateOf(initialAssessment?.targetMaxScore?.toInt()?.toString() ?: "90") }

    var weightType by remember { mutableStateOf(initialAssessment?.weightType ?: "1-5") } // "1-5" or "1-10"
    var aspectCount by remember { mutableStateOf(initialAssessment?.aspectCount ?: 3) } // 1 to 7

    var aspect1 by remember { mutableStateOf(initialAssessment?.aspect1Name ?: "Pemahaman Konsep") }
    var aspect2 by remember { mutableStateOf(initialAssessment?.aspect2Name ?: "Keterampilan / Kerapian") }
    var aspect3 by remember { mutableStateOf(initialAssessment?.aspect3Name ?: "Ketepatan Waktu") }
    var aspect4 by remember { mutableStateOf(initialAssessment?.aspect4Name ?: "Kerjasama Tim") }
    var aspect5 by remember { mutableStateOf(initialAssessment?.aspect5Name ?: "Presentasi & Komunikasi") }
    var aspect6 by remember { mutableStateOf(initialAssessment?.aspect6Name ?: "Kreativitas & Inovasi") }
    var aspect7 by remember { mutableStateOf(initialAssessment?.aspect7Name ?: "Kemandirian") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialAssessment == null) "Buat Penilaian Baru" else "Edit Penilaian",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Penilaian (Contoh: UH 1 / Projek 1)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("assessment_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Materi / Topik Pembelajaran") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Category & Group Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val categories = listOf("Tugas", "Ulangan", "Proyek", "Praktik")
                    var expandedCategory by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expandedCategory,
                        onExpandedChange = { expandedCategory = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kategori") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategory,
                            onDismissRequest = { expandedCategory = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        expandedCategory = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = kkmText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) kkmText = it },
                        label = { Text("KKM") },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true
                    )
                }

                // Group Assessment Switch
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGroup) IndigoSecondaryContainer else SurfaceElevated
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Penilaian Kelompok",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Nilai 1 untuk semua anak dalam tim + bonus individu",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isGroup,
                            onCheckedChange = { isGroup = it },
                            modifier = Modifier.testTag("group_assessment_switch")
                        )
                    }
                }

                // Rentang Nilai Kustom (Min & Max - e.g. 67 s/d 90)
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "🎯 Rentang Nilai Otomatis (Skor Min & Max):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = OnSurfaceLight
                        )
                        Text(
                            "Bobot terendah (1) otomatis jadi skor minimum, dan bobot tertinggi (${if (weightType == "1-10") "10" else "5"}) otomatis jadi skor maksimum.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = targetMinScoreText,
                                onValueChange = { if (it.all { c -> c.isDigit() }) targetMinScoreText = it },
                                label = { Text("Nilai Min (Cth: 67)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = targetMaxScoreText,
                                onValueChange = { if (it.all { c -> c.isDigit() }) targetMaxScoreText = it },
                                label = { Text("Nilai Max (Cth: 90)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        // Presets
                        Text("Pilihan Cepat Rentang:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("67-90" to (67 to 90), "70-95" to (70 to 95), "60-100" to (60 to 100), "0-100" to (0 to 100)).forEach { (label, range) ->
                                val isSelected = targetMinScoreText == range.first.toString() && targetMaxScoreText == range.second.toString()
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) TealPrimary else SurfaceCardLight,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            targetMinScoreText = range.first.toString()
                                            targetMaxScoreText = range.second.toString()
                                        }
                                        .border(0.5.dp, if (isSelected) TealPrimary else OutlineLight, RoundedCornerShape(8.dp))
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else OnSurfaceLight,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Weight System (Bobot 1-5 atau 1-10)
                Text(
                    "Sistem Bobot & Skala Penilaian:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = weightType == "1-5",
                        onClick = { weightType = "1-5" },
                        label = { Text("Skala Bobot 1 - 5") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = weightType == "1-10",
                        onClick = { weightType = "1-10" },
                        label = { Text("Skala Bobot 1 - 10") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Number of Aspects (1 to 7)
                Text(
                    "Jumlah Aspek Penilaian (1 - 7 Aspek): $aspectCount",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    (1..7).forEach { num ->
                        FilterChip(
                            selected = aspectCount == num,
                            onClick = { aspectCount = num },
                            label = { Text("$num", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                // Dynamic Aspect Names
                Text(
                    "Nama Aspek Penilaian:",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                if (aspectCount >= 1) {
                    OutlinedTextField(
                        value = aspect1,
                        onValueChange = { aspect1 = it },
                        label = { Text("Aspek 1") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                if (aspectCount >= 2) {
                    OutlinedTextField(
                        value = aspect2,
                        onValueChange = { aspect2 = it },
                        label = { Text("Aspek 2") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                if (aspectCount >= 3) {
                    OutlinedTextField(
                        value = aspect3,
                        onValueChange = { aspect3 = it },
                        label = { Text("Aspek 3") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                if (aspectCount >= 4) {
                    OutlinedTextField(
                        value = aspect4,
                        onValueChange = { aspect4 = it },
                        label = { Text("Aspek 4") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                if (aspectCount >= 5) {
                    OutlinedTextField(
                        value = aspect5,
                        onValueChange = { aspect5 = it },
                        label = { Text("Aspek 5") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                if (aspectCount >= 6) {
                    OutlinedTextField(
                        value = aspect6,
                        onValueChange = { aspect6 = it },
                        label = { Text("Aspek 6") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                if (aspectCount >= 7) {
                    OutlinedTextField(
                        value = aspect7,
                        onValueChange = { aspect7 = it },
                        label = { Text("Aspek 7") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val max = if (weightType == "1-10") 10 else 5
                        val kkm = kkmText.toIntOrNull() ?: 75
                        val targetMin = targetMinScoreText.toDoubleOrNull() ?: 67.0
                        val targetMax = targetMaxScoreText.toDoubleOrNull() ?: 90.0
                        val result = Assessment(
                            id = initialAssessment?.id ?: 0,
                            classId = classId,
                            title = title.trim(),
                            topic = topic.trim(),
                            category = category,
                            isGroupAssessment = isGroup,
                            kkm = kkm,
                            minScore = 1,
                            maxScore = max,
                            weightType = weightType,
                            targetMinScore = targetMin,
                            targetMaxScore = targetMax,
                            aspectCount = aspectCount,
                            aspect1Name = aspect1.trim().ifBlank { "Aspek 1" },
                            aspect2Name = aspect2.trim().ifBlank { "Aspek 2" },
                            aspect3Name = aspect3.trim().ifBlank { "Aspek 3" },
                            aspect4Name = aspect4.trim().ifBlank { "Aspek 4" },
                            aspect5Name = aspect5.trim().ifBlank { "Aspek 5" },
                            aspect6Name = aspect6.trim().ifBlank { "Aspek 6" },
                            aspect7Name = aspect7.trim().ifBlank { "Aspek 7" }
                        )
                        onConfirm(result)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_assessment_button")
            ) {
                Text("Simpan Penilaian")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// === QUICK SCORE RANGE DIALOG (PENGATURAN CEPAT RENTANG NILAI MIN - MAX) ===
@Composable
fun QuickScoreRangeDialog(
    assessment: Assessment,
    onDismiss: () -> Unit,
    onConfirm: (min: Double, max: Double) -> Unit
) {
    var minText by remember { mutableStateOf(assessment.targetMinScore.toInt().toString()) }
    var maxText by remember { mutableStateOf(assessment.targetMaxScore.toInt().toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Atur Rentang Nilai", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = assessment.title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Konversi otomatis bobot ke rentang nilai siswa:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) minText = it },
                        label = { Text("Nilai Min (Cth: 67)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = { if (it.all { c -> c.isDigit() }) maxText = it },
                        label = { Text("Nilai Max (Cth: 90)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Presets
                Text("Pilihan Cepat Rentang:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("67-90" to (67 to 90), "70-95" to (70 to 95), "60-100" to (60 to 100), "0-100" to (0 to 100)).forEach { (label, range) ->
                        val isSelected = minText == range.first.toString() && maxText == range.second.toString()
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) TealPrimary else SurfaceElevated,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    minText = range.first.toString()
                                    maxText = range.second.toString()
                                }
                                .border(0.5.dp, if (isSelected) TealPrimary else OutlineLight, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else OnSurfaceLight,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Interactive preview
                val minVal = minText.toDoubleOrNull() ?: 67.0
                val maxVal = maxText.toDoubleOrNull() ?: 90.0
                val maxWeight = if (assessment.weightType == "1-10") 10 else 5
                Card(
                    colors = CardDefaults.cardColors(containerColor = TealPrimaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            "💡 Skema Penyesuaian:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = OnTealPrimaryContainer
                        )
                        Text(
                            "• Bobot 1 = ${minVal.toInt()}\n• Bobot $maxWeight = ${maxVal.toInt()}\n• Semua nilai siswa pada penilaian ini akan otomatis dihitung ulang.",
                            fontSize = 11.sp,
                            color = OnTealPrimaryContainer
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val minVal = minText.toDoubleOrNull() ?: 67.0
                    val maxVal = maxText.toDoubleOrNull() ?: 90.0
                    onConfirm(minVal, maxVal)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Terapkan Rentang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// === DIALOG ADD / EDIT STUDENT ===
@Composable
fun StudentDialog(
    classId: Long,
    initialStudent: Student? = null,
    onDismiss: () -> Unit,
    onConfirm: (nis: String, name: String, gender: String) -> Unit
) {
    var nis by remember { mutableStateOf(initialStudent?.nis ?: "") }
    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var gender by remember { mutableStateOf(initialStudent?.gender ?: "L") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initialStudent == null) "Tambah Siswa Baru" else "Edit Data Siswa",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Lengkap Siswa") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nis,
                    onValueChange = { nis = it },
                    label = { Text("NIS / Nomor Induk Siswa (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Jenis Kelamin:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilterChip(
                        selected = gender == "L",
                        onClick = { gender = "L" },
                        label = { Text("Laki-laki (L)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = gender == "P",
                        onClick = { gender = "P" },
                        label = { Text("Perempuan (P)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(nis, name, gender)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.testTag("save_student_button")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// === DIALOG CHAT / COMMA-SEPARATED BATCH ADD STUDENTS ===
@Composable
fun ChatCommaStudentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    val parsedNames = remember(rawText) {
        rawText.split(",", "\n", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Chat, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tambah Siswa via Teks Chat", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Ketik atau tempel (paste) daftar nama siswa yang dipisahkan dengan tanda koma ( , ) seperti di chat WhatsApp:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = { Text("Contoh: Ahmad Dani, Budi Santoso, Clara Putri, Dewi Lestari, Eko Prasetyo") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("chat_comma_input"),
                    maxLines = 6
                )

                if (parsedNames.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TealPrimaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                "Terdeteksi ${parsedNames.size} Siswa:",
                                fontWeight = FontWeight.Bold,
                                color = OnTealPrimaryContainer,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                parsedNames.joinToString(", "),
                                fontSize = 11.sp,
                                color = OnTealPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawText.isNotBlank()) {
                        onConfirm(rawText)
                    }
                },
                enabled = parsedNames.isNotEmpty(),
                modifier = Modifier.testTag("submit_chat_students_button")
            ) {
                Text("Tambah ${parsedNames.size} Siswa")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

// === DIALOG CREATE / EDIT GROUP WITH STUDENT SELECTOR & FILTERING ===
@Composable
fun GroupDialog(
    classStudents: List<Student>,
    assignedStudentIdsInOtherGroups: Set<Long> = emptySet(),
    initialGroupName: String = "",
    initialSelectedStudentIds: List<Long> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (name: String, studentIds: List<Long>) -> Unit
) {
    var groupName by remember { mutableStateOf(initialGroupName.ifBlank { "Kelompok Baru" }) }
    val selectedIds = remember { mutableStateListOf<Long>().apply { addAll(initialSelectedStudentIds) } }
    var searchQuery by remember { mutableStateOf("") }
    var hideAssignedStudents by remember { mutableStateOf(true) }

    // Partition students into available (not in other groups) and already assigned to other groups
    val availableStudents = remember(classStudents, assignedStudentIdsInOtherGroups) {
        classStudents.filter { it.id !in assignedStudentIdsInOtherGroups }
    }
    val assignedInOtherStudents = remember(classStudents, assignedStudentIdsInOtherGroups) {
        classStudents.filter { it.id in assignedStudentIdsInOtherGroups }
    }

    // Displayed students based on hideAssigned toggle and search query
    val displayedStudents = remember(availableStudents, assignedInOtherStudents, hideAssignedStudents, searchQuery) {
        val baseList = if (hideAssignedStudents) availableStudents else classStudents
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.nis.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (initialGroupName.isBlank()) Icons.Default.GroupAdd else Icons.Default.Groups,
                    contentDescription = null,
                    tint = IndigoSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialGroupName.isBlank()) "Buat Kelompok Baru" else "Edit Isi Kelompok",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = OnSurfaceLight
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Nama Kelompok") },
                    placeholder = { Text("Contoh: Kelompok 1, Tim Alfa") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_name_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurfaceLight,
                        unfocusedTextColor = OnSurfaceLight
                    )
                )

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama siswa atau NIS...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = OnSurfaceVariantLight, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = OnSurfaceVariantLight, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("group_student_search_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OnSurfaceLight,
                        unfocusedTextColor = OnSurfaceLight
                    )
                )

                // Info Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = IndigoSecondaryContainer
                    ) {
                        Text(
                            text = "Dipilih: ${selectedIds.size} anak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnIndigoSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TealPrimaryContainer
                    ) {
                        Text(
                            text = "Tersedia: ${availableStudents.size} anak",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnTealPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (assignedInOtherStudents.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (hideAssignedStudents) SurfaceElevated else AmberTertiaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { hideAssignedStudents = !hideAssignedStudents }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (hideAssignedStudents) "+${assignedInOtherStudents.size} di kelompok lain" else "Sembunyikan kelompok lain",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (hideAssignedStudents) OnSurfaceVariantLight else OnAmberTertiaryContainer
                                )
                            }
                        }
                    }
                }

                // Quick Action Select/Deselect All Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val toAdd = availableStudents.map { it.id }.filter { it !in selectedIds }
                            selectedIds.addAll(toAdd)
                        },
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Pilih Semua Yang Tersedia", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TealPrimary)
                    }

                    if (selectedIds.isNotEmpty()) {
                        TextButton(
                            onClick = { selectedIds.clear() },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Kosongkan Pilihan", fontSize = 11.sp, color = RedError)
                        }
                    }
                }

                HorizontalDivider(color = OutlineLight)

                if (classStudents.isEmpty()) {
                    Text(
                        "Belum ada data siswa di kelas ini. Tambahkan siswa terlebih dahulu pada tab Siswa.",
                        fontSize = 12.sp,
                        color = RedError,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else if (displayedStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (availableStudents.isEmpty()) "Semua siswa di kelas ini sudah masuk ke kelompok lain." else "Tidak ada siswa yang cocok dengan pencarian.",
                            fontSize = 12.sp,
                            color = OnSurfaceVariantLight
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(displayedStudents) { student ->
                            val isChecked = selectedIds.contains(student.id)
                            val isInOtherGroup = student.id in assignedStudentIdsInOtherGroups

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isChecked) IndigoSecondaryContainer.copy(alpha = 0.35f) else SurfaceCardLight,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isChecked) IndigoSecondary else OutlineLight.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (isChecked) selectedIds.remove(student.id)
                                        else selectedIds.add(student.id)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked) selectedIds.add(student.id)
                                            else selectedIds.remove(student.id)
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = IndigoSecondary,
                                            checkmarkColor = Color.White
                                        )
                                    )

                                    // Avatar Initial
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(if (student.gender == "P") Color(0xFFFCE7F3) else IndigoSecondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student.name.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = if (student.gender == "P") Color(0xFF9D174D) else OnIndigoSecondaryContainer,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = student.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = OnSurfaceLight
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (student.nis.isNotBlank()) {
                                                Text("NIS: ${student.nis}", fontSize = 11.sp, color = OnSurfaceVariantLight)
                                            }
                                            Text("(${student.gender})", fontSize = 11.sp, color = OnSurfaceVariantLight)
                                            if (isInOtherGroup) {
                                                Text(
                                                    "• Di kelompok lain",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AmberTertiary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank() && selectedIds.isNotEmpty()) {
                        onConfirm(groupName, selectedIds.toList())
                    }
                },
                enabled = groupName.isNotBlank() && selectedIds.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                modifier = Modifier.testTag("save_group_button")
            ) {
                Text(
                    text = if (initialGroupName.isBlank()) "Buat (${selectedIds.size} Siswa)" else "Simpan Perubahan (${selectedIds.size})",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = OnSurfaceVariantLight)
            }
        }
    )
}
