package com.example.data.repository

import com.example.data.local.AppDao
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class GradeRepository(private val dao: AppDao) {

    // === Class Operations ===
    val allClasses: Flow<List<SchoolClass>> = dao.getAllClasses()
    val classCount: Flow<Int> = dao.getClassCount()
    val totalStudentCount: Flow<Int> = dao.getTotalStudentCount()
    val totalAssessmentCount: Flow<Int> = dao.getTotalAssessmentCount()

    fun getClassById(classId: Long): Flow<SchoolClass?> = dao.getClassById(classId)
    suspend fun getClassDirect(classId: Long): SchoolClass? = dao.getClassDirect(classId)
    suspend fun addClass(schoolClass: SchoolClass): Long = dao.insertClass(schoolClass)
    suspend fun updateClass(schoolClass: SchoolClass) = dao.updateClass(schoolClass)
    suspend fun deleteClass(schoolClass: SchoolClass) = dao.deleteClass(schoolClass)

    // === Student Operations ===
    fun getStudentsByClass(classId: Long): Flow<List<Student>> = dao.getStudentsByClass(classId)
    suspend fun getStudentsByClassDirect(classId: Long): List<Student> = dao.getStudentsByClassDirect(classId)
    suspend fun addStudent(student: Student): Long = dao.insertStudent(student)
    suspend fun addStudents(students: List<Student>): List<Long> = dao.insertStudents(students)
    suspend fun updateStudent(student: Student) = dao.updateStudent(student)
    suspend fun deleteStudent(student: Student) = dao.deleteStudent(student)

    // Import from comma-separated text (Chat-style input)
    suspend fun importStudentsFromCommaSeparated(classId: Long, text: String): Int = withContext(Dispatchers.IO) {
        val names = text.split(",", "\n", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (names.isEmpty()) return@withContext 0

        val existingStudents = dao.getStudentsByClassDirect(classId)
        val startNisNum = existingStudents.size + 1

        val newStudents = names.mapIndexed { index, name ->
            Student(
                classId = classId,
                nis = (startNisNum + index).toString().padStart(4, '0'),
                name = name,
                gender = "L"
            )
        }
        dao.insertStudents(newStudents)
        newStudents.size
    }

    // Import from CSV / text data lines
    suspend fun importStudentsFromCsv(classId: Long, csvContent: String): Int = withContext(Dispatchers.IO) {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return@withContext 0

        val studentList = mutableListOf<Student>()
        val existingStudents = dao.getStudentsByClassDirect(classId)
        var autoNis = existingStudents.size + 1

        for (line in lines) {
            // Ignore header row if contains "nama" or "nis"
            val lower = line.lowercase()
            if (lower.contains("nama") || lower.contains("nis") || lower.contains("gender") || lower.contains("student")) {
                continue
            }
            val tokens = line.split(",", ";", "\t").map { it.trim().trim('"', '\'') }
            if (tokens.isEmpty() || tokens[0].isBlank()) continue

            var nis = ""
            var name = ""
            var gender = "L"

            if (tokens.size == 1) {
                name = tokens[0]
                nis = autoNis.toString().padStart(4, '0')
                autoNis++
            } else if (tokens.size == 2) {
                // Check if token[0] looks like a number/NIS
                if (tokens[0].matches(Regex("^[0-9]+$"))) {
                    nis = tokens[0]
                    name = tokens[1]
                } else {
                    name = tokens[0]
                    gender = if (tokens[1].uppercase().startsWith("P")) "P" else "L"
                    nis = autoNis.toString().padStart(4, '0')
                    autoNis++
                }
            } else {
                nis = tokens[0]
                name = tokens[1]
                gender = if (tokens.getOrNull(2)?.uppercase()?.startsWith("P") == true) "P" else "L"
            }

            if (name.isNotBlank()) {
                studentList.add(
                    Student(
                        classId = classId,
                        nis = nis,
                        name = name,
                        gender = gender
                    )
                )
            }
        }

        if (studentList.isNotEmpty()) {
            dao.insertStudents(studentList)
        }
        studentList.size
    }

    // === Group Operations ===
    fun getGroupsByClass(classId: Long): Flow<List<StudentGroup>> = dao.getGroupsByClass(classId)
    suspend fun getGroupsByClassDirect(classId: Long): List<StudentGroup> = dao.getGroupsByClassDirect(classId)
    fun getGroupMembers(groupId: Long): Flow<List<GroupMember>> = dao.getGroupMembers(groupId)
    suspend fun getGroupMembersDirect(groupId: Long): List<GroupMember> = dao.getGroupMembersDirect(groupId)
    fun getAllGroupMembersByClass(classId: Long): Flow<List<GroupMember>> = dao.getAllGroupMembersByClass(classId)
    suspend fun getAllGroupMembersByClassDirect(classId: Long): List<GroupMember> = dao.getAllGroupMembersByClassDirect(classId)

    suspend fun createGroupWithMembers(classId: Long, groupName: String, studentIds: List<Long>): Long = withContext(Dispatchers.IO) {
        val groupId = dao.insertGroup(StudentGroup(classId = classId, name = groupName))
        val members = studentIds.map { GroupMember(groupId = groupId, studentId = it) }
        dao.insertGroupMembers(members)
        groupId
    }

    suspend fun updateGroupWithMembers(groupId: Long, classId: Long, groupName: String, studentIds: List<Long>) = withContext(Dispatchers.IO) {
        dao.updateGroup(StudentGroup(id = groupId, classId = classId, name = groupName))
        dao.clearGroupMembers(groupId)
        val members = studentIds.map { GroupMember(groupId = groupId, studentId = it) }
        dao.insertGroupMembers(members)
    }

    suspend fun removeStudentFromGroup(groupId: Long, studentId: Long) = withContext(Dispatchers.IO) {
        dao.removeGroupMember(groupId, studentId)
    }

    suspend fun updateGroupMembers(groupId: Long, studentIds: List<Long>) = withContext(Dispatchers.IO) {
        dao.clearGroupMembers(groupId)
        val members = studentIds.map { GroupMember(groupId = groupId, studentId = it) }
        dao.insertGroupMembers(members)
    }

    suspend fun deleteGroup(group: StudentGroup) = dao.deleteGroup(group)

    // === Assessment Operations ===
    fun getAssessmentsByClass(classId: Long): Flow<List<Assessment>> = dao.getAssessmentsByClass(classId)
    suspend fun getAssessmentsByClassDirect(classId: Long): List<Assessment> = dao.getAssessmentsByClassDirect(classId)
    fun getAssessmentById(assessmentId: Long): Flow<Assessment?> = dao.getAssessmentById(assessmentId)
    suspend fun getAssessmentDirect(assessmentId: Long): Assessment? = dao.getAssessmentDirect(assessmentId)
    suspend fun addAssessment(assessment: Assessment): Long = dao.insertAssessment(assessment)
    suspend fun updateAssessment(assessment: Assessment) = dao.updateAssessment(assessment)
    suspend fun deleteAssessment(assessment: Assessment) = dao.deleteAssessment(assessment)

    // === Score Operations & Automatic 0-100 Calculation ===
    fun getScoresByAssessment(assessmentId: Long): Flow<List<StudentScore>> = dao.getScoresByAssessment(assessmentId)

    fun calculateNormalizedScore(
        aspectScores: List<Int>,
        aspectCount: Int,
        minScore: Int,
        maxScore: Int,
        targetMinScore: Double = 67.0,
        targetMaxScore: Double = 90.0,
        bonusScore: Int = 0
    ): Double {
        val effectiveCount = aspectCount.coerceIn(1, 7)
        val activeScores = aspectScores.take(effectiveCount)
        val rawSum = activeScores.sum()

        val minPossible = effectiveCount * minScore
        val maxPossible = effectiveCount * maxScore

        val baseScore = if (maxPossible == minPossible) {
            targetMaxScore
        } else {
            val ratio = ((rawSum - minPossible).toDouble() / (maxPossible - minPossible).toDouble()).coerceIn(0.0, 1.0)
            targetMinScore + ratio * (targetMaxScore - targetMinScore)
        }

        val total = (baseScore + bonusScore).coerceIn(0.0, 100.0)
        return (total * 10.0).roundToInt() / 10.0
    }

    suspend fun saveStudentScore(
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
    ) = withContext(Dispatchers.IO) {
        val aspectList = listOf(aspect1, aspect2, aspect3, aspect4, aspect5, aspect6, aspect7)
        val finalScore = calculateNormalizedScore(
            aspectScores = aspectList,
            aspectCount = assessment.aspectCount,
            minScore = assessment.minScore,
            maxScore = assessment.maxScore,
            targetMinScore = assessment.targetMinScore,
            targetMaxScore = assessment.targetMaxScore,
            bonusScore = bonusScore
        )

        val existing = dao.getScoreByAssessmentAndStudent(assessment.id, studentId)
        val scoreEntity = StudentScore(
            id = existing?.id ?: 0,
            assessmentId = assessment.id,
            studentId = studentId,
            aspect1Score = aspect1,
            aspect2Score = aspect2,
            aspect3Score = aspect3,
            aspect4Score = aspect4,
            aspect5Score = aspect5,
            aspect6Score = aspect6,
            aspect7Score = aspect7,
            bonusScore = bonusScore,
            finalScore = finalScore,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        )
        dao.insertOrUpdateScore(scoreEntity)
    }

    // Apply unified score to an entire group with optional individual bonus adjustments
    suspend fun saveGroupScore(
        assessment: Assessment,
        groupId: Long,
        aspect1: Int,
        aspect2: Int,
        aspect3: Int,
        aspect4: Int,
        aspect5: Int,
        aspect6: Int,
        aspect7: Int,
        groupNotes: String = ""
    ) = withContext(Dispatchers.IO) {
        val members = dao.getGroupMembersDirect(groupId)
        val aspectList = listOf(aspect1, aspect2, aspect3, aspect4, aspect5, aspect6, aspect7)

        members.forEach { member ->
            val existing = dao.getScoreByAssessmentAndStudent(assessment.id, member.studentId)
            val bonus = existing?.bonusScore ?: 0
            val finalScore = calculateNormalizedScore(
                aspectScores = aspectList,
                aspectCount = assessment.aspectCount,
                minScore = assessment.minScore,
                maxScore = assessment.maxScore,
                targetMinScore = assessment.targetMinScore,
                targetMaxScore = assessment.targetMaxScore,
                bonusScore = bonus
            )
            val scoreEntity = StudentScore(
                id = existing?.id ?: 0,
                assessmentId = assessment.id,
                studentId = member.studentId,
                aspect1Score = aspect1,
                aspect2Score = aspect2,
                aspect3Score = aspect3,
                aspect4Score = aspect4,
                aspect5Score = aspect5,
                aspect6Score = aspect6,
                aspect7Score = aspect7,
                bonusScore = bonus,
                finalScore = finalScore,
                notes = if (existing?.notes?.isNotBlank() == true) existing.notes else groupNotes,
                updatedAt = System.currentTimeMillis()
            )
            dao.insertOrUpdateScore(scoreEntity)
        }
    }

    // Update individual bonus in a group assessment
    suspend fun updateStudentBonusScore(
        assessment: Assessment,
        studentId: Long,
        newBonus: Int
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getScoreByAssessmentAndStudent(assessment.id, studentId)
        if (existing != null) {
            val aspectList = listOf(
                existing.aspect1Score,
                existing.aspect2Score,
                existing.aspect3Score,
                existing.aspect4Score,
                existing.aspect5Score,
                existing.aspect6Score,
                existing.aspect7Score
            )
            val finalScore = calculateNormalizedScore(
                aspectScores = aspectList,
                aspectCount = assessment.aspectCount,
                minScore = assessment.minScore,
                maxScore = assessment.maxScore,
                targetMinScore = assessment.targetMinScore,
                targetMaxScore = assessment.targetMaxScore,
                bonusScore = newBonus
            )
            dao.insertOrUpdateScore(
                existing.copy(
                    bonusScore = newBonus,
                    finalScore = finalScore,
                    updatedAt = System.currentTimeMillis()
                )
            )
        } else {
            // Default aspects with minScore + bonus
            val aspectList = List(7) { assessment.minScore }
            val finalScore = calculateNormalizedScore(
                aspectScores = aspectList,
                aspectCount = assessment.aspectCount,
                minScore = assessment.minScore,
                maxScore = assessment.maxScore,
                targetMinScore = assessment.targetMinScore,
                targetMaxScore = assessment.targetMaxScore,
                bonusScore = newBonus
            )
            dao.insertOrUpdateScore(
                StudentScore(
                    assessmentId = assessment.id,
                    studentId = studentId,
                    aspect1Score = assessment.minScore,
                    aspect2Score = assessment.minScore,
                    aspect3Score = assessment.minScore,
                    bonusScore = newBonus,
                    finalScore = finalScore
                )
            )
        }
    }

    // === Remedial / Below KKM Queries ===
    suspend fun getRemedialStudents(classId: Long, assessmentId: Long?): List<RemedialItem> = withContext(Dispatchers.IO) {
        val schoolClass = dao.getClassDirect(classId) ?: return@withContext emptyList()
        val students = dao.getStudentsByClassDirect(classId)
        val studentMap = students.associateBy { it.id }

        val assessments = if (assessmentId != null && assessmentId > 0) {
            listOfNotNull(dao.getAssessmentDirect(assessmentId))
        } else {
            dao.getAssessmentsByClassDirect(classId)
        }

        val remedialList = mutableListOf<RemedialItem>()

        for (assessment in assessments) {
            val scores = dao.getScoresByAssessmentDirect(assessment.id)
            val scoredStudentIds = scores.map { it.studentId }.toSet()

            // Check scored students whose score < kkm
            for (score in scores) {
                if (score.finalScore < assessment.kkm) {
                    val student = studentMap[score.studentId]
                    if (student != null) {
                        remedialList.add(
                            RemedialItem(
                                studentId = student.id,
                                studentName = student.name,
                                nis = student.nis,
                                className = schoolClass.name,
                                assessmentId = assessment.id,
                                assessmentTitle = assessment.title,
                                topic = assessment.topic.ifBlank { assessment.title },
                                score = score.finalScore,
                                kkm = assessment.kkm
                            )
                        )
                    }
                }
            }

            // Also check un-scored students (score = 0, below KKM)
            for (student in students) {
                if (!scoredStudentIds.contains(student.id)) {
                    remedialList.add(
                        RemedialItem(
                            studentId = student.id,
                            studentName = student.name,
                            nis = student.nis,
                            className = schoolClass.name,
                            assessmentId = assessment.id,
                            assessmentTitle = assessment.title,
                            topic = assessment.topic.ifBlank { assessment.title },
                            score = 0.0,
                            kkm = assessment.kkm
                        )
                    )
                }
            }
        }

        remedialList.sortedBy { it.studentName }
    }
}
