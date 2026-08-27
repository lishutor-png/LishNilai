package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolClass
import com.example.data.model.Student
import com.example.data.model.StudentGroup
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LishNilaiViewModel
import kotlinx.coroutines.launch

enum class ManageSubTab {
    SISWA,
    KELOMPOK,
    KELAS,
    IMPOR_EKSPOR
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagementScreen(
    viewModel: LishNilaiViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allClasses by viewModel.allClasses.collectAsState()
    val selectedClass by viewModel.selectedClass.collectAsState()
    val students by viewModel.classStudents.collectAsState()
    val groups by viewModel.classGroups.collectAsState()
    val allGroupMembers by viewModel.classAllGroupMembers.collectAsState()

    var activeSubTab by remember { mutableStateOf(ManageSubTab.SISWA) }

    // Dialog state
    var showAddStudentDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showChatCommaDialog by remember { mutableStateOf(false) }

    var showAddGroupDialog by remember { mutableStateOf(false) }
    var editingGroup by remember { mutableStateOf<StudentGroup?>(null) }
    var editingGroupMemberIds by remember { mutableStateOf<List<Long>>(emptyList()) }

    var showAddClassDialog by remember { mutableStateOf(false) }
    var editingClass by remember { mutableStateOf<SchoolClass?>(null) }

    // Group members map
    var groupMembersMap by remember { mutableStateOf<Map<Long, List<Student>>>(emptyMap()) }
    LaunchedEffect(groups, students) {
        val map = mutableMapOf<Long, List<Student>>()
        val studentMap = students.associateBy { it.id }
        groups.forEach { g ->
            val members = viewModel.getGroupMembers(g.id)
            map[g.id] = members.mapNotNull { studentMap[it.studentId] }
        }
        groupMembersMap = map
    }

    // SAF Document Picker for CSV / Excel file import
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null && selectedClass != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() } ?: ""
                if (content.isNotBlank()) {
                    viewModel.importStudentsFromCsv(selectedClass!!.id, content)
                } else {
                    Toast.makeText(context, "File kosong atau tidak dapat dibaca", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal membaca file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // === SUB TABS ROW ===
        ScrollableTabRow(
            selectedTabIndex = activeSubTab.ordinal,
            edgePadding = 0.dp,
            containerColor = SurfaceElevated,
            contentColor = TealPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeSubTab == ManageSubTab.SISWA,
                onClick = { activeSubTab = ManageSubTab.SISWA },
                text = { Text("Siswa (${students.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeSubTab == ManageSubTab.KELOMPOK,
                onClick = { activeSubTab = ManageSubTab.KELOMPOK },
                text = { Text("Kelompok (${groups.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeSubTab == ManageSubTab.KELAS,
                onClick = { activeSubTab = ManageSubTab.KELAS },
                text = { Text("Kelas (${allClasses.size})", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeSubTab == ManageSubTab.IMPOR_EKSPOR,
                onClick = { activeSubTab = ManageSubTab.IMPOR_EKSPOR },
                text = { Text("Impor & Ekspor", fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // === SUB TAB CONTENT ===
        when (activeSubTab) {
            ManageSubTab.SISWA -> {
                // Actions Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddStudentDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_student_button"),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Siswa", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showChatCommaDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_chat_students_button"),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Input Koma", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            documentPickerLauncher.launch(arrayOf("text/*", "text/csv", "application/vnd.ms-excel", "application/octet-stream"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("import_file_button"),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Impor File", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (students.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada data siswa di kelas ${selectedClass?.name ?: ""}. Gunakan tombol di atas untuk menambah siswa atau impor file CSV/Excel.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(students) { student ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCardLight),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
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
                                            Text(student.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurfaceLight)
                                            Text(
                                                text = "NIS: ${student.nis.ifBlank { "-" }} • Gender: ${student.gender}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row {
                                        IconButton(onClick = { editingStudent = student }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TealPrimary, modifier = Modifier.size(20.dp))
                                        }
                                        IconButton(onClick = { viewModel.deleteStudent(student) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = RedError, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ManageSubTab.KELOMPOK -> {
                val assignedStudentIds = remember(allGroupMembers) { allGroupMembers.map { it.studentId }.toSet() }
                val unassignedCount = remember(students, assignedStudentIds) { students.count { it.id !in assignedStudentIds } }

                // Group summary & action header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = IndigoSecondaryContainer.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IndigoSecondary.copy(alpha = 0.2f))
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
                                text = "Distribusi Kelompok Siswa",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = OnIndigoSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Total: ${students.size} Siswa • Masuk Kelompok: ${assignedStudentIds.size} • Belum Ada Kelompok: $unassignedCount",
                                fontSize = 11.sp,
                                color = OnIndigoSecondaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showAddGroupDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoSecondary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_group_button")
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Kelompok Baru (Otomatis Filter Siswa)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (groups.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Belum ada kelompok dibuat. Buat kelompok baru untuk menilai proyek tim secara kolaboratif dan efisien.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(groups) { group ->
                            val members = groupMembersMap[group.id] ?: emptyList()
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SurfaceCardLight),
                                border = androidx.compose.foundation.BorderStroke(1.dp, OutlineLight.copy(alpha = 0.6f)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(group.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = IndigoSecondary)
                                            Text("${members.size} Anggota Siswa", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(
                                                onClick = {
                                                    coroutineScope.launch {
                                                        val mems = viewModel.getGroupMembers(group.id)
                                                        editingGroupMemberIds = mems.map { it.studentId }
                                                        editingGroup = group
                                                    }
                                                },
                                                modifier = Modifier.testTag("edit_group_button_${group.id}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Isi Kelompok", tint = IndigoSecondary)
                                            }
                                            IconButton(
                                                onClick = { viewModel.deleteGroup(group) },
                                                modifier = Modifier.testTag("delete_group_button_${group.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Hapus Kelompok", tint = RedError)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = OutlineLight.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (members.isEmpty()) {
                                        Text(
                                            text = "Belum ada anggota. Klik 'Edit Isi Kelompok' untuk menambahkan siswa.",
                                            fontSize = 12.sp,
                                            color = RedError
                                        )
                                    } else {
                                        // Members Chip Flow
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            members.forEach { student ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = SurfaceElevated,
                                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, OutlineLight),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.weight(1f)
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(24.dp)
                                                                    .clip(CircleShape)
                                                                    .background(if (student.gender == "P") Color(0xFFFCE7F3) else IndigoSecondaryContainer),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(
                                                                    text = student.name.take(1).uppercase(),
                                                                    fontWeight = FontWeight.Bold,
                                                                    fontSize = 11.sp,
                                                                    color = if (student.gender == "P") Color(0xFF9D174D) else OnIndigoSecondaryContainer
                                                                )
                                                            }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = student.name,
                                                                fontWeight = FontWeight.SemiBold,
                                                                fontSize = 13.sp,
                                                                color = OnSurfaceLight
                                                            )
                                                            if (student.nis.isNotBlank()) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = "(${student.nis})",
                                                                    fontSize = 11.sp,
                                                                    color = OnSurfaceVariantLight
                                                                )
                                                            }
                                                        }

                                                        // Remove single student button
                                                        IconButton(
                                                            onClick = {
                                                                viewModel.removeStudentFromGroup(group.id, student, group.name)
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                Icons.Default.Close,
                                                                contentDescription = "Hapus Siswa dari Kelompok",
                                                                tint = RedError,
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Quick Edit Button
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                val mems = viewModel.getGroupMembers(group.id)
                                                editingGroupMemberIds = mems.map { it.studentId }
                                                editingGroup = group
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp), tint = IndigoSecondary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Edit Isi & Anggota Kelompok", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IndigoSecondary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ManageSubTab.KELAS -> {
                Button(
                    onClick = { showAddClassDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_class_screen_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tambah Kelas Baru", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(allClasses) { schoolClass ->
                        val isActive = schoolClass.id == selectedClass?.id
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) TealPrimaryContainer else SurfaceCardLight
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(schoolClass.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = OnSurfaceLight)
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = TealPrimary
                                            ) {
                                                Text("Aktif", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${schoolClass.subject} • KKM: ${schoolClass.defaultKkm}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "TA ${schoolClass.academicYear} (${schoolClass.semester})",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row {
                                    if (!isActive) {
                                        TextButton(onClick = { viewModel.selectClass(schoolClass.id) }) {
                                            Text("Pilih", fontWeight = FontWeight.Bold, color = TealPrimary)
                                        }
                                    }
                                    IconButton(onClick = { editingClass = schoolClass }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TealPrimary)
                                    }
                                    IconButton(onClick = { viewModel.deleteClass(schoolClass) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = RedError)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ManageSubTab.IMPOR_EKSPOR -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Export Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceCardLight),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Ekspor Laporan Administrasi Sekolah", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TealPrimaryDark)
                                Text(
                                    "Unduh dan bagikan rekap nilai lengkap dengan status kelulusan KKM per semester dalam format PDF atau Excel:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.exportPdf(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = RedError),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Ekspor PDF", fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.exportExcel(context) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenSuccess),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.TableView, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Ekspor Excel", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Import Guide Section
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Panduan Impor Data Siswa", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnSurfaceLight)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    "Aplikasi mendukung 3 cara mudah menambah data siswa:",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))
                                Text("1. File CSV / Excel (.csv, .xlsx):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Format kolom: NIS, Nama Siswa, Jenis Kelamin (L/P)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("2. Format Teks Chat Koma (WhatsApp Style):", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Tempel teks: Budi, Citra Dewi, Dimas, Eka", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("3. Input Manual:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Text("Ketik NIS dan nama siswa satu per satu melalui formulir.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }

    // === DIALOGS ===
    if (showAddStudentDialog && selectedClass != null) {
        StudentDialog(
            classId = selectedClass!!.id,
            onDismiss = { showAddStudentDialog = false },
            onConfirm = { nis, name, gender ->
                viewModel.addStudent(selectedClass!!.id, nis, name, gender)
                showAddStudentDialog = false
            }
        )
    }

    if (editingStudent != null && selectedClass != null) {
        StudentDialog(
            classId = selectedClass!!.id,
            initialStudent = editingStudent,
            onDismiss = { editingStudent = null },
            onConfirm = { nis, name, gender ->
                viewModel.updateStudent(
                    editingStudent!!.copy(
                        nis = nis,
                        name = name,
                        gender = gender
                    )
                )
                editingStudent = null
            }
        )
    }

    if (showChatCommaDialog && selectedClass != null) {
        ChatCommaStudentDialog(
            onDismiss = { showChatCommaDialog = false },
            onConfirm = { text ->
                viewModel.importStudentsFromChat(selectedClass!!.id, text)
                showChatCommaDialog = false
            }
        )
    }

    if (showAddGroupDialog && selectedClass != null) {
        val assignedIds = allGroupMembers.map { it.studentId }.toSet()
        GroupDialog(
            classStudents = students,
            assignedStudentIdsInOtherGroups = assignedIds,
            onDismiss = { showAddGroupDialog = false },
            onConfirm = { groupName, studentIds ->
                viewModel.createGroup(selectedClass!!.id, groupName, studentIds)
                showAddGroupDialog = false
            }
        )
    }

    if (editingGroup != null && selectedClass != null) {
        val otherGroupAssignedIds = allGroupMembers
            .filter { it.groupId != editingGroup!!.id }
            .map { it.studentId }
            .toSet()

        GroupDialog(
            classStudents = students,
            assignedStudentIdsInOtherGroups = otherGroupAssignedIds,
            initialGroupName = editingGroup!!.name,
            initialSelectedStudentIds = editingGroupMemberIds,
            onDismiss = {
                editingGroup = null
                editingGroupMemberIds = emptyList()
            },
            onConfirm = { groupName, studentIds ->
                viewModel.updateGroup(editingGroup!!.id, selectedClass!!.id, groupName, studentIds)
                editingGroup = null
                editingGroupMemberIds = emptyList()
            }
        )
    }

    if (showAddClassDialog) {
        ClassDialog(
            onDismiss = { showAddClassDialog = false },
            onConfirm = { name, subject, year, semester, kkm ->
                viewModel.addClass(name, subject, year, semester, kkm)
                showAddClassDialog = false
            }
        )
    }

    if (editingClass != null) {
        ClassDialog(
            initialClass = editingClass,
            onDismiss = { editingClass = null },
            onConfirm = { name, subject, year, semester, kkm ->
                viewModel.updateClass(
                    editingClass!!.copy(
                        name = name,
                        subject = subject,
                        academicYear = year,
                        semester = semester,
                        defaultKkm = kkm
                    )
                )
                editingClass = null
            }
        )
    }
}
