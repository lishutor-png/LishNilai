package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Assessment
import com.example.data.model.RemedialItem
import com.example.data.model.SchoolClass
import com.example.ui.theme.*
import com.example.ui.viewmodel.LishNilaiViewModel
import com.example.ui.viewmodel.MainTab

@Composable
fun RemedialScreen(
    viewModel: LishNilaiViewModel
) {
    val context = LocalContext.current
    val selectedClass by viewModel.selectedClass.collectAsState()
    val assessments by viewModel.classAssessments.collectAsState()
    val selectedAssessment by viewModel.selectedAssessment.collectAsState()
    val remedialList by viewModel.remedialList.collectAsState()

    var filterAllAssessments by remember { mutableStateOf(false) }

    // Trigger remedial list update when filter changes
    LaunchedEffect(selectedClass, selectedAssessment, filterAllAssessments) {
        selectedClass?.let { cls ->
            val aId = if (filterAllAssessments) null else selectedAssessment?.id
            viewModel.loadRemedialList(cls.id, aId)
        }
    }

    val currentAssessmentTitle = if (filterAllAssessments) {
        "Semua Materi / Penilaian"
    } else {
        selectedAssessment?.title ?: "Seluruh Penilaian"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // === HEADER & WHATSAPP BROADCAST ACTION CARD ===
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = RedErrorContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RedError),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PriorityHigh, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Daftar Remedial Siswa",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnRedErrorContainer
                            )
                            Text(
                                text = "${selectedClass?.name ?: "Kelas"} • ${remedialList.size} Siswa di bawah KKM",
                                fontSize = 12.sp,
                                color = OnRedErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = RedError
                    ) {
                        Text(
                            text = "${remedialList.size} Anak",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // WhatsApp Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedClass?.let { cls ->
                                viewModel.copyRemedialWhatsApp(context, cls, currentAssessmentTitle)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("copy_whatsapp_button"),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Format WA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            selectedClass?.let { cls ->
                                viewModel.shareRemedialWhatsApp(context, cls, currentAssessmentTitle)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_whatsapp_button"),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // === FILTER ROW (PILIH PENILAIAN / MATERI) ===
        Text(
            text = "Filter Berdasarkan Penilaian:",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnSurfaceLight
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = filterAllAssessments,
                    onClick = {
                        filterAllAssessments = true
                    },
                    label = { Text("Semua Penilaian", fontWeight = FontWeight.Bold) }
                )
            }

            items(assessments) { a ->
                val isSelected = !filterAllAssessments && a.id == selectedAssessment?.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        filterAllAssessments = false
                        viewModel.selectAssessment(a.id)
                    },
                    label = { Text(a.title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // === LIST OF REMEDIAL STUDENTS ===
        if (remedialList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(GreenSuccessContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Semua Siswa Tuntas!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceLight
                    )
                    Text(
                        "Tidak ada siswa yang berada di bawah KKM pada filter penilaian yang dipilih.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(remedialList) { item ->
                    RemedialStudentCard(
                        item = item,
                        onReGradeClick = {
                            viewModel.selectAssessment(item.assessmentId)
                            viewModel.setTab(MainTab.PENILAIAN)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RemedialStudentCard(
    item: RemedialItem,
    onReGradeClick: () -> Unit
) {
    val diff = item.kkm - item.score

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RedErrorContainer),
        border = androidx.compose.foundation.BorderStroke(1.dp, RedErrorBorder),
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(RedErrorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.studentName.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = RedError,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = item.studentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = OnSurfaceLight
                    )
                    Text(
                        text = "Materi: ${item.topic}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (item.nis.isNotBlank()) {
                            Text("NIS: ${item.nis}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(
                            text = "• Penilaian: ${item.assessmentTitle}",
                            fontSize = 11.sp,
                            color = TealPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Score and Gap Badge
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RedError
                ) {
                    Text(
                        text = "${item.score.toInt()}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "KKM: ${item.kkm} (-${"%.1f".format(diff)})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = RedError,
                    modifier = Modifier.padding(top = 2.dp)
                )

                TextButton(
                    onClick = onReGradeClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Perbaiki Nilai", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
