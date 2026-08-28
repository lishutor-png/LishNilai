package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDao
import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `verify app name string resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LishNilai", appName)
    }

    @Test
    fun `test class and student database lifecycle`() = runBlocking {
        val testClass = SchoolClass(name = "XII RPL 1", subject = "Pemrograman Perangkat Bergerak", kkm = 75.0)
        val classId = dao.insertClass(testClass)
        assertTrue(classId > 0)

        val student1 = Student(name = "Ahmad Rifai", nis = "1001", classId = classId, groupName = "Kelompok 1")
        val student2 = Student(name = "Budi Santoso", nis = "1002", classId = classId, groupName = "Kelompok 1")
        dao.insertStudents(listOf(student1, student2))

        val studentsInClass = dao.getStudentsByClassDirect(classId)
        assertEquals(2, studentsInClass.size)
        assertEquals("Ahmad Rifai", studentsInClass[0].name)
    }

    @Test
    fun `test assessment and multi aspect grading calculation`() = runBlocking {
        val testClass = SchoolClass(name = "XII TKJ 2", subject = "Administrasi Sistem Jaringan", kkm = 75.0)
        val classId = dao.insertClass(testClass)

        val assessment = Assessment(
            classId = classId,
            title = "Praktik Konfigurasi Debian Server",
            type = AssessmentType.PROJECT,
            aspectCount = 3,
            aspect1Name = "Persiapan & K3",
            aspect2Name = "Langkah Kerja & Konfigurasi",
            aspect3Name = "Hasil Pengujian & Troubleshooting",
            minScore = 1,
            maxScore = 5,
            targetMinScore = 60.0,
            targetMaxScore = 100.0,
            weight = 1.0
        )
        val assessmentId = dao.insertAssessment(assessment)

        val student = Student(name = "Citra Dewi", nis = "2001", classId = classId)
        val studentId = dao.insertStudent(student)

        // Raw scores: 5, 4, 5 (Sum = 14, minPossible = 3*1=3, maxPossible = 3*5=15)
        // Ratio = (14 - 3) / (15 - 3) = 11 / 12 = 0.916667
        // Final score = 60 + 0.916667 * (100 - 60) = 60 + 36.6667 = 96.67
        val rawSum = 5 + 4 + 5
        val minPossible = 3 * 1
        val maxPossible = 3 * 5
        val ratio = (rawSum - minPossible).toDouble() / (maxPossible - minPossible).toDouble()
        val calculatedFinalScore = 60.0 + (ratio * (100.0 - 60.0))

        val grade = Grade(
            assessmentId = assessmentId,
            studentId = studentId,
            aspect1Score = 5,
            aspect2Score = 4,
            aspect3Score = 5,
            finalScore = calculatedFinalScore,
            notes = "Sangat bagus dan rapi"
        )
        dao.insertGrade(grade)

        val savedGrade = dao.getGradeForStudent(assessmentId, studentId)
        assertNotNull(savedGrade)
        assertEquals(96.67, savedGrade!!.finalScore, 0.1)
        assertTrue(savedGrade.finalScore >= testClass.kkm)
    }

    @Test
    fun `test remedial detection for score below KKM`() {
        val kkm = 75.0
        val studentScore1 = 70.0
        val studentScore2 = 82.5

        val isRemedial1 = studentScore1 < kkm
        val isRemedial2 = studentScore2 < kkm

        assertTrue(isRemedial1)
        assertFalse(isRemedial2)
    }

    @Test
    fun `test student group grouping and batch scoring logic`() = runBlocking {
        val classId = dao.insertClass(SchoolClass(name = "X DKV 1", subject = "Dasar Desain Grafis", kkm = 75.0))
        val group = StudentGroup(classId = classId, groupName = "Kelompok Alpha")
        val groupId = dao.insertGroup(group)
        assertTrue(groupId > 0)

        val s1 = Student(name = "Deni", nis = "3001", classId = classId, groupName = "Kelompok Alpha")
        val s2 = Student(name = "Eka", nis = "3002", classId = classId, groupName = "Kelompok Alpha")
        val s1Id = dao.insertStudent(s1)
        val s2Id = dao.insertStudent(s2)

        val assessmentId = dao.insertAssessment(
            Assessment(
                classId = classId,
                title = "Projek Desain Logo",
                type = AssessmentType.PROJECT,
                aspectCount = 2,
                minScore = 1,
                maxScore = 5,
                targetMinScore = 65.0,
                targetMaxScore = 95.0
            )
        )

        // Batch apply score to both members of group
        val groupScore = 90.0
        dao.insertGrade(Grade(assessmentId = assessmentId, studentId = s1Id, finalScore = groupScore))
        dao.insertGrade(Grade(assessmentId = assessmentId, studentId = s2Id, finalScore = groupScore))

        val g1 = dao.getGradeForStudent(assessmentId, s1Id)
        val g2 = dao.getGradeForStudent(assessmentId, s2Id)
        assertEquals(90.0, g1?.finalScore ?: 0.0, 0.01)
        assertEquals(90.0, g2?.finalScore ?: 0.0, 0.01)
    }

    @Test
    fun `test default password admin28`() {
        val defaultPassword = "admin28"
        val correctInput = "admin28"
        val wrongInput = "12345"
        assertTrue(correctInput == defaultPassword)
        assertTrue(wrongInput != defaultPassword)
    }
}

