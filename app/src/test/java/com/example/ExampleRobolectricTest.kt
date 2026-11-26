package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.datasource.CsvDataHandler
import com.example.data.datasource.MockSimulatorDataSource
import com.example.domain.engine.AnalyticsEngine
import com.example.domain.engine.BacktestingEngine
import com.example.domain.engine.PredictionEngine
import com.example.domain.model.GameRound
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aviator Analytics", appName)
    }

    @Test
    fun `test analytics engine math calculations`() {
        val sampleRounds = listOf(
            GameRound(roundId = "R1", multiplier = 1.20, timestamp = 1000L),
            GameRound(roundId = "R2", multiplier = 1.50, timestamp = 2000L),
            GameRound(roundId = "R3", multiplier = 2.00, timestamp = 3000L),
            GameRound(roundId = "R4", multiplier = 3.00, timestamp = 4000L),
            GameRound(roundId = "R5", multiplier = 10.00, timestamp = 5000L)
        )

        val analytics = AnalyticsEngine.computeAnalytics(sampleRounds)
        assertEquals(5, analytics.sampleSize)
        assertEquals(3.54, analytics.mean, 0.01)
        assertEquals(2.00, analytics.median, 0.01)
        assertEquals(1.20, analytics.minMultiplier, 0.01)
        assertEquals(10.00, analytics.maxMultiplier, 0.01)
        assertTrue(analytics.standardDeviation > 0.0)
        assertNotNull(analytics.percentiles)
        assertEquals(5, analytics.distributionRanges.size)
    }

    @Test
    fun `test prediction engine multi model ensemble`() {
        val rounds = MockSimulatorDataSource.generateHistoricalBatch(40)
        val prediction = PredictionEngine.predictNext(rounds, windowSize = 30)

        assertNotNull(prediction)
        assertTrue(prediction.expectedMultiplier >= 1.00)
        assertTrue(prediction.ensembleConfidence in 0.0..1.0)
        assertTrue(prediction.modelOutputs.isNotEmpty())
        assertNotNull(prediction.recommendedRange)
    }

    @Test
    fun `test backtesting engine walk forward evaluation`() {
        val rounds = MockSimulatorDataSource.generateHistoricalBatch(50)
        val report = BacktestingEngine.runWalkForwardBacktest(
            rounds = rounds,
            minTrainingSize = 15,
            windowSize = 25
        )

        assertNotNull(report)
        assertTrue(report.totalRoundsEvaluated > 0)
        assertTrue(report.modelOverallAccuracy in 0.0..100.0)
        assertTrue(report.baselineOverallAccuracy in 0.0..100.0)
        assertTrue(report.stepResults.isNotEmpty())
    }

    @Test
    fun `test csv import export handler`() {
        val sampleRounds = listOf(
            GameRound(roundId = "BET-1", multiplier = 1.85, timestamp = 1000000L, source = "MANUAL"),
            GameRound(roundId = "BET-2", multiplier = 4.20, timestamp = 2000000L, source = "SIMULATOR")
        )

        val csv = CsvDataHandler.exportToCsv(sampleRounds)
        assertTrue(csv.contains("BET-1"))
        assertTrue(csv.contains("1.85"))
        assertTrue(csv.contains("BET-2"))

        val parsed = CsvDataHandler.parseCsv(csv)
        assertEquals(2, parsed.size)
        assertEquals(1.85, parsed[0].multiplier, 0.01)
        assertEquals(4.20, parsed[1].multiplier, 0.01)
    }

    @Test
    fun `test navigation screen list integrity`() {
        val navItems = com.example.ui.navigation.Screen.bottomNavItems
        assertEquals(5, navItems.size)
        navItems.forEach { screen ->
            assertNotNull(screen)
            assertTrue(screen.route.isNotBlank())
            assertTrue(screen.title.isNotBlank())
            assertTrue(screen.shortLabel.isNotBlank())
        }
    }
}
