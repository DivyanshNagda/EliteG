package com.dnagda.eliteG

import android.app.Activity
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PermissionDialogTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun dialogAppearsWhenPermissionMissing() {
        // Assume: permission not granted
        onView(withText("Permission Required"))
            .check(matches(isDisplayed()))
    }

    @Test
    fun checkAgainShowsToastIfStillMissing() {
        // Click Check Again
        onView(withText("Check Again")).perform(click())
        onView(withText("Permission not granted yet"))
            .inRoot(withDecorView(not(`is`(getActivity(activityRule)!!.window.decorView))))
            .check(matches(isDisplayed()))
    }

    @Test
    fun restartButtonShowsToast() {
        onView(withText("Restart Device")).perform(click())
        onView(withText("Please reboot your device manually."))
            .inRoot(withDecorView(not(`is`(getActivity(activityRule)!!.window.decorView))))
            .check(matches(isDisplayed()))
    }

    // Helper function to get current Activity
    private fun getActivity(rule: ActivityScenarioRule<*>): Activity? {
        var activity: Activity? = null
        rule.scenario.onActivity { activity = it }
        return activity
    }
}
