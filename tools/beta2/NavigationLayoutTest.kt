package com.lomipe.devhub

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import com.lomipe.devhub.ui.MainActivity
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NavigationLayoutTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun tabsShareOneBaselineAndLocalToolsWork() {
        val labels = listOf("Home", "Repos", "Builds", "Files", "Settings", "Studio")
        val bounds = labels.map { rule.onNodeWithText(it, substring = false, useUnmergedTree = true).fetchSemanticsNode().boundsInRoot }
        assertTrue(bounds.all { kotlin.math.abs(it.center.y - bounds.first().center.y) < 3f })
        assertTrue(bounds.zipWithNext().all { (a, b) -> a.center.x < b.center.x })
        rule.onNodeWithText("Studio", substring = false).performClick()
        rule.onNodeWithText("DevHub Studio").assertIsDisplayed()
    }
}
