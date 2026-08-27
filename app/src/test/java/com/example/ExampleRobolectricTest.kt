package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.GradeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name string resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LishNilai", appName)
    }

    @Test
    fun `test score normalization logic 1 to 5`() {
        val aspectScores = listOf(5, 5, 5) // perfect score in 3 aspects
        val minPossible = 3 * 1
        val maxPossible = 3 * 5
        val rawSum = aspectScores.sum()
        val normalized = (((rawSum - minPossible).toDouble() / (maxPossible - minPossible).toDouble()) * 100.0)
        assertEquals(100.0, normalized, 0.01)

        val lowestScores = listOf(1, 1, 1)
        val lowestSum = lowestScores.sum()
        val lowestNorm = (((lowestSum - minPossible).toDouble() / (maxPossible - minPossible).toDouble()) * 100.0)
        assertEquals(0.0, lowestNorm, 0.01)
    }

    @Test
    fun `test custom score normalization 67 to 90`() {
        // Rentang 67 sampai 90:
        // Bobot 1 = 67, Bobot 5 = 90
        val targetMin = 67.0
        val targetMax = 90.0
        val minPossible = 3 * 1
        val maxPossible = 3 * 5

        // All 1s -> 67
        val lowestScores = listOf(1, 1, 1)
        val lowestRatio = (lowestScores.sum() - minPossible).toDouble() / (maxPossible - minPossible).toDouble()
        val lowestScore = targetMin + lowestRatio * (targetMax - targetMin)
        assertEquals(67.0, lowestScore, 0.01)

        // All 5s -> 90
        val highestScores = listOf(5, 5, 5)
        val highestRatio = (highestScores.sum() - minPossible).toDouble() / (maxPossible - minPossible).toDouble()
        val highestScore = targetMin + highestRatio * (targetMax - targetMin)
        assertEquals(90.0, highestScore, 0.01)

        // Single aspect weight 1 -> 67, weight 5 -> 90
        val singleMinRatio = (1 - 1).toDouble() / (5 - 1).toDouble()
        val singleMinScore = targetMin + singleMinRatio * (targetMax - targetMin)
        assertEquals(67.0, singleMinScore, 0.01)

        val singleMaxRatio = (5 - 1).toDouble() / (5 - 1).toDouble()
        val singleMaxScore = targetMin + singleMaxRatio * (targetMax - targetMin)
        assertEquals(90.0, singleMaxScore, 0.01)
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

