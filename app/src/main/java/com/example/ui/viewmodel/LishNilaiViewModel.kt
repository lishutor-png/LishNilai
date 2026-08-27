package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.GradeRepository
import com.example.util.ExportHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class MainTab {
    DASHBOARD,
    PENILAIAN,
    REKAP,
    REMEDIAL,
    KELOLA
}

data class UiMessage(val id: Long = System.currentTimeMillis(), val text: String, val isError: Boolean = false)

class LishNilaiViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = GradeRepository(database.appDao())

    // Authentication State
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    // Navigation State
    private val _currentTab = MutableStateFlow(MainTab.DASHBOARD)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Selected Class & Assessment
    private val _selectedClassId = MutableStateFlow<Long?>(null)
    val selectedClassId: StateFlow<Long?> = _selectedClassId.asStateFlow()

    private val _selectedAssessmentId = MutableStateFlow<Long?>(null)
    val selectedAssessmentId: StateFlow<Long?> = _selectedAssessmentId.asStateFlow()

    // Transient UI Message / Toast
    private val _uiMessage = MutableStateFlow<UiMessage?>(null)
    val uiMessage: StateFlow<UiMessage?> = _uiMessage.asStateFlow()

    // Classes Flow
    val allClasses: StateFlow<List<SchoolClass>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Class Entity
    val selectedClass: StateFlow<SchoolClass?> = _selectedClassId.flatMapLatest { id ->
        if (id != null) repository.getClassById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Students in Selected Class
    val classStudents: StateFlow<List<Student>> = _selectedClassId.flatMapLatest { id ->
        if (id != null) repository.getStudentsByClass(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Groups in Selected Class
    val classGroups: StateFlow<List<StudentGroup>> = _selectedClassId.flatMapLatest { id ->
        if (id != null) repository.getGroupsByClass(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Group Members in Selected Class
    val classAllGroupMembers: StateFlow<List<GroupMember>> = _selectedClassId.flatMapLatest { id ->
        if (id != null) repository.getAllGroupMembersByClass(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Assessments in Selected Class
    val classAssessments: StateFlow<List<Assessment>> = _selectedClassId.flatMapLatest { id ->
        if (id != null) repository.getAssessmentsByClass(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Assessment Entity
    val selectedAssessment: StateFlow<Assessment?> = _selectedAssessmentId.flatMapLatest { id ->
        if (id != null) repository.getAssessmentById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Scores for Selected Assessment
    val currentScores: StateFlow<List<StudentScore>> = _selectedAssessmentId.flatMapLatest { id ->
        if (id != null) repository.getScoresByAssessment(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Remedial List for Below KKM tab
    private val _remedialList = MutableStateFlow<List<RemedialItem>>(emptyList())
    val remedialList: StateFlow<List<RemedialItem>> = _remedialList.asStateFlow()

    // Generated Report File
    private val _generatedFile = MutableStateFlow<Pair<File, String>?>(null) // (File, mimeType)
    val generatedFile: StateFlow<Pair<File, String>?> = _generatedFile.asStateFlow()

    init {
        // Auto-select first class when list loads if none selected
        viewModelScope.launch {
            allClasses.collect { classes ->
                if (_selectedClassId.value == null && classes.isNotEmpty()) {
                    _selectedClassId.value = classes.first().id
                }
            }
        }

        // Auto-select first assessment when assessments list loads if none selected
        viewModelScope.launch {
            classAssessments.collect { assessments ->
                if (assessments.isNotEmpty()) {
                    if (_selectedAssessmentId.value == null || !assessments.any { it.id == _selectedAssessmentId.value }) {
                        _selectedAssessmentId.value = assessments.first().id
                    }
                } else {
                    _selectedAssessmentId.value = null
                }
            }
        }

        // Keep Remedial list updated
        viewModelScope.launch {
            combine(_selectedClassId, _selectedAssessmentId, currentScores) { classId, assessmentId, _ ->
                Pair(classId, assessmentId)
            }.collect { (classId, assessmentId) ->
                if (classId != null) {
                    loadRemedialList(classId, assessmentId)
                }
            }
        }
    }

    // === AUTHENTICATION ===
    fun authenticate(success: Boolean) {
        _isAuthenticated.value = success
    }

    fun logout() {
        _isAuthenticated.value = false
    }

    fun setTab(tab: MainTab) {
        _currentTab.value = tab
    }

    fun selectClass(classId: Long) {
        _selectedClassId.value = classId
    }

    fun selectAssessment(assessmentId: Long) {
        _selectedAssessmentId.value = assessmentId
    }

    fun clearUiMessage() {
        _uiMessage.value = null
    }

    fun showMessage(msg: String, isError: Boolean = false) {
        _uiMessage.value = UiMessage(text = msg, isError = isError)
    }

    // === CLASS MANAGEMENT ===
    fun addClass(name: String, subject: String, year: String, semester: String, kkm: Int) {
        viewModelScope.launch {
            try {
                val id = repository.addClass(
                    SchoolClass(
                        name = name.trim(),
                        subject = subject.trim().ifBlank { "Umum" },
                        academicYear = year.trim().ifBlank { "2025/2026" },
                        semester = semester.trim().ifBlank { "Ganjil" },
                        defaultKkm = kkm
                    )
                )
                _selectedClassId.value = id
                showMessage("Kelas ${name.trim()} berhasil ditambahkan!")
            } catch (e: Exception) {
                showMessage("Gagal menambah kelas: ${e.message}", true)
            }
        }
    }

    fun updateClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            try {
                repository.updateClass(schoolClass)
                showMessage("Data kelas berhasil diperbarui!")
            } catch (e: Exception) {
                showMessage("Gagal memperbarui kelas: ${e.message}", true)
            }
        }
    }

    fun deleteClass(schoolClass: SchoolClass) {
        viewModelScope.launch {
            try {
                repository.deleteClass(schoolClass)
                if (_selectedClassId.value == schoolClass.id) {
                    _selectedClassId.value = allClasses.value.firstOrNull { it.id != schoolClass.id }?.id
                }
                showMessage("Kelas ${schoolClass.name} berhasil dihapus!")
            } catch (e: Exception) {
                showMessage("Gagal menghapus kelas: ${e.message}", true)
            }
        }
    }

    // === STUDENT MANAGEMENT ===
    fun addStudent(classId: Long, nis: String, name: String, gender: String) {
        viewModelScope.launch {
            try {
                repository.addStudent(
                    Student(
                        classId = classId,
                        nis = nis.trim(),
                        name = name.trim(),
                        gender = gender
                    )
                )
                showMessage("Siswa $name berhasil ditambahkan!")
            } catch (e: Exception) {
                showMessage("Gagal menambah siswa: ${e.message}", true)
            }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.updateStudent(student)
                showMessage("Data siswa ${student.name} diperbarui!")
            } catch (e: Exception) {
                showMessage("Gagal memperbarui siswa: ${e.message}", true)
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            try {
                repository.deleteStudent(student)
                showMessage("Siswa ${student.name} berhasil dihapus!")
            } catch (e: Exception) {
                showMessage("Gagal menghapus siswa: ${e.message}", true)
            }
        }
    }

    // Comma-separated chat-style input
    fun importStudentsFromChat(classId: Long, rawText: String) {
        viewModelScope.launch {
            try {
                val count = repository.importStudentsFromCommaSeparated(classId, rawText)
                if (count > 0) {
                    showMessage("Berhasil menambahkan $count siswa via input teks!")
                } else {
                    showMessage("Tidak ada nama siswa yang valid ditemukan.", true)
                }
            } catch (e: Exception) {
                showMessage("Gagal import siswa: ${e.message}", true)
            }
        }
    }

    // CSV / Excel file content import
    fun importStudentsFromCsv(classId: Long, content: String) {
        viewModelScope.launch {
            try {
                val count = repository.importStudentsFromCsv(classId, content)
                if (count > 0) {
                    showMessage("Berhasil mengimpor $count siswa dari file!", false)
                } else {
                    showMessage("Format file tidak sesuai atau kosong.", true)
                }
            } catch (e: Exception) {
                showMessage("Gagal mengimpor file: ${e.message}", true)
            }
        }
    }

    // === GROUP MANAGEMENT ===
    fun createGroup(classId: Long, groupName: String, studentIds: List<Long>) {
        viewModelScope.launch {
            try {
                repository.createGroupWithMembers(classId, groupName.trim(), studentIds)
                showMessage("Kelompok $groupName berhasil dibuat (${studentIds.size} siswa)!")
            } catch (e: Exception) {
                showMessage("Gagal membuat kelompok: ${e.message}", true)
            }
        }
    }

    fun updateGroup(groupId: Long, classId: Long, groupName: String, studentIds: List<Long>) {
        viewModelScope.launch {
            try {
                repository.updateGroupWithMembers(groupId, classId, groupName.trim(), studentIds)
                showMessage("Kelompok '$groupName' berhasil diperbarui (${studentIds.size} siswa)!")
            } catch (e: Exception) {
                showMessage("Gagal memperbarui kelompok: ${e.message}", true)
            }
        }
    }

    fun removeStudentFromGroup(groupId: Long, student: Student, groupName: String) {
        viewModelScope.launch {
            try {
                repository.removeStudentFromGroup(groupId, student.id)
                showMessage("${student.name} dikeluarkan dari $groupName")
            } catch (e: Exception) {
                showMessage("Gagal mengeluarkan siswa: ${e.message}", true)
            }
        }
    }

    fun updateGroupMembers(groupId: Long, studentIds: List<Long>) {
        viewModelScope.launch {
            try {
                repository.updateGroupMembers(groupId, studentIds)
                showMessage("Anggota kelompok berhasil diperbarui!")
            } catch (e: Exception) {
                showMessage("Gagal memperbarui anggota kelompok: ${e.message}", true)
            }
        }
    }

    fun deleteGroup(group: StudentGroup) {
        viewModelScope.launch {
            try {
                repository.deleteGroup(group)
                showMessage("Kelompok ${group.name} dihapus!")
            } catch (e: Exception) {
                showMessage("Gagal menghapus kelompok: ${e.message}", true)
            }
        }
    }

    suspend fun getGroupMembers(groupId: Long): List<GroupMember> {
        return repository.getGroupMembersDirect(groupId)
    }

    // === ASSESSMENT MANAGEMENT ===
    fun saveAssessment(assessment: Assessment, isNew: Boolean) {
        viewModelScope.launch {
            try {
                if (isNew) {
                    val id = repository.addAssessment(assessment)
                    _selectedAssessmentId.value = id
                    showMessage("Penilaian '${assessment.title}' berhasil dibuat!")
                } else {
                    repository.updateAssessment(assessment)
                    // Recalculate existing scores for this assessment with updated aspect bounds/count
                    recalculateAssessmentScores(assessment)
                    showMessage("Penilaian '${assessment.title}' berhasil diperbarui!")
                }
            } catch (e: Exception) {
                showMessage("Gagal menyimpan penilaian: ${e.message}", true)
            }
        }
    }

    private suspend fun recalculateAssessmentScores(assessment: Assessment) {
        val existingScores = database.appDao().getScoresByAssessmentDirect(assessment.id)
        existingScores.forEach { sc ->
            val aspectList = listOf(
                sc.aspect1Score,
                sc.aspect2Score,
                sc.aspect3Score,
                sc.aspect4Score,
                sc.aspect5Score,
                sc.aspect6Score,
                sc.aspect7Score
            )
            val finalScore = repository.calculateNormalizedScore(
                aspectScores = aspectList,
                aspectCount = assessment.aspectCount,
                minScore = assessment.minScore,
                maxScore = assessment.maxScore,
                targetMinScore = assessment.targetMinScore,
                targetMaxScore = assessment.targetMaxScore,
                bonusScore = sc.bonusScore
            )
            database.appDao().insertOrUpdateScore(sc.copy(finalScore = finalScore))
        }
    }

    fun updateAssessmentScoreRange(assessment: Assessment, targetMin: Double, targetMax: Double) {
        viewModelScope.launch {
            try {
                val updated = assessment.copy(targetMinScore = targetMin, targetMaxScore = targetMax)
                repository.updateAssessment(updated)
                recalculateAssessmentScores(updated)
                _selectedClassId.value?.let { loadRemedialList(it, updated.id) }
                showMessage("Rentang nilai '${updated.title}' diubah jadi ${targetMin.toInt()} - ${targetMax.toInt()}!")
            } catch (e: Exception) {
                showMessage("Gagal mengubah rentang nilai: ${e.message}", true)
            }
        }
    }

    fun deleteAssessment(assessment: Assessment) {
        viewModelScope.launch {
            try {
                repository.deleteAssessment(assessment)
                if (_selectedAssessmentId.value == assessment.id) {
                    _selectedAssessmentId.value = classAssessments.value.firstOrNull { it.id != assessment.id }?.id
                }
                showMessage("Penilaian '${assessment.title}' dihapus!")
            } catch (e: Exception) {
                showMessage("Gagal menghapus penilaian: ${e.message}", true)
            }
        }
    }

    // === SCORING WORKFLOW ===
    fun saveStudentScore(
        assessment: Assessment,
        studentId: Long,
        aspect1: Int,
        aspect2: Int,
        aspect3: Int,
        aspect4: Int,
        aspect5: Int,
        aspect6: Int,
        aspect7: Int,
        bonusScore: Int,
        notes: String = ""
    ) {
        viewModelScope.launch {
            try {
                repository.saveStudentScore(
                    assessment = assessment,
                    studentId = studentId,
                    aspect1 = aspect1,
                    aspect2 = aspect2,
                    aspect3 = aspect3,
                    aspect4 = aspect4,
                    aspect5 = aspect5,
                    aspect6 = aspect6,
                    aspect7 = aspect7,
                    bonusScore = bonusScore,
                    notes = notes
                )
                // Reload remedial list
                _selectedClassId.value?.let { loadRemedialList(it, assessment.id) }
            } catch (e: Exception) {
                showMessage("Gagal menyimpan nilai: ${e.message}", true)
            }
        }
    }

    // Grade whole group in one action
    fun saveGroupScore(
        assessment: Assessment,
        groupId: Long,
        groupName: String,
        aspect1: Int,
        aspect2: Int,
        aspect3: Int,
        aspect4: Int,
        aspect5: Int,
        aspect6: Int,
        aspect7: Int,
        groupNotes: String = ""
    ) {
        viewModelScope.launch {
            try {
                repository.saveGroupScore(
                    assessment = assessment,
                    groupId = groupId,
                    aspect1 = aspect1,
                    aspect2 = aspect2,
                    aspect3 = aspect3,
                    aspect4 = aspect4,
                    aspect5 = aspect5,
                    aspect6 = aspect6,
                    aspect7 = aspect7,
                    groupNotes = groupNotes
                )
                showMessage("Nilai kelompok '$groupName' berhasil diterapkan ke semua anggota!")
                _selectedClassId.value?.let { loadRemedialList(it, assessment.id) }
            } catch (e: Exception) {
                showMessage("Gagal menyimpan nilai kelompok: ${e.message}", true)
            }
        }
    }

    fun updateStudentBonus(assessment: Assessment, studentId: Long, bonus: Int) {
        viewModelScope.launch {
            try {
                repository.updateStudentBonusScore(assessment, studentId, bonus)
                _selectedClassId.value?.let { loadRemedialList(it, assessment.id) }
            } catch (e: Exception) {
                showMessage("Gagal update nilai tambahan: ${e.message}", true)
            }
        }
    }

    // === REMEDIAL / KKM ===
    fun loadRemedialList(classId: Long, assessmentId: Long?) {
        viewModelScope.launch {
            val list = repository.getRemedialStudents(classId, assessmentId)
            _remedialList.value = list
        }
    }

    fun copyRemedialWhatsApp(context: Context, schoolClass: SchoolClass, assessmentTitle: String) {
        val message = ExportHelper.generateWhatsAppRemedialMessage(
            schoolClass = schoolClass,
            assessmentTitle = assessmentTitle,
            remedialList = _remedialList.value
        )
        ExportHelper.copyToClipboard(context, "Daftar Remedial WhatsApp", message)
    }

    fun shareRemedialWhatsApp(context: Context, schoolClass: SchoolClass, assessmentTitle: String) {
        val message = ExportHelper.generateWhatsAppRemedialMessage(
            schoolClass = schoolClass,
            assessmentTitle = assessmentTitle,
            remedialList = _remedialList.value
        )
        ExportHelper.shareToWhatsApp(context, message)
    }

    // === EXPORT (PDF & EXCEL) ===
    fun exportPdf(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentClass = selectedClass.value ?: return@launch
            val students = classStudents.value
            val assessments = classAssessments.value

            val scoresMap = mutableMapOf<Pair<Long, Long>, StudentScore>()
            assessments.forEach { a ->
                val scList = database.appDao().getScoresByAssessmentDirect(a.id)
                scList.forEach { sc ->
                    scoresMap[Pair(sc.assessmentId, sc.studentId)] = sc
                }
            }

            val file = ExportHelper.exportToPdfReport(context, currentClass, students, assessments, scoresMap)
            withContext(Dispatchers.Main) {
                if (file != null) {
                    _generatedFile.value = Pair(file, "application/pdf")
                    ExportHelper.shareGeneratedFile(context, file, "application/pdf", "Bagikan Rekap Nilai PDF")
                    showMessage("Laporan PDF berhasil dibuat!")
                } else {
                    showMessage("Gagal membuat file PDF", true)
                }
            }
        }
    }

    fun exportExcel(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentClass = selectedClass.value ?: return@launch
            val students = classStudents.value
            val assessments = classAssessments.value

            val scoresMap = mutableMapOf<Pair<Long, Long>, StudentScore>()
            assessments.forEach { a ->
                val scList = database.appDao().getScoresByAssessmentDirect(a.id)
                scList.forEach { sc ->
                    scoresMap[Pair(sc.assessmentId, sc.studentId)] = sc
                }
            }

            val file = ExportHelper.exportToExcelCsv(context, currentClass, students, assessments, scoresMap)
            withContext(Dispatchers.Main) {
                if (file != null) {
                    _generatedFile.value = Pair(file, "text/csv")
                    ExportHelper.shareGeneratedFile(context, file, "text/csv", "Bagikan Rekap Nilai Excel/CSV")
                    showMessage("Rekap Excel/CSV berhasil dibuat!")
                } else {
                    showMessage("Gagal membuat file Excel/CSV", true)
                }
            }
        }
    }
}
