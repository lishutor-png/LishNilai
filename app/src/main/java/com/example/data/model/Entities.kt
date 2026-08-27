package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "classes")
data class SchoolClass(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val subject: String = "Umum",
    val academicYear: String = "2025/2026",
    val semester: String = "Ganjil",
    val defaultKkm: Int = 75,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId"])]
)
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long,
    val nis: String = "",
    val name: String,
    val gender: String = "L", // L or P
    val notes: String = ""
)

@Entity(
    tableName = "student_groups",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId"])]
)
data class StudentGroup(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long,
    val name: String,
    val description: String = ""
)

@Entity(
    tableName = "group_members",
    foreignKeys = [
        ForeignKey(
            entity = StudentGroup::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["groupId"]), Index(value = ["studentId"])]
)
data class GroupMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val groupId: Long,
    val studentId: Long
)

@Entity(
    tableName = "assessments",
    foreignKeys = [
        ForeignKey(
            entity = SchoolClass::class,
            parentColumns = ["id"],
            childColumns = ["classId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["classId"])]
)
data class Assessment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val classId: Long,
    val title: String, // e.g. "Ulangan Harian 1"
    val topic: String = "", // Materi, e.g. "Aljabar & Fungsi"
    val category: String = "Tugas", // Tugas, Ulangan, Proyek, Praktik
    val isGroupAssessment: Boolean = false,
    val kkm: Int = 75,
    val minScore: Int = 1,
    val maxScore: Int = 5, // or 10
    val weightType: String = "1-5", // "1-5" or "1-10"
    val targetMinScore: Double = 67.0, // Skor konversi terendah (misal: 67)
    val targetMaxScore: Double = 90.0, // Skor konversi tertinggi (misal: 90)
    val aspectCount: Int = 3, // 1 to 7
    val aspect1Name: String = "Pemahaman Konsep",
    val aspect2Name: String = "Keterampilan / Kerapian",
    val aspect3Name: String = "Ketepatan Waktu",
    val aspect4Name: String = "Kerjasama Tim",
    val aspect5Name: String = "Presentasi & Komunikasi",
    val aspect6Name: String = "Kreativitas / Inovasi",
    val aspect7Name: String = "Kemandirian",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "student_scores",
    foreignKeys = [
        ForeignKey(
            entity = Assessment::class,
            parentColumns = ["id"],
            childColumns = ["assessmentId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Student::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["assessmentId", "studentId"], unique = true),
        Index(value = ["studentId"]),
        Index(value = ["assessmentId"])
    ]
)
data class StudentScore(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assessmentId: Long,
    val studentId: Long,
    val aspect1Score: Int = 0,
    val aspect2Score: Int = 0,
    val aspect3Score: Int = 0,
    val aspect4Score: Int = 0,
    val aspect5Score: Int = 0,
    val aspect6Score: Int = 0,
    val aspect7Score: Int = 0,
    val bonusScore: Int = 0, // Nilai tambahan individu dalam proyek kelompok
    val finalScore: Double = 0.0, // 0 - 100
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

// Helper DTOs for UI
data class StudentWithScore(
    val student: Student,
    val score: StudentScore?,
    val groupName: String? = null
)

data class RemedialItem(
    val studentId: Long,
    val studentName: String,
    val nis: String,
    val className: String,
    val assessmentId: Long,
    val assessmentTitle: String,
    val topic: String,
    val score: Double,
    val kkm: Int
)

data class StudentSemesterRecap(
    val student: Student,
    val assessmentScores: Map<Long, Double>, // assessmentId -> score
    val averageScore: Double,
    val belowKkmCount: Int
)
