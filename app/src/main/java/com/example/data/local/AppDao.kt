package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // === CLASSES ===
    @Query("SELECT * FROM classes ORDER BY createdAt DESC")
    fun getAllClasses(): Flow<List<SchoolClass>>

    @Query("SELECT * FROM classes WHERE id = :classId LIMIT 1")
    fun getClassById(classId: Long): Flow<SchoolClass?>

    @Query("SELECT * FROM classes WHERE id = :classId LIMIT 1")
    suspend fun getClassDirect(classId: Long): SchoolClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(schoolClass: SchoolClass): Long

    @Update
    suspend fun updateClass(schoolClass: SchoolClass)

    @Delete
    suspend fun deleteClass(schoolClass: SchoolClass)

    // === STUDENTS ===
    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY name ASC")
    fun getStudentsByClass(classId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE classId = :classId ORDER BY name ASC")
    suspend fun getStudentsByClassDirect(classId: Long): List<Student>

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>): List<Long>

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE classId = :classId")
    suspend fun deleteStudentsByClass(classId: Long)

    // === GROUPS ===
    @Query("SELECT * FROM student_groups WHERE classId = :classId ORDER BY name ASC")
    fun getGroupsByClass(classId: Long): Flow<List<StudentGroup>>

    @Query("SELECT * FROM student_groups WHERE classId = :classId ORDER BY name ASC")
    suspend fun getGroupsByClassDirect(classId: Long): List<StudentGroup>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: StudentGroup): Long

    @Update
    suspend fun updateGroup(group: StudentGroup)

    @Delete
    suspend fun deleteGroup(group: StudentGroup)

    // Group Members
    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    fun getGroupMembers(groupId: Long): Flow<List<GroupMember>>

    @Query("SELECT * FROM group_members WHERE groupId = :groupId")
    suspend fun getGroupMembersDirect(groupId: Long): List<GroupMember>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembers(members: List<GroupMember>)

    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun clearGroupMembers(groupId: Long)

    @Query("DELETE FROM group_members WHERE groupId = :groupId AND studentId = :studentId")
    suspend fun removeGroupMember(groupId: Long, studentId: Long)

    @Query("SELECT * FROM group_members WHERE groupId IN (SELECT id FROM student_groups WHERE classId = :classId)")
    fun getAllGroupMembersByClass(classId: Long): Flow<List<GroupMember>>

    @Query("SELECT * FROM group_members WHERE groupId IN (SELECT id FROM student_groups WHERE classId = :classId)")
    suspend fun getAllGroupMembersByClassDirect(classId: Long): List<GroupMember>

    // === ASSESSMENTS ===
    @Query("SELECT * FROM assessments WHERE classId = :classId ORDER BY createdAt DESC")
    fun getAssessmentsByClass(classId: Long): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE classId = :classId ORDER BY createdAt DESC")
    suspend fun getAssessmentsByClassDirect(classId: Long): List<Assessment>

    @Query("SELECT * FROM assessments WHERE id = :assessmentId LIMIT 1")
    fun getAssessmentById(assessmentId: Long): Flow<Assessment?>

    @Query("SELECT * FROM assessments WHERE id = :assessmentId LIMIT 1")
    suspend fun getAssessmentDirect(assessmentId: Long): Assessment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: Assessment): Long

    @Update
    suspend fun updateAssessment(assessment: Assessment)

    @Delete
    suspend fun deleteAssessment(assessment: Assessment)

    // === SCORES ===
    @Query("SELECT * FROM student_scores WHERE assessmentId = :assessmentId")
    fun getScoresByAssessment(assessmentId: Long): Flow<List<StudentScore>>

    @Query("SELECT * FROM student_scores WHERE assessmentId = :assessmentId")
    suspend fun getScoresByAssessmentDirect(assessmentId: Long): List<StudentScore>

    @Query("SELECT * FROM student_scores WHERE studentId = :studentId")
    fun getScoresByStudent(studentId: Long): Flow<List<StudentScore>>

    @Query("SELECT * FROM student_scores WHERE assessmentId = :assessmentId AND studentId = :studentId LIMIT 1")
    suspend fun getScoreByAssessmentAndStudent(assessmentId: Long, studentId: Long): StudentScore?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateScore(score: StudentScore): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateScores(scores: List<StudentScore>)

    @Delete
    suspend fun deleteScore(score: StudentScore)

    @Query("DELETE FROM student_scores WHERE assessmentId = :assessmentId")
    suspend fun deleteScoresByAssessment(assessmentId: Long)

    // Count queries for Dashboard
    @Query("SELECT COUNT(*) FROM classes")
    fun getClassCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students")
    fun getTotalStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM assessments")
    fun getTotalAssessmentCount(): Flow<Int>
}
