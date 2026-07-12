package com.dps.businessexpensetracker

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.dps.businessexpensetracker.ui.GuidedTourPrefs
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GuidedTourTest {
    @get:Rule
    val composeRule = createEmptyComposeRule()

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun resetOnboardingState() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("business_expenses", 0).edit().clear().commit()
        context.getSharedPreferences("onboarding", 0).edit().clear().commit()
        scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitForIdle()
    }

    @After
    fun closeActivity() {
        scenario.close()
    }

    @Test
    fun tourShowsOnFirstLaunchAndCompletesThroughAllSteps() {
        composeRule.onNodeWithTag("tour_overlay").assertIsDisplayed()
        composeRule.onNodeWithText("Welcome to Business Tracker").assertIsDisplayed()

        repeat(6) {
            composeRule.onNodeWithTag("tour_next").performClick()
        }
        composeRule.onNodeWithText("You're ready").assertIsDisplayed()
        composeRule.onNodeWithTag("tour_next").performClick()

        composeRule.onNodeWithTag("tour_overlay").assertDoesNotExist()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertTrue(GuidedTourPrefs.isTourSeen(context))
    }

    @Test
    fun skipDismissesTourAndItStaysDismissedAfterRecreation() {
        composeRule.onNodeWithTag("tour_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("tour_skip").performClick()
        composeRule.onNodeWithTag("tour_overlay").assertDoesNotExist()

        scenario.recreate()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tour_overlay").assertDoesNotExist()
    }

    @Test
    fun tourCanBeReplayedFromTheMenu() {
        composeRule.onNodeWithTag("tour_skip").performClick()

        composeRule.onNodeWithContentDescription("Backup and restore").performClick()
        composeRule.onNodeWithText("Replay app tour").performClick()

        composeRule.onNodeWithTag("tour_overlay").assertIsDisplayed()
        composeRule.onNodeWithText("Welcome to Business Tracker").assertIsDisplayed()
    }

    @Test
    fun tourBlocksInteractionWithTheUiBehindIt() {
        composeRule.onNodeWithTag("tour_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("tour_back").assertDoesNotExist()

        composeRule.onNodeWithTag("tour_next").performClick()
        composeRule.onNodeWithTag("tour_back").assertIsDisplayed()
        composeRule.onNodeWithTag("tour_back").performClick()
        composeRule.onNodeWithText("Welcome to Business Tracker").assertIsDisplayed()
    }
}
