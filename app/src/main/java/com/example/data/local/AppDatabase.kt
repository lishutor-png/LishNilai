package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SchoolClass::class,
        Student::class,
        StudentGroup::class,
        GroupMember::class,
        Assessment::class,
        StudentScore::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lishnilai_database.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.appDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(dao: AppDao) {
            // Seed 1 default class with realistic student data
            val classId = dao.insertClass(
                SchoolClass(
                    name = "X RPL 1",
                    subject = "Pemrograman Web & Perangkat Bergerak",
                    academicYear = "2025/2026",
                    semester = "Ganjil",
                    defaultKkm = 75
                )
            )

            // Seed second class
            val classId2 = dao.insertClass(
                SchoolClass(
                    name = "XI TKJ 2",
                    subject = "Administrasi Sistem Jaringan",
                    academicYear = "2025/2026",
                    semester = "Ganjil",
                    defaultKkm = 75
                )
            )

            val studentNames = listOf(
                Pair("1021001", "Ahmad Fauzi"),
                Pair("1021002", "Budi Santoso"),
                Pair("1021003", "Clara Amanda"),
                Pair("1021004", "Dewi Sartika"),
                Pair("1021005", "Eko Prasetyo"),
                Pair("1021006", "Fajar Ramadhan"),
                Pair("1021007", "Gita Gutawa"),
                Pair("1021008", "Hendra Gunawan"),
                Pair("1021009", "Indah Permatasari"),
                Pair("1021010", "Joko Widodo")
            )

            val studentIds = mutableListOf<Long>()
            studentNames.forEachIndexed { index, pair ->
                val sId = dao.insertStudent(
                    Student(
                        classId = classId,
                        nis = pair.first,
                        name = pair.second,
                        gender = if (index in listOf(2, 3, 6, 8)) "P" else "L"
                    )
                )
                studentIds.add(sId)
            }

            // Seed sample groups for Class 1
            val g1 = dao.insertGroup(StudentGroup(classId = classId, name = "Kelompok 1 - Frontend Dev"))
            val g2 = dao.insertGroup(StudentGroup(classId = classId, name = "Kelompok 2 - UI/UX Design"))
            val g3 = dao.insertGroup(StudentGroup(classId = classId, name = "Kelompok 3 - Backend API"))

            if (studentIds.size >= 10) {
                dao.insertGroupMember(GroupMember(groupId = g1, studentId = studentIds[0]))
                dao.insertGroupMember(GroupMember(groupId = g1, studentId = studentIds[1]))
                dao.insertGroupMember(GroupMember(groupId = g1, studentId = studentIds[2]))

                dao.insertGroupMember(GroupMember(groupId = g2, studentId = studentIds[3]))
                dao.insertGroupMember(GroupMember(groupId = g2, studentId = studentIds[4]))
                dao.insertGroupMember(GroupMember(groupId = g2, studentId = studentIds[5]))

                dao.insertGroupMember(GroupMember(groupId = g3, studentId = studentIds[6]))
                dao.insertGroupMember(GroupMember(groupId = g3, studentId = studentIds[7]))
                dao.insertGroupMember(GroupMember(groupId = g3, studentId = studentIds[8]))
                dao.insertGroupMember(GroupMember(groupId = g3, studentId = studentIds[9]))
            }

            // Seed 2 Assessments
            val a1Id = dao.insertAssessment(
                Assessment(
                    classId = classId,
                    title = "Ulangan Harian 1",
                    topic = "Konsep Dasar HTML & CSS Layout",
                    category = "Ulangan",
                    isGroupAssessment = false,
                    kkm = 75,
                    minScore = 1,
                    maxScore = 5,
                    weightType = "1-5",
                    targetMinScore = 67.0,
                    targetMaxScore = 90.0,
                    aspectCount = 4,
                    aspect1Name = "Penguasaan Sintaks",
                    aspect2Name = "Ketepatan Tag Semantik",
                    aspect3Name = "Responsivitas Layout",
                    aspect4Name = "Kerapian Kode"
                )
            )

            val a2Id = dao.insertAssessment(
                Assessment(
                    classId = classId,
                    title = "Projek Kelompok 1",
                    topic = "Pembuatan Landing Page Interaktif",
                    category = "Proyek",
                    isGroupAssessment = true,
                    kkm = 75,
                    minScore = 1,
                    maxScore = 10,
                    weightType = "1-10",
                    targetMinScore = 67.0,
                    targetMaxScore = 90.0,
                    aspectCount = 3,
                    aspect1Name = "Kesesuaian Desain & Fungsi",
                    aspect2Name = "Kerjasama & Pembagian Tugas",
                    aspect3Name = "Presentasi Hasil Karya"
                )
            )

            // Seed scores for a1 (Individual assessment)
            if (studentIds.size >= 6) {
                // High performer
                dao.insertOrUpdateScore(
                    StudentScore(
                        assessmentId = a1Id,
                        studentId = studentIds[0],
                        aspect1Score = 5, aspect2Score = 5, aspect3Score = 4, aspect4Score = 5,
                        finalScore = 95.0
                    )
                )
                // Below KKM student for realistic testing of KKM tab
                dao.insertOrUpdateScore(
                    StudentScore(
                        assessmentId = a1Id,
                        studentId = studentIds[1],
                        aspect1Score = 3, aspect2Score = 2, aspect3Score = 3, aspect4Score = 3,
                        finalScore = 55.0
                    )
                )
                dao.insertOrUpdateScore(
                    StudentScore(
                        assessmentId = a1Id,
                        studentId = studentIds[2],
                        aspect1Score = 4, aspect2Score = 4, aspect3Score = 5, aspect4Score = 4,
                        finalScore = 85.0
                    )
                )
                // Another below KKM
                dao.insertOrUpdateScore(
                    StudentScore(
                        assessmentId = a1Id,
                        studentId = studentIds[3],
                        aspect1Score = 2, aspect2Score = 3, aspect3Score = 3, aspect4Score = 2,
                        finalScore = 50.0
                    )
                )
            }
        }
    }
}
